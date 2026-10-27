package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.model.PrayerName

class PrayerAlarmReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_PRE_PRAYER = "com.example.notification.ACTION_PRE_PRAYER"
    const val ACTION_EXACT_PRAYER = "com.example.notification.ACTION_EXACT_PRAYER"
    const val ACTION_SUHOOR = "com.example.notification.ACTION_SUHOOR"
    const val ACTION_IFTAR = "com.example.notification.ACTION_IFTAR"
    const val ACTION_QURAN_RECITE = "com.example.notification.ACTION_QURAN_RECITE"

    const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
    const val EXTRA_MINUTES_REMAINING = "EXTRA_MINUTES_REMAINING"
    const val EXTRA_SURAH_NAME = "EXTRA_SURAH_NAME"
    const val EXTRA_AYAH_NUMBER = "EXTRA_AYAH_NUMBER"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    Log.d("PrayerAlarmReceiver", "Received alarm action: ${intent.action}")

    when (intent.action) {
      ACTION_PRE_PRAYER -> {
        val prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.ordinal)
        val prayer = PrayerName.values().getOrNull(prayerOrdinal) ?: PrayerName.FAJR
        val minutes = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 10)
        NotificationHelper.showPrayerPreWarningAlert(context, prayer, minutes)
      }

      ACTION_EXACT_PRAYER -> {
        val prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_NAME, PrayerName.FAJR.ordinal)
        val prayer = PrayerName.values().getOrNull(prayerOrdinal) ?: PrayerName.FAJR
        NotificationHelper.showPrayerAlert(context, prayer)
      }

      ACTION_SUHOOR -> {
        val minutes = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 30)
        NotificationHelper.showSuhoorAlert(context, minutes)
      }

      ACTION_IFTAR -> {
        NotificationHelper.showIftarAlert(context)
      }

      ACTION_QURAN_RECITE -> {
        val surah = intent.getStringExtra(EXTRA_SURAH_NAME) ?: "Al-Fatihah"
        val ayah = intent.getIntExtra(EXTRA_AYAH_NUMBER, 1)
        NotificationHelper.showQuranRecitationAlert(context, surah, ayah)
      }
    }
  }
}
