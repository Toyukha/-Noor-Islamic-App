package com.example.ui.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.local.NoorDatabase
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime
import com.example.data.repository.NoorRepository
import com.example.notification.AlarmPreferences
import com.example.ui.components.AlertType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ObligationsViewModelTest : FirestoreEmulatorTestBase() {

  private lateinit var database: NoorDatabase
  private lateinit var repository: NoorRepository

  @Before
  fun initDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, NoorDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    if (isFirestoreInitialized()) {
      repository = NoorRepository(firestore, database)
    }
  }

  @After
  fun cleanupDb() {
    if (::database.isInitialized) {
      database.close()
    }
  }

  @Test
  fun togglePrayer_updatesStateAndTogglesBoolean() = runBlocking {
    signInTestUser("test_viewmodel@example.com")
    val viewModel = ObligationsViewModel(repository)

    // Toggle Fajr
    viewModel.togglePrayer(PrayerName.FAJR)

    val state = viewModel.uiState.value
    assertTrue(state.dailyLog.fajr)

    // Toggle again to uncheck
    viewModel.togglePrayer(PrayerName.FAJR)
    assertEquals(false, viewModel.uiState.value.dailyLog.fajr)
  }

  @Test
  fun toggleFastingAndQuranPages_updatesState() = runBlocking {
    signInTestUser("test_fasting_vm@example.com")
    val viewModel = ObligationsViewModel(repository)

    viewModel.toggleFasting(isFasting = true, fastingType = "White Days")
    viewModel.addQuranPages(10)
    viewModel.updateQuranBookmark("Al-Kahf", 20)

    val state = viewModel.uiState.value
    assertTrue(state.dailyLog.isFasting)
    assertEquals("White Days", state.dailyLog.fastingType)
    assertEquals(10, state.dailyLog.quranPagesRead)
    assertEquals("Al-Kahf", state.dailyLog.lastSurah)
    assertEquals(20, state.dailyLog.lastAyah)
  }

  @Test
  fun evaluateInAppAlerts_triggersSolahPreWarningWhenWithin10Mins() = runBlocking {
    signInTestUser("test_alerts_vm@example.com")
    val viewModel = ObligationsViewModel(repository)

    val now = Calendar.getInstance()
    val mockPrayerTime = SinglePrayerTime(
      prayer = PrayerName.ASR,
      timeFormatted = "03:45 PM",
      hour = now.get(Calendar.HOUR_OF_DAY),
      minute = now.get(Calendar.MINUTE) + 5
    )
    val mockSchedule = DayPrayerSchedule(
      dateFormatted = "Monday, 27 Oct 2026",
      hijriFormatted = "16 Jumada al-Ula 1448",
      prayers = listOf(mockPrayerTime),
      nextPrayer = mockPrayerTime,
      timeUntilNextFormatted = "5m"
    )

    viewModel.evaluateInAppAlerts(mockSchedule)

    val alert = viewModel.uiState.value.activeInAppAlert
    assertNotNull(alert)
    assertEquals(AlertType.SOLAH_PRE_WARNING, alert?.type)
    assertTrue(alert?.title?.contains("Asr") == true)

    // Toggling Asr should dismiss the alert
    viewModel.togglePrayer(PrayerName.ASR)
    assertNull(viewModel.uiState.value.activeInAppAlert)
  }

  @Test
  fun syncTodayRollover_advancesSelectedDateOnlyWhenViewingPreviousToday() = runBlocking {
    signInTestUser("test_rollover_vm@example.com")
    val viewModel = ObligationsViewModel(repository)

    // User is viewing yesterday ("2026-10-26") as current today; midnight advances to "2026-10-27"
    viewModel.selectDate("2026-10-26")
    viewModel.syncTodayRollover(newTodayDateStr = "2026-10-27", previousTodayDateStr = "2026-10-26")
    assertEquals("2026-10-27", viewModel.uiState.value.selectedDate)

    // If user intentionally browsed back to an earlier date ("2026-10-20"), rollover leaves it alone
    viewModel.selectDate("2026-10-20")
    viewModel.syncTodayRollover(newTodayDateStr = "2026-10-28", previousTodayDateStr = "2026-10-27")
    assertEquals("2026-10-20", viewModel.uiState.value.selectedDate)
  }

  @Test
  fun toggleNotificationPreferences_persistsToAlarmPreferencesAndState() = runBlocking {
    signInTestUser("test_prefs_vm@example.com")
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = ObligationsViewModel(repository)

    viewModel.togglePrayerPreWarning(context, false)
    viewModel.togglePrayerAlerts(context, false)
    viewModel.toggleFastingAlerts(context, false)
    viewModel.toggleVerseAlerts(context, false)

    val state = viewModel.uiState.value
    assertFalse(state.prayerPreWarningEnabled)
    assertFalse(state.prayerAlertsEnabled)
    assertFalse(state.fastingAlertsEnabled)
    assertFalse(state.verseAlertsEnabled)

    assertFalse(AlarmPreferences.isPrayerPreWarningEnabled(context))
    assertFalse(AlarmPreferences.isPrayerAlertsEnabled(context))
    assertFalse(AlarmPreferences.isFastingAlertsEnabled(context))
    assertFalse(AlarmPreferences.isVerseAlertsEnabled(context))
  }
}
