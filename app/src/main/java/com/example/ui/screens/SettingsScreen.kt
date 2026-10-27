package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import com.example.data.model.PrayerName
import com.example.notification.NotificationHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.model.CalculationMethod
import com.example.data.model.UserProfile
import com.example.ui.theme.RadiantGold
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  userProfile: UserProfile?,
  onCityChanged: (String) -> Unit,
  onMethodChanged: (String) -> Unit,
  onSignOut: () -> Unit,
  quranReminderHour: Int = 20,
  quranReminderMinute: Int = 30,
  onQuranReminderTimeChanged: (Int, Int) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var showCityDialog by remember { mutableStateOf(false) }
  var showMethodDialog by remember { mutableStateOf(false) }
  var showQuranTimeDialog by remember { mutableStateOf(false) }

  var prayerPreWarningEnabled by remember { mutableStateOf(true) }
  var fastingAlertsEnabled by remember { mutableStateOf(true) }
  var prayerAlertsEnabled by remember { mutableStateOf(true) }
  var verseAlertsEnabled by remember { mutableStateOf(true) }

  val currentCity = userProfile?.city ?: "Mecca"
  val currentMethod = userProfile?.calculationMethod ?: "MUSLIM_WORLD_LEAGUE"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Settings & Profile",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    item {
      // User Profile Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("user_profile_card")
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = userProfile?.displayName?.take(1)?.uppercase() ?: "U",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            Text(
              text = userProfile?.displayName?.ifBlank { "Muslim Servant" } ?: "Muslim Servant",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = userProfile?.email?.ifBlank { "Google Account" } ?: "Google Account",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Multi-Device Sync: Active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Streaks and prayers sync seamlessly across your devices",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      }
    }

    item {
      // Prayer Time Settings
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("prayer_settings_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Calculation & Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(14.dp))

          // City Row
          SettingClickableRow(
            icon = Icons.Default.LocationCity,
            title = "City / Region",
            subtitle = currentCity,
            onClick = { showCityDialog = true }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Calculation Method Row
          SettingClickableRow(
            icon = Icons.Default.Calculate,
            title = "Calculation Method",
            subtitle = PrayerCalculationEngine.getMethodByName(currentMethod).displayName,
            onClick = { showMethodDialog = true }
          )
        }
      }
    }

    item {
      // Notifications Settings Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("notification_settings_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Reminders & Alerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 1. 10m Pre-Prayer Warning
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "10-Minute Solah Warning",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Prepares you for wudu before prayer enters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = prayerPreWarningEnabled,
              onCheckedChange = { prayerPreWarningEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 2. Exact Prayer Time Alerts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Exact Solah Call Alerts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Notifies when call to prayer begins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = prayerAlertsEnabled,
              onCheckedChange = { prayerAlertsEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3. Fasting (Sawm) Suhoor & Iftar Alerts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Suhoor & Iftar Fasting Alerts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Pre-Fajr Suhoor meal and Maghrib Iftar alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = fastingAlertsEnabled,
              onCheckedChange = { fastingAlertsEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 4. Custom Quran Recitation Reminder Time
          SettingClickableRow(
            icon = Icons.Default.Schedule,
            title = "Quran Recitation Time",
            subtitle = String.format("%02d:%02d (%s)", quranReminderHour, quranReminderMinute, if (quranReminderHour >= 12) "PM" else "AM"),
            onClick = { showQuranTimeDialog = true }
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 5. Test Live Phone Notifications Section
          Text(
            text = "Test Phone Notifications:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { NotificationHelper.showPrayerPreWarningAlert(context, PrayerName.FAJR, 10) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("10m Solah", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
              onClick = { NotificationHelper.showSuhoorAlert(context, 30) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Suhoor", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { NotificationHelper.showIftarAlert(context) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Iftar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
              onClick = { NotificationHelper.showQuranRecitationAlert(context, "Al-Mulk", 1) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Quran Time", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      // Sign Out Button
      OutlinedButton(
        onClick = {
          Firebase.auth.signOut()
          coroutineScope.launch {
            try {
              credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
              // ignore
            } finally {
              onSignOut()
            }
          }
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("sign_out_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out of Google Account", fontWeight = FontWeight.Bold)
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // City Picker Dialog
  if (showCityDialog) {
    AlertDialog(
      onDismissRequest = { showCityDialog = false },
      title = { Text("Select City for Prayer Times") },
      text = {
        LazyColumn(modifier = Modifier.height(300.dp)) {
          items(PrayerCalculationEngine.PRESET_CITIES.size) { index ->
            val city = PrayerCalculationEngine.PRESET_CITIES[index]
            val isSelected = city.name.equals(currentCity, ignoreCase = true)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onCityChanged(city.name)
                  showCityDialog = false
                }
                .padding(vertical = 10.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = city.name,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = city.country,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.outline
                )
              }
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCityDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Calculation Method Dialog
  if (showMethodDialog) {
    AlertDialog(
      onDismissRequest = { showMethodDialog = false },
      title = { Text("Select Calculation Method") },
      text = {
        LazyColumn(modifier = Modifier.height(280.dp)) {
          items(CalculationMethod.values().size) { index ->
            val method = CalculationMethod.values()[index]
            val isSelected = method.name == currentMethod
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onMethodChanged(method.name)
                  showMethodDialog = false
                }
                .padding(vertical = 10.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showMethodDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showQuranTimeDialog) {
    val presetTimes = listOf(
      Pair(5, 15) to "05:15 AM (Post-Fajr)",
      Pair(7, 0) to "07:00 AM (Early Morning)",
      Pair(17, 0) to "05:00 PM (Post-Asr)",
      Pair(19, 30) to "07:30 PM (Post-Maghrib)",
      Pair(20, 30) to "08:30 PM (Evening / Post-Isha)",
      Pair(22, 0) to "10:00 PM (Night Contemplation)"
    )

    AlertDialog(
      onDismissRequest = { showQuranTimeDialog = false },
      title = { Text("Daily Quran Recitation Time") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Select your daily preferred time to receive phone reminders for Tilawah:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          presetTimes.forEach { (timePair, label) ->
            val (h, m) = timePair
            val isSelected = h == quranReminderHour && m == quranReminderMinute
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  onQuranReminderTimeChanged(h, m)
                  showQuranTimeDialog = false
                }
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showQuranTimeDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
private fun SettingClickableRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Text(text = "Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
  }
}
