package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verse_reflections")
data class VerseReflectionEntity(
  @PrimaryKey val date: String, // YYYY-MM-DD
  val surahName: String,
  val surahNumber: Int,
  val ayahNumber: Int,
  val arabicText: String,
  val englishTranslation: String,
  val spiritualTheme: String,
  val contemplationNarrative: String,
  val dailyActionSteps: String,
  val supplicationDua: String,
  val generatedAt: Long = System.currentTimeMillis()
)
