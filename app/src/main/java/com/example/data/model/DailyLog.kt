package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DailyLog(
  val id: String = "",
  val userId: String = "",
  val date: String = "", // YYYY-MM-DD
  val fajr: Boolean = false,
  val dhuhr: Boolean = false,
  val asr: Boolean = false,
  val maghrib: Boolean = false,
  val isha: Boolean = false,
  val sunnahPrayers: Int = 0,
  val tahajjud: Boolean = false,
  val duha: Boolean = false,
  val witr: Boolean = false,
  val rawatibCount: Int = 0,
  @get:PropertyName("isFasting") val isFasting: Boolean = false,
  val fastingType: String = "Sunnah", // Ramadan, Sunnah, White Days, Voluntary, Qada
  val quranPagesRead: Int = 0,
  val quranMinutes: Int = 0,
  val lastSurah: String = "Al-Fatihah",
  val lastAyah: Int = 1,
  val morningAdhkar: Boolean = false,
  val eveningAdhkar: Boolean = false,
  val taraweehRakats: Int = 0,
  val laylatAlQadrVigil: Boolean = false,
  val sadaqahGiven: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  @get:Exclude
  val completedPrayersCount: Int
    get() = (if (fajr) 1 else 0) +
            (if (dhuhr) 1 else 0) +
            (if (asr) 1 else 0) +
            (if (maghrib) 1 else 0) +
            (if (isha) 1 else 0)

  @get:Exclude
  val isAllPrayersCompleted: Boolean
    get() = fajr && dhuhr && asr && maghrib && isha
}
