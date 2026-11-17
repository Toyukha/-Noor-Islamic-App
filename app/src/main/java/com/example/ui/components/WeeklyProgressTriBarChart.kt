package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyMetric(
  val dateStr: String,
  val dayLabel: String,
  val prayerCount: Int, // 0 - 5
  val prayerRate: Float, // 0f - 1f
  val isFasting: Boolean,
  val quranPages: Int,
  val quranRate: Float // 0f - 1f (based on 10 page goal)
)

@Composable
fun WeeklyProgressTriBarChart(
  weeklyLogs: List<DailyLog>,
  modifier: Modifier = Modifier
) {
  // Prepare past 7 consecutive days metrics
  val metrics = remember(weeklyLogs) {
    val list = mutableListOf<DailyMetric>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val cal = Calendar.getInstance()

    for (i in 6 downTo 0) {
      val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
      val dStr = sdf.format(dayCal.time)
      val label = dayFormat.format(dayCal.time)
      val log = weeklyLogs.find { it.date == dStr }

      var pCount = 0
      if (log?.fajr == true) pCount++
      if (log?.dhuhr == true) pCount++
      if (log?.asr == true) pCount++
      if (log?.maghrib == true) pCount++
      if (log?.isha == true) pCount++

      val pRate = (pCount / 5f).coerceIn(0f, 1f)
      val isFasting = log?.isFasting == true
      val qPages = log?.quranPagesRead ?: 0
      val qRate = (qPages / 10f).coerceIn(0f, 1f)

      list.add(
        DailyMetric(
          dateStr = dStr,
          dayLabel = label,
          prayerCount = pCount,
          prayerRate = pRate,
          isFasting = isFasting,
          quranPages = qPages,
          quranRate = qRate
        )
      )
    }
    list
  }

  var selectedIndex by remember { mutableStateOf(metrics.size - 1) }
  val selectedMetric = metrics.getOrNull(selectedIndex) ?: metrics.lastOrNull()

  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 750))
  }

  val prayerBarColor = MaterialTheme.colorScheme.primary
  val fastingBarColor = RadiantGold
  val quranBarColor = Color(0xFF00897B) // Elegant Teal/Cyan
  val trackBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  val axisColor = MaterialTheme.colorScheme.outlineVariant

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("weekly_progress_chart_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header & Icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Weekly Progress Report",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "7-Day Solah, Sawm & Tilawah Rates",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Legend Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LegendDot(color = prayerBarColor, label = "Prayers (5/d)")
        LegendDot(color = fastingBarColor, label = "Fasting")
        LegendDot(color = quranBarColor, label = "Quran (10p)")
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Canvas Grouped Tri-Bar Chart
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .pointerInput(metrics) {
              detectTapGestures { offset ->
                val groupWidth = size.width / metrics.size.toFloat()
                val clickedIndex = (offset.x / groupWidth).toInt().coerceIn(0, metrics.size - 1)
                selectedIndex = clickedIndex
              }
            }
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val numDays = metrics.size
          val groupWidth = canvasWidth / numDays.toFloat()
          val barWidth = (groupWidth * 0.22f).coerceAtMost(10.dp.toPx())
          val barSpacing = 2.dp.toPx()
          val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

          // Horizontal baseline
          drawLine(
            color = axisColor,
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 1.dp.toPx()
          )

          metrics.forEachIndexed { index, metric ->
            val groupStartX = index * groupWidth
            val isSelected = index == selectedIndex

            // If selected, draw highlight column background
            if (isSelected) {
              drawRoundRect(
                color = prayerBarColor.copy(alpha = 0.08f),
                topLeft = Offset(groupStartX + 2.dp.toPx(), 0f),
                size = Size(groupWidth - 4.dp.toPx(), canvasHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
              )
            }

            // Tri-bar center offsets
            val totalBarsWidth = (barWidth * 3) + (barSpacing * 2)
            val startBarX = groupStartX + (groupWidth - totalBarsWidth) / 2f

            // 1. Prayer Bar
            val pHeight = (metric.prayerRate * canvasHeight * animationProgress.value).coerceAtLeast(3.dp.toPx())
            drawRoundRect(
              color = trackBackgroundColor,
              topLeft = Offset(startBarX, 0f),
              size = Size(barWidth, canvasHeight),
              cornerRadius = cornerRadius
            )
            drawRoundRect(
              color = prayerBarColor,
              topLeft = Offset(startBarX, canvasHeight - pHeight),
              size = Size(barWidth, pHeight),
              cornerRadius = cornerRadius
            )

            // 2. Fasting Bar
            val fX = startBarX + barWidth + barSpacing
            val fRate = if (metric.isFasting) 1f else 0f
            val fHeight = (fRate * canvasHeight * animationProgress.value).coerceAtLeast(if (metric.isFasting) 6.dp.toPx() else 0f)
            drawRoundRect(
              color = trackBackgroundColor,
              topLeft = Offset(fX, 0f),
              size = Size(barWidth, canvasHeight),
              cornerRadius = cornerRadius
            )
            if (fHeight > 0f) {
              drawRoundRect(
                color = fastingBarColor,
                topLeft = Offset(fX, canvasHeight - fHeight),
                size = Size(barWidth, fHeight),
                cornerRadius = cornerRadius
              )
            }

            // 3. Quran Bar
            val qX = fX + barWidth + barSpacing
            val qHeight = (metric.quranRate * canvasHeight * animationProgress.value).coerceAtLeast(if (metric.quranPages > 0) 4.dp.toPx() else 0f)
            drawRoundRect(
              color = trackBackgroundColor,
              topLeft = Offset(qX, 0f),
              size = Size(barWidth, canvasHeight),
              cornerRadius = cornerRadius
            )
            if (qHeight > 0f) {
              drawRoundRect(
                color = quranBarColor,
                topLeft = Offset(qX, canvasHeight - qHeight),
                size = Size(barWidth, qHeight),
                cornerRadius = cornerRadius
              )
            }
          }
        }

        // X-Axis Day Labels below canvas
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(top = 132.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          metrics.forEachIndexed { idx, m ->
            val isSelected = idx == selectedIndex
            Text(
              text = m.dayLabel,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedIndex = idx }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Inspection Pill showing selected day's exact metrics
      if (selectedMetric != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = selectedMetric.dateStr,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Prayers: ${selectedMetric.prayerCount}/5",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = if (selectedMetric.prayerCount == 5) RadiantGold else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selectedMetric.prayerCount == 5) FontWeight.Bold else FontWeight.Normal
              )
              Text(
                text = if (selectedMetric.isFasting) "Fasted: Yes" else "Fasted: No",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = if (selectedMetric.isFasting) RadiantGold else MaterialTheme.colorScheme.outline
              )
              Text(
                text = "Quran: ${selectedMetric.quranPages}p",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = quranBarColor,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.Medium
    )
  }
}
