package com.example.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.MainActivity
import com.example.audio.SurahAudioPlayer
import com.example.audio.SurahAudioService
import com.example.ui.components.FloatingAlertPayload

object FloatingAlertController {

  private const val TAG = "FloatingAlertController"

  const val EXTRA_NAV_TAB = "extra_nav_tab"
  const val EXTRA_MARK_PRAYER_ORDINAL = "extra_mark_prayer_ordinal"
  const val EXTRA_OPEN_QURAN_FULL_PAGE = "extra_open_quran_full_page"

  fun canDrawSystemOverlay(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Settings.canDrawOverlays(context)
    } else {
      true
    }
  }

  /**
   * Attempts to show the centered floating on-screen modal popup outside the app
   * when both the user preference is enabled and "Appear on Top" permission is granted.
   * Returns true if overlay service launch was initiated.
   */
  fun showOnScreenPopup(context: Context, payload: FloatingAlertPayload): Boolean {
    if (!AlarmPreferences.isFloatingPopupEnabled(context)) return false
    if (!canDrawSystemOverlay(context)) return false

    return try {
      val serviceIntent = Intent(context, FloatingAlertOverlayService::class.java).also {
        payload.putIntoIntent(it)
      }
      context.startService(serviceIntent)
      true
    } catch (e: Exception) {
      Log.w(TAG, "Unable to launch floating overlay service from current state", e)
      false
    }
  }

  fun buildAlertActivityIntent(context: Context, payload: FloatingAlertPayload): Intent {
    return Intent(context, FloatingAlertActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      payload.putIntoIntent(this)
    }
  }

  fun createAlertPendingIntent(
    context: Context,
    requestCode: Int,
    payload: FloatingAlertPayload
  ): PendingIntent {
    val intent = buildAlertActivityIntent(context, payload)
    return PendingIntent.getActivity(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  fun buildOpenOverlayPermissionIntent(context: Context): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        android.net.Uri.parse("package:${context.packageName}")
      ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    } else {
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:${context.packageName}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    }
  }

  fun handlePrimaryAction(context: Context, payload: FloatingAlertPayload) {
    if (payload.hasStopAudio) {
      stopActiveAudio(context)
    }
    val mainIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_NAV_TAB, payload.targetTab)
      putExtra(EXTRA_MARK_PRAYER_ORDINAL, payload.prayerOrdinal)
      putExtra(EXTRA_OPEN_QURAN_FULL_PAGE, payload.openQuranFullPage)
    }
    try {
      context.startActivity(mainIntent)
    } catch (e: Exception) {
      Log.w(TAG, "Failed launching MainActivity from floating alert", e)
    }
  }

  fun stopActiveAudio(context: Context) {
    try {
      SurahAudioService.stopAdhan(context)
      SurahAudioPlayer.getInstance(context).stop()
      NotificationHelper.cancelPrayerAlert(context)
      NotificationHelper.cancelNightlySurahsAlert(context)
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping audio from floating popup", e)
    }
  }
}
