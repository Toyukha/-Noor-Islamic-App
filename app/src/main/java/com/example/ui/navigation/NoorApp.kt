package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.model.CalculationMethod
import com.example.data.model.DailyLog
import com.example.data.model.DailyVerse
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.VerseCatalogue
import com.example.data.repository.NoorRepository
import com.example.notification.NotificationScheduler
import com.example.ui.components.AlertType
import com.example.ui.components.AnimatedInAppAlertBanner
import com.example.ui.screens.BadgesScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.viewmodel.ObligationsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class NoorTab(val label: String, val icon: ImageVector) {
  TODAY("Today", Icons.Default.Today),
  TRACKER("Tracker", Icons.Default.CheckCircle),
  INSIGHTS("Insights", Icons.Default.Equalizer),
  BADGES("Badges", Icons.Default.AutoAwesome),
  SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun NoorApp(
  repository: NoorRepository,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var currentTab by remember { mutableStateOf(NoorTab.TODAY) }

  // Instantiate unified ObligationsViewModel backed by Room SQLite & Firestore
  val obligationsViewModel = remember(repository) { ObligationsViewModel(repository) }
  val obligationsState by obligationsViewModel.uiState.collectAsStateWithLifecycle()

  // Date formatting helpers
  val todayCal = remember { Calendar.getInstance() }
  val todayDateStr = remember {
    val y = todayCal.get(Calendar.YEAR)
    val m = todayCal.get(Calendar.MONTH) + 1
    val d = todayCal.get(Calendar.DAY_OF_MONTH)
    String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
  }

  val sevenDaysAgoStr = remember {
    val pastCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }
    val y = pastCal.get(Calendar.YEAR)
    val m = pastCal.get(Calendar.MONTH) + 1
    val d = pastCal.get(Calendar.DAY_OF_MONTH)
    String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
  }

  // Reactive Firestore States
  val userProfile by repository.observeUserProfile().collectAsState(initial = null)
  val weeklyLogs by repository.observeWeeklyLogs(sevenDaysAgoStr).collectAsState(initial = emptyList())
  val unlockedBadges by repository.observeUnlockedBadges().collectAsState(initial = emptyList())

  // Ensure user profile exists in Firestore upon first session
  LaunchedEffect(Unit) {
    val user = Firebase.auth.currentUser
    if (user != null) {
      repository.ensureUserProfileExists(
        displayName = user.displayName ?: "Muslim Servant",
        email = user.email ?: ""
      )
    }
  }

  // Calculate prayer schedule dynamically based on current user city and method
  val cityName = userProfile?.city ?: "Mecca"
  val methodName = userProfile?.calculationMethod ?: "MUSLIM_WORLD_LEAGUE"
  val cityLocation = remember(cityName) { PrayerCalculationEngine.getCityByName(cityName) }
  val calcMethod = remember(methodName) { PrayerCalculationEngine.getMethodByName(methodName) }

  val prayerSchedule: DayPrayerSchedule = remember(cityLocation, calcMethod) {
    PrayerCalculationEngine.calculatePrayerTimes(
      calendar = Calendar.getInstance(),
      city = cityLocation,
      method = calcMethod
    )
  }

  val todayVerse: DailyVerse = remember {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    VerseCatalogue.getVerseForDayOfYear(dayOfYear)
  }

  // Schedule exact 10m pre-prayer, exact prayer, Suhoor, and Iftar phone alarms
  LaunchedEffect(prayerSchedule, obligationsState.dailyLog.isFasting, obligationsState.preWarningMinutes) {
    NotificationScheduler.scheduleDailyPrayerAlarms(
      context = context,
      schedule = prayerSchedule,
      preWarningMinutes = obligationsState.preWarningMinutes,
      isFastingToday = obligationsState.dailyLog.isFasting
    )
  }

  // Evaluate and display animated in-app alert banner
  LaunchedEffect(prayerSchedule, obligationsState.dailyLog) {
    obligationsViewModel.evaluateInAppAlerts(prayerSchedule)
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("noor_bottom_nav")
      ) {
        NoorTab.values().forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = { Icon(tab.icon, contentDescription = tab.label) },
            label = { Text(tab.label, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimary,
              indicatorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Creative Animated In-App Alert Banner
        AnimatedInAppAlertBanner(
          alert = obligationsState.activeInAppAlert,
          onActionClick = { alert ->
            when (alert.type) {
              AlertType.SOLAH_PRE_WARNING -> {
                val prayerNameStr = alert.id.removePrefix("SOLAH_")
                val prayer = PrayerName.values().find { it.name == prayerNameStr }
                if (prayer != null) {
                  obligationsViewModel.togglePrayer(prayer)
                }
                obligationsViewModel.dismissInAppAlert()
              }
              AlertType.QURAN_RECITE -> {
                obligationsViewModel.selectDate(todayDateStr)
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
                }
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
                  coroutineScope.launch {
                    repository.saveDailyLog(updatedLog)
                  }
                }
              )
            }
            NoorTab.INSIGHTS -> {
              InsightsScreen(
                weeklyLogs = weeklyLogs,
                currentStreak = userProfile?.currentStreak ?: 0
              )
            }
            NoorTab.BADGES -> {
              BadgesScreen(
                currentStreak = userProfile?.currentStreak ?: 0,
                longestStreak = userProfile?.longestStreak ?: 0,
                unlockedBadges = unlockedBadges
              )
            }
            NoorTab.SETTINGS -> {
              SettingsScreen(
                userProfile = userProfile,
                onCityChanged = { newCity ->
                  coroutineScope.launch {
                    repository.updateUserCityAndMethod(newCity, methodName)
                  }
                },
                onMethodChanged = { newMethod ->
                  coroutineScope.launch {
                    repository.updateUserCityAndMethod(cityName, newMethod)
                  }
                },
                onSignOut = {
                  // Sign out triggers AuthStateListener in MainActivity/AppContent
                },
                quranReminderHour = obligationsState.quranReminderHour,
                quranReminderMinute = obligationsState.quranReminderMinute,
                onQuranReminderTimeChanged = { hour, minute ->
                  obligationsViewModel.setCustomQuranReminder(context, hour, minute)
                }
              )
            }
          }
        }
      }
    }
  }
}
