package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.model.PrayerName
import com.example.data.model.VerseCatalogue
import java.util.Calendar

class PrayerAlarmReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_PRE_PRAYER = "com.example.notification.ACTION_PRE_PRAYER"
    const val ACTION_EXACT_PRAYER = "com.example.notification.ACTION_EXACT_PRAYER"
    const val ACTION_SUHOOR = "com.example.notification.ACTION_SUHOOR"
    const val ACTION_IFTAR = "com.example.notification.ACTION_IFTAR"
    const val ACTION_QURAN_RECITE = "com.example.notification.ACTION_QURAN_RECITE"
    const val ACTION_SURAH_MULK_NIGHT = "com.example.notification.ACTION_SURAH_MULK_NIGHT"
    const val ACTION_SURAH_KAHF_FRIDAY = "com.example.notification.ACTION_SURAH_KAHF_FRIDAY"
    const val ACTION_DIRECT_PLAY_MULK = "com.example.notification.ACTION_DIRECT_PLAY_MULK"
    const val ACTION_DIRECT_PLAY_KAHF = "com.example.notification.ACTION_DIRECT_PLAY_KAHF"
    const val ACTION_QURAN_SPLIT_PROMPT = "com.example.notification.ACTION_QURAN_SPLIT_PROMPT"
    const val ACTION_MORNING_ADHKAR = "com.example.notification.ACTION_MORNING_ADHKAR"
    const val ACTION_EVENING_ADHKAR = "com.example.notification.ACTION_EVENING_ADHKAR"
    const val ACTION_DIRECT_PLAY_MORNING_ADHKAR = "com.example.notification.ACTION_DIRECT_PLAY_MORNING_ADHKAR"
    const val ACTION_DIRECT_PLAY_EVENING_ADHKAR = "com.example.notification.ACTION_DIRECT_PLAY_EVENING_ADHKAR"
    const val ACTION_STOP_ADHAN = "com.example.notification.ACTION_STOP_ADHAN"
    const val ACTION_NIGHTLY_SURAHS = "com.example.notification.ACTION_NIGHTLY_SURAHS"
    const val ACTION_DAILY_VERSE = "com.example.notification.ACTION_DAILY_VERSE"

    const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
    const val EXTRA_MINUTES_REMAINING = "EXTRA_MINUTES_REMAINING"
    const val EXTRA_SURAH_NAME = "EXTRA_SURAH_NAME"
    const val EXTRA_AYAH_NUMBER = "EXTRA_AYAH_NUMBER"
    const val EXTRA_DAY_NUMBER = "EXTRA_DAY_NUMBER"
    const val EXTRA_START_PAGE = "EXTRA_START_PAGE"
    const val EXTRA_END_PAGE = "EXTRA_END_PAGE"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    val pendingResult: PendingResult? = goAsync()
    try {
      Log.d("PrayerAlarmReceiver", "Received alarm action: ${intent.action}")
      NotificationHelper.createNotificationChannels(context)

      when (intent.action) {
        ACTION_PRE_PRAYER -> {
          if (AlarmPreferences.isPrayerPreWarningEnabled(context)) {
            val prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.ordinal)
            val prayer = PrayerName.values().getOrNull(prayerOrdinal) ?: PrayerName.FAJR
            val minutes = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 10)
            NotificationHelper.showPrayerPreWarningAlert(context, prayer, minutes)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_EXACT_PRAYER -> {
          if (AlarmPreferences.isPrayerAlertsEnabled(context)) {
            val prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.ordinal)
            val prayer = PrayerName.values().getOrNull(prayerOrdinal) ?: PrayerName.FAJR
            // showPrayerAlert displays the notification and triggers SurahAudioService.startAdhan once if enabled
            NotificationHelper.showPrayerAlert(context, prayer)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_STOP_ADHAN -> {
          com.example.audio.SurahAudioService.stopAdhan(context)
          com.example.audio.SurahAudioPlayer.getInstance(context).stop()
          NotificationHelper.cancelPrayerAlert(context)
          NotificationHelper.cancelNightlySurahsAlert(context)
        }

        ACTION_NIGHTLY_SURAHS -> {
          if (AlarmPreferences.isNightlySurahsEnabled(context)) {
            val h = AlarmPreferences.getNightlySurahsHour(context)
            val m = AlarmPreferences.getNightlySurahsMinute(context)
            val amPm = if (h >= 12) "PM" else "AM"
            val h12 = if (h % 12 == 0) 12 else h % 12
            val formatted = String.format(java.util.Locale.US, "%d:%02d %s", h12, m, amPm)
            NotificationHelper.showNightlySurahsAlert(context, formatted)
            com.example.audio.SurahAudioService.startNightlySurahs(context)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_SUHOOR -> {
          if (AlarmPreferences.isFastingAlertsEnabled(context)) {
            val minutes = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 30)
            NotificationHelper.showSuhoorAlert(context, minutes)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_IFTAR -> {
          if (AlarmPreferences.isFastingAlertsEnabled(context)) {
            NotificationHelper.showIftarAlert(context)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_QURAN_RECITE -> {
          val surah = intent.getStringExtra(EXTRA_SURAH_NAME)
            ?: AlarmPreferences.getLastSurah(context)
          val ayah = intent.getIntExtra(EXTRA_AYAH_NUMBER, AlarmPreferences.getLastAyah(context))
          NotificationHelper.showQuranRecitationAlert(context, surah, ayah)
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_SURAH_MULK_NIGHT -> {
          if (AlarmPreferences.isAutoPlayMulkEnabled(context)) {
            NotificationHelper.showSurahMulkNightAlert(context)
          }
          NotificationScheduler.scheduleSurahMulkNightAlarm(context)
        }

        ACTION_SURAH_KAHF_FRIDAY -> {
          if (AlarmPreferences.isAutoPlayKahfEnabled(context)) {
            NotificationHelper.showSurahKahfFridayAlert(context)
            com.example.audio.SurahAudioService.startFridayKahf(context)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_DIRECT_PLAY_MULK -> {
          com.example.audio.SurahAudioPlayer.getInstance(context).playSurah(
            com.example.audio.SurahAudioCatalog.SURAH_MULK
          )
        }

        ACTION_DIRECT_PLAY_KAHF -> {
          com.example.audio.SurahAudioPlayer.getInstance(context).playSurah(
            com.example.audio.SurahAudioCatalog.SURAH_KAHF
          )
        }

        ACTION_QURAN_SPLIT_PROMPT -> {
          val day = intent.getIntExtra(EXTRA_DAY_NUMBER, AlarmPreferences.getKhatmCurrentDay(context))
          val startPage = intent.getIntExtra(EXTRA_START_PAGE, 1)
          val endPage = intent.getIntExtra(EXTRA_END_PAGE, 20)
          NotificationHelper.showQuranSplitDailyPrompt(context, day, startPage, endPage)
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_MORNING_ADHKAR -> {
          NotificationHelper.showMorningAdhkarAlert(context)
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_EVENING_ADHKAR -> {
          NotificationHelper.showEveningAdhkarAlert(context)
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }

        ACTION_DIRECT_PLAY_MORNING_ADHKAR -> {
          com.example.audio.AdhkarAudioPlayer.getInstance(context).apply {
            setPeriod(com.example.data.model.AdhkarPeriod.MORNING)
            playCurrent()
          }
        }

        ACTION_DIRECT_PLAY_EVENING_ADHKAR -> {
          com.example.audio.AdhkarAudioPlayer.getInstance(context).apply {
            setPeriod(com.example.data.model.AdhkarPeriod.EVENING)
            playCurrent()
          }
        }

        ACTION_DAILY_VERSE -> {
          if (AlarmPreferences.isVerseAlertsEnabled(context)) {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val verse = VerseCatalogue.getVerseForDayOfYear(dayOfYear)
            NotificationHelper.showDailyVerseNotification(context, verse)
          }
          NotificationScheduler.rescheduleAllFromPreferences(context)
        }
      }
    } catch (e: Exception) {
      Log.e("PrayerAlarmReceiver", "Error handling alarm action ${intent.action}", e)
    } finally {
      pendingResult?.finish()
    }
  }
}
