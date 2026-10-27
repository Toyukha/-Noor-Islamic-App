package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ReflectionUiState
import com.example.ai.VerseReflectionService
import com.example.data.local.VerseReflectionEntity
import com.example.data.model.DailyVerse
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold
import kotlinx.coroutines.launch

@Composable
fun DailyVerseReflectionComponent(
  verse: DailyVerse,
  dateStr: String,
  onSendNotification: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val reflectionService = remember { VerseReflectionService(context) }

  // Observe local Room reflection
  val cachedReflection by reflectionService.observeLocalReflection(dateStr).collectAsState(initial = null)

  var uiState by remember { mutableStateOf<ReflectionUiState>(ReflectionUiState.Idle) }
  var isExpanded by remember { mutableStateOf(true) }

  // Sync state if cached reflection is observed
  LaunchedEffect(cachedReflection) {
    if (cachedReflection != null && uiState !is ReflectionUiState.Loading) {
      uiState = ReflectionUiState.Success(cachedReflection!!, isFromLocalCache = true)
    }
  }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("daily_verse_reflection_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header: Verse of the Day tag + AI badge + notification trigger
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

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (cachedReflection != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Saved Offline",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Offline Cached",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          IconButton(
            onClick = onSendNotification,
            modifier = Modifier
              .size(32.dp)
              .testTag("send_verse_notification_btn")
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = "Send notification",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Arabic Calligraphy
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

      // Transliteration
      Text(
        text = verse.transliteration,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // English Translation
      Text(
        text = "\"${verse.englishTranslation}\"",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Citation & Toggle
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
          text = if (isExpanded) "Hide Details ▲" else "Read Reflection ▼",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(4.dp)
        )
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          val currentReflection = cachedReflection

          if (currentReflection != null) {
            // Display Generated Reflection from Room
            ReflectionDetailBody(
              reflection = currentReflection,
              onRegenerate = {
                uiState = ReflectionUiState.Loading
                scope.launch {
                  uiState = reflectionService.fetchReflection(dateStr, verse, forceRefresh = true)
                }
              },
              isLoading = uiState is ReflectionUiState.Loading
            )
          } else {
            // Prompt to generate AI Reflection
            when (uiState) {
              is ReflectionUiState.Loading -> {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp)
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                      text = "Generating Deep Reflection with Gemini...",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }

              is ReflectionUiState.Error -> {
                val errorMsg = (uiState as ReflectionUiState.Error).message
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    .padding(14.dp)
                ) {
                  Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(
                    onClick = {
                      uiState = ReflectionUiState.Loading
                      scope.launch {
                        uiState = reflectionService.fetchReflection(dateStr, verse, forceRefresh = true)
                      }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.End)
                  ) {
                    Text("Retry", fontSize = 12.sp)
                  }
                }
              }

              else -> {
                // Static Default with button to get Gemini AI reflection
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp)
                ) {
                  Column {
                    Text(
                      text = verse.reflectionPrompt,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                      onClick = {
                        uiState = ReflectionUiState.Loading
                        scope.launch {
                          uiState = reflectionService.fetchReflection(dateStr, verse, forceRefresh = false)
                        }
                      },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                      ),
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_reflection_btn")
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.AutoAwesome,
                          contentDescription = null,
                          tint = StarGold,
                          modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "Generate Deep Gemini Reflection & Du'a",
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReflectionDetailBody(
  reflection: VerseReflectionEntity,
  onRegenerate: () -> Unit,
  isLoading: Boolean
) {
  val context = LocalContext.current

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Theme Pill
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .background(
          Brush.horizontalGradient(
            listOf(
              MaterialTheme.colorScheme.primaryContainer,
              MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
          )
        )
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Spiritual Theme: ${reflection.spiritualTheme}",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    // Contemplation Narrative (Tadabbur)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(14.dp)
    ) {
      Column {
        Text(
          text = "Spiritual Contemplation (Tadabbur)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = reflection.contemplationNarrative,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 21.sp
        )
      }
    }

    // Daily Action Steps
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(14.dp)
    ) {
      Column {
        Text(
          text = "Practical Actions for Today",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = reflection.dailyActionSteps,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 20.sp
        )
      }
    }

    // Personalized Supplication (Du'a) Card
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
      border = androidx.compose.foundation.BorderStroke(1.dp, RadiantGold.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Supplication (Du'a)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Daily Dua", reflection.supplicationDua)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Du'a copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Dua",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "\"${reflection.supplicationDua}\"",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          fontStyle = FontStyle.Italic,
          lineHeight = 20.sp
        )
      }
    }

    // Refresh / Regenerate Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      OutlinedButton(
        onClick = onRegenerate,
        enabled = !isLoading,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("regenerate_reflection_btn")
      ) {
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Generating...", fontSize = 11.sp)
        } else {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Regenerate Reflection", fontSize = 11.sp)
        }
      }
    }
  }
}
