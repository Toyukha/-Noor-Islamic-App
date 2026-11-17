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
      morningAdhkarDone = true,
      eveningAdhkarDone = true,
      syncPending = true
    )

    obligationDao.insertOrUpdate(entity)

    val retrieved = obligationDao.getDailyObligation("test_user", "2026-10-27").first()
    assertNotNull(retrieved)
    assertEquals("test_user", retrieved?.userId)
    assertEquals("2026-10-27", retrieved?.date)
    assertTrue(retrieved?.fajr == true)
    assertTrue(retrieved?.isFasting == true)
    assertTrue(retrieved?.morningAdhkarDone == true)
    assertTrue(retrieved?.eveningAdhkarDone == true)
    assertEquals(8, retrieved?.quranPagesRead)
    assertTrue(retrieved?.syncPending == true)

    // Mark as synced
    obligationDao.markAsSynced(entity.id)
    val afterSync = obligationDao.getDailyObligationDirect("test_user", "2026-10-27")
    assertEquals(false, afterSync?.syncPending)
  }

  @Test
  fun insertAndRetrieveObligationWithAllFields() = runBlocking {
    val ramadanEntity = DailyObligationEntity(
      id = "ramadan_test_1",
      userId = "user_ramadan",
      date = "2026-03-15",
      fajr = true,
      dhuhr = true,
      asr = true,
      maghrib = true,
      isha = true,
      isFasting = true,
      fastingType = "Ramadan",
      taraweehRakats = 20,
      laylatAlQadrVigil = true,
      sadaqahGiven = true
    )

    obligationDao.insertOrUpdate(ramadanEntity)

    val retrieved = obligationDao.getDailyObligation("user_ramadan", "2026-03-15").first()
    assertNotNull(retrieved)
    assertEquals(20, retrieved?.taraweehRakats)
    assertTrue(retrieved?.laylatAlQadrVigil == true)
    assertTrue(retrieved?.sadaqahGiven == true)

    // Test conversion to DailyLog
    val log = retrieved?.toDailyLog()
    assertNotNull(log)
    assertEquals(20, log?.taraweehRakats)
    assertTrue(log?.laylatAlQadrVigil == true)
    assertTrue(log?.sadaqahGiven == true)
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

  @Test
  fun insertAndUnlockBadgeInRoom() = runBlocking {
    val badgeDao = database.badgeDao()
    val badge = BadgeEntity(
      badgeKey = "STREAK_7_DAY",
      title = "7-Day Prayer Streak",
      description = "Completed all 5 obligatory daily prayers for 7 consecutive days",
      category = "STREAK",
      iconName = "military_tech",
      targetRequirement = 7,
      currentProgress = 3,
      isUnlocked = false
    )

    badgeDao.insertOrUpdate(badge)

    val list = badgeDao.getAllBadges().first()
    assertEquals(1, list.size)
    assertEquals("7-Day Prayer Streak", list[0].title)
    assertEquals(false, list[0].isUnlocked)

    // Progress badge
    badgeDao.updateProgress("STREAK_7_DAY", 7)
    val afterProgress = badgeDao.getBadgeByKeyDirect("STREAK_7_DAY")
    assertEquals(7, afterProgress?.currentProgress)

    // Unlock badge
    badgeDao.unlockBadge("STREAK_7_DAY", 1700000000000L)
    val unlocked = badgeDao.getBadgeByKeyDirect("STREAK_7_DAY")
    assertEquals(true, unlocked?.isUnlocked)
    assertNotNull(unlocked?.unlockedAt)
  }

  @Test
  fun testGlobalCityDirectorySearchAndNearest() {
    val results = com.example.data.model.GlobalCityDirectory.searchCities("Cairo")
    assertTrue(results.isNotEmpty())
    assertEquals("Egypt", results[0].country)

    // Check nearest city for London coordinates (51.50, -0.12)
    val nearestLondon = com.example.data.model.GlobalCityDirectory.findNearestCity(51.50, -0.12)
    assertEquals("London", nearestLondon.name)

    // Check region filtering
    val africaCities = com.example.data.model.GlobalCityDirectory.searchCities("", "Africa")
    assertTrue(africaCities.size > 15)
  }
}

