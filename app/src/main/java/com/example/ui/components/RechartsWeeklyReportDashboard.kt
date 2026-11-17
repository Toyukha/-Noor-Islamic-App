package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ChartMetricFilter {
  COMBINED,
  PRAYERS_ONLY,
  QURAN_ONLY
}

data class RechartsDayPoint(
  val dateStr: String,
  val dayLabel: String,
  val fullDateFormatted: String,
  val prayersCount: Int, // 0..5
  val prayerList: List<Pair<String, Boolean>>,
  val quranPages: Int,
  val isFasting: Boolean,
  val rawLog: DailyLog?
)

/**
 * Recharts-style interactive summary dashboard for the Weekly Report section.
 * Replaces static progress bars with responsive, interactive grouped bar charts,
 * Cartesian reference grids, smooth animations, and interactive tooltips on tap.
 */
@Composable
fun RechartsWeeklyReportDashboard(
  weeklyLogs: List<DailyLog>,
  modifier: Modifier = Modifier,
  title: String = "Weekly Performance Dashboard",
  subtitle: String = "7-day prayer & Quran consistency",
  currentDateKey: String = ""
) {
  var selectedFilter by remember { mutableStateOf(ChartMetricFilter.COMBINED) }
  var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to today (index 6)

  // Build past 7 days of normalized data
  val chartPoints = remember(weeklyLogs, currentDateKey) {
    val list = mutableListOf<RechartsDayPoint>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val readableFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
    val cal = Calendar.getInstance()

    for (i in 6 downTo 0) {
      val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
      val dStr = sdf.format(dayCal.time)
      val label = dayFormat.format(dayCal.time)
      val readable = readableFormat.format(dayCal.time)
      val log = weeklyLogs.find { it.date == dStr }

      val prayers = listOf(
        "Fajr" to (log?.fajr == true),
        "Dhuhr" to (log?.dhuhr == true),
        "Asr" to (log?.asr == true),
        "Maghrib" to (log?.maghrib == true),
        "Isha" to (log?.isha == true)
      )
      val pCount = prayers.count { it.second }

      list.add(
        RechartsDayPoint(
          dateStr = dStr,
          dayLabel = label,
          fullDateFormatted = readable,
          prayersCount = pCount,
          prayerList = prayers,
          quranPages = log?.quranPagesRead ?: 0,
          isFasting = log?.isFasting == true,
          rawLog = log
        )
      )
    }
    list
  }

  val selectedPoint = chartPoints.getOrNull(selectedDayIndex) ?: chartPoints.lastOrNull()

  // Smooth entrance animation like Recharts <ResponsiveContainer>
  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(selectedFilter) {
    animProgress.snapTo(0f)
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
    )
  }

  // Summary Metrics calculations
  val totalPossiblePrayers = 7 * 5
  val completedPrayers = chartPoints.sumOf { it.prayersCount }
  val prayerPct = ((completedPrayers.toFloat() / totalPossiblePrayers.toFloat()) * 100).toInt()
  val totalQuranPages = chartPoints.sumOf { it.quranPages }
  val perfectPrayerDays = chartPoints.count { it.prayersCount == 5 }

  val prayerColor = EmeraldGreen
  val quranColor = RadiantGold
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
  val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  val highlightDayBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
      .testTag("recharts_weekly_report_dashboard")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // 1. Header with Badge & Title
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
              imageVector = Icons.Default.BarChart,
              contentDescription = null,
              tint = EmeraldGreen,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldGreen.copy(alpha = 0.12f)
        ) {
          Text(
            text = "Interactive",
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

      // 2. High-Level Summary Stat Tiles (Replaces static progress bars)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Tile 1: Prayer Consistency
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Solah Rate",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "$prayerPct%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen,
                maxLines = 1,
                softWrap = false
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$completedPrayers/$totalPossiblePrayers",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        // Tile 2: Quran Volume
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Quran Read",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "$totalQuranPages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RadiantGold,
                maxLines = 1,
                softWrap = false
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "pages",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        // Tile 3: Perfect Days
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Perfect Solah",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "$perfectPrayerDays",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "days 5/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Interactive Filter Chips (Recharts Dataset Selector)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilterChip(
          selected = selectedFilter == ChartMetricFilter.COMBINED,
          onClick = { selectedFilter = ChartMetricFilter.COMBINED },
          label = { Text("Combined", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("filter_combined")
        )

        FilterChip(
          selected = selectedFilter == ChartMetricFilter.PRAYERS_ONLY,
          onClick = { selectedFilter = ChartMetricFilter.PRAYERS_ONLY },
          label = { Text("Solah (0–5)", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldGreen,
            selectedLabelColor = Color.White
          ),
          modifier = Modifier.testTag("filter_prayers")
        )

        FilterChip(
          selected = selectedFilter == ChartMetricFilter.QURAN_ONLY,
          onClick = { selectedFilter = ChartMetricFilter.QURAN_ONLY },
          label = { Text("Quran", fontSize = 11.sp, maxLines = 1, softWrap = false) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RadiantGold,
            selectedLabelColor = Color.Black
          ),
          modifier = Modifier.testTag("filter_quran")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Recharts Interactive Grouped Bar Chart Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
          .pointerInput(chartPoints) {
            detectTapGestures { offset ->
              val totalWidth = size.width
              val columnWidth = totalWidth / 7f
              val clickedIndex = (offset.x / columnWidth).toInt().coerceIn(0, 6)
              selectedDayIndex = clickedIndex
            }
          }
          .testTag("recharts_canvas_box")
      ) {
        val animationVal = animProgress.value

        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
          val canvasWidth = size.width
          val canvasHeight = size.height - 24.dp.toPx() // Reserve 24dp for X-axis labels
          val colWidth = canvasWidth / 7f
          val spacingFraction = 0.22f

          // Draw Cartesian Grid Lines (like Recharts <CartesianGrid strokeDasharray="3 3" />)
          val gridLineCount = 4
          for (g in 0..gridLineCount) {
            val y = canvasHeight * (g / gridLineCount.toFloat())
            drawLine(
              color = gridColor,
              start = Offset(0f, y),
              end = Offset(canvasWidth, y),
              strokeWidth = 1.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
          }

          // Max scaling constants
          val maxPrayer = 5f
          val maxQuran = 20f

          for (i in 0..6) {
            val point = chartPoints[i]
            val colLeft = i * colWidth
            val isSelected = (i == selectedDayIndex)

            // Highlight column background if selected
            if (isSelected) {
              drawRoundRect(
                color = highlightDayBg,
                topLeft = Offset(colLeft + 2.dp.toPx(), 0f),
                size = Size(colWidth - 4.dp.toPx(), canvasHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
              )
            }

            val usableWidth = colWidth * (1f - spacingFraction)
            val paddingLeft = colLeft + (colWidth * spacingFraction / 2f)

            when (selectedFilter) {
              ChartMetricFilter.COMBINED -> {
                val singleBarWidth = usableWidth / 2.2f

                // Prayer Bar (Left)
                val prayerBarHeight = ((point.prayersCount / maxPrayer) * canvasHeight * animationVal).coerceIn(0f, canvasHeight)
                // Track
                drawRoundRect(
                  color = trackColor,
                  topLeft = Offset(paddingLeft, 0f),
                  size = Size(singleBarWidth, canvasHeight),
                  cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Fill
                if (prayerBarHeight > 0f) {
                  drawRoundRect(
                    color = if (point.prayersCount == 5) EmeraldDark else prayerColor,
                    topLeft = Offset(paddingLeft, canvasHeight - prayerBarHeight),
                    size = Size(singleBarWidth, prayerBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                  )
                }

                // Quran Bar (Right)
                val quranX = paddingLeft + singleBarWidth + 2.dp.toPx()
                val quranBarHeight = ((point.quranPages.toFloat() / maxQuran) * canvasHeight * animationVal).coerceIn(0f, canvasHeight)
                // Track
                drawRoundRect(
                  color = trackColor,
                  topLeft = Offset(quranX, 0f),
                  size = Size(singleBarWidth, canvasHeight),
                  cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Fill
                if (quranBarHeight > 0f) {
                  drawRoundRect(
                    color = quranColor,
                    topLeft = Offset(quranX, canvasHeight - quranBarHeight),
                    size = Size(singleBarWidth, quranBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                  )
                }
              }

              ChartMetricFilter.PRAYERS_ONLY -> {
                val barWidth = usableWidth
                val prayerBarHeight = ((point.prayersCount / maxPrayer) * canvasHeight * animationVal).coerceIn(0f, canvasHeight)

                // Track
                drawRoundRect(
                  color = trackColor,
                  topLeft = Offset(paddingLeft, 0f),
                  size = Size(barWidth, canvasHeight),
                  cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                // Fill
                if (prayerBarHeight > 0f) {
                  drawRoundRect(
                    brush = Brush.verticalGradient(listOf(EmeraldDark, prayerColor)),
                    topLeft = Offset(paddingLeft, canvasHeight - prayerBarHeight),
                    size = Size(barWidth, prayerBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                  )
                }
              }

              ChartMetricFilter.QURAN_ONLY -> {
                val barWidth = usableWidth
                val quranBarHeight = ((point.quranPages.toFloat() / maxQuran) * canvasHeight * animationVal).coerceIn(0f, canvasHeight)

                // Track
                drawRoundRect(
                  color = trackColor,
                  topLeft = Offset(paddingLeft, 0f),
                  size = Size(barWidth, canvasHeight),
                  cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                // Fill
                if (quranBarHeight > 0f) {
                  drawRoundRect(
                    brush = Brush.verticalGradient(listOf(StarGold, quranColor)),
                    topLeft = Offset(paddingLeft, canvasHeight - quranBarHeight),
                    size = Size(barWidth, quranBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                  )
                }
              }
            }
          }
        }

        // X-Axis Day Labels Row at Bottom
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          chartPoints.forEachIndexed { idx, point ->
            val isSelected = (idx == selectedDayIndex)
            Text(
              text = point.dayLabel,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .clickable { selectedDayIndex = idx }
                .padding(vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Recharts Interactive Tooltip Overlay Card (<Tooltip content={<CustomTooltip />} />)
      if (selectedPoint != null) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_active_tooltip")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = selectedPoint.fullDateFormatted,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              if (selectedPoint.isFasting) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = RadiantGold.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "Fasting Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prayers Breakdown Pills
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              selectedPoint.prayerList.forEach { (name, done) ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (done) EmeraldGreen else Color.Gray.copy(alpha = 0.15f),
                  modifier = Modifier.weight(1f)
                ) {
                  Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (done) Icons.Default.Check else Icons.Default.Close,
                      contentDescription = null,
                      tint = if (done) Color.White else Color.Gray,
                      modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                      text = name,
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Sub-row
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
                    .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Solah: ${selectedPoint.prayersCount} of 5 fulfilled",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RadiantGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Quran: ${selectedPoint.quranPages} pages read",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 6. Recharts-style Legend Row (<Legend />)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(EmeraldGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Daily Solah (0-5)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(RadiantGold)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Quran Pages Read",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
