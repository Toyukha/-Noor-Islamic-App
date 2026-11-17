package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [DailyObligationEntity::class, QuranProgressEntity::class, VerseReflectionEntity::class, BadgeEntity::class],
  version = 6,
  exportSchema = false
)
abstract class NoorDatabase : RoomDatabase() {

  abstract fun dailyObligationDao(): DailyObligationDao
  abstract fun quranProgressDao(): QuranProgressDao
  abstract fun verseReflectionDao(): VerseReflectionDao
  abstract fun badgeDao(): BadgeDao

  companion object {
    @Volatile
    private var INSTANCE: NoorDatabase? = null

    fun getDatabase(context: Context): NoorDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          NoorDatabase::class.java,
          "noor_local_cache.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
