package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranProgressDao {

  @Query("SELECT * FROM quran_progress WHERE userId = :userId LIMIT 1")
  fun getQuranProgress(userId: String): Flow<QuranProgressEntity?>

  @Query("SELECT * FROM quran_progress WHERE userId = :userId LIMIT 1")
  suspend fun getQuranProgressDirect(userId: String): QuranProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(progress: QuranProgressEntity)

  @Query("DELETE FROM quran_progress WHERE userId = :userId")
  suspend fun clearForUser(userId: String)
}
