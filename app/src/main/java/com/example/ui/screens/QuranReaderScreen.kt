package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.audio.SurahAudioCatalog
import com.example.audio.SurahAudioPlayer
import com.example.data.model.DailyQuranSplit
import com.example.data.model.QuranAyah
import com.example.data.model.QuranKhatmPlanCatalogue
import com.example.data.model.QuranPageCatalogue
import com.example.notification.AlarmPreferences
import com.example.ui.components.GoldHairlineDivider
import com.example.ui.components.islamicGeometricWatermark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold

@Composable
fun QuranReaderScreen(
  currentDay: Int,
  completedDays: Set<Int>,
  audioPlayer: SurahAudioPlayer,
  onToggleDayComplete: (Int, Int) -> Unit,
  onScheduleReminder: () -> Unit,
  initialOpenFullPage: Boolean = false,
  initialTargetPage: Int? = null,
  onFullScreenStateChanged: (Boolean) -> Unit = {},
  khatmMonths: Int = 1,
  onKhatmMonthsChange: (Int) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    QuranPageCatalogue.initialize(context)
  }

  var currentMonths by remember(khatmMonths) { mutableIntStateOf(khatmMonths.coerceIn(1, 3)) }
  val totalDays = remember(currentMonths) { QuranKhatmPlanCatalogue.getTotalDaysForDuration(currentMonths) }
  var selectedDay by remember(currentDay, currentMonths) { mutableIntStateOf(currentDay.coerceIn(1, totalDays)) }
  val split = remember(selectedDay, currentMonths) {
    QuranKhatmPlanCatalogue.getSplitForDayAndDuration(selectedDay, currentMonths)
  }

  var isFullPageReaderOpen by remember { mutableStateOf(initialOpenFullPage || initialTargetPage != null) }
  var activeViewingPageNumber by remember(selectedDay, currentMonths) {
    mutableIntStateOf((initialTargetPage ?: split.startPage).coerceIn(1, QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES))
  }

  val totalCompletedPages = remember(completedDays, currentMonths) {
    completedDays.sumOf { day ->
      QuranKhatmPlanCatalogue.getSplitForDayAndDuration(day, currentMonths).totalPages
    }
  }

  val khatmPercent = (totalCompletedPages.toFloat() / QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES.toFloat()).coerceIn(0f, 1f)
  val isCurrentDayCompleted = completedDays.contains(selectedDay)

  LaunchedEffect(initialOpenFullPage, initialTargetPage) {
    if (initialTargetPage != null) {
      activeViewingPageNumber = initialTargetPage.coerceIn(1, QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES)
      isFullPageReaderOpen = true
    } else if (initialOpenFullPage) {
      activeViewingPageNumber = split.startPage.coerceIn(1, QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES)
      isFullPageReaderOpen = true
    }
  }

  LaunchedEffect(isFullPageReaderOpen) {
    onFullScreenStateChanged(isFullPageReaderOpen)
  }

  DisposableEffect(Unit) {
    onDispose {
      onFullScreenStateChanged(false)
    }
  }

  if (isFullPageReaderOpen) {
    androidx.activity.compose.BackHandler(enabled = true) {
      isFullPageReaderOpen = false
    }
    // ==========================================
    // TRUE FULL-SCREEN DAY/NIGHT ARABIC MUSHAF READER VIEW
    // ==========================================
    FullPageArabicMushafView(
      split = split,
      activeViewingPageNumber = activeViewingPageNumber,
      isDayCompleted = isCurrentDayCompleted,
      onPageChange = { newPage ->
        activeViewingPageNumber = newPage.coerceIn(1, QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES)
      },
      onToggleDayComplete = {
        onToggleDayComplete(selectedDay, split.totalPages)
      },
      onCloseReader = {
        isFullPageReaderOpen = false
      },
      modifier = modifier
    )
  } else {
    // ==========================================
    // 30-DAY KHATM OVERVIEW & SELECTION HUB
    // ==========================================
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(6.dp))

        // 30-Day Khatm Hero Banner
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldDark),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, RadiantGold.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
            .testTag("quran_khatm_hero_banner")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .islamicGeometricWatermark(tint = RadiantGold, alpha = 0.14f)
              .padding(18.dp)
          ) {
            Column {
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
                      .background(RadiantGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.MenuBook,
                      contentDescription = null,
                      tint = RadiantGold,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Quran Khatm (${currentMonths} ${if (currentMonths == 1) "Month" else "Months"})",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "Madani Mushaf • 604 Pages in $totalDays Days",
                      style = MaterialTheme.typography.bodySmall,
                      color = RadiantGold,
                      fontSize = 11.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = EmeraldGreen.copy(alpha = 0.25f)
                ) {
                  Text(
                    text = "${(khatmPercent * 100).toInt()}%",
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              GoldHairlineDivider()
              Spacer(modifier = Modifier.height(12.dp))

              // Duration Pace Selector (1, 2, or 3 months, locked at 3)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black.copy(alpha = 0.25f))
                  .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                listOf(
                  1 to "1 Mo • 20p",
                  2 to "2 Mos • 10p",
                  3 to "3 Mos • 7p 🔒"
                ).forEach { (m, label) ->
                  val isSelected = currentMonths == m
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) RadiantGold else Color.Transparent,
                    modifier = Modifier
                      .weight(1f)
                      .clickable {
                        currentMonths = m
                        onKhatmMonthsChange(m)
                        selectedDay = selectedDay.coerceIn(1, m * 30)
                      }
                      .testTag("khatm_pace_${m}_month")
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 10.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.88f),
                      textAlign = TextAlign.Center,
                      maxLines = 1,
                      softWrap = false,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Progress Bar
              LinearProgressIndicator(
                progress = { khatmPercent },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = RadiantGold,
                trackColor = Color.White.copy(alpha = 0.15f)
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "$totalCompletedPages of ${QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES} pages read",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${completedDays.size} of $totalDays days",
                  style = MaterialTheme.typography.bodySmall,
                  color = RadiantGold,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }

      // Khatm Day Carousel Bar
      item {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Select Daily Portion",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${split.totalPages} Pages Daily",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(totalDays) { index ->
              val day = index + 1
              val isSelected = selectedDay == day
              val isCompleted = completedDays.contains(day)

              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isSelected -> MaterialTheme.colorScheme.primary
                      isCompleted -> EmeraldGreen.copy(alpha = 0.2f)
                      else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                  )
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) RadiantGold else Color.Transparent,
                    shape = CircleShape
                  )
                  .clickable {
                    selectedDay = day
                    activeViewingPageNumber = QuranKhatmPlanCatalogue
                      .getSplitForDayAndDuration(day, currentMonths)
                      .startPage
                  }
                  .testTag("quran_day_button_$day"),
                contentAlignment = Alignment.Center
              ) {
                if (isCompleted && !isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                  )
                } else {
                  Text(
                    text = "$day",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }

      // Active Day Details & Primary Link to Full-Page Reader
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("quran_day_details_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Day $selectedDay Split (${split.juzLabel})",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = split.surahsSummary,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrentDayCompleted) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = if (isCurrentDayCompleted) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = if (isCurrentDayCompleted) EmeraldGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isCurrentDayCompleted) "Completed" else "Pgs ${split.startPage}–${split.endPage}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentDayCompleted) EmeraldGreen else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spiritual Theme Callout
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = RadiantGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Theme: ${split.spiritualTheme}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRIMARY CTA: OPEN FULL-PAGE ARABIC QURAN
            Button(
              onClick = {
                activeViewingPageNumber = split.startPage.coerceIn(1, QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES)
                isFullPageReaderOpen = true
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("open_quran_pages_button")
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

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
              onClick = { onToggleDayComplete(selectedDay, split.totalPages) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().testTag("toggle_khatm_day_btn")
            ) {
              Icon(
                imageVector = if (isCurrentDayCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                contentDescription = null,
                tint = if (isCurrentDayCompleted) EmeraldGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isCurrentDayCompleted) "Mark Day $selectedDay Incomplete" else "Mark Day $selectedDay Complete (${split.totalPages} Pgs)",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      // Daily Prompt Notification Setup Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onScheduleReminder() }
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Daily Quran Reading Notification",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Sends your daily page split prompt automatically",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline
                )
              }
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Sunnah Audio Shortcuts (Al-Mulk & Al-Kahf)
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Sunnah Recitations",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Offline High-Quality Mishary Alafasy Recitations",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Surah Al-Mulk Item
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { audioPlayer.playSurah(SurahAudioCatalog.SURAH_MULK) }
                .padding(12.dp),
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
                    .background(RadiantGold.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Surah Al-Mulk (8:30 PM)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "30 verses • Protection from grave",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = "سورة الملك",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Surah Al-Kahf Item
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { audioPlayer.playSurah(SurahAudioCatalog.SURAH_KAHF) }
                .padding(12.dp),
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
                    .background(RadiantGold.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Surah Al-Kahf (Friday)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "110 verses • Light between Fridays",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = "سورة الكهف",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

/**
 * Immersive Full-Screen Arabic Mushaf Reader Component.
 * Displays authentic Uthmani scripture for every Madani Mushaf page (1..604),
 * supports Day & Night reading themes, adjustable Arabic font size, horizontal page swipe,
 * and tap-to-hide navigation bars for distraction-free reading.
 */
@Composable
private fun FullPageArabicMushafView(
  split: DailyQuranSplit,
  activeViewingPageNumber: Int,
  isDayCompleted: Boolean,
  onPageChange: (Int) -> Unit,
  onToggleDayComplete: () -> Unit,
  onCloseReader: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val view = LocalView.current

  val pageData = remember(activeViewingPageNumber) {
    QuranPageCatalogue.getPage(activeViewingPageNumber, context)
  }

  var isNightMode by remember { mutableStateOf(AlarmPreferences.isQuranNightMode(context)) }
  var arabicFontSizeSp by remember { mutableIntStateOf(AlarmPreferences.getQuranFontSizeSp(context)) }
  var showControls by remember { mutableStateOf(true) }
  var dragAccumulator by remember { mutableFloatStateOf(0f) }

  val scrollState = rememberScrollState()
  LaunchedEffect(activeViewingPageNumber) {
    scrollState.scrollTo(0)
  }

  // Hide Android status bar & system navigation bars while reading full-screen
  DisposableEffect(view) {
    val window = context.findActivity()?.window
    val controller = window?.let { WindowCompat.getInsetsController(it, view) }
    try {
      controller?.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      controller?.hide(WindowInsetsCompat.Type.systemBars())
    } catch (_: Exception) {
    }
    onDispose {
      try {
        controller?.show(WindowInsetsCompat.Type.systemBars())
      } catch (_: Exception) {
      }
    }
  }

  // Animated Day / Night Mushaf Colors
  val screenBg by animateColorAsState(
    targetValue = if (isNightMode) Color(0xFF070C0A) else Color(0xFFF6EFE0),
    label = "mushafScreenBg"
  )
  val pageCardBg by animateColorAsState(
    targetValue = if (isNightMode) Color(0xFF0E1713) else Color(0xFFFFFBF2),
    label = "mushafPageCardBg"
  )
  val arabicInkColor by animateColorAsState(
    targetValue = if (isNightMode) Color(0xFFEDE6D5) else Color(0xFF1A1A1A),
    label = "mushafInkColor"
  )
  val basmalahTint by animateColorAsState(
    targetValue = if (isNightMode) RadiantGold.copy(alpha = 0.92f) else EmeraldDark,
    label = "mushafBasmalahTint"
  )
  val chromeBarColor by animateColorAsState(
    targetValue = if (isNightMode) Color(0xFF0B1410) else EmeraldDark,
    label = "mushafChromeBar"
  )
  val pageBorderGold = if (isNightMode) RadiantGold.copy(alpha = 0.55f) else RadiantGold

  val surahSegments = remember(pageData) {
    buildSurahSegmentsForPage(pageData.ayahs)
  }

  val isWithinDailySplit = activeViewingPageNumber in split.startPage..split.endPage

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(screenBg)
      .pointerInput(activeViewingPageNumber) {
        detectHorizontalDragGestures(
          onDragStart = { dragAccumulator = 0f },
          onDragEnd = {
            if (dragAccumulator < -90f && activeViewingPageNumber < QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES) {
              onPageChange(activeViewingPageNumber + 1)
            } else if (dragAccumulator > 90f && activeViewingPageNumber > 1) {
              onPageChange(activeViewingPageNumber - 1)
            }
            dragAccumulator = 0f
          },
          onHorizontalDrag = { _, dragAmount ->
            dragAccumulator += dragAmount
          }
        )
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        showControls = !showControls
      }
      .testTag("full_page_mushaf_view")
  ) {
    // Edge-to-Edge Scrollable Mushaf Body
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(
          start = 10.dp,
          end = 10.dp,
          top = if (showControls) 78.dp else 14.dp,
          bottom = if (showControls) 152.dp else 22.dp
        )
    ) {
      // Ornate Double Gold Border Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(2.dp, pageBorderGold, RoundedCornerShape(14.dp))
          .padding(3.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, pageBorderGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .background(pageCardBg)
            .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            surahSegments.forEachIndexed { segIndex, segment ->
              if (segIndex > 0) {
                Spacer(modifier = Modifier.height(16.dp))
              }

              // Render Gilded Surah Banner whenever a Surah begins in this segment
              if (segment.startsSurah) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      Brush.linearGradient(
                        if (isNightMode) {
                          listOf(Color(0xFF0A241A), Color(0xFF0E3425))
                        } else {
                          listOf(EmeraldDark, Color(0xFF063323))
                        }
                      )
                    )
                    .border(1.5.dp, pageBorderGold, RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = segment.surahNameArabic,
                    color = RadiantGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (segment.showBismillahBanner) {
                  Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = (arabicFontSizeSp + 1).sp,
                    fontWeight = FontWeight.Bold,
                    color = basmalahTint,
                    textAlign = TextAlign.Center,
                    lineHeight = (arabicFontSizeSp + 14).sp,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                }
              }

              // Flowing Continuous Uthmani Arabic Text with Gilded Ayah End Markers ۝
              val segmentText = remember(segment) {
                buildString {
                  segment.ayahs.forEach { ayah ->
                    append(ayah.textArabic)
                    append(" \u06DD${toArabicNumerals(ayah.ayahNumber)} ")
                  }
                }.trim()
              }

              CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                  text = segmentText,
                  fontSize = arabicFontSizeSp.sp,
                  fontWeight = FontWeight.Medium,
                  color = arabicInkColor,
                  lineHeight = (arabicFontSizeSp * 1.85f).sp,
                  textAlign = TextAlign.Justify,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Ornate Page Footer Meta
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "الجزء ${toArabicNumerals(pageData.juzNumber)}",
                fontSize = 12.sp,
                color = RadiantGold,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "— ${toArabicNumerals(activeViewingPageNumber)} —",
                fontSize = 15.sp,
                color = if (isNightMode) RadiantGold else EmeraldDark,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = pageData.hizbQuarter,
                fontSize = 12.sp,
                color = RadiantGold,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Subtle floating badge when full-screen controls are hidden on tap
    AnimatedVisibility(
      visible = !showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 14.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = chromeBarColor.copy(alpha = 0.78f),
        border = androidx.compose.foundation.BorderStroke(1.dp, pageBorderGold.copy(alpha = 0.45f)),
        modifier = Modifier.testTag("mushaf_hidden_hud_pill")
      ) {
        Text(
          text = "Page $activeViewingPageNumber / 604 • Tap screen for controls",
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
      }
    }

    // Collapsible Top Bar: Close, Surah/Juz Meta, Font Size & Day/Night Mode Toggle
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn() + slideInVertically { -it },
      exit = fadeOut() + slideOutVertically { -it },
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      Surface(
        color = chromeBarColor.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onCloseReader,
            modifier = Modifier.testTag("mushaf_close_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Full-Screen Quran",
              tint = RadiantGold
            )
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = pageData.surahNameArabic,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RadiantGold,
              fontSize = 16.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${pageData.surahNameEnglish} • Juz ${pageData.juzNumber} • Pg $activeViewingPageNumber/604",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 10.5.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.1f),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  val nextSize = (arabicFontSizeSp - 2).coerceAtLeast(18)
                  arabicFontSizeSp = nextSize
                  AlarmPreferences.setQuranFontSizeSp(context, nextSize)
                }
            ) {
              Text(
                text = "A-",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White.copy(alpha = 0.1f),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  val nextSize = (arabicFontSizeSp + 2).coerceAtMost(30)
                  arabicFontSizeSp = nextSize
                  AlarmPreferences.setQuranFontSizeSp(context, nextSize)
                }
            ) {
              Text(
                text = "A+",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
              )
            }

            IconButton(
              onClick = {
                val nextMode = !isNightMode
                isNightMode = nextMode
                AlarmPreferences.setQuranNightMode(context, nextMode)
              },
              modifier = Modifier.testTag("mushaf_theme_toggle_btn")
            ) {
              Icon(
                imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isNightMode) "Switch to Day Reading Mode" else "Switch to Night Reading Mode",
                tint = RadiantGold
              )
            }
          }
        }
      }
    }

    // Collapsible Bottom Control Bar (Page Navigation across 1..604 & Mark Day Complete)
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn() + slideInVertically { it },
      exit = fadeOut() + slideOutVertically { it },
      modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      Surface(
        color = chromeBarColor.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { onPageChange(activeViewingPageNumber - 1) },
              enabled = activeViewingPageNumber > 1,
              modifier = Modifier.size(40.dp).testTag("mushaf_prev_page_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Page",
                tint = if (activeViewingPageNumber > 1) RadiantGold else Color.Gray
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (isWithinDailySplit) {
                  "Portion Page ${(activeViewingPageNumber - split.startPage) + 1} of ${split.totalPages} (Pg $activeViewingPageNumber)"
                } else {
                  "Madani Mushaf • Page $activeViewingPageNumber of 604"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Day ${split.dayNumber} Split (Pages ${split.startPage}–${split.endPage})",
                color = RadiantGold,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            IconButton(
              onClick = { onPageChange(activeViewingPageNumber + 1) },
              enabled = activeViewingPageNumber < QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES,
              modifier = Modifier.size(40.dp).testTag("mushaf_next_page_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Page",
                tint = if (activeViewingPageNumber < QuranKhatmPlanCatalogue.TOTAL_MUSHAF_PAGES) RadiantGold else Color.Gray
              )
            }
          }

          Slider(
            value = activeViewingPageNumber.toFloat().coerceIn(1f, 604f),
            onValueChange = { onPageChange(it.toInt()) },
            valueRange = 1f..604f,
            colors = SliderDefaults.colors(
              thumbColor = RadiantGold,
              activeTrackColor = RadiantGold,
              inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Button(
            onClick = onToggleDayComplete,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isDayCompleted) EmeraldGreen else RadiantGold,
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(42.dp)
          ) {
            Icon(
              imageVector = if (isDayCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
              contentDescription = null,
              tint = if (isDayCompleted) Color.White else Color.Black,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isDayCompleted) "Day ${split.dayNumber} Completed ✓" else "Mark Day ${split.dayNumber} Complete",
              fontWeight = FontWeight.Bold,
              color = if (isDayCompleted) Color.White else Color.Black,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

private data class SurahPageSegment(
  val surahNumber: Int,
  val surahNameArabic: String,
  val startsSurah: Boolean,
  val showBismillahBanner: Boolean,
  val ayahs: List<QuranAyah>
)

private fun buildSurahSegmentsForPage(ayahs: List<QuranAyah>): List<SurahPageSegment> {
  if (ayahs.isEmpty()) return emptyList()
  val segments = mutableListOf<SurahPageSegment>()
  var currentGroup = mutableListOf<QuranAyah>()
  var currentSurah = ayahs.first().surahNumber
  var currentSurahAr = ayahs.first().surahNameArabic
  var currentStartsSurah = ayahs.first().isSurahStart

  ayahs.forEach { ayah ->
    if (ayah.surahNumber != currentSurah || (ayah.isSurahStart && currentGroup.isNotEmpty())) {
      segments.add(
        SurahPageSegment(
          surahNumber = currentSurah,
          surahNameArabic = currentSurahAr,
          startsSurah = currentStartsSurah,
          showBismillahBanner = currentStartsSurah && currentSurah != 1 && currentSurah != 9,
          ayahs = currentGroup.toList()
        )
      )
      currentGroup = mutableListOf(ayah)
      currentSurah = ayah.surahNumber
      currentSurahAr = ayah.surahNameArabic
      currentStartsSurah = ayah.isSurahStart
    } else {
      currentGroup.add(ayah)
    }
  }
  if (currentGroup.isNotEmpty()) {
    segments.add(
      SurahPageSegment(
        surahNumber = currentSurah,
        surahNameArabic = currentSurahAr,
        startsSurah = currentStartsSurah,
        showBismillahBanner = currentStartsSurah && currentSurah != 1 && currentSurah != 9,
        ayahs = currentGroup.toList()
      )
    )
  }
  return segments
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

private fun toArabicNumerals(number: Int): String {
  val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
  val str = number.toString()
  val sb = StringBuilder()
  for (c in str) {
    if (c in '0'..'9') {
      sb.append(arabicDigits[c - '0'])
    } else {
      sb.append(c)
    }
  }
  return sb.toString()
}
