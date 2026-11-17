package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.AdhkarCatalogue
import com.example.data.model.AdhkarItem
import com.example.data.model.AdhkarPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdhkarAudioPlayer private constructor(private val context: Context) {

  companion object {
    private const val TAG = "AdhkarAudioPlayer"

    @Volatile
    private var INSTANCE: AdhkarAudioPlayer? = null

    fun getInstance(context: Context): AdhkarAudioPlayer {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: AdhkarAudioPlayer(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  private var mediaPlayer: MediaPlayer? = null
  private val handler = Handler(Looper.getMainLooper())

  private val _currentPeriod = MutableStateFlow(AdhkarPeriod.MORNING)
  val currentPeriod: StateFlow<AdhkarPeriod> = _currentPeriod.asStateFlow()

  private val _currentIndex = MutableStateFlow(0)
  val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

  private val _currentRepetition = MutableStateFlow(1)
  val currentRepetition: StateFlow<Int> = _currentRepetition.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _autoAdvance = MutableStateFlow(true)
  val autoAdvance: StateFlow<Boolean> = _autoAdvance.asStateFlow()

  private val _currentPositionMs = MutableStateFlow(0)
  val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

  private val _durationMs = MutableStateFlow(1)
  val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

  private val _completedIds = MutableStateFlow<Set<String>>(emptySet())
  val completedIds: StateFlow<Set<String>> = _completedIds.asStateFlow()

  private val _audioError = MutableStateFlow<String?>(null)
  val audioError: StateFlow<String?> = _audioError.asStateFlow()

  fun clearError() {
    _audioError.value = null
  }

  private val updatePositionRunnable = object : Runnable {
    override fun run() {
      try {
        mediaPlayer?.let { player ->
          if (player.isPlaying) {
            _currentPositionMs.value = player.currentPosition
            _durationMs.value = player.duration.coerceAtLeast(1)
            handler.postDelayed(this, 300)
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Error updating progress", e)
      }
    }
  }

  fun setPeriod(period: AdhkarPeriod) {
    if (_currentPeriod.value != period) {
      stop()
      _audioError.value = null
      _currentPeriod.value = period
      _currentIndex.value = 0
      _currentRepetition.value = 1
    }
  }

  fun getCurrentItem(): AdhkarItem {
    val items = AdhkarCatalogue.getAdhkarForPeriod(_currentPeriod.value)
    val index = _currentIndex.value.coerceIn(0, maxOf(0, items.size - 1))
    return if (items.isNotEmpty()) items[index] else AdhkarCatalogue.morningAdhkar.first()
  }

  fun playCurrent() {
    val item = getCurrentItem()
    playUrl(item.audioUrl)
  }

  fun togglePlayPause() {
    if (_isPlaying.value) {
      pause()
    } else {
      val player = mediaPlayer
      if (player != null && !player.isPlaying) {
        try {
          player.start()
          _isPlaying.value = true
          handler.post(updatePositionRunnable)
        } catch (e: Exception) {
          playCurrent()
        }
      } else {
        playCurrent()
      }
    }
  }

  fun playDhikrAtIndex(index: Int) {
    val items = AdhkarCatalogue.getAdhkarForPeriod(_currentPeriod.value)
    if (index in items.indices) {
      _currentIndex.value = index
      _currentRepetition.value = 1
      playCurrent()
    }
  }

  fun toggleAutoAdvance() {
    _autoAdvance.value = !_autoAdvance.value
  }

  fun incrementRepetition() {
    val item = getCurrentItem()
    if (_currentRepetition.value < item.repeatCount) {
      _currentRepetition.value += 1
      playCurrent()
    } else {
      markCurrentCompleted()
      if (_autoAdvance.value) {
        next()
      }
    }
  }

  fun next() {
    val items = AdhkarCatalogue.getAdhkarForPeriod(_currentPeriod.value)
    if (_currentIndex.value < items.size - 1) {
      _currentIndex.value += 1
      _currentRepetition.value = 1
      playCurrent()
    } else {
      stop()
    }
  }

  fun previous() {
    if (_currentIndex.value > 0) {
      _currentIndex.value -= 1
      _currentRepetition.value = 1
      playCurrent()
    }
  }

  fun pause() {
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.pause()
        }
      }
      _isPlaying.value = false
      handler.removeCallbacks(updatePositionRunnable)
    } catch (e: Exception) {
      Log.w(TAG, "Error pausing audio", e)
    }
  }

  fun stop() {
    try {
      handler.removeCallbacks(updatePositionRunnable)
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.stop()
        }
        player.reset()
        player.release()
      }
      mediaPlayer = null
      _isPlaying.value = false
      _currentPositionMs.value = 0
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping audio", e)
    }
  }

  private fun markCurrentCompleted() {
    val current = getCurrentItem()
    _completedIds.value = _completedIds.value + current.id
  }

  private fun playUrl(url: String) {
    stop()
    _audioError.value = null
    if (url.isBlank()) {
      _audioError.value = "Audio stream unavailable for this dhikr. You can still recite and tap the counter below."
      return
    }

    _isLoading.value = true
    try {
      val player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(url)
        setOnPreparedListener { mp ->
          _isLoading.value = false
          _audioError.value = null
          _durationMs.value = mp.duration.coerceAtLeast(1)
          _currentPositionMs.value = 0
          mp.start()
          _isPlaying.value = true
          handler.post(updatePositionRunnable)
        }
        setOnCompletionListener {
          _isPlaying.value = false
          handler.removeCallbacks(updatePositionRunnable)
          handleAudioCompletion()
        }
        setOnErrorListener { _, what, extra ->
          Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
          _isLoading.value = false
          _isPlaying.value = false
          _audioError.value = "Unable to stream Adhkar audio right now. Check your connection or recite directly below."
          handler.removeCallbacks(updatePositionRunnable)
          true
        }
      }
      mediaPlayer = player
      player.prepareAsync()
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize player for url: $url", e)
      _isLoading.value = false
      _isPlaying.value = false
      _audioError.value = "Unable to load Adhkar audio. You can continue reciting using the counter below."
    }
  }

  private fun handleAudioCompletion() {
    val item = getCurrentItem()
    if (_currentRepetition.value < item.repeatCount) {
      _currentRepetition.value += 1
      playCurrent()
    } else {
      markCurrentCompleted()
      if (_autoAdvance.value) {
        next()
      }
    }
  }
}
