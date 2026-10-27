package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import java.util.Calendar

object NotificationScheduler {

  private const val TAG = "NotificationScheduler"

  /**
   * Schedules alarms for Solah: 10 minutes prior to prayer time, exact prayer time,
   * plus Suhoor and Iftar alerts.
   */
  fun scheduleDailyPrayerAlarms(
    context: Context,
    schedule: DayPrayerSchedule,
    preWarningMinutes: Int = 10,
    isFastingToday: Boolean = true
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    schedule.prayers.forEach { prayerTime ->
      if (prayerTime.prayer == PrayerName.SUNRISE) return@forEach

      val prayerCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, prayerTime.hour)
        set(Calendar.MINUTE, prayerTime.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

      val now = System.currentTimeMillis()

      // 1. Pre-warning alarm (e.g. 10 minutes before)
      val preWarningTime = prayerCal.timeInMillis - (preWarningMinutes * 60 * 1000L)
      if (preWarningTime > now) {
        val preIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmReceiver.ACTION_PRE_PRAYER
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerTime.prayer.ordinal)
          putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_REMAINING, preWarningMinutes)
        }
        val prePending = PendingIntent.getBroadcast(
          context,
          prayerTime.prayer.ordinal * 10 + 1,
          preIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrStandardAlarm(alarmManager, preWarningTime, prePending)
      }

      // 2. Exact prayer time alarm
      if (prayerCal.timeInMillis > now) {
        val exactIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmReceiver.ACTION_EXACT_PRAYER
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerTime.prayer.ordinal)
        }
        val exactPending = PendingIntent.getBroadcast(
          context,
          prayerTime.prayer.ordinal * 10 + 2,
          exactIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrStandardAlarm(alarmManager, prayerCal.timeInMillis, exactPending)
      }

      // 3. Fasting: Suhoor alert 30 mins before Fajr
      if (prayerTime.prayer == PrayerName.FAJR && isFastingToday) {
        val suhoorTime = prayerCal.timeInMillis - (30 * 60 * 1000L)
        if (suhoorTime > now) {
          val suhoorIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_SUHOOR
            putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_REMAINING, 30)
          }
          val suhoorPending = PendingIntent.getBroadcast(
            context,
            901,
            suhoorIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setExactOrStandardAlarm(alarmManager, suhoorTime, suhoorPending)
        }
      }

      // 4. Fasting: Iftar alert at Maghrib
      if (prayerTime.prayer == PrayerName.MAGHRIB && isFastingToday) {
        if (prayerCal.timeInMillis > now) {
          val iftarIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_IFTAR
          }
          val iftarPending = PendingIntent.getBroadcast(
            context,
            902,
            iftarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setExactOrStandardAlarm(alarmManager, prayerCal.timeInMillis, iftarPending)
        }
      }
    }
  }

  /**
   * Schedules user's custom daily Quran recitation reminder time.
   */
  fun scheduleQuranRecitationReminder(
    context: Context,
    hour: Int = 20,
    minute: Int = 30,
    lastSurah: String = "Al-Fatihah",
    lastAyah: Int = 1
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val quranCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time passed today
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_QURAN_RECITE
      putExtra(PrayerAlarmReceiver.EXTRA_SURAH_NAME, lastSurah)
      putExtra(PrayerAlarmReceiver.EXTRA_AYAH_NUMBER, lastAyah)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      801,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setExactOrStandardAlarm(alarmManager, quranCal.timeInMillis, pendingIntent)
    Log.d(TAG, "Quran reminder scheduled for ${quranCal.time}")
  }

  private fun setExactOrStandardAlarm(alarmManager: AlarmManager, timeMillis: Long, pendingIntent: PendingIntent) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      }
    } catch (e: SecurityException) {
      Log.w(TAG, "Exact alarm permission not granted, using inexact fallback", e)
      alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    }
  }
}
