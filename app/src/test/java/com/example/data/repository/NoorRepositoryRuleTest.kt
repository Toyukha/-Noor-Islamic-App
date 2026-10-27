package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DailyLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoorRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test(expected = IllegalStateException::class)
  fun unauthenticatedUser_failsAccess() = runBlocking {
    val repository = NoorRepository(firestore)
    auth.signOut()
    repository.saveDailyLog(DailyLog(id = "2026-10-26", date = "2026-10-26"))
  }

  @Test
  fun authenticatedUser_canSaveAndReadLog() = runBlocking {
    val uid = signInTestUser("user_${System.currentTimeMillis()}@example.com")
    val repository = NoorRepository(firestore)

    val log = DailyLog(
      id = "2026-10-26",
      userId = uid,
      date = "2026-10-26",
      fajr = true,
      dhuhr = true,
      asr = true,
      maghrib = true,
      isha = true,
      isFasting = true,
      fastingType = "Sunnah",
      quranPagesRead = 5
    )

    repository.saveDailyLog(log)

    val savedLog = repository.observeDailyLog("2026-10-26").first()
    assertEquals("2026-10-26", savedLog.date)
    assertTrue(savedLog.fajr)
    assertTrue(savedLog.isFasting)
    assertEquals(5, savedLog.quranPagesRead)
  }

  @Test
  fun authenticatedUser_canUnlockBadge() = runBlocking {
    val uid = signInTestUser("badge_user_${System.currentTimeMillis()}@example.com")
    val repository = NoorRepository(firestore)

    repository.unlockBadge("DAWN_SEEKER_1", "Dawn Seeker", "Awakened for Fajr")

    val badges = repository.observeUnlockedBadges().first()
    assertTrue(badges.any { it.badgeKey == "DAWN_SEEKER_1" })
  }
}
