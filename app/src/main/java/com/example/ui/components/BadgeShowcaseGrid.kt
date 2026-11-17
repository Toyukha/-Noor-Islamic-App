package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BadgeEntity
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun BadgeShowcaseGrid(
  badges: List<BadgeEntity>,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("ALL") }
  var selectedBadgeForDetail by remember { mutableStateOf<BadgeEntity?>(null) }

  val categories = listOf("ALL", "PRAYER", "FASTING", "QURAN", "STREAK")

  val filteredBadges = remember(badges, selectedCategory) {
    if (selectedCategory == "ALL") badges else badges.filter { it.category == selectedCategory }
  }

  val unlockedCount = remember(badges) { badges.count { it.isUnlocked } }

  Column(modifier = modifier.fillMaxWidth()) {
    // Header with total unlocked counter
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Spiritual Achievements",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$unlockedCount of ${badges.size} Milestones Unlocked",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Trophy Pill
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = RadiantGold.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RadiantGold.copy(alpha = 0.6f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.MilitaryTech,
            contentDescription = null,
            tint = RadiantGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$unlockedCount Unlocked",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(categories) { cat ->
        val isSelected = selectedCategory == cat
        FilterChip(
          selected = isSelected,
          onClick = { selectedCategory = cat },
          label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2-Column Grid Cards
    val chunked = filteredBadges.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      chunked.forEach { rowBadges ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          rowBadges.forEach { badge ->
            Box(modifier = Modifier.weight(1f)) {
              BadgeCardItem(
                badge = badge,
                onClick = { selectedBadgeForDetail = badge }
              )
            }
          }
          if (rowBadges.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    // Detail Dialog
    if (selectedBadgeForDetail != null) {
      val badge = selectedBadgeForDetail!!
      AlertDialog(
        onDismissRequest = { selectedBadgeForDetail = null },
        icon = {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (badge.isUnlocked) RadiantGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getBadgeIcon(badge.iconName),
              contentDescription = null,
              tint = if (badge.isUnlocked) RadiantGold else MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(32.dp)
            )
          }
        },
        title = {
          Text(
            text = badge.title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        },
        text = {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = badge.description,
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (badge.isUnlocked) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = 4.dp)
              ) {
                Text(
                  text = "✓ Milestone Attained",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            } else {
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                LinearProgressIndicator(
                  progress = { badge.progressPercent },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                  color = MaterialTheme.colorScheme.primary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant,
                  strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Progress: ${badge.currentProgress} / ${badge.targetRequirement} (${(badge.progressPercent * 100).toInt()}%)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.outline
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { selectedBadgeForDetail = null }) {
            Text("Close")
          }
        }
      )
    }
  }
}

@Composable
private fun BadgeCardItem(
  badge: BadgeEntity,
  onClick: () -> Unit
) {
  val isUnlocked = badge.isUnlocked
  val cardBorder = if (isUnlocked) {
    androidx.compose.foundation.BorderStroke(1.5.dp, RadiantGold.copy(alpha = 0.8f))
  } else {
    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ),
    border = cardBorder,
    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("badge_card_${badge.badgeKey}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Icon Circle
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              if (isUnlocked) {
                Brush.radialGradient(listOf(StarGold.copy(alpha = 0.3f), RadiantGold.copy(alpha = 0.1f)))
              } else {
                Brush.radialGradient(listOf(Color.LightGray.copy(alpha = 0.2f), Color.Transparent))
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = getBadgeIcon(badge.iconName),
            contentDescription = null,
            tint = if (isUnlocked) RadiantGold else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
          )
        }

        if (isUnlocked) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(RadiantGold),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Check, contentDescription = "Unlocked", tint = Color.Black, modifier = Modifier.size(12.dp))
          }
        } else {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = badge.title,
        style = MaterialTheme.typography.titleSmall,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = badge.description,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 15.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Progress bar if locked
      if (!isUnlocked) {
        Column {
          LinearProgressIndicator(
            progress = { badge.progressPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${badge.currentProgress}/${badge.targetRequirement}",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline
          )
        }
      } else {
        Text(
          text = "Unlocked",
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = RadiantGold
        )
      }
    }
  }
}

private fun getBadgeIcon(iconName: String): ImageVector {
  return when (iconName) {
    "military_tech" -> Icons.Default.MilitaryTech
    "auto_awesome" -> Icons.Default.AutoAwesome
    "menu_book" -> Icons.Default.MenuBook
    "bookmark" -> Icons.Default.Bookmark
    "nights_stay" -> Icons.Default.NightsStay
    "wb_sunny" -> Icons.Default.WbSunny
    "brightness_low" -> Icons.Default.BrightnessLow
    "check_circle" -> Icons.Default.CheckCircle
    else -> Icons.Default.MilitaryTech
  }
}
