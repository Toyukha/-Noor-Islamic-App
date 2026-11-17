package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val city: String = "Mecca",
  val calculationMethod: String = "MUSLIM_WORLD_LEAGUE",
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val lastActiveDate: String = "",
  val isSubscribed: Boolean = false,
  val subscriptionPlan: String = "FREE",
  val subscriptionCurrency: String = "NGN",
  val subscriptionAmount: Double = 0.0,
  val subscriptionExpiresAt: Long = 0L,
  val subscriptionPaymentMethod: String = "",
  val lastTransactionId: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
