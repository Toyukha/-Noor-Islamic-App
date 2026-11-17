package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import kotlin.concurrent.thread

data class SurahAudioInfo(
  val surahNumber: Int,
  val englishName: String,
  val arabicName: String,
  val ayahCount: Int,
  val recitationQari: String = "Mishary Rashid Alafasy",
  val audioUrl: String,
  val description: String,
  val assetFileName: String? = null
)

object SurahAudioCatalog {
  val SURAH_MULK = SurahAudioInfo(
    surahNumber = 67,
    englishName = "Surah Al-Mulk",
    arabicName = "سورة الملك",
    ayahCount = 30,
    recitationQari = "Mishary Rashid Alafasy",
    audioUrl = "https://cdn.mp3quran.net/audio/mishary-alafasy/r1/067.mp3",
    description = "The Sovereignty • Protection from the punishment of the grave (Recited nightly after Isha)",
    assetFileName = "audio/surah_mulk.mp3"
  )

  val SURAH_KAHF = SurahAudioInfo(
    surahNumber = 18,
    englishName = "Surah Al-Kahf",
    arabicName = "سورة الكهف",
    ayahCount = 110,
    recitationQari = "Mishary Rashid Alafasy",
    audioUrl = "https://cdn.mp3quran.net/audio/mishary-alafasy/r1/018.mp3",
    description = "The Cave • Light between the two Fridays (Recited every Friday before sunrise/sunset)",
    assetFileName = "audio/surah_kahf.mp3"
  )

  val SURAH_WAQIAH = SurahAudioInfo(
    surahNumber = 56,
    englishName = "Surah Al-Waqi'ah",
    arabicName = "سورة الواقعة",
    ayahCount = 96,
    recitationQari = "Mishary Rashid Alafasy",
    audioUrl = "https://cdn.mp3quran.net/audio/mishary-alafasy/r1/056.mp3",
    description = "The Inevitable • Protection against poverty & hardship (Recited nightly after Isha)",
    assetFileName = "audio/surah_waqiah.mp3"
  )

  const val ADHAN_AUDIO_URL = "https://raw.githubusercontent.com/AalianKhan/adhans/master/adhan.mp3"
}

class SurahAudioPlayer private constructor(private val context: Context) {

  companion object {
    private const val TAG = "SurahAudioPlayer"

    @Volatile
    private var INSTANCE: SurahAudioPlayer? = null

    fun getInstance(context: Context): SurahAudioPlayer {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SurahAudioPlayer(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  private var mediaPlayer: MediaPlayer? = null
  private val handler = Handler(Looper.getMainLooper())

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentSurah = MutableStateFlow<SurahAudioInfo?>(null)
  val currentSurah: StateFlow<SurahAudioInfo?> = _currentSurah.asStateFlow()

  private val _currentPositionMs = MutableStateFlow(0)
  val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

  private val _durationMs = MutableStateFlow(1)
  val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val updatePositionRunnable = object : Runnable {
    override fun run() {
      try {
        mediaPlayer?.let { player ->
          if (player.isPlaying) {
            _currentPositionMs.value = player.currentPosition
            _durationMs.value = player.duration.coerceAtLeast(1)
            handler.postDelayed(this, 500)
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Error updating progress", e)
      }
    }
  }

  private val playlistQueue = mutableListOf<SurahAudioInfo>()
  private var adhanPlayer: MediaPlayer? = null
  private val _isPlayingAdhan = MutableStateFlow(false)
  val isPlayingAdhan: StateFlow<Boolean> = _isPlayingAdhan.asStateFlow()

  fun playSequentialSurahs(surahs: List<SurahAudioInfo>) {
    if (surahs.isEmpty()) return
    playlistQueue.clear()
    if (surahs.size > 1) {
      playlistQueue.addAll(surahs.drop(1))
    }
    playSurah(surahs.first())
  }

  fun playSurah(surah: SurahAudioInfo) {
    _isLoading.value = true
    _currentSurah.value = surah

    // Run caching and preparation in background thread
    thread {
      try {
        stopInternal()

        val player = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .build()
          )
        }

        // 1. Try offline pre-bundled asset first (100% offline playback)
        var sourceLoaded = false
        val assetName = surah.assetFileName
        if (!assetName.isNullOrBlank()) {
          try {
            val afd = context.assets.openFd(assetName)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            sourceLoaded = true
            Log.d(TAG, "Playing ${surah.englishName} directly from offline asset: $assetName")
          } catch (e: Exception) {
            Log.w(TAG, "Could not load asset $assetName: ${e.message}")
          }
        }

        // 2. Check if cached locally in app storage for offline playback
        if (!sourceLoaded) {
          val cacheFile = File(context.cacheDir, "surah_${surah.surahNumber}.mp3")
          if (cacheFile.exists() && cacheFile.length() > 1024) {
            player.setDataSource(cacheFile.absolutePath)
          } else {
            // Stream and cache simultaneously or set direct datasource
            player.setDataSource(surah.audioUrl)
          }
        }

        player.setOnPreparedListener { mp ->
          _isLoading.value = false
          mp.start()
          _isPlaying.value = true
          _durationMs.value = mp.duration.coerceAtLeast(1)
          handler.post(updatePositionRunnable)
          SurahAudioService.startService(context, surah)
        }

        player.setOnCompletionListener {
          _isPlaying.value = false
          _currentPositionMs.value = 0
          handler.removeCallbacks(updatePositionRunnable)
          if (playlistQueue.isNotEmpty()) {
            val nextSurah = playlistQueue.removeAt(0)
            playSurah(nextSurah)
          } else {
            SurahAudioService.stopService(context)
          }
        }

        player.setOnErrorListener { _, what, extra ->
          Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
          _isLoading.value = false
          _isPlaying.value = false
          handler.removeCallbacks(updatePositionRunnable)
          true
        }

        player.prepareAsync()
        mediaPlayer = player

      } catch (e: Exception) {
        Log.e(TAG, "Failed to start surah audio playback", e)
        _isLoading.value = false
        _isPlaying.value = false
      }
    }
  }

  fun togglePlayPause() {
    try {
      mediaPlayer?.let { player ->
        if (player.isPlaying) {
          player.pause()
          _isPlaying.value = false
          handler.removeCallbacks(updatePositionRunnable)
        } else {
          player.start()
          _isPlaying.value = true
          handler.post(updatePositionRunnable)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error toggling play/pause", e)
    }
  }

  fun seekTo(positionMs: Int) {
    try {
      mediaPlayer?.seekTo(positionMs)
      _currentPositionMs.value = positionMs
    } catch (e: Exception) {
      Log.e(TAG, "Error seeking", e)
    }
  }

  fun stop() {
    stopInternal()
    _currentSurah.value = null
    SurahAudioService.stopService(context)
  }

  fun playAdhan(prayerName: String, onFinished: (() -> Unit)? = null) {
    stopAdhan()
    stopInternal()
    _currentSurah.value = null
    thread {
      try {
        val isFajr = prayerName.contains("fajr", ignoreCase = true)
        Log.d(TAG, "playAdhan requested for: '$prayerName' (isFajr: $isFajr)")

        val player = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .setUsage(AudioAttributes.USAGE_ALARM)
              .build()
          )
          setVolume(1.0f, 1.0f)
          try {
            setWakeMode(context, android.os.PowerManager.PARTIAL_WAKE_LOCK)
          } catch (e: Exception) {
            Log.w(TAG, "Could not set wake mode on MediaPlayer", e)
          }
        }

        val assetCandidates = if (isFajr) {
          listOf("audio/adhan_fajr.mp3", "audio/adhan_makkah.mp3", "audio/adhan.mp3")
        } else {
          listOf("audio/adhan_makkah.mp3", "audio/adhan.mp3", "audio/adhan_fajr.mp3")
        }

        var assetLoaded = false
        for (candidate in assetCandidates) {
          try {
            val afd = context.assets.openFd(candidate)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            assetLoaded = true
            Log.d(TAG, "Successfully loaded offline Adhan asset '$candidate' for $prayerName")
            break
          } catch (e: Exception) {
            Log.w(TAG, "Candidate asset $candidate not loadable: ${e.message}")
          }
        }

        if (!assetLoaded) {
          Log.w(TAG, "Offline assets failed, falling back to remote url")
          player.setDataSource(SurahAudioCatalog.ADHAN_AUDIO_URL)
        }

        player.setOnPreparedListener { mp ->
          mp.start()
          _isPlayingAdhan.value = true
        }
        player.setOnCompletionListener {
          _isPlayingAdhan.value = false
          stopAdhan()
          onFinished?.invoke()
        }
        player.setOnErrorListener { _, what, extra ->
          Log.e(TAG, "Adhan MediaPlayer error: what=$what extra=$extra")
          _isPlayingAdhan.value = false
          stopAdhan()
          onFinished?.invoke()
          true
        }

        adhanPlayer = player
        player.prepareAsync()
      } catch (e: Exception) {
        Log.e(TAG, "Failed to start Adhan playback", e)
        _isPlayingAdhan.value = false
        onFinished?.invoke()
      }
    }
  }

  fun stopAdhan() {
    try {
      adhanPlayer?.apply {
        if (isPlaying) stop()
        reset()
        release()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping Adhan", e)
    } finally {
      adhanPlayer = null
      _isPlayingAdhan.value = false
    }
  }

  private fun stopInternal() {
    handler.removeCallbacks(updatePositionRunnable)
    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        reset()
        release()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing player", e)
    } finally {
      mediaPlayer = null
      _isPlaying.value = false
      _currentPositionMs.value = 0
    }
  }
}
