package com.example.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

  @Test
  fun calculate_firstActiveDayInitializesStreakToOne() {
    val result = StreakCalculator.calculate(
      lastActiveDate = "",
      logDate = "2026-10-27",
      currentStreak = 0,
      longestStreak = 0
    )

    assertTrue(result.changed)
    assertEquals(1, result.currentStreak)
    assertEquals(1, result.longestStreak)
    assertEquals("2026-10-27", result.lastActiveDate)
  }

  @Test
  fun calculate_consecutiveNextDayIncrementsStreakAndLongest() {
    val result = StreakCalculator.calculate(
      lastActiveDate = "2026-10-27",
      logDate = "2026-10-28",
      currentStreak = 4,
      longestStreak = 4
    )

    assertTrue(result.changed)
    assertEquals(5, result.currentStreak)
    assertEquals(5, result.longestStreak)
    assertEquals("2026-10-28", result.lastActiveDate)
  }

  @Test
  fun calculate_gapInDaysResetsCurrentStreakWhilePreservingLongest() {
    val result = StreakCalculator.calculate(
      lastActiveDate = "2026-10-27",
      logDate = "2026-10-30",
      currentStreak = 6,
      longestStreak = 12
    )

    assertTrue(result.changed)
    assertEquals(1, result.currentStreak)
    assertEquals(12, result.longestStreak)
    assertEquals("2026-10-30", result.lastActiveDate)
  }

  @Test
  fun calculate_sameDayUpdateDoesNotInflateStreak() {
    val result = StreakCalculator.calculate(
      lastActiveDate = "2026-10-27",
      logDate = "2026-10-27",
      currentStreak = 3,
      longestStreak = 5
    )

    assertFalse(result.changed)
    assertEquals(3, result.currentStreak)
    assertEquals(5, result.longestStreak)
    assertEquals("2026-10-27", result.lastActiveDate)
  }

  @Test
  fun calculate_backdatedLogDoesNotMutateStreak() {
    val result = StreakCalculator.calculate(
      lastActiveDate = "2026-10-27",
      logDate = "2026-10-25",
      currentStreak = 3,
      longestStreak = 5
    )

    assertFalse(result.changed)
    assertEquals(3, result.currentStreak)
    assertEquals(5, result.longestStreak)
    assertEquals("2026-10-27", result.lastActiveDate)
  }
}
