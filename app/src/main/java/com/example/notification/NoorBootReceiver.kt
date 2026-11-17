package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Re-arms all scheduled Solah, Adhkar, Quran, and Daily Verse alarms after
 * device reboot, system clock/timezone changes, or app updates.
 */
class NoorBootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    if (
      action == Intent.ACTION_BOOT_COMPLETED ||
      action == "android.intent.action.QUICKBOOT_POWERON" ||
      action == Intent.ACTION_TIME_CHANGED ||
      action == Intent.ACTION_TIMEZONE_CHANGED ||
      action == Intent.ACTION_MY_PACKAGE_REPLACED
    ) {
      Log.d("NoorBootReceiver", "Rescheduling alarms for system action: $action")
      val pendingResult: PendingResult? = goAsync()
      try {
        NotificationHelper.createNotificationChannels(context)
        NotificationScheduler.rescheduleAllFromPreferences(context)
      } finally {
        pendingResult?.finish()
      }
    }
  }
}
