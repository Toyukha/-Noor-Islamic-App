package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NoorDatabaseTest {

  private lateinit var database: NoorDatabase
  private lateinit var obligationDao: DailyObligationDao
  private lateinit var quranDao: QuranProgressDao

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, NoorDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    obligationDao = database.dailyObligationDao()
    quranDao = database.quranProgressDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun insertAndRetrieveDailyObligation() = runBlocking {
    val entity = DailyObligationEntity(
      id = "2026-10-27_test_user",
      userId = "test_user",
      date = "2026-10-27",
      fajr = true,
      dhuhr = true,
      asr = true,
      maghrib = true,
      isha = true,
      isFasting = true,
      fastingType = "Sunnah",
      quranPagesRead = 8,
      syncPending = true
    )

    obligationDao.insertOrUpdate(entity)

    val retrieved = obligationDao.getDailyObligation("test_user", "2026-10-27").first()
    assertNotNull(retrieved)
    assertEquals("test_user", retrieved?.userId)
    assertEquals("2026-10-27", retrieved?.date)
    assertTrue(retrieved?.fajr == true)
    assertTrue(retrieved?.isFasting == true)
    assertEquals(8, retrieved?.quranPagesRead)
    assertTrue(retrieved?.syncPending == true)

    // Mark as synced
    obligationDao.markAsSynced(entity.id)
    val afterSync = obligationDao.getDailyObligationDirect("test_user", "2026-10-27")
    assertEquals(false, afterSync?.syncPending)
  }

  @Test
  fun insertAndRetrieveQuranProgress() = runBlocking {
    val progress = QuranProgressEntity(
      userId = "user_quran_1",
      lastSurah = "Al-Kahf",
      lastAyah = 110,
      totalPagesRead = 45,
      totalMinutesSpent = 120
    )

    quranDao.insertOrUpdate(progress)

    val retrieved = quranDao.getQuranProgress("user_quran_1").first()
    assertNotNull(retrieved)
    assertEquals("Al-Kahf", retrieved?.lastSurah)
    assertEquals(110, retrieved?.lastAyah)
    assertEquals(45, retrieved?.totalPagesRead)
  }

  @Test
  fun insertAndRetrieveVerseReflection() = runBlocking {
    val reflectionDao = database.verseReflectionDao()
    val reflection = VerseReflectionEntity(
      date = "2026-10-27",
      surahName = "Ash-Sharh",
      surahNumber = 94,
      ayahNumber = 5,
      arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
      englishTranslation = "For indeed, with hardship [will be] ease.",
      spiritualTheme = "Relief & Perseverance",
      contemplationNarrative = "Hardship does not isolate you; ease is already companioning it.",
      dailyActionSteps = "Pause when stressed. Recite SubhanAllah 33 times.",
      supplicationDua = "O Allah, replace every sorrow with Your enduring ease."
    )

    reflectionDao.insertOrUpdate(reflection)

    val retrieved = reflectionDao.getReflectionForDate("2026-10-27").first()
    assertNotNull(retrieved)
    assertEquals("Ash-Sharh", retrieved?.surahName)
    assertEquals("Relief & Perseverance", retrieved?.spiritualTheme)
    assertEquals("O Allah, replace every sorrow with Your enduring ease.", retrieved?.supplicationDua)
  }
}
