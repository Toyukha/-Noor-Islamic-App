package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DailyLog

@Entity(tableName = "daily_obligations")
data class DailyObligationEntity(
  @PrimaryKey val id: String, // format: "${date}_${userId}"
  val userId: String,
  val date: String, // YYYY-MM-DD
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
  val isFasting: Boolean = false,
  val fastingType: String = "Sunnah",
  val quranPagesRead: Int = 0,
  val quranMinutes: Int = 0,
  val lastSurah: String = "Al-Fatihah",
  val lastAyah: Int = 1,
  val morningAdhkarDone: Boolean = false,
  val eveningAdhkarDone: Boolean = false,
  val taraweehRakats: Int = 0,
  val laylatAlQadrVigil: Boolean = false,
  val sadaqahGiven: Boolean = false,
  val syncPending: Boolean = false,
  val lastModified: Long = System.currentTimeMillis()
) {
  fun toDailyLog(): DailyLog {
    return DailyLog(
      id = date,
      userId = userId,
      date = date,
      fajr = fajr,
      dhuhr = dhuhr,
      asr = asr,
      maghrib = maghrib,
      isha = isha,
      sunnahPrayers = sunnahPrayers,
      tahajjud = tahajjud,
      duha = duha,
      witr = witr,
      rawatibCount = rawatibCount,
      isFasting = isFasting,
      fastingType = fastingType,
      quranPagesRead = quranPagesRead,
      quranMinutes = quranMinutes,
      lastSurah = lastSurah,
      lastAyah = lastAyah,
      morningAdhkar = morningAdhkarDone,
      eveningAdhkar = eveningAdhkarDone,
      taraweehRakats = taraweehRakats,
      laylatAlQadrVigil = laylatAlQadrVigil,
      sadaqahGiven = sadaqahGiven
    )
  }

  companion object {
    fun fromDailyLog(log: DailyLog, syncPending: Boolean = false): DailyObligationEntity {
      val key = "${log.date}_${log.userId}"
      return DailyObligationEntity(
        id = key,
        userId = log.userId,
        date = log.date,
        fajr = log.fajr,
        dhuhr = log.dhuhr,
        asr = log.asr,
        maghrib = log.maghrib,
        isha = log.isha,
        sunnahPrayers = log.sunnahPrayers,
        tahajjud = log.tahajjud,
        duha = log.duha,
        witr = log.witr,
        rawatibCount = log.rawatibCount,
        isFasting = log.isFasting,
        fastingType = log.fastingType,
        quranPagesRead = log.quranPagesRead,
        quranMinutes = log.quranMinutes,
        lastSurah = log.lastSurah,
        lastAyah = log.lastAyah,
        morningAdhkarDone = log.morningAdhkar,
        eveningAdhkarDone = log.eveningAdhkar,
        taraweehRakats = log.taraweehRakats,
        laylatAlQadrVigil = log.laylatAlQadrVigil,
        sadaqahGiven = log.sadaqahGiven,
        syncPending = syncPending,
        lastModified = System.currentTimeMillis()
      )
    }
  }
}
