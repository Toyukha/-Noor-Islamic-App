package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuranProgressEntity
import com.example.data.model.DailyLog
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.repository.NoorRepository
import com.example.notification.NotificationHelper
import com.example.notification.NotificationScheduler
import com.example.ui.components.AlertType
import com.example.ui.components.InAppAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

fun getInitialTodayDate(): String {
  val cal = Calendar.getInstance()
  return String.format(Locale.US, "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
}

data class ObligationsUiState(
  val selectedDate: String = getInitialTodayDate(),
  val dailyLog: DailyLog = DailyLog(),
  val quranProgress: QuranProgressEntity? = null,
  val activeInAppAlert: InAppAlert? = null,
  val quranReminderHour: Int = 20,
  val quranReminderMinute: Int = 30,
  val preWarningMinutes: Int = 10,
  val isLoading: Boolean = false
)

class ObligationsViewModel(
  private val repository: NoorRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ObligationsUiState())
  val uiState: StateFlow<ObligationsUiState> = _uiState.asStateFlow()

  init {
    loadDateData(_uiState.value.selectedDate)
    observeQuranProgress()
  }

  fun selectDate(dateStr: String) {
    _uiState.value = _uiState.value.copy(selectedDate = dateStr)
    loadDateData(dateStr)
  }

  private fun loadDateData(dateStr: String) {
    viewModelScope.launch {
      repository.observeDailyLog(dateStr).collectLatest { log ->
        _uiState.value = _uiState.value.copy(dailyLog = log)
      }
    }
  }

  private fun observeQuranProgress() {
    viewModelScope.launch {
      val uid = repository.currentUserId ?: return@launch
      repository.observeQuranProgress(uid).collectLatest { progress ->
        _uiState.value = _uiState.value.copy(quranProgress = progress)
      }
    }
  }

  fun togglePrayer(prayer: PrayerName) {
    val current = _uiState.value.dailyLog
    val updated = when (prayer) {
      PrayerName.FAJR -> current.copy(fajr = !current.fajr)
      PrayerName.DHUHR -> current.copy(dhuhr = !current.dhuhr)
      PrayerName.ASR -> current.copy(asr = !current.asr)
      PrayerName.MAGHRIB -> current.copy(maghrib = !current.maghrib)
      PrayerName.ISHA -> current.copy(isha = !current.isha)
      PrayerName.SUNRISE -> current
    }
    saveLog(updated)

    // If active alert was for this prayer, dismiss it
    val activeAlert = _uiState.value.activeInAppAlert
    if (activeAlert != null && activeAlert.id.contains(prayer.name, ignoreCase = true)) {
      dismissInAppAlert()
    }
  }

  fun updateSunnahPrayers(count: Int) {
    val updated = _uiState.value.dailyLog.copy(sunnahPrayers = maxOf(0, count))
    saveLog(updated)
  }

  fun toggleFasting(isFasting: Boolean, fastingType: String = "Sunnah") {
    val updated = _uiState.value.dailyLog.copy(
      isFasting = isFasting,
      fastingType = fastingType
    )
    saveLog(updated)
  }

  fun addQuranPages(pages: Int) {
    val current = _uiState.value.dailyLog
    val newPages = maxOf(0, current.quranPagesRead + pages)
    val updated = current.copy(quranPagesRead = newPages)
    saveLog(updated)
  }

  fun updateQuranBookmark(surah: String, ayah: Int) {
    val current = _uiState.value.dailyLog
    val updated = current.copy(lastSurah = surah, lastAyah = ayah)
    saveLog(updated)
  }

  private fun saveLog(log: DailyLog) {
    _uiState.value = _uiState.value.copy(dailyLog = log)
    viewModelScope.launch {
      try {
        repository.saveDailyLog(log)
      } catch (e: Exception) {
        // Log is already safely cached in local Room SQLite!
      }
    }
  }

  fun setCustomQuranReminder(context: Context, hour: Int, minute: Int) {
    _uiState.value = _uiState.value.copy(
      quranReminderHour = hour,
      quranReminderMinute = minute
    )
    val lastSurah = _uiState.value.dailyLog.lastSurah
    val lastAyah = _uiState.value.dailyLog.lastAyah
    NotificationScheduler.scheduleQuranRecitationReminder(context, hour, minute, lastSurah, lastAyah)
  }

  fun dismissInAppAlert() {
    _uiState.value = _uiState.value.copy(activeInAppAlert = null)
  }

  /**
   * Scans prayer schedule and obligation state to trigger creative in-app banners.
   */
  fun evaluateInAppAlerts(schedule: DayPrayerSchedule) {
    val nextPrayer = schedule.nextPrayer ?: return
    val prayer = nextPrayer.prayer
    val dailyLog = _uiState.value.dailyLog

    val isCompleted = when (prayer) {
      PrayerName.FAJR -> dailyLog.fajr
      PrayerName.DHUHR -> dailyLog.dhuhr
      PrayerName.ASR -> dailyLog.asr
      PrayerName.MAGHRIB -> dailyLog.maghrib
      PrayerName.ISHA -> dailyLog.isha
      PrayerName.SUNRISE -> false
    }

    if (isCompleted) {
      if (_uiState.value.activeInAppAlert?.id?.startsWith("SOLAH_") == true) {
        dismissInAppAlert()
      }
      return
    }

    val now = Calendar.getInstance()
    val prayerCal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, nextPrayer.hour)
      set(Calendar.MINUTE, nextPrayer.minute)
      set(Calendar.SECOND, 0)
    }
    val diffMillis = prayerCal.timeInMillis - now.timeInMillis
    val minutesUntil = (diffMillis / (60 * 1000)).toInt()

    // 1. Solah 10-Minute Pre-warning: Check if minutes until prayer <= 10
    if (minutesUntil in 1..10) {
      _uiState.value = _uiState.value.copy(
        activeInAppAlert = InAppAlert(
          id = "SOLAH_${prayer.name}",
          type = AlertType.SOLAH_PRE_WARNING,
          title = "Upcoming Solah: ${prayer.displayName}",
          message = "Begins in $minutesUntil mins. Perform wudu and prepare for prayer.",
          actionLabel = "Mark Prayed"
        )
      )
      return
    }

    // 2. Fasting: Check Suhoor before Fajr
    if (dailyLog.isFasting && prayer == PrayerName.FAJR && minutesUntil in 11..45) {
      _uiState.value = _uiState.value.copy(
        activeInAppAlert = InAppAlert(
          id = "FASTING_SUHOOR",
          type = AlertType.SUHOOR_FASTING,
          title = "Suhoor Meal Time",
          message = "Fajr begins in $minutesUntil mins. Conclude your meal and make your intention.",
          actionLabel = "Acknowledge"
        )
      )
      return
    }

    // 3. Fasting: Iftar approaching
    if (dailyLog.isFasting && prayer == PrayerName.MAGHRIB && minutesUntil in 1..15) {
      _uiState.value = _uiState.value.copy(
        activeInAppAlert = InAppAlert(
          id = "FASTING_IFTAR",
          type = AlertType.IFTAR_FASTING,
          title = "Iftar Approaching",
          message = "Sunset in $minutesUntil mins! Prepare your supplication (du'a).",
          actionLabel = "Open Du'a"
        )
      )
      return
    }

    // 4. Quran Recitation Reminder during chosen hour
    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    if (currentHour == _uiState.value.quranReminderHour && dailyLog.quranPagesRead == 0) {
      _uiState.value = _uiState.value.copy(
        activeInAppAlert = InAppAlert(
          id = "QURAN_DAILY",
          type = AlertType.QURAN_RECITE,
          title = "Daily Quran Recitation Time",
          message = "Bookmark: Surah ${dailyLog.lastSurah}, Ayah ${dailyLog.lastAyah}. Dedicate 10 minutes to Allah's words.",
          actionLabel = "Log Pages"
        )
      )
    }
  }
}
