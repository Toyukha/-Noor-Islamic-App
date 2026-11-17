package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.BadgeCatalogue
import com.example.data.local.BadgeEntity
import com.example.data.local.DailyObligationEntity
import com.example.data.local.NoorDatabase
import com.example.data.local.QuranProgressEntity
import com.example.data.model.DailyLog
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class StreakUpdateResult(
  val currentStreak: Int,
  val longestStreak: Int,
  val lastActiveDate: String,
  val changed: Boolean
)

object StreakCalculator {
  private fun parseUtcEpochDay(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    return try {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
      }
      val date = sdf.parse(dateStr) ?: return null
      TimeUnit.MILLISECONDS.toDays(date.time)
    } catch (_: Exception) {
      null
    }
  }

  fun calculate(
    lastActiveDate: String,
    logDate: String,
    currentStreak: Int,
    longestStreak: Int
  ): StreakUpdateResult {
    val logDay = parseUtcEpochDay(logDate)
      ?: return StreakUpdateResult(currentStreak, longestStreak, lastActiveDate, changed = false)

    if (lastActiveDate.isBlank()) {
      val next = maxOf(1, currentStreak)
      return StreakUpdateResult(next, maxOf(longestStreak, next), logDate, changed = true)
    }

    val lastDay = parseUtcEpochDay(lastActiveDate)
    if (lastDay == null) {
      val next = maxOf(1, currentStreak)
      return StreakUpdateResult(next, maxOf(longestStreak, next), logDate, changed = true)
    }

    val diff = logDay - lastDay
    return when {
      diff == 1L -> {
        val next = currentStreak + 1
        StreakUpdateResult(next, maxOf(longestStreak, next), logDate, changed = true)
      }
      diff > 1L -> {
        StreakUpdateResult(1, maxOf(longestStreak, 1), logDate, changed = true)
      }
      else -> {
        // Same day or backdated edit does not artificially inflate streak
        StreakUpdateResult(currentStreak, longestStreak, lastActiveDate, changed = false)
      }
    }
  }
}

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

  // Observe current user profile gracefully (never crashes stream on offline transient error)
  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = currentUserId ?: return@flow
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
        }
        .catch { error ->
          if (error is CancellationException) throw error
          if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
          emit(null)
        }
    )
  }

  suspend fun ensureUserProfileExists(displayName: String, email: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
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
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
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
    }
  }

  suspend fun activateSubscription(
    plan: String,
    currency: String,
    amount: Double,
    paymentMethod: String,
    durationDays: Int = 30
  ): String {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val now = System.currentTimeMillis()
    val expiresAt = now + (durationDays.toLong() * 24L * 60L * 60L * 1000L)
    val txnId = "NOOR-TXN-" + (100000..999999).random()

    try {
      docRef.update(
        mapOf(
          "isSubscribed" to true,
          "subscriptionPlan" to plan,
          "subscriptionCurrency" to currency,
          "subscriptionAmount" to amount,
          "subscriptionExpiresAt" to expiresAt,
          "subscriptionPaymentMethod" to paymentMethod,
          "lastTransactionId" to txnId,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
    return txnId
  }

  suspend fun cancelSubscription() {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      docRef.update(
        mapOf(
          "isSubscribed" to false,
          "subscriptionPlan" to "FREE",
          "subscriptionAmount" to 0.0,
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

  // Observe a specific day's obligation log with instant local Room cache & offline resilience
  fun observeDailyLog(dateStr: String): Flow<DailyLog> = flow {
    val uid = requireUserId()
    localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.let { localEntity ->
      emit(localEntity.toDailyLog())
    }

    val docRef = db.collection("users").document(uid).collection("daily_logs").document(dateStr)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          val remote = if (snapshot.exists()) snapshot.toObject(DailyLog::class.java) else null
          if (remote != null) {
            localDb?.dailyObligationDao()?.insertOrUpdate(
              DailyObligationEntity.fromDailyLog(remote, syncPending = false)
            )
            remote
          } else {
            localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.toDailyLog()
              ?: DailyLog(id = dateStr, userId = uid, date = dateStr)
          }
        }
        .catch { error ->
          if (error is CancellationException) throw error
          if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
          val fallback = localDb?.dailyObligationDao()?.getDailyObligationDirect(uid, dateStr)?.toDailyLog()
            ?: DailyLog(id = dateStr, userId = uid, date = dateStr)
          emit(fallback)
        }
    )
  }

  // Save or update an obligation log for a date (Room-first then Firestore sync)
  suspend fun saveDailyLog(log: DailyLog) {
    val uid = requireUserId()
    val normalizedLog = log.copy(
      id = log.date,
      userId = uid
    )
    val key = "${normalizedLog.date}_${uid}"

    // 1. Immediately persist locally in Room so data is never lost offline
    localDb?.dailyObligationDao()?.insertOrUpdate(
      DailyObligationEntity.fromDailyLog(normalizedLog, syncPending = true)
    )
    localDb?.quranProgressDao()?.insertOrUpdate(
      QuranProgressEntity(
        userId = uid,
        lastSurah = normalizedLog.lastSurah,
        lastAyah = normalizedLog.lastAyah,
        totalPagesRead = normalizedLog.quranPagesRead,
        totalMinutesSpent = normalizedLog.quranMinutes
      )
    )

    // Update local Room badge progress immediately even if offline
    updateLocalBadgeProgressIdempotent(uid)

    // 2. Sync to Firestore
    val docRef = db.collection("users").document(uid).collection("daily_logs").document(normalizedLog.date)
    try {
      syncLogToFirestore(uid, normalizedLog)
      localDb?.dailyObligationDao()?.markAsSynced(key)
      checkAndTriggerAchievements(uid, normalizedLog)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      // Data remains safely persisted in Room with syncPending = true for later flush
    }
  }

  suspend fun flushPendingSync() {
    val uid = currentUserId ?: return
    val dao = localDb?.dailyObligationDao() ?: return
    val pending = dao.getPendingSyncObligations().filter { it.userId == uid }
    for (entity in pending) {
      try {
        val log = entity.toDailyLog()
        syncLogToFirestore(uid, log)
        dao.markAsSynced(entity.id)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.w("NoorRepository", "Pending sync failed for ${entity.date}: ${e.message}")
      }
    }
  }

  private suspend fun syncLogToFirestore(uid: String, log: DailyLog) {
    val docRef = db.collection("users").document(uid).collection("daily_logs").document(log.date)
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
      "tahajjud" to log.tahajjud,
      "duha" to log.duha,
      "witr" to log.witr,
      "rawatibCount" to log.rawatibCount,
      "isFasting" to log.isFasting,
      "fastingType" to log.fastingType,
      "quranPagesRead" to log.quranPagesRead,
      "quranMinutes" to log.quranMinutes,
      "lastSurah" to log.lastSurah,
      "lastAyah" to log.lastAyah,
      "morningAdhkar" to log.morningAdhkar,
      "eveningAdhkar" to log.eveningAdhkar,
      "taraweehRakats" to log.taraweehRakats,
      "laylatAlQadrVigil" to log.laylatAlQadrVigil,
      "sadaqahGiven" to log.sadaqahGiven,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    if (!existing.exists()) {
      payload["createdAt"] = FieldValue.serverTimestamp()
    } else {
      payload["createdAt"] = existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()
    }

    docRef.set(payload).await()
  }

  // Observe last 7 days of daily logs for weekly reports with Room caching & offline resilience
  fun observeWeeklyLogs(sevenDaysAgoDateStr: String): Flow<List<DailyLog>> = flow {
    val uid = currentUserId ?: run {
      emit(emptyList())
      return@flow
    }

    val initialCached = localDb?.dailyObligationDao()?.getWeeklyObligations(uid, sevenDaysAgoDateStr)?.first()
    if (!initialCached.isNullOrEmpty()) {
      emit(initialCached.map { it.toDailyLog() })
    }

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
          if (error is CancellationException) throw error
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
          val cached = localDb?.dailyObligationDao()?.getWeeklyObligations(uid, sevenDaysAgoDateStr)?.first()
          emit(cached?.map { it.toDailyLog() } ?: emptyList())
        }
    )
  }

  // Observe Quran completion progress from Room cache
  fun observeQuranProgress(userId: String): Flow<QuranProgressEntity?> {
    return localDb?.quranProgressDao()?.getQuranProgress(userId) ?: flow { emit(null) }
  }

  // Observe Badges from local Room database
  fun observeRoomBadges(): Flow<List<BadgeEntity>> {
    return localDb?.badgeDao()?.getAllBadges() ?: flow { emit(BadgeCatalogue.DEFAULT_BADGES) }
  }

  suspend fun ensureDefaultBadgesInitialized() {
    val dao = localDb?.badgeDao() ?: return
    val existing = dao.getAllBadgesDirect()
    if (existing.isEmpty()) {
      dao.insertOrUpdateBadges(BadgeCatalogue.DEFAULT_BADGES)
    }
  }

  // Observe unlocked badges safely
  fun observeUnlockedBadges(): Flow<List<UnlockedBadge>> = flow {
    val uid = currentUserId ?: run {
      emit(emptyList())
      return@flow
    }
    val colRef = db.collection("users").document(uid).collection("badges")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(UnlockedBadge::class.java)
        }
        .catch { error ->
          if (error is CancellationException) throw error
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, colRef.path)
          emit(emptyList())
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

  private suspend fun updateLocalBadgeProgressIdempotent(uid: String) {
    val badgeDao = localDb?.badgeDao() ?: return
    val obligationDao = localDb.dailyObligationDao()
    val history = obligationDao.getAllObligationsDirect(uid)
    if (history.isEmpty()) return

    val now = System.currentTimeMillis()
    val fajrDays = history.count { it.fajr }
    if (fajrDays >= 7) badgeDao.unlockBadge("DAWN_SEEKER", now)
    else badgeDao.updateProgress("DAWN_SEEKER", fajrDays)

    val fastingDays = history.count { it.isFasting }
    if (fastingDays >= 3) badgeDao.unlockBadge("SHIELD_OF_SAWM", now)
    else badgeDao.updateProgress("SHIELD_OF_SAWM", fastingDays)

    val whiteDays = history.count { it.isFasting && it.fastingType.contains("White", ignoreCase = true) }
    if (whiteDays >= 3) badgeDao.unlockBadge("WHITE_DAYS", now)
    else badgeDao.updateProgress("WHITE_DAYS", whiteDays)

    val totalQuran = history.sumOf { it.quranPagesRead }
    if (totalQuran >= 20) badgeDao.unlockBadge("QURAN_JUZ_1", now)
    else badgeDao.updateProgress("QURAN_JUZ_1", totalQuran)

    if (totalQuran >= 100) badgeDao.unlockBadge("QURAN_KHATM", now)
    else badgeDao.updateProgress("QURAN_KHATM", totalQuran)

    val totalSunnah = history.sumOf {
      it.sunnahPrayers + it.rawatibCount +
        (if (it.tahajjud) 2 else 0) +
        (if (it.duha) 2 else 0) +
        (if (it.witr) 1 else 0)
    }
    if (totalSunnah >= 50) badgeDao.unlockBadge("SUNNAH_DEVOTEE", now)
    else badgeDao.updateProgress("SUNNAH_DEVOTEE", totalSunnah)
  }

  private suspend fun checkAndTriggerAchievements(uid: String, log: DailyLog) {
    val badgeDao = localDb?.badgeDao()
    val history = localDb?.dailyObligationDao()?.getAllObligationsDirect(uid).orEmpty()

    if (log.fajr) {
      unlockBadge("DAWN_SEEKER_1", "Dawn Seeker", "Awakened for Fajr prayer and established your connection with Allah.")
    }

    if (log.isFasting) {
      unlockBadge("GUARDIAN_OF_FAST_1", "Shield of Sawm", "Fasted for the sake of Allah outside of Ramadan.")
      if (log.fastingType.contains("White", ignoreCase = true)) {
        unlockBadge("WHITE_DAYS_FAST", "Ayyam al-Bid Observer", "Revived the Sunnah by fasting the luminous White Days.")
      }
    }

    val totalQuranPages = if (history.isNotEmpty()) history.sumOf { it.quranPagesRead } else log.quranPagesRead
    if (totalQuranPages >= 20) {
      unlockBadge("QURAN_COMPANION_20", "Full Quran Juz", "Recited a complete Juz (20 pages) of the Holy Quran.")
    }
    if (totalQuranPages >= 100) {
      unlockBadge("QURAN_JOURNEY_100", "Quran Companion", "Immersed your soul in 100 pages of the Divine Revelation.")
    }

    // Update user streak only on meaningful spiritual activity and proper consecutive-day gap
    val hasActivity = log.completedPrayersCount > 0 ||
      log.quranPagesRead > 0 ||
      log.isFasting ||
      log.morningAdhkar ||
      log.eveningAdhkar

    if (!hasActivity) return

    val userRef = db.collection("users").document(uid)
    val userSnap = userRef.get().await()
    if (userSnap.exists()) {
      val lastActive = userSnap.getString("lastActiveDate") ?: ""
      val currentStreak = (userSnap.getLong("currentStreak") ?: 0L).toInt()
      val longestStreak = (userSnap.getLong("longestStreak") ?: 0L).toInt()

      val result = StreakCalculator.calculate(lastActive, log.date, currentStreak, longestStreak)
      if (result.changed) {
        userRef.update(
          mapOf(
            "lastActiveDate" to result.lastActiveDate,
            "currentStreak" to result.currentStreak,
            "longestStreak" to result.longestStreak,
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()

        if (result.currentStreak >= 7) {
          unlockBadge("STEADFAST_SOUL_7", "7-Day Prayer Streak", "Completed daily spiritual obligations for 7 consecutive days.")
          badgeDao?.unlockBadge("STREAK_7_DAY", System.currentTimeMillis())
        } else {
          badgeDao?.updateProgress("STREAK_7_DAY", result.currentStreak)
        }
        if (result.currentStreak >= 14) {
          unlockBadge("SPIRITUAL_STREAK_14", "Fortress of Faith", "Maintained unbroken spiritual habit streak for 14 continuous days.")
          badgeDao?.unlockBadge("STREAK_14_DAY", System.currentTimeMillis())
        } else {
          badgeDao?.updateProgress("STREAK_14_DAY", result.currentStreak)
        }
      }
    }
  }
}
