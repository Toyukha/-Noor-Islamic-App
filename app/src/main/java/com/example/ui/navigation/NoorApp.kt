package com.example.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.model.DailyVerse
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.QuranKhatmPlanCatalogue
import com.example.data.model.VerseCatalogue
import com.example.data.repository.NoorRepository
import com.example.notification.AlarmPreferences
import com.example.notification.FloatingAlertController
import com.example.notification.NotificationScheduler
import com.example.ui.components.AlertType
import com.example.ui.components.AnimatedInAppAlertBanner
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TadabburScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.viewmodel.ObligationsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class NoorTab(val label: String, val icon: ImageVector) {
  TODAY("Today", Icons.Default.Today),
  QURAN("Quran", Icons.AutoMirrored.Filled.MenuBook),
  TRACKER("Tracker", Icons.Default.CheckCircle),
  TADABBUR("Tadabbur", Icons.Default.AutoAwesome),
  SETTINGS("Settings", Icons.Default.Settings)
}

private fun formatCalDate(cal: Calendar): String {
  val y = cal.get(Calendar.YEAR)
  val m = cal.get(Calendar.MONTH) + 1
  val d = cal.get(Calendar.DAY_OF_MONTH)
  return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NoorApp(
  repository: NoorRepository,
  modifier: Modifier = Modifier,
  launchIntent: Intent? = null,
  onConsumeLaunchIntent: () -> Unit = {}
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()
  var currentTab by remember { mutableStateOf(NoorTab.TODAY) }
  var openFullPageQuranDirectly by remember { mutableStateOf(false) }
  var targetFullPageQuran by remember { mutableStateOf<Int?>(null) }
  var isFullScreenQuranActive by remember { mutableStateOf(false) }
  val audioPlayer = remember { com.example.audio.SurahAudioPlayer.getInstance(context) }

  LaunchedEffect(Unit) {
    com.example.data.model.QuranPageCatalogue.initialize(context)
  }

  // Instantiate unified ObligationsViewModel backed by Room SQLite & Firestore
  val obligationsViewModel = remember(repository) { ObligationsViewModel(repository) }
  val obligationsState by obligationsViewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(launchIntent) {
    if (launchIntent != null) {
      val prayerOrdinal = launchIntent.getIntExtra(FloatingAlertController.EXTRA_MARK_PRAYER_ORDINAL, -1)
      if (prayerOrdinal >= 0) {
        PrayerName.values().getOrNull(prayerOrdinal)?.let { prayer ->
          obligationsViewModel.markPrayerCompleted(prayer)
        }
      }
      val openQuranFull = launchIntent.getBooleanExtra(FloatingAlertController.EXTRA_OPEN_QURAN_FULL_PAGE, false)
      val targetTab = launchIntent.getStringExtra(FloatingAlertController.EXTRA_NAV_TAB)
      if (targetTab != null) {
        val mapped = NoorTab.values().firstOrNull { it.name == targetTab }
        if (mapped != null) {
          openFullPageQuranDirectly = mapped == NoorTab.QURAN && openQuranFull
          currentTab = mapped
        }
      }
      if (prayerOrdinal >= 0 || targetTab != null) {
        onConsumeLaunchIntent()
      }
    }
  }

  // Live clock ticker + ON_RESUME refresh so Day 2+ rollover, prayer countdown, and alarms stay current
  var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(30_000L)
      nowMillis = System.currentTimeMillis()
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        nowMillis = System.currentTimeMillis()
        obligationsViewModel.flushPendingSync()
        NotificationScheduler.rescheduleAllFromPreferences(context, nowMillis)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val nowCal = remember(nowMillis) {
    Calendar.getInstance().apply { timeInMillis = nowMillis }
  }
  val currentMinuteBucket = remember(nowMillis) { nowMillis / 60_000L }
  val todayDateStr = remember(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.DAY_OF_YEAR)) {
    formatCalDate(nowCal)
  }

  var previousTodayDateStr by remember { mutableStateOf(todayDateStr) }
  LaunchedEffect(todayDateStr) {
    if (todayDateStr != previousTodayDateStr) {
      obligationsViewModel.syncTodayRollover(todayDateStr, previousTodayDateStr)
      previousTodayDateStr = todayDateStr
    }
  }

  val sevenDaysAgoStr = remember(todayDateStr) {
    val pastCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      add(Calendar.DAY_OF_MONTH, -7)
    }
    formatCalDate(pastCal)
  }

  // Reactive Firestore States (offline-resilient)
  val userProfile by repository.observeUserProfile().collectAsState(initial = null)
  val weeklyLogs by repository.observeWeeklyLogs(sevenDaysAgoStr).collectAsState(initial = emptyList())

  // Ensure user profile exists in Firestore, load persisted preferences, and flush offline writes on launch
  LaunchedEffect(Unit) {
    obligationsViewModel.loadSavedQuranKhatmPreferences(context)
    obligationsViewModel.flushPendingSync()
    val user = Firebase.auth.currentUser
    if (user != null) {
      repository.ensureUserProfileExists(
        displayName = user.displayName ?: "Muslim Servant",
        email = user.email ?: ""
      )
    }
  }

  // Calculate prayer schedule dynamically based on current user city, method, and current minute
  val cityName = userProfile?.city?.ifBlank { null } ?: AlarmPreferences.getCityName(context)
  val methodName = userProfile?.calculationMethod?.ifBlank { null } ?: AlarmPreferences.getCalculationMethod(context)
  val cityLocation = remember(cityName) { PrayerCalculationEngine.getCityByName(cityName) }
  val calcMethod = remember(methodName) { PrayerCalculationEngine.getMethodByName(methodName) }

  val prayerSchedule: DayPrayerSchedule = remember(cityLocation, calcMethod, currentMinuteBucket) {
    PrayerCalculationEngine.calculatePrayerTimes(
      calendar = Calendar.getInstance().apply { timeInMillis = nowMillis },
      city = cityLocation,
      method = calcMethod
    )
  }

  val todayVerse: DailyVerse = remember(todayDateStr) {
    val dayOfYear = nowCal.get(Calendar.DAY_OF_YEAR)
    VerseCatalogue.getVerseForDayOfYear(dayOfYear)
  }

  // Persist active prayer/bookmark/fasting parameters and re-arm all alarms whenever schedule or settings change
  LaunchedEffect(
    todayDateStr,
    cityName,
    methodName,
    obligationsState.dailyLog.isFasting,
    obligationsState.dailyLog.lastSurah,
    obligationsState.dailyLog.lastAyah,
    obligationsState.preWarningMinutes,
    obligationsState.prayerPreWarningEnabled,
    obligationsState.prayerAlertsEnabled,
    obligationsState.fastingAlertsEnabled,
    obligationsState.verseAlertsEnabled,
    obligationsState.nightlySurahsEnabled,
    obligationsState.nightlySurahsHour,
    obligationsState.nightlySurahsMinute,
    obligationsState.autoPlaySurahKahfEnabled,
    obligationsState.autoPlaySurahMulkEnabled,
    obligationsState.quranKhatmCurrentDay,
    obligationsState.quranKhatmDurationMonths,
    obligationsState.quranReminderHour,
    obligationsState.quranReminderMinute,
    obligationsState.quranSplitReminderHour,
    obligationsState.quranSplitReminderMinute
  ) {
    AlarmPreferences.setCityName(context, cityName)
    AlarmPreferences.setCalculationMethod(context, methodName)
    AlarmPreferences.setFastingState(context, obligationsState.dailyLog.isFasting, todayDateStr)
    AlarmPreferences.setBookmark(context, obligationsState.dailyLog.lastSurah, obligationsState.dailyLog.lastAyah)
    NotificationScheduler.rescheduleAllFromPreferences(context, System.currentTimeMillis())
    if (obligationsState.autoPlaySurahMulkEnabled) {
      NotificationScheduler.scheduleSurahMulkNightAlarm(context)
    } else {
      NotificationScheduler.cancelSurahMulkNight(context)
    }
  }

  // Evaluate and display animated in-app alert banner
  LaunchedEffect(prayerSchedule, obligationsState.dailyLog) {
    obligationsViewModel.evaluateInAppAlerts(prayerSchedule)
  }

  // Check 3-day donation reminder status on session launch
  LaunchedEffect(Unit) {
    obligationsViewModel.checkDonationCardStatus(context)
  }

  if (obligationsState.isDonationSheetOpen) {
    com.example.ui.components.DonationDetailsBottomSheet(
      onDismiss = { obligationsViewModel.setDonationSheetOpen(false) }
    )
  }

  Scaffold(
    bottomBar = {
      if (!isFullScreenQuranActive) {
        Column {
          com.example.ui.components.SurahAudioBar(
            audioPlayer = audioPlayer,
            modifier = Modifier.fillMaxWidth()
          )

          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("noor_bottom_nav")
          ) {
            NoorTab.values().forEach { tab ->
              val isSelected = currentTab == tab
              NavigationBarItem(
                selected = isSelected,
                onClick = {
                  if (tab != NoorTab.QURAN) {
                    openFullPageQuranDirectly = false
                    targetFullPageQuran = null
                  }
                  currentTab = tab
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                  Text(
                    text = tab.label,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                  indicatorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
              )
            }
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(if (isFullScreenQuranActive) Modifier else Modifier.padding(innerPadding))
    ) {
      AnimatedInAppAlertBanner(
        alert = obligationsState.activeInAppAlert,
        onActionClick = { alert ->
          when (alert.type) {
            AlertType.SOLAH_PRE_WARNING, AlertType.SOLAH_EXACT -> {
              val prayerNameStr = alert.id.removePrefix("SOLAH_").removePrefix("EXACT_")
              val prayer = PrayerName.values().find { it.name == prayerNameStr }
              if (prayer != null) {
                obligationsViewModel.markPrayerCompleted(prayer)
              }
              obligationsViewModel.dismissInAppAlert()
            }
            AlertType.QURAN_RECITE, AlertType.SURAH_RECITATION -> {
              openFullPageQuranDirectly = alert.type == AlertType.QURAN_RECITE
              currentTab = NoorTab.QURAN
              obligationsViewModel.dismissInAppAlert()
            }
            AlertType.DAILY_VERSE -> {
              currentTab = NoorTab.TADABBUR
              obligationsViewModel.dismissInAppAlert()
            }
            AlertType.ADHKAR_REMINDER -> {
              currentTab = NoorTab.TRACKER
              obligationsViewModel.dismissInAppAlert()
            }
            AlertType.SUHOOR_FASTING, AlertType.IFTAR_FASTING -> {
              obligationsViewModel.dismissInAppAlert()
            }
          }
        },
        onDismiss = {
          obligationsViewModel.dismissInAppAlert()
        }
      )

      Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
          when (currentTab) {
            NoorTab.TODAY -> {
              TodayScreen(
                schedule = prayerSchedule,
                verse = todayVerse,
                dailyLog = obligationsState.dailyLog,
                cityName = cityName,
                onNavigateToTracker = {
                  obligationsViewModel.selectDate(todayDateStr)
                  currentTab = NoorTab.TRACKER
                },
                onNavigateToSettings = {
                  currentTab = NoorTab.SETTINGS
                },
                weeklyLogs = weeklyLogs,
                currentQuranDay = obligationsState.quranKhatmCurrentDay,
                onOpenQuranPages = {
                  openFullPageQuranDirectly = true
                  currentTab = NoorTab.QURAN
                },
                onNavigateToTadabbur = {
                  currentTab = NoorTab.TADABBUR
                },
                onToggleMorningAdhkar = {
                  obligationsViewModel.toggleMorningAdhkar()
                },
                onToggleEveningAdhkar = {
                  obligationsViewModel.toggleEveningAdhkar()
                },
                onTogglePrayer = { prayer ->
                  obligationsViewModel.togglePrayer(prayer)
                },
                showDonationCard = obligationsState.showDonationCard,
                onOpenDonationDetails = { obligationsViewModel.setDonationSheetOpen(true) },
                onDismissDonationCard = { obligationsViewModel.dismissDonationCard(context) },
                currentDateKey = todayDateStr
              )
            }
            NoorTab.QURAN -> {
              QuranReaderScreen(
                currentDay = obligationsState.quranKhatmCurrentDay,
                completedDays = obligationsState.quranKhatmCompletedDays,
                audioPlayer = audioPlayer,
                onToggleDayComplete = { day, pages ->
                  obligationsViewModel.toggleQuranDayComplete(day, pages, context)
                },
                onScheduleReminder = {
                  obligationsViewModel.setQuranSplitReminder(context, 19, 0)
                },
                initialOpenFullPage = openFullPageQuranDirectly,
                initialTargetPage = targetFullPageQuran,
                onFullScreenStateChanged = { isActive ->
                  isFullScreenQuranActive = isActive
                  if (!isActive) {
                    openFullPageQuranDirectly = false
                    targetFullPageQuran = null
                  }
                },
                khatmMonths = obligationsState.quranKhatmDurationMonths,
                onKhatmMonthsChange = { obligationsViewModel.setKhatmDurationMonths(it, context) }
              )
            }
            NoorTab.TRACKER -> {
              TrackerScreen(
                dailyLog = obligationsState.dailyLog,
                currentDateStr = obligationsState.selectedDate,
                onDateChange = { newDate ->
                  obligationsViewModel.selectDate(newDate)
                },
                onUpdateLog = { updatedLog ->
                  obligationsViewModel.saveDailyLog(updatedLog)
                },
                allLogs = weeklyLogs,
                cityName = cityName
              )
            }
            NoorTab.TADABBUR -> {
              TadabburScreen(
                verse = todayVerse,
                dateStr = todayDateStr,
                onOpenVerseInQuran = { dailyVerse ->
                  val page = com.example.data.model.QuranPageCatalogue.findPageForAyah(
                    surahNumber = dailyVerse.surahNumber,
                    ayahNumber = dailyVerse.ayahNumber,
                    context = context
                  )
                  targetFullPageQuran = page
                  openFullPageQuranDirectly = true
                  currentTab = NoorTab.QURAN
                }
              )
            }
            NoorTab.SETTINGS -> {
              SettingsScreen(
                userProfile = userProfile,
                onCityChanged = { newCity ->
                  AlarmPreferences.setCityName(context, newCity)
                  NotificationScheduler.rescheduleAllFromPreferences(context)
                  coroutineScope.launch {
                    repository.updateUserCityAndMethod(newCity, methodName)
                  }
                },
                onMethodChanged = { newMethod ->
                  AlarmPreferences.setCalculationMethod(context, newMethod)
                  NotificationScheduler.rescheduleAllFromPreferences(context)
                  coroutineScope.launch {
                    repository.updateUserCityAndMethod(cityName, newMethod)
                  }
                },
                onSignOut = {},
                quranReminderHour = obligationsState.quranReminderHour,
                quranReminderMinute = obligationsState.quranReminderMinute,
                onQuranReminderTimeChanged = { hour, minute ->
                  obligationsViewModel.setCustomQuranReminder(context, hour, minute)
                },
                badges = obligationsState.allBadges,
                autoPlaySurahMulkEnabled = obligationsState.autoPlaySurahMulkEnabled,
                onToggleAutoPlaySurahMulk = { enabled ->
                  obligationsViewModel.setAutoPlaySurahMulk(context, enabled)
                },
                autoPlaySurahKahfEnabled = obligationsState.autoPlaySurahKahfEnabled,
                onToggleAutoPlaySurahKahf = { enabled ->
                  val sunrise = prayerSchedule.sunriseTime ?: prayerSchedule.prayers.find { it.prayer == PrayerName.SUNRISE }
                  val h = sunrise?.hour ?: 6
                  val m = sunrise?.minute ?: 15
                  obligationsViewModel.setAutoPlaySurahKahf(context, enabled, h, m)
                },
                adhanAudioEnabled = obligationsState.adhanAudioEnabled,
                onToggleAdhanAudio = { obligationsViewModel.toggleAdhanAudio(context, it) },
                nightlySurahsEnabled = obligationsState.nightlySurahsEnabled,
                onToggleNightlySurahs = { obligationsViewModel.toggleNightlySurahs(it, context) },
                nightlySurahsHour = obligationsState.nightlySurahsHour,
                nightlySurahsMinute = obligationsState.nightlySurahsMinute,
                onNightlySurahsTimeChanged = { h, m -> obligationsViewModel.setNightlySurahsTime(h, m, context) },
                prayerPreWarningEnabled = obligationsState.prayerPreWarningEnabled,
                onTogglePrayerPreWarning = { obligationsViewModel.togglePrayerPreWarning(context, it) },
                prayerAlertsEnabled = obligationsState.prayerAlertsEnabled,
                onTogglePrayerAlerts = { obligationsViewModel.togglePrayerAlerts(context, it) },
                fastingAlertsEnabled = obligationsState.fastingAlertsEnabled,
                onToggleFastingAlerts = { obligationsViewModel.toggleFastingAlerts(context, it) },
                verseAlertsEnabled = obligationsState.verseAlertsEnabled,
                onToggleVerseAlerts = { obligationsViewModel.toggleVerseAlerts(context, it) },
                khatmMonths = obligationsState.quranKhatmDurationMonths,
                onKhatmMonthsChange = { obligationsViewModel.setKhatmDurationMonths(it, context) },
                quranKhatmCompletedDaysCount = obligationsState.quranKhatmCompletedDays.size,
                floatingPopupEnabled = obligationsState.floatingPopupEnabled,
                onToggleFloatingPopup = { obligationsViewModel.toggleFloatingPopup(context, it) },
                onOpenQuranReader = {
                  currentTab = NoorTab.QURAN
                }
              )
            }
          }
        }
      }
    }
  }
}
