package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import com.example.data.model.PrayerName
import com.example.notification.NotificationHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.data.model.GlobalCityDirectory
import com.example.location.GpsLocationHelper
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.calculation.PrayerCalculationEngine
import com.example.data.config.DonationConfig
import com.example.data.config.PrivacyPolicyConfig
import com.example.data.local.BadgeEntity
import com.example.data.model.CalculationMethod
import com.example.data.model.UserProfile
import com.example.notification.FloatingAlertController
import com.example.ui.components.AlertType
import com.example.ui.components.BadgeShowcaseGrid
import com.example.ui.components.FloatingAlertPayload
import com.example.ui.components.GoldHairlineDivider
import com.example.ui.components.PrivacyPolicyModal
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
  badges: List<BadgeEntity> = emptyList(),
  autoPlaySurahMulkEnabled: Boolean = true,
  onToggleAutoPlaySurahMulk: (Boolean) -> Unit = {},
  autoPlaySurahKahfEnabled: Boolean = true,
  onToggleAutoPlaySurahKahf: (Boolean) -> Unit = {},
  adhanAudioEnabled: Boolean = true,
  onToggleAdhanAudio: (Boolean) -> Unit = {},
  nightlySurahsEnabled: Boolean = true,
  onToggleNightlySurahs: (Boolean) -> Unit = {},
  nightlySurahsHour: Int = 20,
  nightlySurahsMinute: Int = 30,
  onNightlySurahsTimeChanged: (Int, Int) -> Unit = { _, _ -> },
  prayerPreWarningEnabled: Boolean = true,
  onTogglePrayerPreWarning: (Boolean) -> Unit = {},
  prayerAlertsEnabled: Boolean = true,
  onTogglePrayerAlerts: (Boolean) -> Unit = {},
  fastingAlertsEnabled: Boolean = true,
  onToggleFastingAlerts: (Boolean) -> Unit = {},
  verseAlertsEnabled: Boolean = true,
  onToggleVerseAlerts: (Boolean) -> Unit = {},
  khatmMonths: Int = 1,
  onKhatmMonthsChange: (Int) -> Unit = {},
  quranKhatmCompletedDaysCount: Int = 0,
  floatingPopupEnabled: Boolean = true,
  onToggleFloatingPopup: (Boolean) -> Unit = {},
  onOpenQuranReader: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var showCityDialog by remember { mutableStateOf(false) }
  var showMethodDialog by remember { mutableStateOf(false) }
  var showQuranTimeDialog by remember { mutableStateOf(false) }
  var showPrivacyPolicyModal by remember { mutableStateOf(false) }

  var citySearchQuery by remember { mutableStateOf("") }
  var cityRegionFilter by remember { mutableStateOf("All") }
  var isGpsDetecting by remember { mutableStateOf(false) }
  var gpsStatusMessage by remember { mutableStateOf<String?>(null) }
  var canScheduleExactAlarms by remember {
    mutableStateOf(com.example.notification.NotificationScheduler.canScheduleExactAlarms(context))
  }
  var canDrawSystemOverlays by remember {
    mutableStateOf(FloatingAlertController.canDrawSystemOverlay(context))
  }

  androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        canScheduleExactAlarms = com.example.notification.NotificationScheduler.canScheduleExactAlarms(context)
        canDrawSystemOverlays = FloatingAlertController.canDrawSystemOverlay(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val granted = perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                  perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      isGpsDetecting = true
      gpsStatusMessage = null
      GpsLocationHelper.detectCurrentCity(context) { detected ->
        isGpsDetecting = false
        if (detected != null) {
          onCityChanged(detected.name)
          showCityDialog = false
        } else {
          gpsStatusMessage = "Could not obtain GPS fix. Select your city below."
        }
      }
    } else {
      gpsStatusMessage = "Location permission denied. Select your city manually."
    }
  }

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
                text = "100% Free • Multi-Device Sync Active",
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
      // Automated Surah Audio Recitations Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("automated_surah_settings_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RadiantGold.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Automated Sunnah Recitations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Auto-play offline audio with background player",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Nightly Surahs (Al-Mulk & Al-Waqi'ah)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Nightly Surahs (Mulk & Waqi'ah)",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                  modifier = Modifier
                    .background(com.example.ui.theme.EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ui.theme.EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
              Text(
                text = "Sequential Mishary Alafasy recitations pre-bundled locally (0 data used)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = nightlySurahsEnabled,
              onCheckedChange = { onToggleNightlySurahs(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_nightly_surahs")
            )
          }

          if (nightlySurahsEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Recitation Time",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              val is830 = nightlySurahsHour == 20 && nightlySurahsMinute == 30
              val is900 = nightlySurahsHour == 21 && nightlySurahsMinute == 0

              FilterChip(
                selected = is830,
                onClick = { onNightlySurahsTimeChanged(20, 30) },
                label = { Text("8:30 PM (20:30)", fontSize = 12.sp, maxLines = 1, softWrap = false, fontWeight = if (is830) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("chip_nightly_830")
              )

              FilterChip(
                selected = is900,
                onClick = { onNightlySurahsTimeChanged(21, 0) },
                label = { Text("9:00 PM (21:00)", fontSize = 12.sp, maxLines = 1, softWrap = false, fontWeight = if (is900) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("chip_nightly_900")
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(12.dp))

          // Surah Al-Kahf Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Surah Al-Kahf (Friday Dawn)",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                  modifier = Modifier
                    .background(com.example.ui.theme.EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ui.theme.EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
              Text(
                text = "Pre-bundled offline audio recited every blessed Friday before sunrise",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = autoPlaySurahKahfEnabled,
              onCheckedChange = { onToggleAutoPlaySurahKahf(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }
        }
      }
    }

    item {
      // Quran Khatm Split Status Card with 1, 2, or 3 Months Selector
      val totalDaysForDuration = com.example.data.model.QuranKhatmPlanCatalogue.getTotalDaysForDuration(khatmMonths)
      val pagesPerDay = when (khatmMonths) {
        1 -> 20
        2 -> 10
        else -> 7
      }
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("quran_khatm_settings_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Quran Khatm Plan Pace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$quranKhatmCompletedDaysCount of $totalDaysForDuration days completed (${(quranKhatmCompletedDaysCount * pagesPerDay).coerceAtMost(604)}/604 pages)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Button(
              onClick = onOpenQuranReader,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("open_quran_khatm_btn")
            ) {
              Text("Read", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Target Duration (Locked at 3 months max):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              Triple(1, "1 Mo", "20 pgs/day"),
              Triple(2, "2 Mos", "10 pgs/day"),
              Triple(3, "3 Mos", "~7 pgs/day")
            ).forEach { (m, label, sub) ->
              val isSel = khatmMonths == m
              FilterChip(
                selected = isSel,
                onClick = { onKhatmMonthsChange(m) },
                label = {
                  Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(label, fontSize = 11.sp, maxLines = 1, softWrap = false, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    Text(sub, fontSize = 9.sp, maxLines = 1, softWrap = false)
                  }
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f).testTag("khatm_pace_setting_$m")
              )
            }
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

          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !canScheduleExactAlarms) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("exact_alarm_warning_card")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "Exact Alarm Permission Needed",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Enable 'Alarms & reminders' so Solah and Adhan notifications fire on time every day even when your phone is asleep.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                  onClick = {
                    val intent = android.content.Intent(
                      android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                      android.net.Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                  },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("grant_exact_alarm_btn")
                ) {
                  Text("Enable Exact Alarms", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          // 0. Floating On-Screen Popups Toggle & Permission
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Floating On-Screen Popups",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                  modifier = Modifier
                    .background(RadiantGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (canDrawSystemOverlays) "Ready" else "Setup",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
              Text(
                text = "Centered alert modal over your screen outside Noor (not full screen)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = floatingPopupEnabled,
              onCheckedChange = { onToggleFloatingPopup(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_floating_popups")
            )
          }

          if (floatingPopupEnabled && !canDrawSystemOverlays) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = RadiantGold.copy(alpha = 0.14f),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RadiantGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .testTag("overlay_permission_prompt_card")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "Allow 'Appear on Top' for Outside-App Popups",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Optional permission that lets Solah & Adhkar popups float centered on screen while using other apps.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                  onClick = {
                    context.startActivity(FloatingAlertController.buildOpenOverlayPermissionIntent(context))
                  },
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("grant_overlay_permission_btn")
                ) {
                  Text(
                    text = "Enable Appear on Top",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

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
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Prepares you for wudu before prayer enters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = prayerPreWarningEnabled,
              onCheckedChange = { onTogglePrayerPreWarning(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_pre_warning")
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
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Notifies when call to prayer begins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = prayerAlertsEnabled,
              onCheckedChange = { onTogglePrayerAlerts(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_prayer_alerts")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 2b. Automated Solah Adhan Audio
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "Solah Adhan Audio",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                  modifier = Modifier
                    .background(com.example.ui.theme.EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ui.theme.EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
              Text(
                text = "Melodic Makkah Adhan bundled locally (0 data used) with instant stop action",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = adhanAudioEnabled,
              onCheckedChange = { onToggleAdhanAudio(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_adhan_audio")
            )
          }

          if (adhanAudioEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              OutlinedButton(
                onClick = {
                  com.example.audio.SurahAudioService.startAdhan(context, "Fajr")
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_test_fajr_adhan")
              ) {
                Text("Test Fajr", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
              }
              OutlinedButton(
                onClick = {
                  com.example.audio.SurahAudioService.startAdhan(context, "Dhuhr")
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_test_offline_adhan")
              ) {
                Text("Standard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
              }
              OutlinedButton(
                onClick = {
                  com.example.audio.SurahAudioService.stopAdhan(context)
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(0.75f)
                  .testTag("btn_stop_test_adhan")
              ) {
                Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
              }
            }
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
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Pre-Fajr Suhoor meal and Maghrib Iftar alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = fastingAlertsEnabled,
              onCheckedChange = { onToggleFastingAlerts(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_fasting_alerts")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3b. Daily Verse Reminder
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Daily Verse Notification",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Morning 8:00 AM Quranic reflection reminder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
              checked = verseAlertsEnabled,
              onCheckedChange = { onToggleVerseAlerts(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("toggle_verse_alerts")
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

          // 5. Preview Floating Popups & Live Phone Notifications
          Text(
            text = "Preview Floating Popups & Notifications:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = {
              val previewPayload = FloatingAlertPayload(
                id = "PREVIEW_FLOATING_POPUP",
                type = AlertType.SOLAH_PRE_WARNING,
                categoryLabel = "ON-SCREEN PREVIEW • NOOR",
                title = "Dhuhr begins in 10 minutes",
                arabicSubtitle = "الظهر",
                message = "Floating alerts appear cleanly centered on your screen without taking over the whole display.",
                actionLabel = "Mark Prayed",
                targetTab = "TODAY",
                prayerOrdinal = PrayerName.DHUHR.ordinal
              )
              val shownOverlay = FloatingAlertController.showOnScreenPopup(context, previewPayload)
              if (!shownOverlay) {
                context.startActivity(FloatingAlertController.buildAlertActivityIntent(context, previewPayload))
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("preview_floating_popup_btn")
          ) {
            Text(
              text = "Preview Centered Floating Popup",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { NotificationHelper.showPrayerPreWarningAlert(context, PrayerName.FAJR, 10) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("10m Solah", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
              onClick = { NotificationHelper.showSuhoorAlert(context, 30) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Suhoor", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
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
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Iftar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
            OutlinedButton(
              onClick = { NotificationHelper.showQuranRecitationAlert(context, "Al-Mulk", 1) },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Quran Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
          }
        }
      }
    }

    item {
      // Permanent Support Noor (Sadaqah Jariyah) Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
              listOf(RadiantGold.copy(alpha = 0.6f), com.example.ui.theme.EmeraldGreen.copy(alpha = 0.6f))
            ),
            shape = RoundedCornerShape(20.dp)
          )
          .testTag("settings_donation_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(RadiantGold.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VolunteerActivism,
                contentDescription = null,
                tint = RadiantGold,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Support the Project (Sadaqah Jariyah)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Keep Noor 100% Ad-Free & Maintained",
                style = MaterialTheme.typography.bodySmall,
                color = RadiantGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Noor is built without commercial advertising to preserve reverence and focus during ibadah. Support our infrastructure, high-fidelity recitations, and ongoing development with any amount.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Flutterwave Online Donation Box
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Official Flutterwave Checkout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "Cards • Transfer • USSD • Mobile Money",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = DonationConfig.FLUTTERWAVE_DISPLAY_HOST,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(com.example.ui.theme.EmeraldGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "Verified",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.EmeraldGreen,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              GoldHairlineDivider()
              Spacer(modifier = Modifier.height(12.dp))

              // Primary Action: Open Flutterwave Checkout
              Button(
                onClick = {
                  DonationConfig.openDonationCheckout(context)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = RadiantGold,
                  contentColor = com.example.ui.theme.EmeraldDark
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("donate_flutterwave_settings_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Favorite,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Donate via Flutterwave",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  maxLines = 1,
                  softWrap = false
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Secondary Action: Copy Link
              OutlinedButton(
                onClick = {
                  DonationConfig.copyDonationLink(context)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("copy_flutterwave_link_settings_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Copy Donation Link",
                  fontSize = 12.sp,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }
    }

    item {
      // Dedicated App Information & Features Catalog Hub
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_app_information_hub")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "About Noor",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "v${com.example.BuildConfig.VERSION_NAME} • Complete Guide",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
              modifier = Modifier
                .background(com.example.ui.theme.EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Up to Date",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.EmeraldGreen,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Noor is a modern, reverent, privacy-first companion designed for consistent Islamic devotion without intrusive advertisements.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Feature Grid Highlights
          val features = listOf(
            Triple("100% Offline Adhan", "Authentic Makkah & Fajr audio with zero internet requirement", Icons.Default.Alarm),
            Triple("Offline Friday & Nightly Surahs", "Auto-play Surah Al-Kahf Fridays & nightly Al-Mulk and Al-Waqi'ah (0 data)", Icons.Default.Schedule),
            Triple("Daily Adhkar Tracker", "Morning & Evening remembrance reminders & in-tracker accountability", Icons.Default.Check),
            Triple("Worldwide Cities & GPS", "Search 150+ global cities across all continents or auto-detect with device GPS", Icons.Default.LocationCity),
            Triple("Quran Khatm Pacing", "Flexible 1, 2, or 3-month schedules locked at 3 months max", Icons.AutoMirrored.Filled.MenuBook),
            Triple("Tadabbur Social Card", "Share daily reflections with Play Store invite link", Icons.Default.Share),
            Triple("PDF & CSV Reports", "Export your prayer and Quran history anytime", Icons.Default.TableChart),
            Triple("Sadaqah Jariyah", "100% ad-free maintained by generous community contributions", Icons.Default.Favorite)
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            features.forEach { (featTitle, featDesc, featIcon) ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = featIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(featTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(featDesc, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          GoldHairlineDivider()
          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            onClick = { showPrivacyPolicyModal = true },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 56.dp)
              .border(
                1.dp,
                RadiantGold.copy(alpha = 0.45f),
                RoundedCornerShape(14.dp)
              )
              .testTag("settings_privacy_policy_btn")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Privacy Policy & Data Safety",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "Effective ${PrivacyPolicyConfig.EFFECTIVE_DATE} • Zero Ads & Offline Policy",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Privacy Policy",
                tint = MaterialTheme.colorScheme.primary
              )
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

  // Worldwide Cities & Regions Dialog with Live Search & GPS Auto-Detection
  if (showCityDialog) {
    val filteredCities = remember(citySearchQuery, cityRegionFilter) {
      GlobalCityDirectory.searchCities(citySearchQuery, cityRegionFilter)
    }

    AlertDialog(
      onDismissRequest = {
        showCityDialog = false
        citySearchQuery = ""
        cityRegionFilter = "All"
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Worldwide Cities & Regions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          // GPS Auto-detect Button
          OutlinedButton(
            onClick = {
              if (GpsLocationHelper.hasLocationPermission(context)) {
                isGpsDetecting = true
                gpsStatusMessage = null
                GpsLocationHelper.detectCurrentCity(context) { detected ->
                  isGpsDetecting = false
                  if (detected != null) {
                    onCityChanged(detected.name)
                    showCityDialog = false
                  } else {
                    gpsStatusMessage = "Could not obtain GPS fix. Select your city below."
                  }
                }
              } else {
                locationPermissionLauncher.launch(
                  arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                  )
                )
              }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("gps_auto_detect_btn")
          ) {
            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isGpsDetecting) "Detecting GPS..." else "Auto-Detect My City via GPS", fontSize = 12.sp)
          }

          if (gpsStatusMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = gpsStatusMessage!!,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.error,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search Input
          OutlinedTextField(
            value = citySearchQuery,
            onValueChange = { citySearchQuery = it },
            placeholder = { Text("Search 150+ global cities...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("city_search_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Region Filter Chips
          androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(GlobalCityDirectory.REGIONS.size) { rIdx ->
              val reg = GlobalCityDirectory.REGIONS[rIdx]
              val isSel = cityRegionFilter == reg
              FilterChip(
                selected = isSel,
                onClick = { cityRegionFilter = reg },
                label = { Text(reg, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "${filteredCities.size} cities available",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Filtered Cities List
          LazyColumn(modifier = Modifier.height(260.dp)) {
            items(filteredCities.size) { index ->
              val city = filteredCities[index]
              val isSelected = city.name.equals(currentCity, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onCityChanged(city.name)
                    showCityDialog = false
                    citySearchQuery = ""
                    cityRegionFilter = "All"
                  }
                  .padding(vertical = 4.dp)
                  .testTag("city_item_${city.name.lowercase().replace(" ", "_")}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = city.name,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                      )
                      if (city.region.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                          Text(city.region, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                      }
                    }
                    Text(
                      text = "${city.country} • UTC${if (city.timezoneOffset >= 0) "+${city.timezoneOffset}" else "${city.timezoneOffset}"}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.outline,
                      fontSize = 11.sp
                    )
                  }
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          showCityDialog = false
          citySearchQuery = ""
          cityRegionFilter = "All"
        }) {
          Text("Close")
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

  if (showPrivacyPolicyModal) {
    PrivacyPolicyModal(
      onDismiss = { showPrivacyPolicyModal = false }
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
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
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
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = "Change",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      softWrap = false
    )
  }
}
