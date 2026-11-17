package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
  @PrimaryKey val badgeKey: String,
  val title: String,
  val description: String,
  val category: String, // "PRAYER", "FASTING", "QURAN", "STREAK"
  val iconName: String,
  val targetRequirement: Int,
  val currentProgress: Int = 0,
  val isUnlocked: Boolean = false,
  val unlockedAt: Long? = null
) {
  val progressPercent: Float
    get() = if (targetRequirement > 0) {
      (currentProgress.toFloat() / targetRequirement.toFloat()).coerceIn(0f, 1f)
    } else {
      if (isUnlocked) 1f else 0f
    }
}
