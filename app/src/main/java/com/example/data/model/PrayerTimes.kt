package com.example.data.model

enum class PrayerName(val displayName: String, val arabicName: String) {
  FAJR("Fajr", "الفجر"),
  SUNRISE("Sunrise", "الشروق"),
  DHUHR("Dhuhr", "الظهر"),
  ASR("Asr", "العصر"),
  MAGHRIB("Maghrib", "المغرب"),
  ISHA("Isha", "العشاء")
}

data class SinglePrayerTime(
  val prayer: PrayerName,
  val timeFormatted: String, // HH:mm
  val hour: Int,
  val minute: Int,
  val isCompleted: Boolean = false
)

data class DayPrayerSchedule(
  val dateFormatted: String, // e.g. "Monday, 26 Oct 2026"
  val hijriFormatted: String, // e.g. "15 Jumada al-Awwal 1448"
  val prayers: List<SinglePrayerTime>,
  val nextPrayer: SinglePrayerTime?,
  val timeUntilNextFormatted: String // e.g. "2h 15m"
)

data class CityLocation(
  val name: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val timezoneOffset: Double
)

enum class CalculationMethod(val displayName: String, val fajrAngle: Double, val ishaAngle: Double) {
  MUSLIM_WORLD_LEAGUE("Muslim World League", 18.0, 17.0),
  ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
  EGYPTIAN("Egyptian General Authority", 19.5, 17.5),
  UMM_AL_QURA("Umm al-Qura (Makkah)", 18.5, 19.0), // 90 min after Maghrib
  KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0)
}
