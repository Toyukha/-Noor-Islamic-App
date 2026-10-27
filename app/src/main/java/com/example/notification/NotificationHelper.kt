package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DailyVerse
import com.example.data.model.PrayerName

object NotificationHelper {

  const val CHANNEL_PRAYERS = "noor_prayer_alerts"
  const val CHANNEL_FASTING = "noor_fasting_alerts"
  const val CHANNEL_QURAN = "noor_quran_reminders"
  const val CHANNEL_VERSE = "noor_daily_verse"

  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val prayerChannel = NotificationChannel(
        CHANNEL_PRAYERS,
        "Solah & Prayer Time Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifies 10 minutes before and at exact prayer times for Fajr, Dhuhr, Asr, Maghrib, and Isha"
        enableVibration(true)
      }

      val fastingChannel = NotificationChannel(
        CHANNEL_FASTING,
        "Fasting (Sawm) Suhoor & Iftar Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts for Suhoor meal cutoff before Fajr and joyous Iftar at Maghrib"
        enableVibration(true)
      }

      val quranChannel = NotificationChannel(
        CHANNEL_QURAN,
        "Quran Recitation Daily Reminders",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Scheduled daily reminders to maintain your Quran recitation (Tilawah)"
        enableVibration(true)
      }

      val verseChannel = NotificationChannel(
        CHANNEL_VERSE,
        "Daily Quranic Verse & Reflections",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Daily inspirational Quranic verses with contemplation prompts"
      }

      notificationManager.createNotificationChannel(prayerChannel)
      notificationManager.createNotificationChannel(fastingChannel)
      notificationManager.createNotificationChannel(quranChannel)
      notificationManager.createNotificationChannel(verseChannel)
    }
  }

  private fun createContentIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  /**
   * 10-Minute Pre-warning notification for Solah
   */
  fun showPrayerPreWarningAlert(context: Context, prayer: PrayerName, minutesRemaining: Int = 10) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_PRAYERS)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("10m Until ${prayer.displayName} (${prayer.arabicName})")
      .setContentText("Prepare for wudu and prayer. Solah begins in $minutesRemaining minutes.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Prepare for wudu and prayer. ${prayer.displayName} (${prayer.arabicName}) will be called in $minutesRemaining minutes.\n\n\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" (4:103)"
        )
      )
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(prayer.ordinal + 200, notification)
  }

  /**
   * Exact Prayer Time Alert
   */
  fun showPrayerAlert(context: Context, prayer: PrayerName) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_PRAYERS)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Time for ${prayer.displayName} (${prayer.arabicName})")
      .setContentText("The call to prayer has begun. Hayya 'alas-Salah, Hayya 'alal-Falah.")
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(prayer.ordinal + 100, notification)
  }

  /**
   * Suhoor Alert (pre-Fajr fasting meal alert)
   */
  fun showSuhoorAlert(context: Context, minutesUntilFajr: Int = 30) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_FASTING)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Suhoor Blessing Reminder (السحور)")
      .setContentText("Fajr approaches in $minutesUntilFajr minutes. Conclude your meal and renew your intention.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Fajr approaches in $minutesUntilFajr minutes. Conclude your meal and renew your intention.\n\nThe Prophet ﷺ said: 'Take Suhoor, for indeed in Suhoor there is blessing.' (Bukhari & Muslim)"
        )
      )
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(301, notification)
  }

  /**
   * Iftar Alert (sunset breaking of the fast)
   */
  fun showIftarAlert(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_FASTING)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Iftar Mubarak! Time to Break Fast (الإفطار)")
      .setContentText("The sun has set. Dhahaba adh-dhama'u wabtallat al-'urooq...")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "The sun has set and Maghrib has arrived. May Allah accept your fast!\n\n\"ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ\"\n(The thirst is gone, the veins are wet, and the reward is confirmed, if Allah wills)."
        )
      )
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(302, notification)
  }

  /**
   * Daily Quran Recitation Reminder
   */
  fun showQuranRecitationAlert(context: Context, lastSurah: String = "Al-Fatihah", lastAyah: Int = 1) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Daily Quran Recitation Time (تلاوة القرآن)")
      .setContentText("Continue your spiritual journey from Surah $lastSurah, Ayah $lastAyah.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Continue your spiritual journey from Surah $lastSurah, Ayah $lastAyah.\n\n\"This is a blessed Book which We have revealed to you, that they might reflect upon its verses.\" (38:29)"
        )
      )
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(401, notification)
  }

  fun showDailyVerseNotification(context: Context, verse: DailyVerse) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val pendingIntent = createContentIntent(context)

    val notification = NotificationCompat.Builder(context, CHANNEL_VERSE)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Daily Verse • Surah ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})")
      .setContentText(verse.englishTranslation)
      .setStyle(NotificationCompat.BigTextStyle().bigText("${verse.arabicText}\n\n\"${verse.englishTranslation}\"\n\nReflect: ${verse.reflectionPrompt}"))
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(999, notification)
  }
}
