package com.example.notification

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.Calendar

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationSchedulerTest {

  private lateinit var context: Context
  private lateinit var alarmManager: AlarmManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    AlarmPreferences.prefs(context).edit().clear().commit()
    AlarmPreferences.setCityName(context, "Mecca")
    AlarmPreferences.setCalculationMethod(context, "MUSLIM_WORLD_LEAGUE")
    AlarmPreferences.setPrayerAlertsEnabled(context, true)
    AlarmPreferences.setPrayerPreWarningEnabled(context, true)
    AlarmPreferences.setFastingAlertsEnabled(context, true)
    AlarmPreferences.setVerseAlertsEnabled(context, true)
    AlarmPreferences.setNightlySurahsEnabled(context, true)
    AlarmPreferences.setAutoPlayKahfEnabled(context, true)
    AlarmPreferences.setAdhanAudioEnabled(context, false)
  }

  @Test
  fun rescheduleAllFromPreferences_rollsPastTimesForwardToFuture() {
    // Late evening timestamp (23:55) so all 5 daily prayers today have passed
    val lateEveningCal = Calendar.getInstance().apply {
      set(2026, Calendar.OCTOBER, 27, 23, 55, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val nowMillis = lateEveningCal.timeInMillis

    NotificationScheduler.rescheduleAllFromPreferences(context, nowMillis = nowMillis)

    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms
    assertTrue("Expected alarms to be scheduled", scheduled.isNotEmpty())
    assertTrue(
      "Every alarm trigger time must be strictly after nowMillis (Day 2+ rollover)",
      scheduled.all { it.triggerAtTime > nowMillis }
    )
  }

  @Test
  fun rescheduleAllFromPreferences_doesNotCollideBetweenQuranReminderAndMorningAdhkar() {
    val morningCal = Calendar.getInstance().apply {
      set(2026, Calendar.OCTOBER, 27, 2, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }

    NotificationScheduler.rescheduleAllFromPreferences(context, nowMillis = morningCal.timeInMillis)

    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms
    val actions = scheduled.mapNotNull { alarm ->
      Shadows.shadowOf(alarm.operation).savedIntent?.action
    }.toSet()

    assertTrue(
      "Quran recitation reminder action must be scheduled independently",
      actions.contains(PrayerAlarmReceiver.ACTION_QURAN_RECITE)
    )
    assertTrue(
      "Morning Adhkar action must be scheduled independently without colliding on requestCode",
      actions.contains(PrayerAlarmReceiver.ACTION_MORNING_ADHKAR)
    )
    assertTrue(
      "Evening Adhkar action must be scheduled independently",
      actions.contains(PrayerAlarmReceiver.ACTION_EVENING_ADHKAR)
    )
    assertTrue(
      "Exact prayer alarm must be scheduled",
      actions.contains(PrayerAlarmReceiver.ACTION_EXACT_PRAYER)
    )
    assertTrue(
      "Daily verse alarm must be scheduled",
      actions.contains(PrayerAlarmReceiver.ACTION_DAILY_VERSE)
    )
  }

  @Test
  fun rescheduleAllFromPreferences_cancelsPrayerAndVerseAlarmsWhenDisabled() {
    val nowMillis = Calendar.getInstance().apply {
      set(2026, Calendar.OCTOBER, 27, 3, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    NotificationScheduler.rescheduleAllFromPreferences(context, nowMillis = nowMillis)

    // Disable exact prayer alerts and daily verse alerts
    AlarmPreferences.setPrayerAlertsEnabled(context, false)
    AlarmPreferences.setVerseAlertsEnabled(context, false)
    NotificationScheduler.rescheduleAllFromPreferences(context, nowMillis = nowMillis)

    val actions = Shadows.shadowOf(alarmManager).scheduledAlarms.mapNotNull { alarm ->
      Shadows.shadowOf(alarm.operation).savedIntent?.action
    }.toSet()

    assertFalse(
      "Disabled exact prayer alarms should be cancelled",
      actions.contains(PrayerAlarmReceiver.ACTION_EXACT_PRAYER)
    )
    assertFalse(
      "Disabled daily verse alarm should be cancelled",
      actions.contains(PrayerAlarmReceiver.ACTION_DAILY_VERSE)
    )
  }

  @Test
  fun bootReceiver_rearmsAlarmsAfterBootCompleted() {
    val receiver = NoorBootReceiver()
    receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms
    assertTrue("Boot receiver must reschedule alarms from AlarmPreferences", scheduled.isNotEmpty())
  }

  @Test
  fun prayerAlarmReceiver_rearmsNextDayAlarmsOnReceive() {
    val receiver = PrayerAlarmReceiver()
    val intent = Intent(PrayerAlarmReceiver.ACTION_EXACT_PRAYER).apply {
      putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, com.example.data.model.PrayerName.FAJR.ordinal)
    }

    receiver.onReceive(context, intent)

    val scheduled = Shadows.shadowOf(alarmManager).scheduledAlarms
    val nowMillis = System.currentTimeMillis()
    assertTrue("PrayerAlarmReceiver must reschedule Day 2+ alarms", scheduled.isNotEmpty())
    assertTrue(
      "All rescheduled alarms must be in the future",
      scheduled.all { it.triggerAtTime >= nowMillis - 1000L }
    )
  }
}
