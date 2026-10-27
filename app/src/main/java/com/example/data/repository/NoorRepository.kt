package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.DailyObligationEntity
import com.example.data.local.NoorDatabase
import com.example.data.local.QuranProgressEntity
import com.example.data.model.DailyLog
import com.example.data.model.PredefinedBadges
import com.example.data.model.UnlockedBadge
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoorRepository(
  private val db: FirebaseFirestore,
  private val localDb: NoorDatabase? = null
) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    NoorDatabase.getDatabase(context.applicationContext)
  )

  private val auth = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Noor data.")
  }

  val isUserSignedIn: Boolean
    get() = auth.currentUser != null

  val currentUserId: String?
    get() = auth.currentUser?.uid

  // Observe current user profile
  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
          throw error
        }
    )
  }

  suspend fun ensureUserProfileExists(displayName: String, email: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val snapshot = docRef.get().await()
    if (!snapshot.exists()) {
      val profile = UserProfile(
        userId = uid,
        displayName = displayName.ifBlank { "Devout Muslim" },
        email = email,
        city = "Mecca",
        calculationMethod = "MUSLIM_WORLD_LEAGUE",
        currentStreak = 0,
        longestStreak = 0,
        lastActiveDate = ""
      )
      val payload = mapOf(
        "userId" to uid,
        "displayName" to profile.displayName,
        "email" to profile.email,
        "city" to profile.city,
        "calculationMethod" to profile.calculationMethod,
        "currentStreak" to profile.currentStreak,
        "longestStreak" to profile.longestStreak,
        "lastActiveDate" to profile.lastActiveDate,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(payload).await()
    }
  }

  suspend fun updateUserCityAndMethod(city: String, calculationMethod: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      docRef.update(
        mapOf(
          "city" to city,
          "calculationMethod" to calculationMethod,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // Observe a specific day's obligation log with instant local Room cache
  fun observeDailyLog(dateStr: String): Flow<DailyLog> = flow {
    val uid = requireUserId()
    // First, emit cached record from Room if available
    localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.let { localEntity ->
      emit(localEntity.toDailyLog())
    }

    val docRef = db.collection("users").document(uid).collection("daily_logs").document(dateStr)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          val remote = snapshot.toObject(DailyLog::class.java)
          if (remote != null) {
            localDb?.dailyObligationDao()?.insertOrUpdate(
              DailyObligationEntity.fromDailyLog(remote, syncPending = false)
            )
            remote
          } else {
            val fallback = localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.toDailyLog()
              ?: DailyLog(id = dateStr, userId = uid, date = dateStr)
            fallback
          }
        }
        .catch { error ->
          val cached = localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.toDailyLog()
          if (cached != null) {
            emit(cached)
          } else {
            if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
            throw error
          }
        }
    )
  }

  // Save or update an obligation log for a date (Room-first then Firestore sync)
  suspend fun saveDailyLog(log: DailyLog) {
    val uid = requireUserId()
    val key = "${log.date}_${uid}"

    // 1. Immediately cache in Room
    localDb?.dailyObligationDao()?.insertOrUpdate(
      DailyObligationEntity.fromDailyLog(log, syncPending = true)
    )
    localDb?.quranProgressDao()?.insertOrUpdate(
      QuranProgressEntity(
        userId = uid,
        lastSurah = log.lastSurah,
        lastAyah = log.lastAyah,
        totalPagesRead = log.quranPagesRead,
        totalMinutesSpent = log.quranMinutes
      )
    )

    // 2. Sync to Firestore
    val docRef = db.collection("users").document(uid).collection("daily_logs").document(log.date)
    try {
      val existing = docRef.get().await()
      val payload = mutableMapOf<String, Any>(
        "id" to log.date,
        "userId" to uid,
        "date" to log.date,
        "fajr" to log.fajr,
        "dhuhr" to log.dhuhr,
        "asr" to log.asr,
        "maghrib" to log.maghrib,
        "isha" to log.isha,
        "sunnahPrayers" to log.sunnahPrayers,
        "isFasting" to log.isFasting,
        "fastingType" to log.fastingType,
        "quranPagesRead" to log.quranPagesRead,
        "quranMinutes" to log.quranMinutes,
        "lastSurah" to log.lastSurah,
        "lastAyah" to log.lastAyah,
        "updatedAt" to FieldValue.serverTimestamp()
      )

      if (!existing.exists()) {
        payload["createdAt"] = FieldValue.serverTimestamp()
      } else {
        payload["createdAt"] = existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()
      }

      docRef.set(payload).await()
      localDb?.dailyObligationDao()?.markAsSynced(key)
      checkAndTriggerAchievements(uid, log)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  // Observe last 7 days of daily logs for weekly reports with Room caching
  fun observeWeeklyLogs(sevenDaysAgoDateStr: String): Flow<List<DailyLog>> = flow {
    val uid = requireUserId()
    val colRef = db.collection("users").document(uid).collection("daily_logs")
    emitAll(
      colRef
        .whereGreaterThanOrEqualTo("date", sevenDaysAgoDateStr)
        .snapshots()
        .map { snapshot ->
          val remoteLogs = snapshot.toObjects(DailyLog::class.java).sortedBy { it.date }
          remoteLogs.forEach { rLog ->
            localDb?.dailyObligationDao()?.insertOrUpdate(
              DailyObligationEntity.fromDailyLog(rLog, syncPending = false)
            )
          }
          remoteLogs
        }
        .catch { error ->
          val cached = localDb?.dailyObligationDao()?.getWeeklyObligations(uid, sevenDaysAgoDateStr)?.first()
          if (!cached.isNullOrEmpty()) {
            emit(cached.map { it.toDailyLog() })
          } else {
            if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
            throw error
          }
        }
    )
  }

  // Observe Quran completion progress from Room cache
  fun observeQuranProgress(userId: String): Flow<QuranProgressEntity?> {
    return localDb?.quranProgressDao()?.getQuranProgress(userId) ?: flow { emit(null) }
  }

  // Observe unlocked badges
  fun observeUnlockedBadges(): Flow<List<UnlockedBadge>> = flow {
    val uid = requireUserId()
    val colRef = db.collection("users").document(uid).collection("badges")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(UnlockedBadge::class.java)
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
          throw error
        }
    )
  }

  suspend fun unlockBadge(badgeKey: String, title: String, description: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("badges").document(badgeKey)
    try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val payload = mapOf(
          "id" to badgeKey,
          "userId" to uid,
          "badgeKey" to badgeKey,
          "title" to title,
          "description" to description,
          "unlockedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
    }
  }

  private suspend fun checkAndTriggerAchievements(uid: String, log: DailyLog) {
    // 1. Dawn Seeker
    if (log.fajr) {
      unlockBadge("DAWN_SEEKER_1", "Dawn Seeker", "Awakened for Fajr prayer and established your connection with Allah.")
    }

    // 2. Fasting Badges
    if (log.isFasting) {
      unlockBadge("GUARDIAN_OF_FAST_1", "Shield of Sawm", "Fasted for the sake of Allah outside of Ramadan.")
      if (log.fastingType.contains("White", ignoreCase = true)) {
        unlockBadge("WHITE_DAYS_FAST", "Ayyam al-Bid Observer", "Revived the Sunnah by fasting the luminous White Days.")
      }
    }

    // 3. Quran Badges
    if (log.quranPagesRead >= 10) {
      unlockBadge("QURAN_COMPANION_10", "Noble Reciter", "Recited 10 pages of the Holy Quran.")
    }
    if (log.quranPagesRead >= 100) {
      unlockBadge("QURAN_JOURNEY_100", "Heart Illuminator", "Immersed your soul in 100 pages of the Divine Revelation.")
    }

    // 4. Update user streak
    val userRef = db.collection("users").document(uid)
    val userSnap = userRef.get().await()
    if (userSnap.exists()) {
      val lastActive = userSnap.getString("lastActiveDate") ?: ""
      val currentStreak = (userSnap.getLong("currentStreak") ?: 0L).toInt()
      val longestStreak = (userSnap.getLong("longestStreak") ?: 0L).toInt()

      if (lastActive != log.date) {
        val newStreak = currentStreak + 1
        val newLongest = maxOf(longestStreak, newStreak)
        userRef.update(
          mapOf(
            "lastActiveDate" to log.date,
            "currentStreak" to newStreak,
            "longestStreak" to newLongest,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()

        if (newStreak >= 3) {
          unlockBadge("SPIRITUAL_STREAK_3", "Sparks of Consistency", "Kept your spiritual habits active for 3 continuous days.")
        }
        if (newStreak >= 7) {
          unlockBadge("STEADFAST_SOUL_7", "Steadfast Soul", "Completed daily spiritual obligations for 7 consecutive days.")
        }
        if (newStreak >= 14) {
          unlockBadge("SPIRITUAL_STREAK_14", "Unbreakable Light", "Maintained a 14-day spiritual habit streak.")
        }
      }
    }
  }
}
