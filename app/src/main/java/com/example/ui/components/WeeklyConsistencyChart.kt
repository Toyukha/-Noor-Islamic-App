package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ConsistencyDayData(
  val dateStr: String,
  val dayLabel: String,
  val formattedDate: String,
  val prayerCount: Int, // 0..5
  val completedPrayers: List<String>,
  val missingPrayers: List<String>,
  val quranPages: Int,
  val isFasting: Boolean,
  val originalLog: DailyLog?
)

/**
 * 7-Day Consistency Visualization:
 * Displays dual-metric grouped bars (Emerald Solah bar + Radiant Gold Quran reading bar)
 * with touch interaction: tapping on any day opens a rich detailed tooltip breakdown.
 */
@Composable
fun WeeklyConsistencyChart(
  weeklyLogs: List<DailyLog>,
  modifier: Modifier = Modifier,
  title: String = "7-Day Consistency: Solah & Quran",
  subtitle: String = "Tap any day to inspect your prayers and reading breakdown"
) {
  val daysData = remember(weeklyLogs) {
    val list = mutableListOf<ConsistencyDayData>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val readableFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val cal = Calendar.getInstance()

    for (i in 6 downTo 0) {
      val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
      val dStr = sdf.format(dayCal.time)
      val label = dayFormat.format(dayCal.time)
      val readable = readableFormat.format(dayCal.time)
      val log = weeklyLogs.find { it.date == dStr }

      val completed = mutableListOf<String>()
      val missing = mutableListOf<String>()

      if (log?.fajr == true) completed.add("Fajr") else missing.add("Fajr")
      if (log?.dhuhr == true) completed.add("Dhuhr") else missing.add("Dhuhr")
      if (log?.asr == true) completed.add("Asr") else missing.add("Asr")
      if (log?.maghrib == true) completed.add("Maghrib") else missing.add("Maghrib")
      if (log?.isha == true) completed.add("Isha") else missing.add("Isha")

      list.add(
        ConsistencyDayData(
          dateStr = dStr,
          dayLabel = label,
          formattedDate = readable,
          prayerCount = completed.size,
          completedPrayers = completed,
          missingPrayers = missing,
          quranPages = log?.quranPagesRead ?: 0,
          isFasting = log?.isFasting == true,
          originalLog = log
        )
      )
    }
    list
  }

  var selectedDayIndex by remember { mutableStateOf<Int?>(6) } // Default to today (last index)

  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    animProgress.animateTo(1f, animationSpec = tween(700))
  }

  val primaryGreen = EmeraldGreen
  val goldAccent = RadiantGold
  val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.testTag("weekly_consistency_chart")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
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

        // Legend
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(primaryGreen)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Solah", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(goldAccent)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Quran", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Canvas Interactive Chart
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
      ) {
        Canvas(
          modifier = Modifier
            .matchParentSize()
            .pointerInput(daysData) {
              detectTapGestures { offset ->
                val slotWidth = size.width / 7f
                val tappedIndex = (offset.x / slotWidth).toInt().coerceIn(0, 6)
                selectedDayIndex = tappedIndex
              }
            }
        ) {
          val slotWidth = size.width / 7f
          val barWidth = 12.dp.toPx()
          val gapBetweenBars = 3.dp.toPx()
          val maxSolah = 5f
          val maxQuranPagesBenchmark = 20f
          val usableHeight = size.height - 24.dp.toPx()

          // Draw benchmark lines
          val midY = usableHeight * 0.5f
          drawLine(
            color = trackColor,
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 1.dp.toPx()
          )

          for (i in 0..6) {
            val day = daysData.getOrNull(i) ?: continue
            val slotStartX = i * slotWidth
            val isSelected = selectedDayIndex == i

            // Highlight column background if selected
            if (isSelected) {
              drawRoundRect(
                color = highlightColor,
                topLeft = Offset(slotStartX + 2.dp.toPx(), 0f),
                size = Size(slotWidth - 4.dp.toPx(), usableHeight + 20.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
              )
            }

            val groupCenterX = slotStartX + (slotWidth / 2f)
            val solahBarX = groupCenterX - barWidth - (gapBetweenBars / 2f)
            val quranBarX = groupCenterX + (gapBetweenBars / 2f)

            // 1. Solah Track & Bar
            drawRoundRect(
              color = trackColor,
              topLeft = Offset(solahBarX, 0f),
              size = Size(barWidth, usableHeight),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            val solahRatio = (day.prayerCount / maxSolah).coerceIn(0f, 1f)
            val solahHeight = solahRatio * usableHeight * animProgress.value
            val solahFill = if (day.prayerCount >= 5) StarGold else primaryGreen

            drawRoundRect(
              color = solahFill,
              topLeft = Offset(solahBarX, usableHeight - solahHeight),
              size = Size(barWidth, solahHeight),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // 2. Quran Track & Bar
            drawRoundRect(
              color = trackColor,
              topLeft = Offset(quranBarX, 0f),
              size = Size(barWidth, usableHeight),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            val quranRatio = (day.quranPages.toFloat() / maxQuranPagesBenchmark).coerceIn(0f, 1f)
            val quranHeight = quranRatio * usableHeight * animProgress.value

            drawRoundRect(
              color = goldAccent,
              topLeft = Offset(quranBarX, usableHeight - quranHeight),
              size = Size(barWidth, quranHeight),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
          }
        }
      }

      // X-Axis Day Labels
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        daysData.forEachIndexed { index, day ->
          val isSelected = selectedDayIndex == index
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable { selectedDayIndex = index }
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Text(
              text = day.dayLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Indicator dot for perfect days (5/5)
            if (day.prayerCount == 5) {
              Box(
                modifier = Modifier
                  .size(4.dp)
                  .clip(CircleShape)
                  .background(StarGold)
              )
            } else {
              Spacer(modifier = Modifier.height(4.dp))
            }
          }
        }
      }

      // Interactive Selected Day Tooltip Sheet
      val selectedDay = selectedDayIndex?.let { daysData.getOrNull(it) }
      AnimatedVisibility(
        visible = selectedDay != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        selectedDay?.let { day ->
          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("day_details_tooltip")
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${day.dayLabel} • ${day.formattedDate}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (day.isFasting) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .background(RadiantGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                      Text("Fasting", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RadiantGold)
                    }
                  }
                }

                IconButton(
                  onClick = { selectedDayIndex = null },
                  modifier = Modifier.size(20.dp)
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Solah Status
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (day.prayerCount == 5) Icons.Default.Star else Icons.Default.Check,
                      contentDescription = null,
                      tint = if (day.prayerCount == 5) StarGold else EmeraldGreen,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Solah: ${day.prayerCount}/5 completed",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  if (day.completedPrayers.isNotEmpty()) {
                    Text(
                      text = "Done: ${day.completedPrayers.joinToString(", ")}",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.outline
                    )
                  }
                  if (day.missingPrayers.isNotEmpty()) {
                    Text(
                      text = "Missed: ${day.missingPrayers.joinToString(", ")}",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                  }
                }

                // Quran Status
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.End
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.MenuBook,
                      contentDescription = null,
                      tint = RadiantGold,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Quran: ${day.quranPages} pages",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  Text(
                    text = if (day.quranPages > 0) "Tilawah recorded" else "No pages read",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                  )
                }
              }

              // Adhkar Remembrance Status
              val mAdhkar = day.originalLog?.morningAdhkar == true
              val eAdhkar = day.originalLog?.eveningAdhkar == true
              if (mAdhkar || eAdhkar) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Adhkar: " + listOfNotNull(
                    if (mAdhkar) "Morning ✓" else null,
                    if (eAdhkar) "Evening ✓" else null
                  ).joinToString(" • "),
                  fontSize = 10.sp,
                  color = EmeraldGreen,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
