package com.example.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R

class SurahAudioService : Service() {

  companion object {
    private const val TAG = "SurahAudioService"
    const val CHANNEL_ID = "noor_surah_playback_channel"
    const val NOTIFICATION_ID = 7771
    const val NOTIFICATION_ID_ADHAN = 7772

    const val ACTION_PLAY_PAUSE = "com.example.audio.ACTION_PLAY_PAUSE"
    const val ACTION_STOP = "com.example.audio.ACTION_STOP"
    const val ACTION_PLAY_ADHAN = "com.example.audio.ACTION_PLAY_ADHAN"
    const val ACTION_STOP_ADHAN = "com.example.audio.ACTION_STOP_ADHAN"
    const val ACTION_PLAY_NIGHTLY_SURAHS = "com.example.audio.ACTION_PLAY_NIGHTLY_SURAHS"
    const val ACTION_PLAY_FRIDAY_KAHF = "com.example.audio.ACTION_PLAY_FRIDAY_KAHF"

    const val EXTRA_SURAH_NAME = "EXTRA_SURAH_NAME"
    const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"

    fun startService(context: Context, surah: SurahAudioInfo) {
      val intent = Intent(context, SurahAudioService::class.java).apply {
        putExtra(EXTRA_SURAH_NAME, "${surah.englishName} (${surah.arabicName})")
      }
      safeStartForegroundService(context, intent)
    }

    fun startAdhan(context: Context, prayerName: String): Boolean {
      val intent = Intent(context, SurahAudioService::class.java).apply {
        action = ACTION_PLAY_ADHAN
        putExtra(EXTRA_PRAYER_NAME, prayerName)
      }
      val started = safeStartForegroundService(context, intent)
      if (!started) {
        // Fallback: play directly if OS temporarily blocks background FGS start
        try {
          SurahAudioPlayer.getInstance(context).playAdhan(prayerName)
        } catch (e: Exception) {
          Log.w(TAG, "Direct fallback Adhan playback failed", e)
        }
      }
      return started
    }

    fun stopAdhan(context: Context) {
      try {
        SurahAudioPlayer.getInstance(context).stopAdhan()
      } catch (e: Exception) {
        Log.w(TAG, "Error stopping Adhan player", e)
      }
      try {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID_ADHAN)
      } catch (_: Exception) {}
      try {
        context.stopService(Intent(context, SurahAudioService::class.java))
      } catch (e: Exception) {
        Log.w(TAG, "Error stopping SurahAudioService", e)
      }
    }

    fun startNightlySurahs(context: Context): Boolean {
      val intent = Intent(context, SurahAudioService::class.java).apply {
        action = ACTION_PLAY_NIGHTLY_SURAHS
      }
      return safeStartForegroundService(context, intent)
    }

    fun startFridayKahf(context: Context): Boolean {
      val intent = Intent(context, SurahAudioService::class.java).apply {
        action = ACTION_PLAY_FRIDAY_KAHF
      }
      return safeStartForegroundService(context, intent)
    }

    fun stopService(context: Context) {
      try {
        val intent = Intent(context, SurahAudioService::class.java)
        context.stopService(intent)
      } catch (e: Exception) {
        Log.w(TAG, "Error stopping service", e)
      }
    }

    private fun safeStartForegroundService(context: Context, intent: Intent): Boolean {
      return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
        true
      } catch (e: Exception) {
        Log.w(TAG, "Could not start foreground service (${intent.action}): ${e.message}")
        false
      }
    }
  }

  private var surahTitle: String = "Surah Recitation"

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent == null) {
      stopSelf()
      return START_NOT_STICKY
    }

    when (intent.action) {
      ACTION_PLAY_ADHAN -> {
        val prayer = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
        if (!safeStartForeground(NOTIFICATION_ID_ADHAN, buildAdhanNotification(prayer))) {
          stopSelf()
          return START_NOT_STICKY
        }
        SurahAudioPlayer.getInstance(this).playAdhan(prayer) {
          stopForegroundSafely()
          stopSelf()
        }
      }
      ACTION_STOP_ADHAN -> {
        SurahAudioPlayer.getInstance(this).stopAdhan()
        stopForegroundSafely()
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_PLAY_NIGHTLY_SURAHS -> {
        surahTitle = "Surah Al-Mulk & Surah Al-Waqi'ah"
        if (!safeStartForeground(NOTIFICATION_ID, buildNightlyNotification())) {
          stopSelf()
          return START_NOT_STICKY
        }
        SurahAudioPlayer.getInstance(this).playSequentialSurahs(
          listOf(SurahAudioCatalog.SURAH_MULK, SurahAudioCatalog.SURAH_WAQIAH)
        )
      }
      ACTION_PLAY_FRIDAY_KAHF -> {
        surahTitle = "Surah Al-Kahf (Blessed Friday)"
        if (!safeStartForeground(NOTIFICATION_ID, buildFridayKahfNotification())) {
          stopSelf()
          return START_NOT_STICKY
        }
        SurahAudioPlayer.getInstance(this).playSurah(SurahAudioCatalog.SURAH_KAHF)
      }
      ACTION_PLAY_PAUSE -> {
        SurahAudioPlayer.getInstance(this).togglePlayPause()
        updateNotification()
      }
      ACTION_STOP -> {
        SurahAudioPlayer.getInstance(this).stop()
        stopForegroundSafely()
        stopSelf()
        return START_NOT_STICKY
      }
      else -> {
        val name = intent.getStringExtra(EXTRA_SURAH_NAME)
        if (!name.isNullOrBlank()) {
          surahTitle = name
        }
        if (!safeStartForeground(NOTIFICATION_ID, buildNotification())) {
          stopSelf()
          return START_NOT_STICKY
        }
      }
    }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun safeStartForeground(id: Int, notification: Notification): Boolean {
    return try {
      startForeground(id, notification)
      true
    } catch (e: Exception) {
      Log.w(TAG, "startForeground failed for id=$id: ${e.message}")
      false
    }
  }

  private fun stopForegroundSafely() {
    try {
      ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    } catch (_: Exception) {}
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Noor Audio Playback",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Controls for background Adhan and Quranic audio recitation (100% Offline)"
        setShowBadge(false)
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun updateNotification() {
    try {
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.notify(NOTIFICATION_ID, buildNotification())
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update notification", e)
    }
  }

  private fun buildAdhanNotification(prayerName: String): Notification {
    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = PendingIntent.getBroadcast(
      this,
      303,
      Intent(this, com.example.notification.PrayerAlarmReceiver::class.java).apply {
        action = com.example.notification.PrayerAlarmReceiver.ACTION_STOP_ADHAN
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopAction = NotificationCompat.Action(
      android.R.drawable.ic_menu_close_clear_cancel,
      "Stop Adhan",
      stopIntent
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Adhan — Call to $prayerName Prayer")
      .setContentText("Melodic Makkah Grand Mosque Adhan • Playing Offline")
      .setContentIntent(contentIntent)
      .setOngoing(true)
      .addAction(stopAction)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()
  }

  private fun buildNightlyNotification(): Notification {
    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = PendingIntent.getBroadcast(
      this,
      304,
      Intent(this, com.example.notification.PrayerAlarmReceiver::class.java).apply {
        action = com.example.notification.PrayerAlarmReceiver.ACTION_STOP_ADHAN
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopAction = NotificationCompat.Action(
      android.R.drawable.ic_menu_close_clear_cancel,
      "Stop Recitation",
      stopIntent
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Nightly Surahs: Al-Mulk & Al-Waqi'ah")
      .setContentText("Reciter: Mishary Rashid Alafasy • 100% Offline Audio")
      .setContentIntent(contentIntent)
      .setOngoing(true)
      .addAction(stopAction)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()
  }

  private fun buildFridayKahfNotification(): Notification {
    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = PendingIntent.getBroadcast(
      this,
      305,
      Intent(this, com.example.notification.PrayerAlarmReceiver::class.java).apply {
        action = com.example.notification.PrayerAlarmReceiver.ACTION_STOP_ADHAN
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopAction = NotificationCompat.Action(
      android.R.drawable.ic_menu_close_clear_cancel,
      "Stop Recitation",
      stopIntent
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Blessed Friday: Surah Al-Kahf")
      .setContentText("Reciter: Mishary Rashid Alafasy • 100% Offline Audio")
      .setContentIntent(contentIntent)
      .setOngoing(true)
      .addAction(stopAction)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()
  }

  private fun buildNotification(): Notification {
    val isPlaying = SurahAudioPlayer.getInstance(this).isPlaying.value

    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val playPauseIntent = PendingIntent.getService(
      this,
      1,
      Intent(this, SurahAudioService::class.java).apply { action = ACTION_PLAY_PAUSE },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = PendingIntent.getService(
      this,
      2,
      Intent(this, SurahAudioService::class.java).apply { action = ACTION_STOP },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val playPauseAction = NotificationCompat.Action(
      android.R.drawable.ic_media_play,
      if (isPlaying) "Pause" else "Play",
      playPauseIntent
    )

    val stopAction = NotificationCompat.Action(
      android.R.drawable.ic_menu_close_clear_cancel,
      "Stop",
      stopIntent
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle(surahTitle)
      .setContentText("Qari: Mishary Rashid Alafasy • 100% Offline Audio")
      .setContentIntent(contentIntent)
      .setOngoing(isPlaying)
      .addAction(playPauseAction)
      .addAction(stopAction)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }
}
