package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeDefinition
import com.example.data.model.PredefinedBadges
import com.example.data.model.UnlockedBadge
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun BadgesScreen(
  currentStreak: Int,
  longestStreak: Int,
  unlockedBadges: List<UnlockedBadge>,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }
  var selectedBadgeForDetail by remember { mutableStateOf<Pair<BadgeDefinition, Boolean>?>(null) }

  val unlockedKeys = remember(unlockedBadges) {
    unlockedBadges.map { it.badgeKey }.toSet()
  }

  val filteredBadges = remember(selectedCategory) {
    if (selectedCategory == null) {
      PredefinedBadges.ALL_BADGES
    } else {
      PredefinedBadges.ALL_BADGES.filter { it.category == selectedCategory }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Streak Flame Card
      StreakFlameHeroCard(
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        unlockedCount = unlockedBadges.size,
        totalBadges = PredefinedBadges.ALL_BADGES.size
      )
    }

    item {
      // Category Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { selectedCategory = null },
          label = { Text("All", fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
          )
        )
        BadgeCategory.values().forEach { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    item {
      Text(
        text = "Milestones & Spiritual Badges",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // List of Badges
    items(filteredBadges.size) { index ->
      val badge = filteredBadges[index]
      val isUnlocked = unlockedKeys.contains(badge.key)

      BadgeItemCard(
        badge = badge,
        isUnlocked = isUnlocked,
        onClick = {
          selectedBadgeForDetail = Pair(badge, isUnlocked)
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Detail Dialog
  selectedBadgeForDetail?.let { (badge, isUnlocked) ->
    AlertDialog(
      onDismissRequest = { selectedBadgeForDetail = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = badge.iconEmoji, fontSize = 28.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = badge.title, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = badge.description,
            style = MaterialTheme.typography.bodyMedium
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Requirement: ${badge.targetRequirement}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isUnlocked) "Status: Unlocked • Saved to Cloud" else "Status: Locked • Keep practicing to unlock!",
            style = MaterialTheme.typography.bodySmall,
            color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedBadgeForDetail = null }) {
          Text("Close", color = MaterialTheme.colorScheme.primary)
        }
      }
    )
  }
}

@Composable
fun StreakFlameHeroCard(
  currentStreak: Int,
  longestStreak: Int,
  unlockedCount: Int,
  totalBadges: Int
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    modifier = Modifier.fillMaxWidth().testTag("streak_flame_card")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.primaryContainer
            )
          )
        )
        .padding(22.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🔥", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SPIRITUAL STREAK",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = StarGold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "$currentStreak Days",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )

          Text(
            text = "Personal Best: $longestStreak Days",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f)
          )
        }

        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$unlockedCount/$totalBadges",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = StarGold
            )
            Text(
              text = "BADGES",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@Composable
fun BadgeItemCard(
  badge: BadgeDefinition,
  isUnlocked: Boolean,
  onClick: () -> Unit
) {
  val borderModifier = if (isUnlocked) {
    Modifier.border(1.5.dp, RadiantGold, RoundedCornerShape(16.dp))
  } else {
    Modifier
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    tonalElevation = if (isUnlocked) 3.dp else 0.dp,
    modifier = Modifier
      .fillMaxWidth()
      .then(borderModifier)
      .clickable(onClick = onClick)
      .testTag("badge_card_${badge.key.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(
            if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isUnlocked) {
          Text(text = badge.iconEmoji, fontSize = 24.sp)
        } else {
          Icon(Icons.Default.Lock, contentDescription = "Locked Badge", tint = MaterialTheme.colorScheme.outline)
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = badge.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
          )
          if (isUnlocked) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.Star, contentDescription = "Unlocked", tint = RadiantGold, modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = badge.description,
          style = MaterialTheme.typography.bodySmall,
          color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = badge.targetRequirement,
          style = MaterialTheme.typography.labelSmall,
          color = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}
