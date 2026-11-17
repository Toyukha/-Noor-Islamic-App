package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import com.example.data.export.ReportExportManager
import com.example.ui.components.RechartsWeeklyReportDashboard
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.data.model.PrayerName
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
  dailyLog: DailyLog,
  currentDateStr: String,
  onDateChange: (String) -> Unit,
  onUpdateLog: (DailyLog) -> Unit,
  allLogs: List<DailyLog> = emptyList(),
  cityName: String = "Mecca",
  modifier: Modifier = Modifier
) {
  var showExportSheet by remember { mutableStateOf(false) }

  // Digital Tasbih state
  var tasbihCount by remember { mutableStateOf(0) }
  var tasbihPhraseIndex by remember { mutableStateOf(0) }
  val tasbihPhrases = listOf("SubhanAllah", "Alhamdulillah", "Allahu Akbar", "La ilaha illa Allah")

  if (showExportSheet) {
    ExportReportBottomSheet(
      logs = if (allLogs.isNotEmpty()) allLogs else listOf(dailyLog),
      cityName = cityName,
      onDismiss = { showExportSheet = false }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Date Selector Header
      DateSelectorHeader(
        dateStr = currentDateStr,
        onPreviousDay = {
          // Switch to yesterday
          val prev = adjustDate(currentDateStr, -1)
          onDateChange(prev)
        },
        onNextDay = {
          val next = adjustDate(currentDateStr, 1)
          onDateChange(next)
        },
        onExportClick = { showExportSheet = true }
      )
    }

    item {
      // Summary Dashboard: Recharts-style interactive bar charts for Prayer Consistency & Quran Completion
      RechartsWeeklyReportDashboard(
        weeklyLogs = if (allLogs.isNotEmpty()) allLogs else listOf(dailyLog),
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      // 5 Daily Prayers Checklist Card
      PrayersTrackerCard(
        dailyLog = dailyLog,
        onUpdateLog = onUpdateLog
      )
    }

    item {
      // Daily Adhkar (Remembrance) Tracker Card
      DailyAdhkarTrackerCard(
        dailyLog = dailyLog,
        onUpdateLog = onUpdateLog
      )
    }

    item {
      // Dedicated High-Reward Sunnah & Nawafil Tracker Card
      NawafilTrackerCard(
        dailyLog = dailyLog,
        onUpdateLog = onUpdateLog
      )
    }

    item {
      // Fasting (Sawm) Card
      FastingTrackerCard(
        dailyLog = dailyLog,
        onUpdateLog = onUpdateLog
      )
    }


    item {
      // Digital Tasbih Counter Card
      TasbihCounterCard(
        currentCount = tasbihCount,
        phrase = tasbihPhrases[tasbihPhraseIndex],
        onTap = {
          if (tasbihCount < 33) {
            tasbihCount++
          } else {
            tasbihCount = 1
            tasbihPhraseIndex = (tasbihPhraseIndex + 1) % tasbihPhrases.size
          }
        },
        onReset = {
          tasbihCount = 0
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun DateSelectorHeader(
  dateStr: String,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onExportClick: () -> Unit = {}
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onPreviousDay,
        modifier = Modifier.testTag("prev_day_btn")
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = MaterialTheme.colorScheme.primary)
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Spiritual Obligations",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = dateStr,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          softWrap = false
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onExportClick,
          modifier = Modifier.testTag("export_report_btn")
        ) {
          Icon(Icons.Default.Download, contentDescription = "Export Report", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(
          onClick = onNextDay,
          modifier = Modifier.testTag("next_day_btn")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = MaterialTheme.colorScheme.primary)
        }
      }
    }
  }
}

@Composable
fun PrayersTrackerCard(
  dailyLog: DailyLog,
  onUpdateLog: (DailyLog) -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("prayers_tracker_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Daily Prayers (Salah)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${dailyLog.completedPrayersCount}/5 Done",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          softWrap = false
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      PrayerCheckboxRow(
        name = "Fajr (Dawn)",
        arabic = "الفجر",
        checked = dailyLog.fajr,
        onCheckedChange = { onUpdateLog(dailyLog.copy(fajr = it)) }
      )
      PrayerCheckboxRow(
        name = "Dhuhr (Noon)",
        arabic = "الظهر",
        checked = dailyLog.dhuhr,
        onCheckedChange = { onUpdateLog(dailyLog.copy(dhuhr = it)) }
      )
      PrayerCheckboxRow(
        name = "Asr (Afternoon)",
        arabic = "العصر",
        checked = dailyLog.asr,
        onCheckedChange = { onUpdateLog(dailyLog.copy(asr = it)) }
      )
      PrayerCheckboxRow(
        name = "Maghrib (Sunset)",
        arabic = "المغرب",
        checked = dailyLog.maghrib,
        onCheckedChange = { onUpdateLog(dailyLog.copy(maghrib = it)) }
      )
      PrayerCheckboxRow(
        name = "Isha (Night)",
        arabic = "العشاء",
        checked = dailyLog.isha,
        onCheckedChange = { onUpdateLog(dailyLog.copy(isha = it)) }
      )
    }
  }
}

@Composable
fun NawafilTrackerCard(
  dailyLog: DailyLog,
  onUpdateLog: (DailyLog) -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("nawafil_tracker_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(RadiantGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = RadiantGold,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Sunnah & Nawafil",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Voluntary devotion & night vigil",
              style = MaterialTheme.typography.labelSmall,
              color = RadiantGold,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val nawafilCompletedCount = (if (dailyLog.tahajjud) 1 else 0) +
                                    (if (dailyLog.duha) 1 else 0) +
                                    (if (dailyLog.witr) 1 else 0)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = "$nawafilCompletedCount Active",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 1. Tahajjud (Qiyam al-Layl)
      NawafilCheckboxRow(
        name = "Tahajjud (Qiyam al-Layl)",
        arabic = "قيام الليل",
        virtue = "The best prayer after obligatory prayers (Muslim)",
        checked = dailyLog.tahajjud,
        testTag = "tahajjud_checkbox_row",
        onCheckedChange = { onUpdateLog(dailyLog.copy(tahajjud = it)) }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Salat al-Duha
      NawafilCheckboxRow(
        name = "Salat ad-Duha (Forenoon)",
        arabic = "صلاة الضحى",
        virtue = "Charity for each of your 360 bodily joints (Muslim)",
        checked = dailyLog.duha,
        testTag = "duha_checkbox_row",
        onCheckedChange = { onUpdateLog(dailyLog.copy(duha = it)) }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // 3. Salat al-Witr
      NawafilCheckboxRow(
        name = "Salat al-Witr (Night Seal)",
        arabic = "صلاة الوتر",
        virtue = "Allah is Witr (One) and loves the Witr (Tirmidhi)",
        checked = dailyLog.witr,
        testTag = "witr_checkbox_row",
        onCheckedChange = { onUpdateLog(dailyLog.copy(witr = it)) }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 4. 12 Sunnah Rawatib Tracker
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "12 Sunnah Rawatib",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "2 Fajr • 4+2 Dhuhr • 2 Maghrib • 2 Isha",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
          )
          Text(
            text = "\"A house will be built in Jannah\" (Muslim)",
            style = MaterialTheme.typography.labelSmall,
            color = RadiantGold,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              if (dailyLog.rawatibCount > 0) {
                onUpdateLog(dailyLog.copy(rawatibCount = dailyLog.rawatibCount - 2))
              }
            },
            modifier = Modifier.size(32.dp).testTag("decrease_rawatib_btn")
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Rawatib", modifier = Modifier.size(16.dp))
          }

          Text(
            text = "${dailyLog.rawatibCount}/12",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp).testTag("rawatib_count_text")
          )

          IconButton(
            onClick = {
              if (dailyLog.rawatibCount < 12) {
                onUpdateLog(dailyLog.copy(rawatibCount = dailyLog.rawatibCount + 2))
              }
            },
            modifier = Modifier.size(32.dp).testTag("increase_rawatib_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Rawatib", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun NawafilCheckboxRow(
  name: String,
  arabic: String,
  virtue: String,
  checked: Boolean,
  testTag: String = "nawafil_row",
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onCheckedChange(!checked) }
      .background(if (checked) EmeraldGreen.copy(alpha = 0.08f) else Color.Transparent)
      .padding(vertical = 8.dp, horizontal = 6.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Checkbox(
        checked = checked,
        onCheckedChange = null,
        colors = CheckboxDefaults.colors(
          checkedColor = EmeraldGreen,
          checkmarkColor = Color.White
        )
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = virtue,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline,
          fontSize = 10.sp
        )
      }
    }

    Text(
      text = arabic,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = if (checked) EmeraldGreen else MaterialTheme.colorScheme.outline
    )
  }
}

@Composable
private fun PrayerCheckboxRow(
  name: String,
  arabic: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 6.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Checkbox(
        checked = checked,
        onCheckedChange = null,
        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = arabic,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.outline,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Composable
fun FastingTrackerCard(
  dailyLog: DailyLog,
  onUpdateLog: (DailyLog) -> Unit
) {
  val fastingTypes = listOf("Sunnah (Mon/Thu)", "White Days", "Ramadan", "Voluntary", "Qada")

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("fasting_tracker_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Fasting Tracker (Sawm)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (dailyLog.isFasting) "Fasting Logged • May Allah accept" else "Not fasting today",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Switch(
          checked = dailyLog.isFasting,
          onCheckedChange = { isFasting ->
            onUpdateLog(dailyLog.copy(isFasting = isFasting))
          },
          colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("fasting_toggle_switch")
        )
      }

      if (dailyLog.isFasting) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Fast Type:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        androidx.compose.foundation.layout.FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          fastingTypes.forEach { type ->
            FilterChip(
              selected = dailyLog.fastingType == type,
              onClick = { onUpdateLog(dailyLog.copy(fastingType = type)) },
              label = { Text(text = type, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun TasbihCounterCard(
  currentCount: Int,
  phrase: String,
  onTap: () -> Unit,
  onReset: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("tasbih_counter_card")
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Digital Tasbih (Dhikr)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onReset, modifier = Modifier.testTag("reset_tasbih_btn")) {
          Icon(Icons.Default.Refresh, contentDescription = "Reset Tasbih", tint = MaterialTheme.colorScheme.outline)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = phrase,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Large Tap Button
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(110.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary)
          .clickable(onClick = onTap)
          .testTag("tasbih_tap_button")
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "$currentCount",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
          Text(
            text = "of 33",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Tap circle to count remembrance",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun adjustDate(dateStr: String, dayOffset: Int): String {
  // Simple YYYY-MM-DD date stepping
  try {
    val parts = dateStr.split("-")
    if (parts.size == 3) {
      val year = parts[0].toInt()
      val month = parts[1].toInt() - 1
      val day = parts[2].toInt()
      val cal = java.util.Calendar.getInstance()
      cal.set(year, month, day)
      cal.add(java.util.Calendar.DAY_OF_MONTH, dayOffset)
      val y = cal.get(java.util.Calendar.YEAR)
      val m = cal.get(java.util.Calendar.MONTH) + 1
      val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
      return String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m, d)
    }
  } catch (e: Exception) {
    // fallback
  }
  return dateStr
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportBottomSheet(
  logs: List<DailyLog>,
  cityName: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("export_sheet_modal")
    ) {
      Text(
        text = "Weekly Report & Spiritual Insights",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Interactive summary dashboard of your prayer consistency and Quran progress (${logs.size} logged days)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Recharts Interactive Bar Chart Summary Dashboard
      RechartsWeeklyReportDashboard(
        weeklyLogs = logs,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Export Documents",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Save or share full records for spreadsheet and PDF archiving",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Option 1: CSV / Excel Export
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            val file = ReportExportManager.generateCsvReport(context, logs)
            ReportExportManager.saveToDownloads(context, file)
            ReportExportManager.shareReportFile(context, file, "text/csv")
            onDismiss()
          }
          .testTag("export_csv_btn")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(EmeraldGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "CSV Spreadsheet (Excel / Google Sheets)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "All logged prayers, fasting, nawafil & Quran pages in raw data",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldGreen)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Option 2: PDF Document Export
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            val file = ReportExportManager.generatePdfReport(context, logs, userCity = cityName)
            ReportExportManager.saveToDownloads(context, file)
            ReportExportManager.shareReportFile(context, file, "application/pdf")
            onDismiss()
          }
          .testTag("export_pdf_btn")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(RadiantGold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = RadiantGold)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Printable PDF Document",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Official summary document with completion percentages & tables",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(Icons.Default.Download, contentDescription = null, tint = RadiantGold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun DailyAdhkarTrackerCard(
  dailyLog: DailyLog,
  onUpdateLog: (DailyLog) -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("adhkar_tracker_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(EmeraldGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = EmeraldGreen,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Daily Adhkar",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Fortress of the Muslim • أذكار اليوم",
              style = MaterialTheme.typography.labelSmall,
              color = EmeraldGreen,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val completedCount = (if (dailyLog.morningAdhkar) 1 else 0) + (if (dailyLog.eveningAdhkar) 1 else 0)
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (completedCount == 2) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "$completedCount / 2 Done",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (completedCount == 2) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Morning Adhkar Item
      AdhkarCheckItem(
        title = "Morning Adhkar",
        arabicTitle = "أذكار الصباح",
        description = "Recited after Fajr until sunrise for divine protection",
        isChecked = dailyLog.morningAdhkar,
        accentColor = RadiantGold,
        testTag = "morning_adhkar_checkbox",
        onToggle = {
          onUpdateLog(dailyLog.copy(morningAdhkar = !dailyLog.morningAdhkar))
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Evening Adhkar Item
      AdhkarCheckItem(
        title = "Evening Adhkar",
        arabicTitle = "أذكار المساء",
        description = "Recited after Asr until sunset for night defense",
        isChecked = dailyLog.eveningAdhkar,
        accentColor = EmeraldGreen,
        testTag = "evening_adhkar_checkbox",
        onToggle = {
          onUpdateLog(dailyLog.copy(eveningAdhkar = !dailyLog.eveningAdhkar))
        }
      )
    }
  }
}

@Composable
fun AdhkarCheckItem(
  title: String,
  arabicTitle: String,
  description: String,
  isChecked: Boolean,
  accentColor: Color,
  testTag: String,
  onToggle: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isChecked) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onToggle)
      .testTag(testTag)
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
        Checkbox(
          checked = isChecked,
          onCheckedChange = null,
          colors = CheckboxDefaults.colors(
            checkedColor = accentColor,
            checkmarkColor = Color.White
          )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = arabicTitle,
              style = MaterialTheme.typography.labelSmall,
              color = accentColor,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
