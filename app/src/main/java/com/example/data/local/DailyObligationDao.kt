package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyObligationDao {

  @Query("SELECT * FROM daily_obligations WHERE userId = :userId AND date = :date LIMIT 1")
  fun getDailyObligation(userId: String, date: String): Flow<DailyObligationEntity?>

  @Query("SELECT * FROM daily_obligations WHERE userId = :userId AND date = :date LIMIT 1")
  suspend fun getDailyObligationDirect(userId: String, date: String): DailyObligationEntity?

  @Query("SELECT * FROM daily_obligations WHERE userId = :userId AND date >= :startDate ORDER BY date ASC")
  fun getWeeklyObligations(userId: String, startDate: String): Flow<List<DailyObligationEntity>>

  @Query("SELECT * FROM daily_obligations WHERE syncPending = 1")
  suspend fun getPendingSyncObligations(): List<DailyObligationEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(obligation: DailyObligationEntity)

  @Query("UPDATE daily_obligations SET syncPending = 0 WHERE id = :id")
  suspend fun markAsSynced(id: String)

  @Query("DELETE FROM daily_obligations WHERE userId = :userId")
  suspend fun clearForUser(userId: String)
}
