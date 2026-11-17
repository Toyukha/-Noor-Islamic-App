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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.data.model.DailyVerse
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerName
import com.example.data.model.QuranKhatmPlanCatalogue
import com.example.data.model.SinglePrayerTime
import com.example.notification.NotificationHelper
import com.example.ui.components.GoldHairlineDivider
import com.example.ui.components.WeeklyProgressTriBarChart
import com.example.ui.components.islamicGeometricWatermark
import com.example.ui.theme.EmeraldDark
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import com.example.ui.theme.EmeraldGreen
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
  weeklyLogs: List<DailyLog> = emptyList(),
  currentQuranDay: Int = 1,
  onOpenQuranPages: (Int) -> Unit = {},
  onNavigateToTadabbur: () -> Unit = {},
  onToggleMorningAdhkar: () -> Unit = {},
  onToggleEveningAdhkar: () -> Unit = {},
  onTogglePrayer: (PrayerName) -> Unit = {},
  showDonationCard: Boolean = false,
  onOpenDonationDetails: () -> Unit = {},
  onDismissDonationCard: () -> Unit = {},
  currentDateKey: String = "",
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
          .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Cloud Sync Active",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Real-time • Synced",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.outline,
          maxLines = 1,
          softWrap = false
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
      // Recharts-style Weekly Summary Dashboard: Interactive Bar Charts for Prayer Consistency & Quran Completion
      com.example.ui.components.RechartsWeeklyReportDashboard(
        weeklyLogs = weeklyLogs,
        currentDateKey = currentDateKey,
        modifier = Modifier.fillMaxWidth()
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
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = schedule.hijriFormatted,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
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
        onToggleCompletion = {
          if (prayerTime.prayer != PrayerName.SUNRISE) {
            onTogglePrayer(prayerTime.prayer)
          }
        },
        onTriggerAlertTest = {
          NotificationHelper.showPrayerAlert(context, prayerTime.prayer)
          testAlertSentPrayer = prayerTime.prayer
        }
      )
    }

    item {
      // Daily Adhkar Remembrance Card
      TodayAdhkarTrackerCard(
        dailyLog = dailyLog,
        onToggleMorning = onToggleMorningAdhkar,
        onToggleEvening = onToggleEveningAdhkar
      )
    }

    item {
      // Prominent Tadabbur Reflection & Open Quran Card (Exclusive on Today tab)
      TodayTadabburCard(
        verse = verse,
        currentDay = currentQuranDay,
        onOpenQuran = { onOpenQuranPages(currentQuranDay) },
        onOpenTadabbur = onNavigateToTadabbur
      )
    }

    if (showDonationCard) {
      item {
        com.example.ui.components.TodayDonationSupportCard(
          onViewDetails = onOpenDonationDetails,
          onDismissThreeDays = onDismissDonationCard
        )
      }
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
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, RadiantGold.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
      .testTag("hero_countdown_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              EmeraldDark,
              MaterialTheme.colorScheme.primary
            )
          )
        )
        .islamicGeometricWatermark(tint = StarGold, alpha = 0.14f)
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
              .weight(1f, fill = false)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.18f))
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
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = schedule.dateFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(14.dp))
        GoldHairlineDivider()
        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
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
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = nextPrayer?.timeFormatted ?: "--:--",
              style = MaterialTheme.typography.bodyLarge,
              color = Color.White.copy(alpha = 0.9f),
              maxLines = 1,
              softWrap = false
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "COUNTDOWN",
              fontSize = 11.sp,
              letterSpacing = 1.sp,
              color = Color.White.copy(alpha = 0.72f),
              maxLines = 1,
              softWrap = false
            )
            Text(
              text = schedule.timeUntilNextFormatted,
              fontSize = 30.sp,
              fontWeight = FontWeight.Black,
              color = StarGold,
              maxLines = 1,
              softWrap = false
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
  onToggleCompletion: () -> Unit = {},
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
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onToggleCompletion)
      .testTag("prayer_card_${prayerTime.prayer.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
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

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = prayerTime.prayer.displayName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
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
                  color = Color.White,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
          Text(
            text = prayerTime.prayer.arabicName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = prayerTime.timeFormatted,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          softWrap = false
        )

        Spacer(modifier = Modifier.width(4.dp))

        if (prayerTime.prayer != PrayerName.SUNRISE) {
          IconButton(
            onClick = onToggleCompletion,
            modifier = Modifier
              .size(44.dp)
              .testTag("toggle_prayer_${prayerTime.prayer.name.lowercase()}")
          ) {
            Icon(
              imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
              contentDescription = if (isCompleted) "Prayer Completed" else "Prayer Pending",
              tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        IconButton(
          onClick = onTriggerAlertTest,
          modifier = Modifier
            .size(44.dp)
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
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Today's Spiritual Obligations",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          val adhkarSummary = when {
            dailyLog.morningAdhkar && dailyLog.eveningAdhkar -> "Adhkar 2/2"
            dailyLog.morningAdhkar || dailyLog.eveningAdhkar -> "Adhkar 1/2"
            else -> null
          }
          val parts = listOfNotNull(
            if (dailyLog.isFasting) "Fasting" else null,
            if (dailyLog.quranPagesRead > 0) "${dailyLog.quranPagesRead} Quran pgs" else null,
            adhkarSummary
          )
          val detailText = if (parts.isNotEmpty()) parts.joinToString(" • ") else "Track prayers, Quran & daily Adhkar"

          Text(
            text = detailText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "Track →",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}

/**
 * Prominent Tadabbur Reflection & Open Quran Card for Today Screen.
 * Exclusively displays spiritual contemplation, daily 20-page Khatm portion,
 * and a primary link to open the in-app Arabic Quran full-page reader.
 */
@Composable
fun TodayTadabburCard(
  verse: DailyVerse,
  currentDay: Int,
  onOpenQuran: () -> Unit,
  onOpenTadabbur: () -> Unit
) {
  val split = remember(currentDay) {
    QuranKhatmPlanCatalogue.getSplitForDay(currentDay)
  }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, RadiantGold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
      .testTag("today_tadabbur_quran_card")
  ) {
    Column(
      modifier = Modifier
        .islamicGeometricWatermark(tint = RadiantGold, alpha = 0.06f)
        .padding(18.dp)
    ) {
      // Header: Luminous Theme & Khatm Day Indicator
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
              .size(36.dp)
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
              text = "Daily Tadabbur & Quran",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Contemplation & 30-Day Khatm",
              style = MaterialTheme.typography.labelSmall,
              color = RadiantGold,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldGreen.copy(alpha = 0.15f)
        ) {
          Text(
            text = "Day $currentDay / 30",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EmeraldGreen,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Today's Spiritual Focus Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(12.dp)
      ) {
        Column {
          Text(
            text = "Today's Ayah: ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = verse.reflectionPrompt,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
            maxLines = 3
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Quran Split Info Chip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(RadiantGold.copy(alpha = 0.12f))
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pages ${split.startPage} – ${split.endPage} Today",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${split.totalPages} Pages • ${split.juzLabel}",
          style = MaterialTheme.typography.labelSmall,
          color = RadiantGold,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          softWrap = false
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // PRIMARY CTA BUTTON: OPEN TODAY'S QURAN PAGES
      Button(
        onClick = onOpenQuran,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("open_quran_from_today_btn")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.MenuBook,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Open Today's Quran Pages",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // SECONDARY BUTTON: VIEW FULL TADABBUR
      OutlinedButton(
        onClick = onOpenTadabbur,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("view_full_tadabbur_btn")
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = RadiantGold,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "View Tadabbur Reflection",
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun TodayAdhkarTrackerCard(
  dailyLog: DailyLog,
  onToggleMorning: () -> Unit,
  onToggleEvening: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth().testTag("today_adhkar_tracker_card")
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
              .size(38.dp)
              .clip(CircleShape)
              .background(EmeraldGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = EmeraldGreen,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Daily Adhkar Tracker",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "أذكار الصباح والمساء • Remembrance",
              style = MaterialTheme.typography.labelSmall,
              color = EmeraldGreen,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val completedCount = (if (dailyLog.morningAdhkar) 1 else 0) + (if (dailyLog.eveningAdhkar) 1 else 0)
        Surface(
          shape = RoundedCornerShape(10.dp),
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

      Spacer(modifier = Modifier.height(14.dp))

      // Morning Adhkar Check Tile
      AdhkarTodayTile(
        title = "Morning Adhkar • أذكار الصباح",
        subtitle = "Post-Fajr remembrance for divine protection",
        isChecked = dailyLog.morningAdhkar,
        accentColor = RadiantGold,
        testTag = "today_morning_adhkar_tile",
        onToggle = onToggleMorning
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Evening Adhkar Check Tile
      AdhkarTodayTile(
        title = "Evening Adhkar • أذكار المساء",
        subtitle = "Post-Asr remembrance for fortress of the night",
        isChecked = dailyLog.eveningAdhkar,
        accentColor = EmeraldGreen,
        testTag = "today_evening_adhkar_tile",
        onToggle = onToggleEvening
      )
    }
  }
}

@Composable
fun AdhkarTodayTile(
  title: String,
  subtitle: String,
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
        .padding(horizontal = 14.dp, vertical = 10.dp),
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
          Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}


