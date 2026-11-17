package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.data.model.DailyVerse
import com.example.ui.components.GoldHairlineDivider
import com.example.ui.components.islamicGeometricWatermark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TadabburScreen(
  verse: DailyVerse,
  dateStr: String = "",
  onOpenVerseInQuran: (DailyVerse) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val effectiveDate = remember(dateStr) {
    if (dateStr.isNotBlank()) dateStr else com.example.ui.viewmodel.getInitialTodayDate()
  }
  val journalPrefs = remember {
    context.getSharedPreferences("noor_journal_prefs", Context.MODE_PRIVATE)
  }
  var userJournalNote by remember(effectiveDate) {
    mutableStateOf(journalPrefs.getString("note_$effectiveDate", "") ?: "")
  }
  var isNoteSaved by remember(effectiveDate) {
    mutableStateOf(!journalPrefs.getString("note_$effectiveDate", "").isNullOrBlank())
  }
  var showShareModal by remember { mutableStateOf(false) }
  var includeJournalInShare by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))

      // Header Banner
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, RadiantGold.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
          .testTag("tadabbur_hero_banner")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .islamicGeometricWatermark(tint = RadiantGold, alpha = 0.14f)
            .padding(18.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(RadiantGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = RadiantGold,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Tadabbur — Verse Contemplation",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Linguistic Nuance • Soul Reflection • Action",
                  style = MaterialTheme.typography.bodySmall,
                  color = RadiantGold,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            GoldHairlineDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "\"كِتَابٌ أَنزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِّيَدَّبَّرُوا آيَاتِهِ\"",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
              textAlign = TextAlign.Right,
              modifier = Modifier.fillMaxWidth(),
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "\"A blessed Book We have revealed to you, that they might reflect upon its verses.\" (38:29)",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // Daily Verse Arabic Display Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("tadabbur_verse_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val verseRef = "${verse.surahName} ${verse.surahNumber}:${verse.ayahNumber}"
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = RadiantGold.copy(alpha = 0.15f)
            ) {
              Text(
                text = verseRef,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RadiantGold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            // Share & Copy Actions
            Row {
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Quran Verse", "${verse.arabicText}\n\n\"${verse.englishTranslation}\"\n($verseRef)")
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Ayah copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy",
                  tint = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.size(18.dp)
                )
              }

              IconButton(
                onClick = { showShareModal = true },
                modifier = Modifier.size(34.dp).testTag("verse_share_card_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Pure Arabic Calligraphy
          Text(
            text = verse.arabicText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Right,
            lineHeight = 36.sp,
            fontSize = 22.sp,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Translation
          Text(
            text = "\"${verse.englishTranslation}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedButton(
            onClick = { onOpenVerseInQuran(verse) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_verse_fullscreen_quran_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.MenuBook,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Read Surah in Full-Screen Quran",
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // 1. Heart & Soul Pillar (Spiritual Core)
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth().testTag("pillar_heart_soul")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = Color(0xFFE53935),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Heart & Soul: ${verse.surahName}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = verse.reflectionPrompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }
    }

    // 2. Linguistic Gem Pillar
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth().testTag("pillar_linguistic_gem")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = RadiantGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Linguistic & Rhetoric Gem",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Notice the divine precision in the phrasing. The word structure creates an immediate reassurance in the heart of the believer, emphasizing certainty over probability and anchoring faith in Allah's boundless mercy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }
    }

    // 3. Living the Ayah Today (Action Steps)
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth().testTag("pillar_action_steps")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bookmark,
              contentDescription = null,
              tint = EmeraldGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Living the Ayah Today",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Take 3 minutes after your next prayer to make conscious remembrance (Dhikr), internalize the promise of this ayah, and perform one hidden act of kindness for someone today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        }
      }
    }

    // 4. Personal Tadabbur Journaling Box
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth().testTag("personal_journal_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "My Personal Reflection Journal",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "What personal lesson or commitment does this verse inspire in your heart today?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = userJournalNote,
            onValueChange = {
              userJournalNote = it
              isNoteSaved = false
            },
            placeholder = { Text("Write your personal contemplation...", fontSize = 13.sp) },
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag("tadabbur_note_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              journalPrefs.edit().putString("note_$effectiveDate", userJournalNote).apply()
              isNoteSaved = true
              Toast.makeText(context, "Reflection saved for today", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNoteSaved) EmeraldGreen else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("save_tadabbur_note_btn")
          ) {
            Icon(
              imageVector = if (isNoteSaved) Icons.Default.Check else Icons.Default.Bookmark,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isNoteSaved) "Reflection Saved ✓" else "Save Reflection")
          }
        }
      }
    }

    // 5. Dedicated Social Share Card (WhatsApp Status, Instagram, Telegram)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.dp,
            androidx.compose.ui.graphics.Brush.horizontalGradient(
              listOf(RadiantGold.copy(alpha = 0.6f), EmeraldGreen.copy(alpha = 0.6f))
            ),
            RoundedCornerShape(20.dp)
          )
          .testTag("tadabbur_social_share_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                  .background(RadiantGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = null,
                  tint = RadiantGold,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Share Today's Tadabbur",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "WhatsApp Status • Stories • Friends",
                  style = MaterialTheme.typography.bodySmall,
                  color = RadiantGold,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Spread Islamic benefit and invite others to daily Quran contemplation. Generates a beautifully formatted viral message with reflection insights and Play Store link.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showShareModal = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = RadiantGold,
              contentColor = EmeraldDark
            ),
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("open_share_card_btn")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Social Share Card", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  if (showShareModal) {
    TadabburShareBottomSheet(
      verse = verse,
      userNote = userJournalNote,
      includeNote = includeJournalInShare,
      onToggleIncludeNote = { includeJournalInShare = it },
      onDismiss = { showShareModal = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TadabburShareBottomSheet(
  verse: DailyVerse,
  userNote: String,
  includeNote: Boolean,
  onToggleIncludeNote: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val shareText = remember(verse, userNote, includeNote) {
    buildTadabburShareMessage(verse, userNote, includeNote, context.packageName)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("tadabbur_share_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Share Tadabbur Card",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "Social Media Status & WhatsApp",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Toggle Options for Share
      if (userNote.isNotBlank()) {
        Text(
          text = "Choose Share Content:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = !includeNote,
            onClick = { onToggleIncludeNote(false) },
            label = { Text("Verse Only", fontSize = 11.sp, maxLines = 1, softWrap = false) },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = includeNote,
            onClick = { onToggleIncludeNote(true) },
            label = { Text("With My Note", fontSize = 11.sp, maxLines = 1, softWrap = false) },
            modifier = Modifier.weight(1f)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      // Live Formatted Text Preview Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Preview Message:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = shareText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 8,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            lineHeight = 18.sp,
            fontSize = 12.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Tadabbur Reflection", shareText))
            Toast.makeText(context, "Copied formatted reflection to clipboard! ✓", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(48.dp).testTag("copy_share_text_btn")
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Text", fontSize = 12.sp, maxLines = 1, softWrap = false)
        }

        Button(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, shareText)
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Tadabbur on Status / Social"))
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.weight(1f).height(48.dp).testTag("send_social_share_btn")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Share Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        }
      }
    }
  }
}

private fun buildTadabburShareMessage(
  verse: DailyVerse,
  userNote: String,
  includeNote: Boolean,
  packageName: String
): String {
  val sb = StringBuilder()
  sb.append("✨ Daily Quran Tadabbur • Surah ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})\n\n")
  sb.append("${verse.arabicText}\n\n")
  sb.append("\"${verse.englishTranslation}\"\n\n")
  sb.append("💡 Reflection Insight:\n${verse.reflectionPrompt}\n\n")
  if (includeNote && userNote.isNotBlank()) {
    sb.append("✍️ My Personal Reflection & Action:\n$userNote\n\n")
  }
  sb.append("───────────────────────\n")
  sb.append("📲 Reflect on the Holy Quran daily and build consistent prayer habits with Noor app.\n")
  sb.append("Download free on Google Play:\n")
  sb.append("https://play.google.com/store/apps/details?id=$packageName\n\n")
  sb.append("#Quran #Tadabbur #IslamicReminders #NoorApp")
  return sb.toString()
}
