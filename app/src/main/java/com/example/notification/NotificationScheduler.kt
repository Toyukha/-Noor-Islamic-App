package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.QuranKhatmPlanCatalogue
import java.util.Calendar

object NotificationScheduler {

  private const val TAG = "NotificationScheduler"

  // Strictly unique, collision-free PendingIntent request codes
  const val REQ_PRE_PRAYER_BASE = 100   // 100..109 (ordinal)
  const val REQ_EXACT_PRAYER_BASE = 200 // 200..209 (ordinal)
  const val REQ_QURAN_REMINDER = 401
  const val REQ_SURAH_MULK_NIGHT = 501
  const val REQ_SURAH_KAHF_FRIDAY = 601
  const val REQ_QURAN_SPLIT = 701
  const val REQ_MORNING_ADHKAR = 801
  const val REQ_EVENING_ADHKAR = 802
  const val REQ_SUHOOR = 901
  const val REQ_IFTAR = 902
  const val REQ_NIGHTLY_SURAHS = 950
  const val REQ_DAILY_VERSE = 999

  fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  /**
   * Recomputes prayer times for today and tomorrow from persisted preferences and
   * arms every enabled notification/alarm so the app continues firing reliably
   * across Day 2+, midnight transitions, and device reboots.
   */
  fun rescheduleAllFromPreferences(
    context: Context,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val city = PrayerCalculationEngine.getCityByName(AlarmPreferences.getCityName(context))
    val method = PrayerCalculationEngine.getMethodByName(AlarmPreferences.getCalculationMethod(context))

    val todayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val tomorrowCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      add(Calendar.DAY_OF_YEAR, 1)
    }

    val todaySchedule = PrayerCalculationEngine.calculatePrayerTimes(todayCal, city, method)
    val tomorrowSchedule = PrayerCalculationEngine.calculatePrayerTimes(tomorrowCal, city, method)

    scheduleDailyPrayerAlarms(
      context = context,
      schedule = todaySchedule,
      tomorrowSchedule = tomorrowSchedule,
      preWarningMinutes = AlarmPreferences.getPreWarningMinutes(context),
      isFastingToday = AlarmPreferences.isFastingActive(context),
      preWarningEnabled = AlarmPreferences.isPrayerPreWarningEnabled(context),
      prayerAlertsEnabled = AlarmPreferences.isPrayerAlertsEnabled(context),
      fastingAlertsEnabled = AlarmPreferences.isFastingAlertsEnabled(context),
      nowMillis = nowMillis
    )

    scheduleDailyAdhkarAlarms(
      context = context,
      schedule = todaySchedule,
      tomorrowSchedule = tomorrowSchedule,
      nowMillis = nowMillis
    )

    scheduleQuranRecitationReminder(
      context = context,
      hour = AlarmPreferences.getQuranReminderHour(context),
      minute = AlarmPreferences.getQuranReminderMinute(context),
      lastSurah = AlarmPreferences.getLastSurah(context),
      lastAyah = AlarmPreferences.getLastAyah(context),
      nowMillis = nowMillis
    )

    if (AlarmPreferences.isNightlySurahsEnabled(context)) {
      scheduleNightlySurahsAlarm(
        context = context,
        hour = AlarmPreferences.getNightlySurahsHour(context),
        minute = AlarmPreferences.getNightlySurahsMinute(context),
        nowMillis = nowMillis
      )
    } else {
      cancelNightlySurahs(context)
    }

    if (AlarmPreferences.isAutoPlayKahfEnabled(context)) {
      val sunrise = todaySchedule.sunriseTime ?: todaySchedule.prayers.find { it.prayer == PrayerName.SUNRISE }
      scheduleSurahKahfFridayAlarm(
        context = context,
        sunriseHour = sunrise?.hour ?: 6,
        sunriseMinute = sunrise?.minute ?: 15,
        nowMillis = nowMillis
      )
    } else {
      cancelSurahKahfFriday(context)
    }

    val curDay = AlarmPreferences.getKhatmCurrentDay(context)
    val durationMonths = AlarmPreferences.getKhatmDurationMonths(context)
    val split = QuranKhatmPlanCatalogue.getSplitForDayAndDuration(curDay, durationMonths)
    scheduleQuranSplitDailyPrompt(
      context = context,
      dayNumber = curDay,
      startPage = split.startPage,
      endPage = split.endPage,
      hour = AlarmPreferences.getKhatmSplitHour(context),
      minute = AlarmPreferences.getKhatmSplitMinute(context),
      nowMillis = nowMillis
    )

    if (AlarmPreferences.isVerseAlertsEnabled(context)) {
      scheduleDailyVerseAlarm(context, nowMillis = nowMillis)
    } else {
      cancelDailyVerse(context)
    }
  }

  /**
   * Schedules alarms for Solah (pre-warning + exact time) plus Suhoor and Iftar.
   * Automatically rolls forward to tomorrow's prayer time if today's time has passed.
   */
  fun scheduleDailyPrayerAlarms(
    context: Context,
    schedule: DayPrayerSchedule,
    preWarningMinutes: Int = 10,
    isFastingToday: Boolean = true,
    tomorrowSchedule: DayPrayerSchedule? = null,
    preWarningEnabled: Boolean = AlarmPreferences.isPrayerPreWarningEnabled(context),
    prayerAlertsEnabled: Boolean = AlarmPreferences.isPrayerAlertsEnabled(context),
    fastingAlertsEnabled: Boolean = AlarmPreferences.isFastingAlertsEnabled(context),
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    schedule.prayers.forEach { todayPrayer ->
      if (todayPrayer.prayer == PrayerName.SUNRISE) return@forEach

      val tomorrowPrayer = tomorrowSchedule?.prayers?.find { it.prayer == todayPrayer.prayer }

      fun nextOccurrenceMillis(offsetMinutes: Int): Long {
        val todayCal = Calendar.getInstance().apply {
          timeInMillis = nowMillis
          set(Calendar.HOUR_OF_DAY, todayPrayer.hour)
          set(Calendar.MINUTE, todayPrayer.minute)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          if (offsetMinutes != 0) add(Calendar.MINUTE, offsetMinutes)
        }
        if (todayCal.timeInMillis > nowMillis) {
          return todayCal.timeInMillis
        }

        // Roll forward to tomorrow's calculated time (or +1 day if tomorrowSchedule omitted)
        return Calendar.getInstance().apply {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, tomorrowPrayer?.hour ?: todayPrayer.hour)
          set(Calendar.MINUTE, tomorrowPrayer?.minute ?: todayPrayer.minute)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          if (offsetMinutes != 0) add(Calendar.MINUTE, offsetMinutes)
        }.timeInMillis
      }

      // 1. Pre-warning alarm (e.g., 10 minutes before)
      val preReqCode = REQ_PRE_PRAYER_BASE + todayPrayer.prayer.ordinal
      if (preWarningEnabled) {
        val preIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmReceiver.ACTION_PRE_PRAYER
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, todayPrayer.prayer.ordinal)
          putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_REMAINING, preWarningMinutes)
        }
        val prePending = PendingIntent.getBroadcast(
          context,
          preReqCode,
          preIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactOrStandardAlarm(alarmManager, nextOccurrenceMillis(-preWarningMinutes), prePending)
      } else {
        cancelIntent(context, alarmManager, preReqCode, PrayerAlarmReceiver.ACTION_PRE_PRAYER)
      }

      // 2. Exact prayer time alarm (uses AlarmClock when allowed for Doze & FGS exemption)
      val exactReqCode = REQ_EXACT_PRAYER_BASE + todayPrayer.prayer.ordinal
      if (prayerAlertsEnabled) {
        val exactIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmReceiver.ACTION_EXACT_PRAYER
          putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, todayPrayer.prayer.ordinal)
        }
        val exactPending = PendingIntent.getBroadcast(
          context,
          exactReqCode,
          exactIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarmClockOrExact(context, alarmManager, nextOccurrenceMillis(0), exactPending)
      } else {
        cancelIntent(context, alarmManager, exactReqCode, PrayerAlarmReceiver.ACTION_EXACT_PRAYER)
      }

      // 3. Fasting: Suhoor alert 30 mins before Fajr
      if (todayPrayer.prayer == PrayerName.FAJR) {
        if (fastingAlertsEnabled && isFastingToday) {
          val suhoorIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_SUHOOR
            putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_REMAINING, 30)
          }
          val suhoorPending = PendingIntent.getBroadcast(
            context,
            REQ_SUHOOR,
            suhoorIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setAlarmClockOrExact(context, alarmManager, nextOccurrenceMillis(-30), suhoorPending)
        } else {
          cancelIntent(context, alarmManager, REQ_SUHOOR, PrayerAlarmReceiver.ACTION_SUHOOR)
        }
      }

      // 4. Fasting: Iftar alert at Maghrib
      if (todayPrayer.prayer == PrayerName.MAGHRIB) {
        if (fastingAlertsEnabled && isFastingToday) {
          val iftarIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_IFTAR
          }
          val iftarPending = PendingIntent.getBroadcast(
            context,
            REQ_IFTAR,
            iftarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          setAlarmClockOrExact(context, alarmManager, nextOccurrenceMillis(0), iftarPending)
        } else {
          cancelIntent(context, alarmManager, REQ_IFTAR, PrayerAlarmReceiver.ACTION_IFTAR)
        }
      }
    }
  }

  /**
   * Schedules user's custom daily Quran recitation reminder time (request code 401).
   */
  fun scheduleQuranRecitationReminder(
    context: Context,
    hour: Int = 20,
    minute: Int = 30,
    lastSurah: String = "Al-Fatihah",
    lastAyah: Int = 1,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val quranCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_QURAN_RECITE
      putExtra(PrayerAlarmReceiver.EXTRA_SURAH_NAME, lastSurah)
      putExtra(PrayerAlarmReceiver.EXTRA_AYAH_NUMBER, lastAyah)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_QURAN_REMINDER,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setExactOrStandardAlarm(alarmManager, quranCal.timeInMillis, pendingIntent)
    Log.d(TAG, "Quran reminder scheduled for ${quranCal.time}")
  }

  fun cancelQuranReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    cancelIntent(context, alarmManager, REQ_QURAN_REMINDER, PrayerAlarmReceiver.ACTION_QURAN_RECITE)
  }

  /**
   * Schedules nightly Surah Al-Mulk alarm at 8:30 PM (20:30).
   */
  fun scheduleSurahMulkNightAlarm(
    context: Context,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val mulkCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 20)
      set(Calendar.MINUTE, 30)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_SURAH_MULK_NIGHT
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_SURAH_MULK_NIGHT,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setExactOrStandardAlarm(alarmManager, mulkCal.timeInMillis, pendingIntent)
    Log.d(TAG, "Surah Al-Mulk alarm scheduled for ${mulkCal.time}")
  }

  fun cancelSurahMulkNight(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    cancelIntent(context, alarmManager, REQ_SURAH_MULK_NIGHT, PrayerAlarmReceiver.ACTION_SURAH_MULK_NIGHT)
  }

  /**
   * Schedules nightly Surah Al-Mulk & Surah Al-Waqi'ah sequential playback (8:30 PM or 9:00 PM).
   */
  fun scheduleNightlySurahsAlarm(
    context: Context,
    hour: Int = 20,
    minute: Int = 30,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_NIGHTLY_SURAHS
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_NIGHTLY_SURAHS,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setAlarmClockOrExact(context, alarmManager, cal.timeInMillis, pendingIntent)
    Log.d(TAG, "Nightly Surahs (Mulk & Waqiah) scheduled for ${cal.time}")
  }

  fun cancelNightlySurahs(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    cancelIntent(context, alarmManager, REQ_NIGHTLY_SURAHS, PrayerAlarmReceiver.ACTION_NIGHTLY_SURAHS)
  }

  /**
   * Schedules Friday Surah Al-Kahf alarm before Shurooq (Sunrise) in user's timezone.
   */
  fun scheduleSurahKahfFridayAlarm(
    context: Context,
    sunriseHour: Int = 6,
    sunriseMinute: Int = 15,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, sunriseHour)
      set(Calendar.MINUTE, sunriseMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      add(Calendar.MINUTE, -30)

      while (get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY || timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_SURAH_KAHF_FRIDAY
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_SURAH_KAHF_FRIDAY,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setAlarmClockOrExact(context, alarmManager, cal.timeInMillis, pendingIntent)
    Log.d(TAG, "Friday Surah Al-Kahf alarm scheduled for ${cal.time}")
  }

  fun cancelSurahKahfFriday(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    cancelIntent(context, alarmManager, REQ_SURAH_KAHF_FRIDAY, PrayerAlarmReceiver.ACTION_SURAH_KAHF_FRIDAY)
  }

  /**
   * Schedules daily prompt for the Quran Khatm Split.
   */
  fun scheduleQuranSplitDailyPrompt(
    context: Context,
    dayNumber: Int,
    startPage: Int,
    endPage: Int,
    hour: Int = 19,
    minute: Int = 0,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_QURAN_SPLIT_PROMPT
      putExtra(PrayerAlarmReceiver.EXTRA_DAY_NUMBER, dayNumber)
      putExtra(PrayerAlarmReceiver.EXTRA_START_PAGE, startPage)
      putExtra(PrayerAlarmReceiver.EXTRA_END_PAGE, endPage)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_QURAN_SPLIT,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setExactOrStandardAlarm(alarmManager, cal.timeInMillis, pendingIntent)
    Log.d(TAG, "Quran Split prompt scheduled for ${cal.time}")
  }

  /**
   * Schedule daily Morning Adhkar (15m after Fajr) and Evening Adhkar (15m after Asr).
   */
  fun scheduleDailyAdhkarAlarms(
    context: Context,
    schedule: DayPrayerSchedule,
    tomorrowSchedule: DayPrayerSchedule? = null,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    // 1. Morning Adhkar: 15 minutes after Fajr
    val todayFajr = schedule.prayers.find { it.prayer == PrayerName.FAJR }
    val tomorrowFajr = tomorrowSchedule?.prayers?.find { it.prayer == PrayerName.FAJR }
    if (todayFajr != null) {
      val morningCal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, todayFajr.hour)
        set(Calendar.MINUTE, todayFajr.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, 15)
        if (timeInMillis <= nowMillis) {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, tomorrowFajr?.hour ?: todayFajr.hour)
          set(Calendar.MINUTE, tomorrowFajr?.minute ?: todayFajr.minute)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          add(Calendar.MINUTE, 15)
        }
      }
      val morningIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
        action = PrayerAlarmReceiver.ACTION_MORNING_ADHKAR
      }
      val morningPending = PendingIntent.getBroadcast(
        context,
        REQ_MORNING_ADHKAR,
        morningIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      setExactOrStandardAlarm(alarmManager, morningCal.timeInMillis, morningPending)
      Log.d(TAG, "Morning Adhkar scheduled for ${morningCal.time}")
    }

    // 2. Evening Adhkar: 15 minutes after Asr
    val todayAsr = schedule.prayers.find { it.prayer == PrayerName.ASR }
    val tomorrowAsr = tomorrowSchedule?.prayers?.find { it.prayer == PrayerName.ASR }
    if (todayAsr != null) {
      val eveningCal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, todayAsr.hour)
        set(Calendar.MINUTE, todayAsr.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, 15)
        if (timeInMillis <= nowMillis) {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, tomorrowAsr?.hour ?: todayAsr.hour)
          set(Calendar.MINUTE, tomorrowAsr?.minute ?: todayAsr.minute)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
          add(Calendar.MINUTE, 15)
        }
      }
      val eveningIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
        action = PrayerAlarmReceiver.ACTION_EVENING_ADHKAR
      }
      val eveningPending = PendingIntent.getBroadcast(
        context,
        REQ_EVENING_ADHKAR,
        eveningIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      setExactOrStandardAlarm(alarmManager, eveningCal.timeInMillis, eveningPending)
      Log.d(TAG, "Evening Adhkar scheduled for ${eveningCal.time}")
    }
  }

  /**
   * Schedules daily inspirational Quranic Verse notification at 8:00 AM.
   */
  fun scheduleDailyVerseAlarm(
    context: Context,
    hour: Int = 8,
    minute: Int = 0,
    nowMillis: Long = System.currentTimeMillis()
  ) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= nowMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = PrayerAlarmReceiver.ACTION_DAILY_VERSE
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQ_DAILY_VERSE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    setExactOrStandardAlarm(alarmManager, cal.timeInMillis, pendingIntent)
    Log.d(TAG, "Daily Verse scheduled for ${cal.time}")
  }

  fun cancelDailyVerse(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    cancelIntent(context, alarmManager, REQ_DAILY_VERSE, PrayerAlarmReceiver.ACTION_DAILY_VERSE)
  }

  private fun cancelIntent(
    context: Context,
    alarmManager: AlarmManager,
    requestCode: Int,
    action: String
  ) {
    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      this.action = action
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  private fun setAlarmClockOrExact(
    context: Context,
    alarmManager: AlarmManager,
    timeMillis: Long,
    pendingIntent: PendingIntent
  ) {
    val canExact = canScheduleExactAlarms(context)
    if (canExact) {
      try {
        val showIntent = PendingIntent.getActivity(
          context,
          0,
          Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          },
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
          AlarmManager.AlarmClockInfo(timeMillis, showIntent),
          pendingIntent
        )
        return
      } catch (e: SecurityException) {
        Log.w(TAG, "setAlarmClock permission issue, falling back", e)
      } catch (e: Exception) {
        Log.w(TAG, "setAlarmClock failed, falling back", e)
      }
    }
    setExactOrStandardAlarm(alarmManager, timeMillis, pendingIntent)
  }

  private fun setExactOrStandardAlarm(
    alarmManager: AlarmManager,
    timeMillis: Long,
    pendingIntent: PendingIntent
  ) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      }
    } catch (e: SecurityException) {
      Log.w(TAG, "Exact alarm permission not granted, using inexact while-idle fallback", e)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
      }
    }
  }
}
