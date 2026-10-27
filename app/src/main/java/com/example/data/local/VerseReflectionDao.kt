package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseReflectionDao {

  @Query("SELECT * FROM verse_reflections WHERE date = :date LIMIT 1")
  fun getReflectionForDate(date: String): Flow<VerseReflectionEntity?>

  @Query("SELECT * FROM verse_reflections WHERE date = :date LIMIT 1")
  suspend fun getReflectionForDateDirect(date: String): VerseReflectionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(reflection: VerseReflectionEntity)

  @Query("SELECT * FROM verse_reflections ORDER BY date DESC")
  fun getAllReflections(): Flow<List<VerseReflectionEntity>>

  @Query("DELETE FROM verse_reflections WHERE date = :date")
  suspend fun deleteReflectionForDate(date: String)
}
