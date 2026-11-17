package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SurahAudioInfo
import com.example.audio.SurahAudioPlayer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun SurahAudioBar(
  audioPlayer: SurahAudioPlayer,
  modifier: Modifier = Modifier
) {
  val isPlaying by audioPlayer.isPlaying.collectAsState()
  val currentSurah by audioPlayer.currentSurah.collectAsState()
  val currentPosition by audioPlayer.currentPositionMs.collectAsState()
  val duration by audioPlayer.durationMs.collectAsState()
  val isLoading by audioPlayer.isLoading.collectAsState()

  var isExpanded by remember { mutableStateOf(false) }

  AnimatedVisibility(
    visible = currentSurah != null,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier
  ) {
    val surah = currentSurah ?: return@AnimatedVisibility

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = EmeraldDark,
      shadowElevation = 8.dp,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, RadiantGold.copy(alpha = 0.8f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .clickable { isExpanded = !isExpanded }
        .testTag("surah_audio_bar")
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Animated Soundwave Icon
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(RadiantGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
              contentDescription = "Audio Recitation",
              tint = RadiantGold,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // Surah Titles
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = surah.englishName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = surah.arabicName,
                fontSize = 13.sp,
                color = RadiantGold,
                fontWeight = FontWeight.SemiBold
              )
            }
            Text(
              text = "Offline Audio • ${surah.recitationQari}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 11.sp,
              maxLines = 1
            )
          }

          // Play / Pause Button
          IconButton(
            onClick = { audioPlayer.togglePlayPause() },
            modifier = Modifier.size(38.dp).testTag("audio_play_pause_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = RadiantGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
              )
            } else {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = RadiantGold,
                modifier = Modifier.size(26.dp)
              )
            }
          }

          // Stop / Dismiss Button
          IconButton(
            onClick = { audioPlayer.stop() },
            modifier = Modifier.size(32.dp).testTag("audio_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Expanded Slider or Progress Line
        val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

        if (isExpanded) {
          Spacer(modifier = Modifier.height(6.dp))
          Slider(
            value = progress,
            onValueChange = { newProg ->
              val seekPos = (newProg * duration).toInt()
              audioPlayer.seekTo(seekPos)
            },
            colors = SliderDefaults.colors(
              thumbColor = RadiantGold,
              activeTrackColor = RadiantGold,
              inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth().height(24.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = formatTime(currentPosition),
              fontSize = 10.sp,
              color = Color.White.copy(alpha = 0.7f)
            )
            Text(
              text = formatTime(duration),
              fontSize = 10.sp,
              color = Color.White.copy(alpha = 0.7f)
            )
          }
        } else {
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { progress },
            color = RadiantGold,
            trackColor = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
              .fillMaxWidth()
              .height(3.dp)
              .clip(RoundedCornerShape(2.dp))
          )
        }
      }
    }
  }
}

private fun formatTime(millis: Int): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format("%02d:%02d", minutes, seconds)
}
