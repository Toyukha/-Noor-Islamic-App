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
import com.example.ui.components.AlertType
import com.example.ui.components.FloatingAlertPayload

object NotificationHelper {

  const val CHANNEL_PRAYERS = "noor_prayer_alerts"
  const val CHANNEL_FASTING = "noor_fasting_alerts"
  const val CHANNEL_QURAN = "noor_quran_reminders"
  const val CHANNEL_VERSE = "noor_daily_verse"
  const val CHANNEL_ADHKAR = "noor_adhkar_alerts"

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

      val adhkarChannel = NotificationChannel(
        CHANNEL_ADHKAR,
        "Morning & Evening Adhkar (Remembrance)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Daily morning (after Fajr) and evening (after Asr) protective supplications and auto-reading reminders"
        enableVibration(true)
      }

      notificationManager.createNotificationChannel(prayerChannel)
      notificationManager.createNotificationChannel(fastingChannel)
      notificationManager.createNotificationChannel(quranChannel)
      notificationManager.createNotificationChannel(verseChannel)
      notificationManager.createNotificationChannel(adhkarChannel)
    }
  }

  private fun createContentIntent(
    context: Context,
    requestCode: Int = 0,
    payload: FloatingAlertPayload? = null
  ): PendingIntent {
    if (payload != null) {
      return FloatingAlertController.createAlertPendingIntent(context, requestCode, payload)
    }
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  /**
   * 10-Minute Pre-warning notification for Solah
   */
  fun showPrayerPreWarningAlert(context: Context, prayer: PrayerName, minutesRemaining: Int = 10) {
    val payload = FloatingAlertPayload(
      id = "SOLAH_${prayer.name}",
      type = AlertType.SOLAH_PRE_WARNING,
      categoryLabel = "UPCOMING SOLAH • ${minutesRemaining}M",
      title = "${prayer.displayName} in $minutesRemaining minutes",
      arabicSubtitle = prayer.arabicName,
      message = "Prepare for wudu and enter a state of calm focus before ${prayer.displayName} begins.",
      actionLabel = "Mark Prayed",
      targetTab = "TODAY",
      prayerOrdinal = prayer.ordinal
    )
    val pendingIntent = createContentIntent(context, prayer.ordinal + 2200, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_PRAYERS)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("${minutesRemaining}m Until ${prayer.displayName} (${prayer.arabicName})")
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

    safeNotify(context, prayer.ordinal + 200, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Exact Prayer Time Alert with Adhan Audio and Stop Control
   */
  fun showPrayerAlert(context: Context, prayer: PrayerName) {
    val prefs = context.getSharedPreferences("noor_app_prefs", Context.MODE_PRIVATE)
    val adhanAudioEnabled = prefs.getBoolean("key_adhan_audio_enabled", true)

    val payload = FloatingAlertPayload(
      id = "EXACT_${prayer.name}",
      type = AlertType.SOLAH_EXACT,
      categoryLabel = "ADHAN • PRAYER TIME",
      title = "Time for ${prayer.displayName} Prayer",
      arabicSubtitle = prayer.arabicName,
      message = "Hayya 'alas-Salah, Hayya 'alal-Falah. Stand in serene devotion for ${prayer.displayName}.",
      actionLabel = "Mark ${prayer.displayName} Prayed",
      targetTab = "TODAY",
      prayerOrdinal = prayer.ordinal,
      hasStopAudio = adhanAudioEnabled
    )
    val pendingIntent = createContentIntent(context, prayer.ordinal + 2100, payload)

    val stopIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_STOP_ADHAN
    }
    val stopPendingIntent = PendingIntent.getBroadcast(
      context,
      prayer.ordinal + 500,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_PRAYERS)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Adhan: Time for ${prayer.displayName} (${prayer.arabicName})")
      .setContentText("Hayya 'alas-Salah, Hayya 'alal-Falah. The call to prayer is sounding.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "The call to prayer has begun for ${prayer.displayName} (${prayer.arabicName}).\n\n\"Hayya 'alas-Salah, Hayya 'alal-Falah\"\n(Come to prayer, come to success)\n\nTap 'Stop Adhan' to silence audio."
        )
      )
      .setContentIntent(pendingIntent)
      .addAction(android.R.drawable.ic_media_pause, "Stop Adhan", stopPendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, prayer.ordinal + 100, notification)

    if (adhanAudioEnabled) {
      try {
        com.example.audio.SurahAudioService.startAdhan(context, prayer.displayName)
      } catch (e: Exception) {
        android.util.Log.w("NotificationHelper", "Could not start Adhan service", e)
      }
    }
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  fun cancelPrayerAlert(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    PrayerName.values().forEach {
      notificationManager.cancel(it.ordinal + 100)
    }
  }

  /**
   * Suhoor Alert (pre-Fajr fasting meal alert)
   */
  fun showSuhoorAlert(context: Context, minutesUntilFajr: Int = 30) {
    val payload = FloatingAlertPayload(
      id = "FASTING_SUHOOR",
      type = AlertType.SUHOOR_FASTING,
      categoryLabel = "SUNNAH SUHOOR",
      title = "Suhoor Blessing Reminder",
      arabicSubtitle = "السحور",
      message = "Fajr approaches in $minutesUntilFajr minutes. Conclude your meal and renew your fasting intention.",
      actionLabel = "Open Today",
      targetTab = "TODAY"
    )
    val pendingIntent = createContentIntent(context, 2301, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_FASTING)
      .setSmallIcon(R.drawable.ic_stat_noor)
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

    safeNotify(context, 301, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Iftar Alert (sunset breaking of the fast)
   */
  fun showIftarAlert(context: Context) {
    val payload = FloatingAlertPayload(
      id = "FASTING_IFTAR",
      type = AlertType.IFTAR_FASTING,
      categoryLabel = "MAGHRIB IFTAR",
      title = "Iftar Mubarak • Time to Break Fast",
      arabicSubtitle = "الإفطار",
      message = "Maghrib has arrived. May Allah accept your fast and answer your supplications at this blessed hour.",
      actionLabel = "Open Noor",
      targetTab = "TODAY"
    )
    val pendingIntent = createContentIntent(context, 2302, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_FASTING)
      .setSmallIcon(R.drawable.ic_stat_noor)
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

    safeNotify(context, 302, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Daily Quran Recitation Reminder
   */
  fun showQuranRecitationAlert(context: Context, lastSurah: String = "Al-Fatihah", lastAyah: Int = 1) {
    val payload = FloatingAlertPayload(
      id = "QURAN_DAILY",
      type = AlertType.QURAN_RECITE,
      categoryLabel = "QURAN TILAWAH",
      title = "Daily Quran Recitation Time",
      arabicSubtitle = "تلاوة القرآن",
      message = "Continue from Surah $lastSurah, Ayah $lastAyah. Dedicate 10 peaceful minutes to reflection.",
      actionLabel = "Open Quran",
      targetTab = "QURAN",
      openQuranFullPage = true
    )
    val pendingIntent = createContentIntent(context, 2401, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.drawable.ic_stat_noor)
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

    safeNotify(context, 401, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  fun showDailyVerseNotification(context: Context, verse: DailyVerse) {
    val payload = FloatingAlertPayload(
      id = "DAILY_VERSE_${verse.surahNumber}_${verse.ayahNumber}",
      type = AlertType.DAILY_VERSE,
      categoryLabel = "DAILY TADABBUR",
      title = "Surah ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})",
      arabicSubtitle = verse.arabicText.take(40),
      message = "\"${verse.englishTranslation}\"",
      actionLabel = "Read Reflection",
      targetTab = "TADABBUR"
    )
    val pendingIntent = createContentIntent(context, 2999, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_VERSE)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Daily Verse • Surah ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})")
      .setContentText(verse.englishTranslation)
      .setStyle(NotificationCompat.BigTextStyle().bigText("${verse.arabicText}\n\n\"${verse.englishTranslation}\"\n\nReflect: ${verse.reflectionPrompt}"))
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 999, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Nightly Surah Al-Mulk Reminder & Playback (8:30 PM after Isha)
   */
  fun showSurahMulkNightAlert(context: Context) {
    val payload = FloatingAlertPayload(
      id = "SURAH_MULK_NIGHT",
      type = AlertType.SURAH_RECITATION,
      categoryLabel = "NIGHTLY SUNNAH",
      title = "Surah Al-Mulk Time",
      arabicSubtitle = "سورة الملك",
      message = "Listen to or recite Surah Al-Mulk before sleep — the protector from the trial of the grave.",
      actionLabel = "Open Quran",
      targetTab = "QURAN"
    )
    val contentIntent = createContentIntent(context, 2501, payload)

    val playIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_DIRECT_PLAY_MULK
    }
    val playPendingIntent = PendingIntent.getBroadcast(
      context,
      1501,
      playIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Surah Al-Mulk Time (سورة الملك)")
      .setContentText("It is 8:30 PM. Listen to Surah Al-Mulk, the protector from the grave.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "\"Indeed, there is a Surah of thirty verses which intercedes for a person until he is forgiven: Tabarak alladhi bi-yadihi al-Mulk...\" (Tirmidhi)\n\nTap 'Play Audio' to listen to the offline recitation by Mishary Rashid Alafasy."
        )
      )
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.ic_media_play, "Play Audio", playPendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 501, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Nightly Surahs Alert: Sequential Surah Al-Mulk & Surah Al-Waqi'ah post-Isha (8:30 PM or 9:00 PM)
   */
  fun showNightlySurahsAlert(context: Context, timeFormatted: String = "8:30 PM") {
    val payload = FloatingAlertPayload(
      id = "NIGHTLY_SURAHS",
      type = AlertType.SURAH_RECITATION,
      categoryLabel = "NIGHTLY SUNNAH • $timeFormatted",
      title = "Nightly Surahs: Al-Mulk & Al-Waqi'ah",
      arabicSubtitle = "الملك والواقعة",
      message = "Sequential offline recitation is starting. Reflect and wind down your evening in tranquility.",
      actionLabel = "Open Quran",
      targetTab = "QURAN",
      hasStopAudio = true
    )
    val contentIntent = createContentIntent(context, 2950, payload)

    val stopIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_STOP_ADHAN
    }
    val stopPendingIntent = PendingIntent.getBroadcast(
      context,
      951,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Nightly Surahs: Al-Mulk & Al-Waqi'ah")
      .setContentText("It is $timeFormatted. Reciting Surah Al-Mulk followed by Surah Al-Waqi'ah.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Nightly Sunnah Recitation:\n• Surah Al-Mulk (67) — Intercedes until forgiven & protects from grave\n• Surah Al-Waqi'ah (56) — Sunnah protection against hardship\n\nSequential audio recitation playing now."
        )
      )
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.ic_media_pause, "Stop Audio", stopPendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 950, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  fun cancelNightlySurahsAlert(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(950)
  }

  /**
   * Blessed Friday Surah Al-Kahf Reminder & Playback (before Shurooq / sunrise)
   */
  fun showSurahKahfFridayAlert(context: Context) {
    val payload = FloatingAlertPayload(
      id = "SURAH_KAHF_FRIDAY",
      type = AlertType.SURAH_RECITATION,
      categoryLabel = "BLESSED FRIDAY",
      title = "Surah Al-Kahf Illumination",
      arabicSubtitle = "سورة الكهف",
      message = "Recite or listen to Surah Al-Kahf before sunset for light between the two Fridays.",
      actionLabel = "Open Quran",
      targetTab = "QURAN",
      hasStopAudio = true
    )
    val contentIntent = createContentIntent(context, 2601, payload)

    val playIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_DIRECT_PLAY_KAHF
    }
    val playPendingIntent = PendingIntent.getBroadcast(
      context,
      1601,
      playIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Blessed Friday: Surah Al-Kahf (سورة الكهف)")
      .setContentText("Recite or listen to Surah Al-Kahf before sunset.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "The Prophet ﷺ said:\n\"Whoever reads Surah Al-Kahf on Friday, Allah will illuminate for him light between the two Fridays.\" (Al-Hakim & Bayhaqi)\n\nTap 'Listen Now' to begin offline recitation."
        )
      )
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.ic_media_play, "Listen Now", playPendingIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 601, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Quran Khatm Split Daily Prompt
   */
  fun showQuranSplitDailyPrompt(context: Context, dayNumber: Int, startPage: Int, endPage: Int) {
    val payload = FloatingAlertPayload(
      id = "QURAN_SPLIT_DAY_$dayNumber",
      type = AlertType.QURAN_RECITE,
      categoryLabel = "KHATM PLAN • DAY $dayNumber",
      title = "Read Pages $startPage – $endPage Today",
      arabicSubtitle = "ختمة القرآن",
      message = "Complete today's guided pages to stay on track with your Khatm al-Quran journey.",
      actionLabel = "Open Today's Pages",
      targetTab = "QURAN",
      openQuranFullPage = true
    )
    val contentIntent = createContentIntent(context, 2701, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_QURAN)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Day $dayNumber: Read Pages $startPage – $endPage")
      .setContentText("Complete today's guided pages to stay on track with your Khatm al-Quran plan.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Quran Khatm Split:\nToday's target: Pages $startPage to $endPage.\n\n\"The one who recites the Quran beautifully, smoothly, and precisely will be in the company of the noble and obedient angels.\" (Bukhari)"
        )
      )
      .setContentIntent(contentIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 701, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Morning Adhkar Alert (Post-Fajr)
   */
  fun showMorningAdhkarAlert(context: Context) {
    val payload = FloatingAlertPayload(
      id = "MORNING_ADHKAR",
      type = AlertType.ADHKAR_REMINDER,
      categoryLabel = "MORNING REMEMBRANCE",
      title = "Morning Adhkar Time",
      arabicSubtitle = "أذكار الصباح",
      message = "Begin your morning under Allah's protection with peaceful supplications and remembrance.",
      actionLabel = "Open Adhkar Tracker",
      targetTab = "TRACKER"
    )
    val contentIntent = createContentIntent(context, 2801, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_ADHKAR)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Morning Adhkar • أذكار الصباح")
      .setContentText("Begin your day under the divine protection of Allah.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Time for Morning Remembrance (أذكار الصباح):\n\n\"O you who have believed, remember Allah with much remembrance and exalt Him morning and afternoon.\" (Al-Ahzab 33:41-42)\n\nOpen Noor to recite your adhkar and mark it in your Daily Tracker."
        )
      )
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.ic_input_add, "Open Tracker", contentIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 801, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  /**
   * Evening Adhkar Alert (Post-Asr)
   */
  fun showEveningAdhkarAlert(context: Context) {
    val payload = FloatingAlertPayload(
      id = "EVENING_ADHKAR",
      type = AlertType.ADHKAR_REMINDER,
      categoryLabel = "EVENING REMEMBRANCE",
      title = "Evening Adhkar Time",
      arabicSubtitle = "أذكار المساء",
      message = "Fortify your evening and night with heartfelt praise and protective remembrance of Allah.",
      actionLabel = "Open Adhkar Tracker",
      targetTab = "TRACKER"
    )
    val contentIntent = createContentIntent(context, 2802, payload)

    val notification = NotificationCompat.Builder(context, CHANNEL_ADHKAR)
      .setSmallIcon(R.drawable.ic_stat_noor)
      .setContentTitle("Evening Adhkar • أذكار المساء")
      .setContentText("Fortify your evening and night in the remembrance of Allah.")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Time for Evening Remembrance (أذكار المساء):\n\n\"And remember your Lord within yourself in humility and in fear... in the mornings and the evenings.\" (Al-A'raf 7:205)\n\nOpen Noor to recite your adhkar and mark it in your Daily Tracker."
        )
      )
      .setContentIntent(contentIntent)
      .addAction(android.R.drawable.ic_input_add, "Open Tracker", contentIntent)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()

    safeNotify(context, 802, notification)
    FloatingAlertController.showOnScreenPopup(context, payload)
  }

  private fun safeNotify(context: Context, id: Int, notification: android.app.Notification) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) return
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
      notificationManager.notify(id, notification)
    } catch (e: SecurityException) {
      android.util.Log.w("NotificationHelper", "Notification permission denied for id=$id", e)
    } catch (e: Exception) {
      android.util.Log.w("NotificationHelper", "Failed showing notification id=$id", e)
    }
  }
}
