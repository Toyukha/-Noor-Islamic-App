package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UnlockedBadge(
  val id: String = "",
  val userId: String = "",
  val badgeKey: String = "",
  val title: String = "",
  val description: String = "",
  @ServerTimestamp val unlockedAt: Timestamp? = null
)

data class BadgeDefinition(
  val key: String,
  val title: String,
  val description: String,
  val category: BadgeCategory,
  val iconEmoji: String,
  val targetRequirement: String
)

enum class BadgeCategory {
  PRAYER,
  FASTING,
  QURAN,
  STREAK
}

object PredefinedBadges {
  val ALL_BADGES = listOf(
    BadgeDefinition(
      key = "DAWN_SEEKER_1",
      title = "Dawn Seeker",
      description = "Awakened for Fajr prayer and established your connection with Allah.",
      category = BadgeCategory.PRAYER,
      iconEmoji = "🌅",
      targetRequirement = "Complete 1 Fajr prayer"
    ),
    BadgeDefinition(
      key = "STEADFAST_SOUL_7",
      title = "Steadfast Soul",
      description = "Completed all 5 daily prayers for 7 consecutive days without missing one.",
      category = BadgeCategory.PRAYER,
      iconEmoji = "🕌",
      targetRequirement = "7-day 5-prayer streak"
    ),
    BadgeDefinition(
      key = "FAITH_GUARDIAN_30",
      title = "Faith Guardian",
      description = "Achieved 30 days of consistent prayer adherence.",
      category = BadgeCategory.PRAYER,
      iconEmoji = "⭐",
      targetRequirement = "30-day prayer streak"
    ),
    BadgeDefinition(
      key = "GUARDIAN_OF_FAST_1",
      title = "Shield of Sawm",
      description = "Fasted for the sake of Allah outside of Ramadan.",
      category = BadgeCategory.FASTING,
      iconEmoji = "🌙",
      targetRequirement = "Complete 1 voluntary fast"
    ),
    BadgeDefinition(
      key = "WHITE_DAYS_FAST",
      title = "Ayyam al-Bid Observer",
      description = "Revived the Sunnah by fasting the luminous 13th, 14th, and 15th lunar days.",
      category = BadgeCategory.FASTING,
      iconEmoji = "✨",
      targetRequirement = "Fast 3 voluntary days"
    ),
    BadgeDefinition(
      key = "FASTING_CHAMPION_10",
      title = "Covenant of Devotion",
      description = "Logged 10 days of fasting with mindfulness and patience.",
      category = BadgeCategory.FASTING,
      iconEmoji = "🤲",
      targetRequirement = "Complete 10 total fasts"
    ),
    BadgeDefinition(
      key = "QURAN_COMPANION_10",
      title = "Noble Reciter",
      description = "Recited 10 pages of the Holy Quran.",
      category = BadgeCategory.QURAN,
      iconEmoji = "📖",
      targetRequirement = "Read 10 pages of Quran"
    ),
    BadgeDefinition(
      key = "QURAN_JOURNEY_100",
      title = "Heart Illuminator",
      description = "Immersed your soul in 100 pages of the Divine Revelation.",
      category = BadgeCategory.QURAN,
      iconEmoji = "📜",
      targetRequirement = "Read 100 pages of Quran"
    ),
    BadgeDefinition(
      key = "SPIRITUAL_STREAK_3",
      title = "Sparks of Consistency",
      description = "Kept your spiritual habits active for 3 continuous days.",
      category = BadgeCategory.STREAK,
      iconEmoji = "🔥",
      targetRequirement = "3-day spiritual streak"
    ),
    BadgeDefinition(
      key = "SPIRITUAL_STREAK_14",
      title = "Unbreakable Light",
      description = "Maintained a 14-day spiritual habit streak.",
      category = BadgeCategory.STREAK,
      iconEmoji = "⚡",
      targetRequirement = "14-day streak"
    )
  )
}
