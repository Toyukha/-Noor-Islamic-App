package com.example.data.calculation

import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime
import java.util.Calendar
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalculationEngine {

  val PRESET_CITIES = listOf(
    CityLocation("Mecca", "Saudi Arabia", 21.4225, 39.8262, 3.0),
    CityLocation("Medina", "Saudi Arabia", 24.4672, 39.6111, 3.0),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 2.0),
    CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0),
    CityLocation("Dubai", "UAE", 25.2048, 55.2708, 4.0),
    CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0),
    CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0),
    CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0),
    CityLocation("London", "United Kingdom", 51.5074, -0.1278, 0.0),
    CityLocation("New York", "United States", 40.7128, -74.0060, -5.0),
    CityLocation("Toronto", "Canada", 43.6532, -79.3832, -5.0),
    CityLocation("Lagos", "Nigeria", 6.5244, 3.3792, 1.0),
    CityLocation("Paris", "France", 48.8566, 2.3522, 1.0)
  )

  fun getCityByName(name: String): CityLocation {
    return PRESET_CITIES.firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: PRESET_CITIES[0] // Default to Mecca
  }

  fun getMethodByName(name: String): CalculationMethod {
    return CalculationMethod.values().firstOrNull { it.name.equals(name, ignoreCase = true) }
      ?: CalculationMethod.MUSLIM_WORLD_LEAGUE
  }

  /**
   * Astronomical calculation of Islamic prayer times for a given date, city, and method.
   */
  fun calculatePrayerTimes(
    calendar: Calendar,
    city: CityLocation,
    method: CalculationMethod
  ): DayPrayerSchedule {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Julian Date calculation
    val jd = julianDate(year, month, day)
    val d = jd - 2451545.0 // Days since J2000.0

    // Solar coordinates
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sin(toRadians(g)) + 0.020 * sin(toRadians(2 * g)))

    val e = 23.439 - 0.00000036 * d
    val dSin = sin(toRadians(e)) * sin(toRadians(l))
    val declination = toDegrees(asin(dSin))

    val raRad = atan(cos(toRadians(e)) * tan(toRadians(l)))
    var ra = toDegrees(raRad)
    ra = fixAngle(ra + (l - ra) / 90.0 * 90.0) // Quadrant alignment

    val equationOfTime = (q / 15.0) - (ra / 15.0)

    // Midday (Dhuhr)
    val dhuhrHours = 12.0 + city.timezoneOffset - (city.longitude / 15.0) - equationOfTime

    // Sun altitude angles
    val sunriseAngle = 0.833 // Standard atmospheric refraction
    val fajrHours = dhuhrHours - sunHourAngle(declination, city.latitude, method.fajrAngle, isSunBelow = true)
    val sunriseHours = dhuhrHours - sunHourAngle(declination, city.latitude, sunriseAngle, isSunBelow = false)

    // Asr: standard Shafi'i / Maliki / Hanbali shadow = shadow of midday + object height
    val asrAlt = toDegrees(atan(1.0 + tan(toRadians(Math.abs(city.latitude - declination)))))
    val asrHours = dhuhrHours + sunHourAngle(declination, city.latitude, 90.0 - asrAlt, isSunBelow = false)

    val maghribHours = dhuhrHours + sunHourAngle(declination, city.latitude, sunriseAngle, isSunBelow = false)

    val ishaHours = if (method == CalculationMethod.UMM_AL_QURA) {
      maghribHours + 1.5 // 90 minutes after Maghrib
    } else {
      dhuhrHours + sunHourAngle(declination, city.latitude, method.ishaAngle, isSunBelow = true)
    }

    val prayersList = listOf(
      toPrayerTime(PrayerName.FAJR, fajrHours),
      toPrayerTime(PrayerName.SUNRISE, sunriseHours),
      toPrayerTime(PrayerName.DHUHR, dhuhrHours),
      toPrayerTime(PrayerName.ASR, asrHours),
      toPrayerTime(PrayerName.MAGHRIB, maghribHours),
      toPrayerTime(PrayerName.ISHA, ishaHours)
    )

    // Determine Next Prayer
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMin = calendar.get(Calendar.MINUTE)
    val currentTotalMins = currentHour * 60 + currentMin

    val nextPrayer = prayersList.firstOrNull { (it.hour * 60 + it.minute) > currentTotalMins }
      ?: prayersList.first() // After Isha, next is tomorrow's Fajr

    val diffMins = if (nextPrayer != null) {
      val nextTotalMins = nextPrayer.hour * 60 + nextPrayer.minute
      if (nextTotalMins >= currentTotalMins) {
        nextTotalMins - currentTotalMins
      } else {
        (24 * 60 - currentTotalMins) + nextTotalMins
      }
    } else {
      0
    }

    val timeUntilNextFormatted = "${diffMins / 60}h ${diffMins % 60}m"
    val dateFormatted = String.format(Locale.US, "%tA, %te %tB %tY", calendar, calendar, calendar, calendar)
    val hijriFormatted = calculateEstimatedHijri(year, month, day)

    return DayPrayerSchedule(
      dateFormatted = dateFormatted,
      hijriFormatted = hijriFormatted,
      prayers = prayersList,
      nextPrayer = nextPrayer,
      timeUntilNextFormatted = timeUntilNextFormatted
    )
  }

  private fun sunHourAngle(declination: Double, latitude: Double, angle: Double, isSunBelow: Boolean): Double {
    val h = if (isSunBelow) -angle else angle
    val cosH = (sin(toRadians(h)) - sin(toRadians(latitude)) * sin(toRadians(declination))) /
      (cos(toRadians(latitude)) * cos(toRadians(declination)))
    val clamped = cosH.coerceIn(-1.0, 1.0)
    return toDegrees(acos(clamped)) / 15.0
  }

  private fun toPrayerTime(name: PrayerName, decimalHours: Double): SinglePrayerTime {
    var normalized = decimalHours
    while (normalized < 0) normalized += 24.0
    while (normalized >= 24.0) normalized -= 24.0

    val hour = normalized.toInt()
    val min = ((normalized - hour) * 60.0 + 0.5).toInt().coerceIn(0, 59)
    val timeFormatted = String.format(Locale.US, "%02d:%02d", hour, min)

    return SinglePrayerTime(
      prayer = name,
      timeFormatted = timeFormatted,
      hour = hour,
      minute = min
    )
  }

  private fun julianDate(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
      y -= 1
      m += 12
    }
    val a = (y / 100.0).toInt()
    val b = 2 - a + (a / 4.0).toInt()
    return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
  }

  private fun fixAngle(angle: Double): Double {
    var a = angle - (360.0 * (angle / 360.0).toInt())
    if (a < 0) a += 360.0
    return a
  }

  private fun toRadians(deg: Double): Double = deg * Math.PI / 180.0
  private fun toDegrees(rad: Double): Double = rad * 180.0 / Math.PI

  /**
   * Kuwaiti Tabular Islamic Calendar Algorithm for reliable Hijri date estimation.
   */
  fun calculateEstimatedHijri(year: Int, month: Int, day: Int): String {
    val jd = julianDate(year, month, day)
    val l = (jd - 1948440.0 + 10632.0).toInt()
    val n = ((l - 1) / 10631.0).toInt()
    val l2 = l - 10631 * n + 354
    val j = (((10985 - l2) / 5316.0).toInt()) * (((50 * l2) / 17719.0).toInt()) +
      ((l2 / 5670.0).toInt()) * (((43 * l2) / 15238.0).toInt())
    val l3 = l2 - (((30 - j) / 15.0).toInt()) * (((17719 * j) / 50.0).toInt()) -
      ((j / 16.0).toInt()) * (((15238 * j) / 43.0).toInt()) + 29
    val hijriMonth = ((24 * l3) / 709.0).toInt()
    val hijriDay = l3 - ((709 * hijriMonth) / 24.0).toInt()
    val hijriYear = 30 * n + j - 30

    val monthNames = arrayOf(
      "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
      "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
      "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
    val mName = monthNames.getOrElse(hijriMonth - 1) { "Islamic Month" }
    return "$hijriDay $mName $hijriYear AH"
  }
}
