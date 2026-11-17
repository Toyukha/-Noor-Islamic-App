package com.example.notification

import android.content.Context
import android.content.SharedPreferences

object AlarmPreferences {
  const val PREFS_NAME = "noor_app_prefs"

  private const val KEY_CITY_NAME = "key_alarm_city_name"
  private const val KEY_CALC_METHOD = "key_alarm_calc_method"
  private const val KEY_PRE_WARNING_ENABLED = "key_prayer_pre_warning_enabled"
  private const val KEY_PRE_WARNING_MINUTES = "key_prayer_pre_warning_minutes"
  private const val KEY_PRAYER_ALERTS_ENABLED = "key_prayer_alerts_enabled"
  private const val KEY_FASTING_ALERTS_ENABLED = "key_fasting_alerts_enabled"
  private const val KEY_VERSE_ALERTS_ENABLED = "key_verse_alerts_enabled"
  private const val KEY_ADHAN_AUDIO_ENABLED = "key_adhan_audio_enabled"
  private const val KEY_IS_FASTING_TODAY = "key_is_fasting_today"
  private const val KEY_FASTING_LOGGED_DATE = "key_fasting_logged_date"
  private const val KEY_QURAN_REMINDER_HOUR = "key_quran_reminder_hour"
  private const val KEY_QURAN_REMINDER_MINUTE = "key_quran_reminder_minute"
  private const val KEY_LAST_SURAH = "key_last_surah"
  private const val KEY_LAST_AYAH = "key_last_ayah"
  private const val KEY_NIGHTLY_SURAHS_ENABLED = "key_nightly_surahs_enabled"
  private const val KEY_NIGHTLY_SURAHS_HOUR = "key_nightly_surahs_hour"
  private const val KEY_NIGHTLY_SURAHS_MINUTE = "key_nightly_surahs_minute"
  private const val KEY_AUTO_PLAY_MULK_ENABLED = "key_auto_play_mulk_enabled"
  private const val KEY_AUTO_PLAY_KAHF_ENABLED = "key_auto_play_kahf_enabled"
  private const val KEY_KHATM_SPLIT_HOUR = "key_quran_split_reminder_hour"
  private const val KEY_KHATM_SPLIT_MINUTE = "key_quran_split_reminder_minute"
  private const val KEY_KHATM_CURRENT_DAY = "key_quran_khatm_current_day"
  private const val KEY_KHATM_DURATION_MONTHS = "key_quran_khatm_duration_months"
  private const val KEY_FLOATING_POPUP_ENABLED = "key_floating_popup_overlay_enabled"
  private const val KEY_QURAN_NIGHT_MODE = "key_quran_reader_night_mode"
  private const val KEY_QURAN_FONT_SIZE_SP = "key_quran_reader_font_size_sp"

  fun prefs(context: Context): SharedPreferences {
    return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun getCityName(context: Context): String =
    prefs(context).getString(KEY_CITY_NAME, "Mecca") ?: "Mecca"

  fun setCityName(context: Context, city: String) {
    prefs(context).edit().putString(KEY_CITY_NAME, city).apply()
  }

  fun getCalculationMethod(context: Context): String =
    prefs(context).getString(KEY_CALC_METHOD, "MUSLIM_WORLD_LEAGUE") ?: "MUSLIM_WORLD_LEAGUE"

  fun setCalculationMethod(context: Context, method: String) {
    prefs(context).edit().putString(KEY_CALC_METHOD, method).apply()
  }

  fun isPrayerPreWarningEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_PRE_WARNING_ENABLED, true)

  fun setPrayerPreWarningEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_PRE_WARNING_ENABLED, enabled).apply()
  }

  fun getPreWarningMinutes(context: Context): Int =
    prefs(context).getInt(KEY_PRE_WARNING_MINUTES, 10)

  fun setPreWarningMinutes(context: Context, minutes: Int) {
    prefs(context).edit().putInt(KEY_PRE_WARNING_MINUTES, minutes).apply()
  }

  fun isPrayerAlertsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_PRAYER_ALERTS_ENABLED, true)

  fun setPrayerAlertsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_PRAYER_ALERTS_ENABLED, enabled).apply()
  }

  fun isFastingAlertsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_FASTING_ALERTS_ENABLED, true)

  fun setFastingAlertsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_FASTING_ALERTS_ENABLED, enabled).apply()
  }

  fun isVerseAlertsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_VERSE_ALERTS_ENABLED, true)

  fun setVerseAlertsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_VERSE_ALERTS_ENABLED, enabled).apply()
  }

  fun isAdhanAudioEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_ADHAN_AUDIO_ENABLED, true)

  fun setAdhanAudioEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_ADHAN_AUDIO_ENABLED, enabled).apply()
  }

  fun setFastingState(context: Context, isFasting: Boolean, dateStr: String) {
    prefs(context).edit()
      .putBoolean(KEY_IS_FASTING_TODAY, isFasting)
      .putString(KEY_FASTING_LOGGED_DATE, dateStr)
      .apply()
  }

  fun isFastingActive(context: Context): Boolean {
    return prefs(context).getBoolean(KEY_IS_FASTING_TODAY, false)
  }

  fun getQuranReminderHour(context: Context): Int =
    prefs(context).getInt(KEY_QURAN_REMINDER_HOUR, 20)

  fun getQuranReminderMinute(context: Context): Int =
    prefs(context).getInt(KEY_QURAN_REMINDER_MINUTE, 30)

  fun setQuranReminderTime(context: Context, hour: Int, minute: Int) {
    prefs(context).edit()
      .putInt(KEY_QURAN_REMINDER_HOUR, hour)
      .putInt(KEY_QURAN_REMINDER_MINUTE, minute)
      .apply()
  }

  fun getLastSurah(context: Context): String =
    prefs(context).getString(KEY_LAST_SURAH, "Al-Fatihah") ?: "Al-Fatihah"

  fun getLastAyah(context: Context): Int =
    prefs(context).getInt(KEY_LAST_AYAH, 1)

  fun setBookmark(context: Context, surah: String, ayah: Int) {
    prefs(context).edit()
      .putString(KEY_LAST_SURAH, surah)
      .putInt(KEY_LAST_AYAH, ayah)
      .apply()
  }

  fun isNightlySurahsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NIGHTLY_SURAHS_ENABLED, true)

  fun setNightlySurahsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NIGHTLY_SURAHS_ENABLED, enabled).apply()
  }

  fun getNightlySurahsHour(context: Context): Int =
    prefs(context).getInt(KEY_NIGHTLY_SURAHS_HOUR, 20)

  fun getNightlySurahsMinute(context: Context): Int =
    prefs(context).getInt(KEY_NIGHTLY_SURAHS_MINUTE, 30)

  fun setNightlySurahsTime(context: Context, hour: Int, minute: Int) {
    prefs(context).edit()
      .putInt(KEY_NIGHTLY_SURAHS_HOUR, hour)
      .putInt(KEY_NIGHTLY_SURAHS_MINUTE, minute)
      .apply()
  }

  fun isAutoPlayMulkEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_AUTO_PLAY_MULK_ENABLED, true)

  fun setAutoPlayMulkEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_AUTO_PLAY_MULK_ENABLED, enabled).apply()
  }

  fun isAutoPlayKahfEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_AUTO_PLAY_KAHF_ENABLED, true)

  fun setAutoPlayKahfEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_AUTO_PLAY_KAHF_ENABLED, enabled).apply()
  }

  fun getKhatmSplitHour(context: Context): Int =
    prefs(context).getInt(KEY_KHATM_SPLIT_HOUR, 19)

  fun getKhatmSplitMinute(context: Context): Int =
    prefs(context).getInt(KEY_KHATM_SPLIT_MINUTE, 0)

  fun setKhatmSplitTime(context: Context, hour: Int, minute: Int) {
    prefs(context).edit()
      .putInt(KEY_KHATM_SPLIT_HOUR, hour)
      .putInt(KEY_KHATM_SPLIT_MINUTE, minute)
      .apply()
  }

  fun getKhatmCurrentDay(context: Context): Int =
    prefs(context).getInt(KEY_KHATM_CURRENT_DAY, 1).coerceAtLeast(1)

  fun getKhatmDurationMonths(context: Context): Int =
    prefs(context).getInt(KEY_KHATM_DURATION_MONTHS, 1).coerceIn(1, 3)

  fun isFloatingPopupEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_FLOATING_POPUP_ENABLED, true)

  fun setFloatingPopupEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_FLOATING_POPUP_ENABLED, enabled).apply()
  }

  fun isQuranNightMode(context: Context): Boolean =
    prefs(context).getBoolean(KEY_QURAN_NIGHT_MODE, false)

  fun setQuranNightMode(context: Context, nightMode: Boolean) {
    prefs(context).edit().putBoolean(KEY_QURAN_NIGHT_MODE, nightMode).apply()
  }

  fun getQuranFontSizeSp(context: Context): Int =
    prefs(context).getInt(KEY_QURAN_FONT_SIZE_SP, 22).coerceIn(18, 30)

  fun setQuranFontSizeSp(context: Context, fontSizeSp: Int) {
    prefs(context).edit().putInt(KEY_QURAN_FONT_SIZE_SP, fontSizeSp.coerceIn(18, 30)).apply()
  }
}
