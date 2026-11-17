package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AdhkarAudioPlayer
import com.example.data.model.AdhkarCatalogue
import com.example.data.model.AdhkarPeriod
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarPlayerScreen(
  adhkarAudioPlayer: AdhkarAudioPlayer,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentPeriod by adhkarAudioPlayer.currentPeriod.collectAsState()
  val currentIndex by adhkarAudioPlayer.currentIndex.collectAsState()
  val currentRepetition by adhkarAudioPlayer.currentRepetition.collectAsState()
  val isPlaying by adhkarAudioPlayer.isPlaying.collectAsState()
  val isLoading by adhkarAudioPlayer.isLoading.collectAsState()
  val autoAdvance by adhkarAudioPlayer.autoAdvance.collectAsState()
  val completedIds by adhkarAudioPlayer.completedIds.collectAsState()
  val audioError by adhkarAudioPlayer.audioError.collectAsState()

  val items = AdhkarCatalogue.getAdhkarForPeriod(currentPeriod)
  val safeIndex = currentIndex.coerceIn(0, maxOf(0, items.size - 1))
  val currentItem = if (items.isNotEmpty()) items[safeIndex] else AdhkarCatalogue.morningAdhkar.first()

  val isMorning = currentPeriod == AdhkarPeriod.MORNING
  val accentColor = if (isMorning) RadiantGold else EmeraldGreen

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (isMorning) "Morning Adhkar • أذكار الصباح" else "Evening Adhkar • أذكار المساء",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Prophetic Daily Fortress of Remembrance",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              adhkarAudioPlayer.pause()
              onNavigateBack()
            },
            modifier = Modifier.testTag("adhkar_back_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        actions = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text(
              text = "Auto",
              style = MaterialTheme.typography.labelSmall,
              color = if (autoAdvance) accentColor else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
              checked = autoAdvance,
              onCheckedChange = { adhkarAudioPlayer.toggleAutoAdvance() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f)
              ),
              modifier = Modifier.testTag("auto_advance_switch")
            )
          }
        }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Period Selector Tabs (Morning / Evening)
      TabRow(
        selectedTabIndex = if (isMorning) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[if (isMorning) 0 else 1]),
            color = accentColor
          )
        }
      ) {
        Tab(
          selected = isMorning,
          onClick = { adhkarAudioPlayer.setPeriod(AdhkarPeriod.MORNING) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.WbSunny, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Morning (Fajr)", fontWeight = if (isMorning) FontWeight.Bold else FontWeight.Normal)
            }
          },
          modifier = Modifier.testTag("morning_adhkar_tab")
        )
        Tab(
          selected = !isMorning,
          onClick = { adhkarAudioPlayer.setPeriod(AdhkarPeriod.EVENING) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.NightsStay, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Evening (Asr)", fontWeight = if (!isMorning) FontWeight.Bold else FontWeight.Normal)
            }
          },
          modifier = Modifier.testTag("evening_adhkar_tab")
        )
      }

      // Horizontal Dhikr Indicator Carousel
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(items) { idx, item ->
          val isSelected = idx == safeIndex
          val isCompleted = completedIds.contains(item.id)

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              isSelected -> accentColor
              isCompleted -> accentColor.copy(alpha = 0.2f)
              else -> MaterialTheme.colorScheme.surface
            },
            modifier = Modifier
              .clickable { adhkarAudioPlayer.playDhikrAtIndex(idx) }
              .testTag("adhkar_item_pill_$idx")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isCompleted) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Completed",
                  tint = if (isSelected) Color.White else accentColor,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = "${idx + 1}. ${item.title.take(14)}...",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      AnimatedVisibility(
        visible = audioError != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("adhkar_audio_error_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Audio playback warning",
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = audioError.orEmpty(),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { adhkarAudioPlayer.playCurrent() }) {
              Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer)
            }
            IconButton(onClick = { adhkarAudioPlayer.clearError() }) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss audio error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // Main Dhikr Display Card
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("adhkar_main_card")
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Header with Title and Virtues Badge
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = accentColor.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "${safeIndex + 1} of ${items.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(
                      Icons.Default.Repeat,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${currentItem.repeatCount}x",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = currentItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(18.dp))

              // Authentic Arabic Uthmani Text Display
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = currentItem.arabicText,
                  style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 38.sp
                  ),
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Right,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(20.dp).testTag("adhkar_arabic_text")
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // English Translation
              Text(
                text = currentItem.englishTranslation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().testTag("adhkar_translation_text")
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Virtue & Hadith Reference
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(accentColor.copy(alpha = 0.08f))
                  .padding(12.dp),
                verticalAlignment = Alignment.Top
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = currentItem.virtue,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 11.sp,
                  lineHeight = 16.sp
                )
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Interactive Repetition Counter Ring
              RepetitionCounterButton(
                currentRepetition = currentRepetition,
                targetRepetition = currentItem.repeatCount,
                accentColor = accentColor,
                onIncrement = { adhkarAudioPlayer.incrementRepetition() }
              )
            }
          }
        }
      }

      // Bottom Audio Controls Bar
      Surface(
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous Dhikr Button
          IconButton(
            onClick = { adhkarAudioPlayer.previous() },
            enabled = safeIndex > 0,
            modifier = Modifier.testTag("adhkar_prev_btn")
          ) {
            Icon(Icons.Default.FastRewind, contentDescription = "Previous Dhikr")
          }

          // Main Play / Pause FAB
          FloatingActionButton(
            onClick = { adhkarAudioPlayer.togglePlayPause() },
            containerColor = accentColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(56.dp).testTag("adhkar_play_pause_fab")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(28.dp)
              )
            }
          }

          // Next Dhikr Button
          IconButton(
            onClick = { adhkarAudioPlayer.next() },
            enabled = safeIndex < items.size - 1,
            modifier = Modifier.testTag("adhkar_next_btn")
          ) {
            Icon(Icons.Default.FastForward, contentDescription = "Next Dhikr")
          }
        }
      }
    }
  }
}

@Composable
fun RepetitionCounterButton(
  currentRepetition: Int,
  targetRepetition: Int,
  accentColor: Color,
  onIncrement: () -> Unit
) {
  val isComplete = currentRepetition >= targetRepetition

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isComplete) EmeraldGreen.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.12f),
    modifier = Modifier
      .clickable { onIncrement() }
      .testTag("adhkar_counter_btn")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (isComplete) {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = "Completed",
          tint = EmeraldGreen,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Completed ($targetRepetition/$targetRepetition)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = EmeraldGreen
        )
      } else {
        Icon(
          Icons.Default.Repeat,
          contentDescription = "Tap to Count",
          tint = accentColor,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Tap to Recite: $currentRepetition of $targetRepetition",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = accentColor
        )
      }
    }
  }
}
