package com.example.ui.screens

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
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.data.model.PrayerName
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun TrackerScreen(
  dailyLog: DailyLog,
  currentDateStr: String,
  onDateChange: (String) -> Unit,
  onUpdateLog: (DailyLog) -> Unit,
  modifier: Modifier = Modifier
) {
  // Digital Tasbih state
  var tasbihCount by remember { mutableStateOf(0) }
  var tasbihPhraseIndex by remember { mutableStateOf(0) }
  val tasbihPhrases = listOf("SubhanAllah", "Alhamdulillah", "Allahu Akbar", "La ilaha illa Allah")

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
        }
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
      // Fasting (Sawm) Card
      FastingTrackerCard(
        dailyLog = dailyLog,
        onUpdateLog = onUpdateLog
      )
    }

    item {
      // Quran Reading Tracker Card
      QuranTrackerCard(
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
  onNextDay: () -> Unit
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
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onPreviousDay,
        modifier = Modifier.testTag("prev_day_btn")
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = MaterialTheme.colorScheme.primary)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "Spiritual Obligations",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = dateStr,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
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
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${dailyLog.completedPrayersCount}/5 Completed",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
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

      Spacer(modifier = Modifier.height(14.dp))

      // Sunnah & Nafl Prayers Counter
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Sunnah & Voluntary Rak'ahs",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "e.g. Rawatib, Duha, Witr",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              if (dailyLog.sunnahPrayers > 0) {
                onUpdateLog(dailyLog.copy(sunnahPrayers = dailyLog.sunnahPrayers - 2))
              }
            },
            modifier = Modifier.size(32.dp).testTag("decrease_sunnah_btn")
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Sunnah", modifier = Modifier.size(16.dp))
          }

          Text(
            text = "${dailyLog.sunnahPrayers}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
          )

          IconButton(
            onClick = {
              onUpdateLog(dailyLog.copy(sunnahPrayers = dailyLog.sunnahPrayers + 2))
            },
            modifier = Modifier.size(32.dp).testTag("increase_sunnah_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increase Sunnah", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
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
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Text(
      text = arabic,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.outline
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
        Column {
          Text(
            text = "Fasting Tracker (Sawm)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (dailyLog.isFasting) "Fasting Logged • May Allah accept" else "Not fasting today",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          fastingTypes.take(3).forEach { type ->
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
fun QuranTrackerCard(
  dailyLog: DailyLog,
  onUpdateLog: (DailyLog) -> Unit
) {
  var surahInput by remember(dailyLog.lastSurah) { mutableStateOf(dailyLog.lastSurah) }
  var ayahInput by remember(dailyLog.lastAyah) { mutableStateOf(dailyLog.lastAyah.toString()) }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("quran_tracker_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Quran Recitation (Tilawah)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${dailyLog.quranPagesRead} pages recited today",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Quick Add Pages:",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(1, 2, 5, 10).forEach { pages ->
          Button(
            onClick = {
              onUpdateLog(dailyLog.copy(quranPagesRead = dailyLog.quranPagesRead + pages))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("add_${pages}_pages_btn")
          ) {
            Text("+$pages", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Surah & Ayah Bookmark
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = surahInput,
          onValueChange = {
            surahInput = it
            onUpdateLog(dailyLog.copy(lastSurah = it))
          },
          label = { Text("Surah Bookmark") },
          modifier = Modifier.weight(2f).testTag("surah_bookmark_input")
        )

        OutlinedTextField(
          value = ayahInput,
          onValueChange = {
            ayahInput = it
            val ayahNum = it.toIntOrNull() ?: 1
            onUpdateLog(dailyLog.copy(lastAyah = ayahNum))
          },
          label = { Text("Ayah") },
          modifier = Modifier.weight(1f).testTag("ayah_bookmark_input")
        )
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
