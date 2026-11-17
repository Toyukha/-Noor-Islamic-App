package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuranProgressEntity
import com.example.data.model.DailyLog
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.repository.NoorRepository
import com.example.notification.AlarmPreferences
import com.example.notification.NotificationScheduler
import com.example.ui.components.AlertType
import com.example.ui.components.InAppAlert
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

fun getInitialTodayDate(): String {
  val cal = Calendar.getInstance()
  return String.format(
    Locale.US,
    "%04d-%02d-%02d",
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH) + 1,
    cal.get(Calendar.DAY_OF_MONTH)
  )
}

fun detectDefaultCurrency(): String {
  val country = Locale.getDefault().country
  return if (country.equals("NG", ignoreCase = true) || country.equals("GH", ignoreCase = true) || country.equals("KE", ignoreCase = true)) {
    "NGN"
  } else {
    "USD"
  }
}

data class TransactionReceipt(
  val transactionId: String,
  val planName: String,
  val amountFormatted: String,
  val currency: String,
  val paymentMethod: String,
  val timestampFormatted: String,
  val nextBillingFormatted: String
)

data class ObligationsUiState(
  val selectedDate: String = getInitialTodayDate(),
  val dailyLog: DailyLog = DailyLog(),
  val quranProgress: QuranProgressEntity? = null,
  val allBadges: List<com.example.data.local.BadgeEntity> = emptyList(),
  val activeInAppAlert: InAppAlert? = null,
  val quranReminderHour: Int = 20,
  val quranReminderMinute: Int = 30,
  val preWarningMinutes: Int = 10,
  val prayerPreWarningEnabled: Boolean = true,
  val prayerAlertsEnabled: Boolean = true,
  val fastingAlertsEnabled: Boolean = true,
  val verseAlertsEnabled: Boolean = true,
  val quranKhatmCurrentDay: Int = 1,
  val quranKhatmCompletedDays: Set<Int> = emptySet(),
  val quranKhatmDurationMonths: Int = 1,
  val autoPlaySurahMulkEnabled: Boolean = true,
  val autoPlaySurahKahfEnabled: Boolean = true,
  val nightlySurahsEnabled: Boolean = true,
  val nightlySurahsHour: Int = 20,
  val nightlySurahsMinute: Int = 30,
  val adhanAudioEnabled: Boolean = true,
  val quranSplitReminderHour: Int = 19,
  val quranSplitReminderMinute: Int = 0,
  val floatingPopupEnabled: Boolean = true,
  val selectedCurrency: String = detectDefaultCurrency(),
  val isCheckoutModalOpen: Boolean = false,
  val checkoutSelectedGateway: String = "PAYSTACK",
  val isPaymentProcessing: Boolean = false,
  val lastTransactionReceipt: TransactionReceipt? = null,
  val showDonationCard: Boolean = true,
  val isDonationSheetOpen: Boolean = false,
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
    initAndObserveBadges()
  }

  private fun initAndObserveBadges() {
    viewModelScope.launch {
      repository.ensureDefaultBadgesInitialized()
      repository.observeRoomBadges().collectLatest { badges ->
        _uiState.value = _uiState.value.copy(allBadges = badges)
      }
    }
  }

  private var dateDataJob: Job? = null

  fun selectDate(dateStr: String) {
    _uiState.value = _uiState.value.copy(selectedDate = dateStr)
    loadDateData(dateStr)
  }

  fun syncTodayRollover(newTodayDateStr: String, previousTodayDateStr: String) {
    if (_uiState.value.selectedDate == previousTodayDateStr && newTodayDateStr != previousTodayDateStr) {
      selectDate(newTodayDateStr)
    }
  }

  fun flushPendingSync() {
    viewModelScope.launch {
      try {
        repository.flushPendingSync()
      } catch (_: Exception) {}
    }
  }

  private fun loadDateData(dateStr: String) {
    dateDataJob?.cancel()
    dateDataJob = viewModelScope.launch {
      try {
        repository.observeDailyLog(dateStr).collectLatest { log ->
          _uiState.value = _uiState.value.copy(dailyLog = log)
        }
      } catch (_: Exception) {
        // Fallback already emitted by repository when signed in
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

    val activeAlert = _uiState.value.activeInAppAlert
    if (activeAlert != null && activeAlert.id.contains(prayer.name, ignoreCase = true)) {
      dismissInAppAlert()
    }
  }

  fun markPrayerCompleted(prayer: PrayerName) {
    val current = _uiState.value.dailyLog
    val updated = when (prayer) {
      PrayerName.FAJR -> current.copy(fajr = true)
      PrayerName.DHUHR -> current.copy(dhuhr = true)
      PrayerName.ASR -> current.copy(asr = true)
      PrayerName.MAGHRIB -> current.copy(maghrib = true)
      PrayerName.ISHA -> current.copy(isha = true)
      PrayerName.SUNRISE -> current
    }
    if (updated != current) {
      saveLog(updated)
    }
    val activeAlert = _uiState.value.activeInAppAlert
    if (activeAlert != null && activeAlert.id.contains(prayer.name, ignoreCase = true)) {
      dismissInAppAlert()
    }
  }

  fun updateSunnahPrayers(count: Int) {
    val updated = _uiState.value.dailyLog.copy(sunnahPrayers = maxOf(0, count))
    saveLog(updated)
  }

  fun toggleTahajjud() {
    val current = _uiState.value.dailyLog
    val updated = current.copy(tahajjud = !current.tahajjud)
    saveLog(updated)
  }

  fun toggleDuha() {
    val current = _uiState.value.dailyLog
    val updated = current.copy(duha = !current.duha)
    saveLog(updated)
  }

  fun toggleWitr() {
    val current = _uiState.value.dailyLog
    val updated = current.copy(witr = !current.witr)
    saveLog(updated)
  }

  fun updateRawatib(count: Int) {
    val updated = _uiState.value.dailyLog.copy(rawatibCount = maxOf(0, minOf(12, count)))
    saveLog(updated)
  }

  fun toggleFasting(isFasting: Boolean, fastingType: String = "Sunnah") {
    val updated = _uiState.value.dailyLog.copy(
      isFasting = isFasting,
      fastingType = fastingType
    )
    saveLog(updated)
  }

  fun toggleMorningAdhkar() {
    val current = _uiState.value.dailyLog
    val updated = current.copy(morningAdhkar = !current.morningAdhkar)
    saveLog(updated)
  }

  fun toggleEveningAdhkar() {
    val current = _uiState.value.dailyLog
    val updated = current.copy(eveningAdhkar = !current.eveningAdhkar)
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

  fun saveDailyLog(log: DailyLog) {
    saveLog(log)
  }

  private fun saveLog(log: DailyLog) {
    _uiState.value = _uiState.value.copy(dailyLog = log)
    viewModelScope.launch {
      try {
        repository.saveDailyLog(log)
      } catch (_: Exception) {
        // Offline data stays cached safely in local Room SQLite
      }
    }
  }

  fun setCustomQuranReminder(context: Context, hour: Int, minute: Int) {
    _uiState.value = _uiState.value.copy(
      quranReminderHour = hour,
      quranReminderMinute = minute
    )
    AlarmPreferences.setQuranReminderTime(context, hour, minute)
    val lastSurah = _uiState.value.dailyLog.lastSurah
    val lastAyah = _uiState.value.dailyLog.lastAyah
    NotificationScheduler.scheduleQuranRecitationReminder(context, hour, minute, lastSurah, lastAyah)
  }

  fun togglePrayerPreWarning(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(prayerPreWarningEnabled = enabled)
    AlarmPreferences.setPrayerPreWarningEnabled(context, enabled)
    NotificationScheduler.rescheduleAllFromPreferences(context)
  }

  fun togglePrayerAlerts(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(prayerAlertsEnabled = enabled)
    AlarmPreferences.setPrayerAlertsEnabled(context, enabled)
    NotificationScheduler.rescheduleAllFromPreferences(context)
  }

  fun toggleFastingAlerts(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(fastingAlertsEnabled = enabled)
    AlarmPreferences.setFastingAlertsEnabled(context, enabled)
    NotificationScheduler.rescheduleAllFromPreferences(context)
  }

  fun toggleVerseAlerts(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(verseAlertsEnabled = enabled)
    AlarmPreferences.setVerseAlertsEnabled(context, enabled)
    NotificationScheduler.rescheduleAllFromPreferences(context)
  }

  fun toggleFloatingPopup(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(floatingPopupEnabled = enabled)
    AlarmPreferences.setFloatingPopupEnabled(context, enabled)
  }

  fun dismissInAppAlert() {
    _uiState.value = _uiState.value.copy(activeInAppAlert = null)
  }

  fun loadSavedQuranKhatmPreferences(context: Context) {
    val prefs = AlarmPreferences.prefs(context)
    val savedMonths = AlarmPreferences.getKhatmDurationMonths(context)
    val savedCurrentDay = AlarmPreferences.getKhatmCurrentDay(context)
    val completedDaysStrings = prefs.getStringSet("key_quran_khatm_completed_days", emptySet()) ?: emptySet()
    val completedDaysSet = completedDaysStrings.mapNotNull { it.toIntOrNull() }.toSet()

    _uiState.value = _uiState.value.copy(
      quranKhatmDurationMonths = savedMonths,
      quranKhatmCurrentDay = savedCurrentDay,
      quranKhatmCompletedDays = completedDaysSet,
      quranReminderHour = AlarmPreferences.getQuranReminderHour(context),
      quranReminderMinute = AlarmPreferences.getQuranReminderMinute(context),
      preWarningMinutes = AlarmPreferences.getPreWarningMinutes(context),
      prayerPreWarningEnabled = AlarmPreferences.isPrayerPreWarningEnabled(context),
      prayerAlertsEnabled = AlarmPreferences.isPrayerAlertsEnabled(context),
      fastingAlertsEnabled = AlarmPreferences.isFastingAlertsEnabled(context),
      verseAlertsEnabled = AlarmPreferences.isVerseAlertsEnabled(context),
      adhanAudioEnabled = AlarmPreferences.isAdhanAudioEnabled(context),
      nightlySurahsEnabled = AlarmPreferences.isNightlySurahsEnabled(context),
      nightlySurahsHour = AlarmPreferences.getNightlySurahsHour(context),
      nightlySurahsMinute = AlarmPreferences.getNightlySurahsMinute(context),
      autoPlaySurahMulkEnabled = AlarmPreferences.isAutoPlayMulkEnabled(context),
      autoPlaySurahKahfEnabled = AlarmPreferences.isAutoPlayKahfEnabled(context),
      quranSplitReminderHour = AlarmPreferences.getKhatmSplitHour(context),
      quranSplitReminderMinute = AlarmPreferences.getKhatmSplitMinute(context),
      floatingPopupEnabled = AlarmPreferences.isFloatingPopupEnabled(context)
    )
  }

  fun setKhatmDurationMonths(months: Int, context: Context? = null) {
    val clamped = months.coerceIn(1, 3)
    _uiState.value = _uiState.value.copy(quranKhatmDurationMonths = clamped)
    context?.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
      ?.edit()
      ?.putInt("key_quran_khatm_duration_months", clamped)
      ?.apply()
  }

  fun setNightlySurahsTime(hour: Int, minute: Int, context: Context? = null) {
    _uiState.value = _uiState.value.copy(nightlySurahsHour = hour, nightlySurahsMinute = minute)
    context?.let {
      AlarmPreferences.setNightlySurahsTime(it, hour, minute)
      if (_uiState.value.nightlySurahsEnabled) {
        NotificationScheduler.scheduleNightlySurahsAlarm(it, hour, minute)
      }
    }
  }

  fun toggleAdhanAudio(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(adhanAudioEnabled = enabled)
    AlarmPreferences.setAdhanAudioEnabled(context, enabled)
  }

  fun toggleNightlySurahs(enabled: Boolean, context: Context? = null) {
    _uiState.value = _uiState.value.copy(nightlySurahsEnabled = enabled)
    context?.let {
      AlarmPreferences.setNightlySurahsEnabled(it, enabled)
      if (enabled) {
        NotificationScheduler.scheduleNightlySurahsAlarm(
          it,
          _uiState.value.nightlySurahsHour,
          _uiState.value.nightlySurahsMinute
        )
      } else {
        NotificationScheduler.cancelNightlySurahs(it)
      }
    }
  }

  fun checkDonationCardStatus(context: Context) {
    val prefs = AlarmPreferences.prefs(context)
    val lastDismissed = prefs.getLong("key_donation_last_dismissed", 0L)
    val adhan = AlarmPreferences.isAdhanAudioEnabled(context)
    val now = System.currentTimeMillis()
    val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
    val shouldShow = (now - lastDismissed) >= threeDaysMs
    _uiState.value = _uiState.value.copy(
      showDonationCard = shouldShow,
      adhanAudioEnabled = adhan
    )
  }

  fun dismissDonationCard(context: Context) {
    val prefs = AlarmPreferences.prefs(context)
    val now = System.currentTimeMillis()
    prefs.edit().putLong("key_donation_last_dismissed", now).apply()
    _uiState.value = _uiState.value.copy(showDonationCard = false)
  }

  fun setDonationSheetOpen(open: Boolean) {
    _uiState.value = _uiState.value.copy(isDonationSheetOpen = open)
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
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= now.timeInMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }
    val diffMillis = prayerCal.timeInMillis - now.timeInMillis
    val minutesUntil = (diffMillis / (60 * 1000)).toInt()

    // 1. Solah 10-Minute Pre-warning: Check if minutes until prayer <= 10
    if (_uiState.value.prayerPreWarningEnabled && minutesUntil in 1..10) {
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
    if (_uiState.value.fastingAlertsEnabled && dailyLog.isFasting && prayer == PrayerName.FAJR && minutesUntil in 11..45) {
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
    if (_uiState.value.fastingAlertsEnabled && dailyLog.isFasting && prayer == PrayerName.MAGHRIB && minutesUntil in 1..15) {
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

  fun setCurrency(currency: String) {
    _uiState.value = _uiState.value.copy(selectedCurrency = currency)
  }

  fun openCheckoutModal(gateway: String = "PAYSTACK") {
    _uiState.value = _uiState.value.copy(
      isCheckoutModalOpen = true,
      checkoutSelectedGateway = gateway
    )
  }

  fun closeCheckoutModal() {
    _uiState.value = _uiState.value.copy(
      isCheckoutModalOpen = false,
      isPaymentProcessing = false
    )
  }

  fun setCheckoutGateway(gateway: String) {
    _uiState.value = _uiState.value.copy(checkoutSelectedGateway = gateway)
  }

  fun dismissReceipt() {
    _uiState.value = _uiState.value.copy(lastTransactionReceipt = null)
  }

  fun processSubscriptionPayment(
    gateway: String,
    currency: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isPaymentProcessing = true)
      kotlinx.coroutines.delay(1800)
      val amount = if (currency == "NGN") 1000.0 else 5.0
      val planName = if (currency == "NGN") "Noor Monthly (₦1,000)" else "Noor Monthly ($5.00)"
      val txnId = try {
        repository.activateSubscription(
          plan = planName,
          currency = currency,
          amount = amount,
          paymentMethod = gateway,
          durationDays = 30
        )
      } catch (e: Exception) {
        "NOOR-TXN-" + (100000..999999).random()
      }

      val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
      val nextCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
      val nextSdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.US)

      val receipt = TransactionReceipt(
        transactionId = txnId,
        planName = planName,
        amountFormatted = if (currency == "NGN") "₦1,000" else "$5.00",
        currency = currency,
        paymentMethod = gateway,
        timestampFormatted = sdf.format(java.util.Date()),
        nextBillingFormatted = nextSdf.format(nextCal.time)
      )

      _uiState.value = _uiState.value.copy(
        isPaymentProcessing = false,
        isCheckoutModalOpen = false,
        lastTransactionReceipt = receipt
      )
      onSuccess()
    }
  }

  fun activateFreeTrial(onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      val curr = _uiState.value.selectedCurrency
      val amount = if (curr == "NGN") 1000.0 else 5.0
      val txnId = try {
        repository.activateSubscription(
          plan = "7-Day Free Trial",
          currency = curr,
          amount = amount,
          paymentMethod = "FREE_TRIAL",
          durationDays = 7
        )
      } catch (e: Exception) {
        "NOOR-TRIAL-" + (100000..999999).random()
      }

      val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
      val nextCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
      val nextSdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.US)

      val receipt = TransactionReceipt(
        transactionId = txnId,
        planName = "7-Day Complimentary Trial",
        amountFormatted = "₦0 (Free)",
        currency = curr,
        paymentMethod = "FREE_TRIAL",
        timestampFormatted = sdf.format(java.util.Date()),
        nextBillingFormatted = nextSdf.format(nextCal.time)
      )

      _uiState.value = _uiState.value.copy(
        isLoading = false,
        lastTransactionReceipt = receipt
      )
      onSuccess()
    }
  }

  fun toggleQuranDayComplete(day: Int, totalPages: Int, context: Context? = null) {
    val currentSet = _uiState.value.quranKhatmCompletedDays.toMutableSet()
    val isNowCompleted = if (currentSet.contains(day)) {
      currentSet.remove(day)
      false
    } else {
      currentSet.add(day)
      true
    }

    val totalDays = com.example.data.model.QuranKhatmPlanCatalogue.getTotalDaysForDuration(_uiState.value.quranKhatmDurationMonths)
    val nextDay = if (isNowCompleted && day < totalDays) day + 1 else day
    _uiState.value = _uiState.value.copy(
      quranKhatmCompletedDays = currentSet,
      quranKhatmCurrentDay = nextDay
    )

    context?.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
      putInt("key_quran_khatm_current_day", nextDay)
      putStringSet("key_quran_khatm_completed_days", currentSet.map { it.toString() }.toSet())
      apply()
    }

    if (isNowCompleted) {
      addQuranPages(totalPages)
      viewModelScope.launch {
        if (currentSet.size >= 1) {
          repository.unlockBadge("QURAN_JUZ_1", "First Juz Achieved", "Completed 20 pages of the Quran")
        }
        if (currentSet.size >= totalDays) {
          repository.unlockBadge("QURAN_KHATM", "Khatm Al-Quran", "Completed entire 604 pages of the Holy Quran")
        }
      }
    }
  }

  fun setAutoPlaySurahMulk(context: Context, enabled: Boolean) {
    _uiState.value = _uiState.value.copy(autoPlaySurahMulkEnabled = enabled)
    AlarmPreferences.setAutoPlayMulkEnabled(context, enabled)
    if (enabled) {
      NotificationScheduler.scheduleSurahMulkNightAlarm(context)
    } else {
      NotificationScheduler.cancelSurahMulkNight(context)
    }
  }

  fun setAutoPlaySurahKahf(context: Context, enabled: Boolean, sunriseHour: Int = 6, sunriseMinute: Int = 15) {
    _uiState.value = _uiState.value.copy(autoPlaySurahKahfEnabled = enabled)
    AlarmPreferences.setAutoPlayKahfEnabled(context, enabled)
    if (enabled) {
      NotificationScheduler.scheduleSurahKahfFridayAlarm(context, sunriseHour, sunriseMinute)
    } else {
      NotificationScheduler.cancelSurahKahfFriday(context)
    }
  }

  fun setQuranSplitReminder(context: Context, hour: Int, minute: Int) {
    _uiState.value = _uiState.value.copy(
      quranSplitReminderHour = hour,
      quranSplitReminderMinute = minute
    )
    AlarmPreferences.setKhatmSplitTime(context, hour, minute)
    val curDay = _uiState.value.quranKhatmCurrentDay
    val split = com.example.data.model.QuranKhatmPlanCatalogue.getSplitForDayAndDuration(
      curDay,
      _uiState.value.quranKhatmDurationMonths
    )
    NotificationScheduler.scheduleQuranSplitDailyPrompt(
      context = context,
      dayNumber = curDay,
      startPage = split.startPage,
      endPage = split.endPage,
      hour = hour,
      minute = minute
    )
  }
}
