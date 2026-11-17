package com.example.notification

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PrayerName
import com.example.ui.components.AlertType
import com.example.ui.components.FloatingAlertPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FloatingAlertControllerTest {

  private lateinit var context: Application

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    AlarmPreferences.prefs(context).edit().clear().commit()
  }

  @Test
  fun `floating popup preference defaults to true and toggles cleanly`() {
    assertTrue(AlarmPreferences.isFloatingPopupEnabled(context))

    AlarmPreferences.setFloatingPopupEnabled(context, false)
    assertFalse(AlarmPreferences.isFloatingPopupEnabled(context))

    AlarmPreferences.setFloatingPopupEnabled(context, true)
    assertTrue(AlarmPreferences.isFloatingPopupEnabled(context))
  }

  @Test
  fun `floating alert payload round trips through intent extras`() {
    val payload = FloatingAlertPayload(
      id = "EXACT_MAGHRIB",
      type = AlertType.SOLAH_EXACT,
      categoryLabel = "ADHAN • PRAYER TIME",
      title = "Time for Maghrib Prayer",
      arabicSubtitle = "المغرب",
      message = "Stand in serene devotion.",
      actionLabel = "Mark Maghrib Prayed",
      targetTab = "TODAY",
      prayerOrdinal = PrayerName.MAGHRIB.ordinal,
      hasStopAudio = true,
      openQuranFullPage = false
    )

    val intent = Intent().also { payload.putIntoIntent(it) }
    val restored = FloatingAlertPayload.fromIntent(intent)

    assertNotNull(restored)
    assertEquals(payload, restored)
    assertNull(FloatingAlertPayload.fromIntent(null))
  }

  @Test
  fun `showOnScreenPopup returns false when disabled or missing overlay permission`() {
    val payload = FloatingAlertPayload(
      id = "TEST_1",
      type = AlertType.SOLAH_PRE_WARNING,
      categoryLabel = "UPCOMING SOLAH",
      title = "Asr in 10 minutes",
      message = "Prepare for wudu.",
      actionLabel = "Mark Prayed"
    )

    // Default test environment has overlay permission denied
    assertFalse(FloatingAlertController.showOnScreenPopup(context, payload))

    // Also returns false when user turns setting off
    AlarmPreferences.setFloatingPopupEnabled(context, false)
    assertFalse(FloatingAlertController.showOnScreenPopup(context, payload))
  }

  @Test
  fun `buildAlertActivityIntent and overlay permission intent use valid targets`() {
    val payload = FloatingAlertPayload(
      id = "QURAN_1",
      type = AlertType.QURAN_RECITE,
      categoryLabel = "QURAN TILAWAH",
      title = "Daily Quran Recitation",
      message = "Continue your reading.",
      actionLabel = "Open Quran",
      targetTab = "QURAN",
      openQuranFullPage = true
    )

    val activityIntent = FloatingAlertController.buildAlertActivityIntent(context, payload)
    assertEquals(FloatingAlertActivity::class.java.name, activityIntent.component?.className)
    assertTrue((activityIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    assertEquals(payload, FloatingAlertPayload.fromIntent(activityIntent))

    val permissionIntent = FloatingAlertController.buildOpenOverlayPermissionIntent(context)
    assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, permissionIntent.action)
    assertEquals("package:${context.packageName}", permissionIntent.data.toString())
  }

  @Test
  fun `prayer notifications attach FloatingAlertActivity content intent and omit fullScreenIntent`() {
    shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    NotificationHelper.createNotificationChannels(context)
    AlarmPreferences.setAdhanAudioEnabled(context, false)

    NotificationHelper.showPrayerPreWarningAlert(context, PrayerName.DHUHR, 10)
    NotificationHelper.showPrayerAlert(context, PrayerName.ASR)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val shadowNm = shadowOf(notificationManager)

    val preWarning = shadowNm.getNotification(PrayerName.DHUHR.ordinal + 200)
    assertNotNull(preWarning)
    assertNull(preWarning.fullScreenIntent)
    val preIntent = shadowOf(preWarning.contentIntent).savedIntent
    assertEquals(FloatingAlertActivity::class.java.name, preIntent.component?.className)
    assertEquals("SOLAH_DHUHR", FloatingAlertPayload.fromIntent(preIntent)?.id)

    val exactAlert = shadowNm.getNotification(PrayerName.ASR.ordinal + 100)
    assertNotNull(exactAlert)
    assertNull(exactAlert.fullScreenIntent)
    val exactIntent = shadowOf(exactAlert.contentIntent).savedIntent
    assertEquals(FloatingAlertActivity::class.java.name, exactIntent.component?.className)
    assertEquals("EXACT_ASR", FloatingAlertPayload.fromIntent(exactIntent)?.id)
  }
}
