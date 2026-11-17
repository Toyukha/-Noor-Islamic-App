package com.example.data.local

object BadgeCatalogue {

  val DEFAULT_BADGES = listOf(
    BadgeEntity(
      badgeKey = "STREAK_7_DAY",
      title = "7-Day Prayer Streak",
      description = "Completed daily spiritual obligations for 7 consecutive days.",
      category = "STREAK",
      iconName = "military_tech",
      targetRequirement = 7
    ),
    BadgeEntity(
      badgeKey = "STREAK_14_DAY",
      title = "Fortress of Faith",
      description = "Maintained unbroken spiritual habit streak for 14 continuous days.",
      category = "STREAK",
      iconName = "auto_awesome",
      targetRequirement = 14
    ),
    BadgeEntity(
      badgeKey = "QURAN_JUZ_1",
      title = "Full Quran Juz",
      description = "Recited a complete Juz (20 pages) of the Holy Quran.",
      category = "QURAN",
      iconName = "menu_book",
      targetRequirement = 20
    ),
    BadgeEntity(
      badgeKey = "QURAN_KHATM",
      title = "Quran Companion",
      description = "Read 100 cumulative pages of the Divine Revelation.",
      category = "QURAN",
      iconName = "bookmark",
      targetRequirement = 100
    ),
    BadgeEntity(
      badgeKey = "DAWN_SEEKER",
      title = "Dawn Seeker",
      description = "Awakened for Fajr prayer and established your connection with Allah 7 times.",
      category = "PRAYER",
      iconName = "nights_stay",
      targetRequirement = 7
    ),
    BadgeEntity(
      badgeKey = "SHIELD_OF_SAWM",
      title = "Shield of Sawm",
      description = "Completed 3 days of fasting for the sake of Allah.",
      category = "FASTING",
      iconName = "wb_sunny",
      targetRequirement = 3
    ),
    BadgeEntity(
      badgeKey = "WHITE_DAYS",
      title = "Ayyam al-Bid Observer",
      description = "Observed fasting during the middle White Days of the Islamic month.",
      category = "FASTING",
      iconName = "brightness_low",
      targetRequirement = 3
    ),
    BadgeEntity(
      badgeKey = "SUNNAH_DEVOTEE",
      title = "Sunnah Reviver",
      description = "Offered 50 voluntary Sunnah, Duha, or Witr rak'ahs.",
      category = "PRAYER",
      iconName = "check_circle",
      targetRequirement = 50
    )
  )
}
