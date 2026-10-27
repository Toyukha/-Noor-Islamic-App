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
import com.example.ui.components.AlertType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
    repository = NoorRepository(firestore, database)
  }

  @After
  fun cleanupDb() {
    database.close()
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
}
