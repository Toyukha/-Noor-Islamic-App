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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.data.model.DailyVerse
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.SinglePrayerTime
import com.example.notification.NotificationHelper
import com.example.ui.components.DailyVerseReflectionComponent
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun TodayScreen(
  schedule: DayPrayerSchedule,
  verse: DailyVerse,
  dailyLog: DailyLog,
  cityName: String,
  onNavigateToTracker: () -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var testAlertSentPrayer by remember { mutableStateOf<PrayerName?>(null) }
  var testVerseAlertSent by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Multi-device Cloud Sync Status Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Multi-Device Cloud Sync Active",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "Real-time • Streaks Synced",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.outline
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      // Next Prayer Countdown Hero Card
      HeroCountdownCard(
        schedule = schedule,
        cityName = cityName,
        onCityClick = onNavigateToSettings
      )
    }

    item {
      // Today Obligations Summary Pill
      TodayProgressSummaryCard(
        dailyLog = dailyLog,
        onClick = onNavigateToTracker
      )
    }

    item {
      // Prayer Times Schedule List Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Prayer Times",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = schedule.hijriFormatted,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // List of 6 prayer cards (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
    items(schedule.prayers.size) { index ->
      val prayerTime = schedule.prayers[index]
      val isNext = schedule.nextPrayer?.prayer == prayerTime.prayer
      val isCompleted = when (prayerTime.prayer) {
        PrayerName.FAJR -> dailyLog.fajr
        PrayerName.DHUHR -> dailyLog.dhuhr
        PrayerName.ASR -> dailyLog.asr
        PrayerName.MAGHRIB -> dailyLog.maghrib
        PrayerName.ISHA -> dailyLog.isha
        PrayerName.SUNRISE -> false
      }

      PrayerTimeRowCard(
        prayerTime = prayerTime,
        isNext = isNext,
        isCompleted = isCompleted,
        onTriggerAlertTest = {
          NotificationHelper.showPrayerAlert(context, prayerTime.prayer)
          testAlertSentPrayer = prayerTime.prayer
        }
      )
    }

    item {
      // Daily Quranic Verse & Gemini AI Reflection Card with Room Caching
      DailyVerseReflectionComponent(
        verse = verse,
        dateStr = dailyLog.date,
        onSendNotification = {
          NotificationHelper.showDailyVerseNotification(context, verse)
          testVerseAlertSent = true
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun HeroCountdownCard(
  schedule: DayPrayerSchedule,
  cityName: String,
  onCityClick: () -> Unit
) {
  val nextPrayer = schedule.nextPrayer

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("hero_countdown_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.primaryContainer
            )
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.2f))
              .clickable(onClick = onCityClick)
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Selected City",
              tint = StarGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = cityName,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = schedule.dateFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "NEXT PRAYER",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 1.sp,
              color = StarGold
            )
            Text(
              text = nextPrayer?.prayer?.displayName ?: "Fajr",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = nextPrayer?.timeFormatted ?: "--:--",
              style = MaterialTheme.typography.bodyLarge,
              color = Color.White.copy(alpha = 0.9f)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "COUNTDOWN",
              fontSize = 11.sp,
              letterSpacing = 1.sp,
              color = Color.White.copy(alpha = 0.7f)
            )
            Text(
              text = schedule.timeUntilNextFormatted,
              fontSize = 32.sp,
              fontWeight = FontWeight.Black,
              color = StarGold
            )
          }
        }
      }
    }
  }
}

@Composable
fun PrayerTimeRowCard(
  prayerTime: SinglePrayerTime,
  isNext: Boolean,
  isCompleted: Boolean,
  onTriggerAlertTest: () -> Unit
) {
  val cardBackground = if (isNext) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
  } else {
    MaterialTheme.colorScheme.surface
  }

  val borderModifier = if (isNext) {
    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
  } else {
    Modifier
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = cardBackground,
    tonalElevation = if (isNext) 4.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .then(borderModifier)
      .testTag("prayer_card_${prayerTime.prayer.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = prayerTime.prayer.displayName.take(1),
            fontWeight = FontWeight.Bold,
            color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = prayerTime.prayer.displayName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (isNext) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.primary)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "NOW",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
          Text(
            text = prayerTime.prayer.arabicName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = prayerTime.timeFormatted,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (prayerTime.prayer != PrayerName.SUNRISE) {
          Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isCompleted) "Prayer Completed" else "Prayer Pending",
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = onTriggerAlertTest,
          modifier = Modifier
            .size(36.dp)
            .testTag("test_alert_${prayerTime.prayer.name.lowercase()}")
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Test Notification Alert",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun DailyVerseCard(
  verse: DailyVerse,
  onSendNotification: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("daily_verse_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(RadiantGold)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "VERSE OF THE DAY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Row {
          IconButton(
            onClick = onSendNotification,
            modifier = Modifier
              .size(32.dp)
              .testTag("send_verse_notification_btn")
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = "Test Verse Notification",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Arabic Calligraphy Text
      Text(
        text = verse.arabicText,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        textAlign = TextAlign.End,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = verse.transliteration,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "\"${verse.englishTranslation}\"",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Surah ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.secondary
        )

        Text(
          text = if (isExpanded) "Hide Reflection ▲" else "Reflect ▼",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(4.dp)
        )
      }

      if (isExpanded) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
        ) {
          Text(
            text = verse.reflectionPrompt,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp
          )
        }
      }
    }
  }
}

@Composable
fun TodayProgressSummaryCard(
  dailyLog: DailyLog,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("today_progress_card")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${dailyLog.completedPrayersCount}/5",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Today's Spiritual Obligations",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (dailyLog.isFasting) "Fasting • ${dailyLog.quranPagesRead} Quran pages" else "${dailyLog.quranPagesRead} Quran pages recited",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Text(
        text = "Track →",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}
