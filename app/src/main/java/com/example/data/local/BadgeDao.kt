package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

  @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, category ASC")
  fun getAllBadges(): Flow<List<BadgeEntity>>

  @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, category ASC")
  suspend fun getAllBadgesDirect(): List<BadgeEntity>

  @Query("SELECT * FROM badges WHERE badgeKey = :key LIMIT 1")
  fun getBadgeByKey(key: String): Flow<BadgeEntity?>

  @Query("SELECT * FROM badges WHERE badgeKey = :key LIMIT 1")
  suspend fun getBadgeByKeyDirect(key: String): BadgeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateBadges(badges: List<BadgeEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(badge: BadgeEntity)

  @Query("UPDATE badges SET currentProgress = :progress WHERE badgeKey = :key")
  suspend fun updateProgress(key: String, progress: Int)

  @Query("UPDATE badges SET isUnlocked = 1, currentProgress = targetRequirement, unlockedAt = :timestamp WHERE badgeKey = :key")
  suspend fun unlockBadge(key: String, timestamp: Long)

  @Query("SELECT COUNT(*) FROM badges WHERE isUnlocked = 1")
  fun getUnlockedCount(): Flow<Int>
}
