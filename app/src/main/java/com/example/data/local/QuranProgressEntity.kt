package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_progress")
data class QuranProgressEntity(
  @PrimaryKey val userId: String,
  val lastSurah: String = "Al-Fatihah",
  val lastAyah: Int = 1,
  val totalPagesRead: Int = 0,
  val totalMinutesSpent: Int = 0,
  val khatmCount: Int = 0,
  val lastUpdated: Long = System.currentTimeMillis()
)
