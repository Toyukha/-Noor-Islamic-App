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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.StarGold

@Composable
fun SubscriptionPaywall(
  selectedCurrency: String,
  onCurrencyToggle: (String) -> Unit,
  onStartFreeTrial: () -> Unit,
  onOpenCheckout: (gateway: String) -> Unit,
  onBypassDemo: () -> Unit,
  isLoading: Boolean = false,
  modifier: Modifier = Modifier
) {
  val priceFormatted = if (selectedCurrency == "NGN") "₦1,000" else "$5.00"
  val periodText = if (selectedCurrency == "NGN") "1,000 Naira / month" else "5.00 USD / month"

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            EmeraldDark,
            Color(0xFF041810),
            Color(0xFF062015)
          )
        )
      )
      .padding(horizontal = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      Spacer(modifier = Modifier.height(36.dp))

      // Top Currency Switcher Pill
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White.copy(alpha = 0.12f))
          .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CurrencyPill(
          label = "₦ NGN (Nigeria)",
          isSelected = selectedCurrency == "NGN",
          onClick = { onCurrencyToggle("NGN") },
          tag = "currency_pill_ngn"
        )
        CurrencyPill(
          label = "$ USD (Global)",
          isSelected = selectedCurrency == "USD",
          onClick = { onCurrencyToggle("USD") },
          tag = "currency_pill_usd"
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Luminous App Brand Badge
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(RadiantGold.copy(alpha = 0.35f), Color.Transparent)
            )
          )
          .border(2.dp, RadiantGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Noor Premium",
          tint = RadiantGold,
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Noor Premium",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )

      Text(
        text = "Unlock Your Complete Spiritual Journey",
        style = MaterialTheme.typography.bodyMedium,
        color = RadiantGold,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Daily Prayer times, Fasting alerts, Quran milestones, streaks, and visual analytics.",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Pricing Hero Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, RadiantGold, RoundedCornerShape(24.dp))
          .testTag("pricing_hero_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = RadiantGold
          ) {
            Text(
              text = "7-DAY FREE TRIAL INCLUDED",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.Black,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = priceFormatted,
              style = MaterialTheme.typography.displaySmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = " / month",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
          }

          Text(
            text = "Auto-detected regional plan ($periodText)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Value Features List
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White.copy(alpha = 0.05f))
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        FeatureItem(
          icon = Icons.Default.Alarm,
          title = "Exact Prayer Alarms & 10m Pre-Warning",
          description = "Customizable Adhan notifications for Fajr, Dhuhr, Asr, Maghrib, and Isha."
        )
        FeatureItem(
          icon = Icons.Default.NightsStay,
          title = "Fasting (Sawm) Discipline",
          description = "Suhoor preparation countdown and sunset Iftar notification with du'a."
        )
        FeatureItem(
          icon = Icons.Default.MenuBook,
          title = "Daily Quran Recitation & Bookmarks",
          description = "Track daily read pages, Surah progress, and custom evening recitation alerts."
        )
        FeatureItem(
          icon = Icons.Default.Star,
          title = "Spiritual Badges & 7-Day Streaks",
          description = "Unlock Room SQLite milestone trophies as you maintain prayer discipline."
        )
        FeatureItem(
          icon = Icons.Default.BarChart,
          title = "Weekly Tri-Bar Visual Reports",
          description = "Interactive Native Compose charts comparing prayers, fasting, and Quran."
        )
        FeatureItem(
          icon = Icons.Default.CloudSync,
          title = "Multi-Device Real-Time Cloud Sync",
          description = "Your habits and prayer logs safely synced with your Google account."
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    // Action CTA Buttons
    item {
      // 1. Primary Button: Start 7-Day Free Trial
      Button(
        onClick = onStartFreeTrial,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RadiantGold),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("start_free_trial_button")
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = Color.Black,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(24.dp)
          )
        } else {
          Text(
            text = "Start 7-Day Free Trial (₦0 Today)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Secondary Button: Subscribe Directly
      OutlinedButton(
        onClick = { onOpenCheckout("PAYSTACK") },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("subscribe_monthly_button")
      ) {
        Text(
          text = "Subscribe Now ($priceFormatted / mo)",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Instant Demo / Test Bypass Option
      TextButton(
        onClick = onBypassDemo,
        modifier = Modifier.testTag("bypass_demo_button")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = RadiantGold,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Enter Demo Mode (Instant Full-Access Preview)",
            fontSize = 12.sp,
            color = RadiantGold,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
private fun CurrencyPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  tag: String
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (isSelected) RadiantGold else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag(tag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Color.Black else Color.White
    )
  }
}

@Composable
private fun FeatureItem(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(RadiantGold.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = RadiantGold,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 11.sp,
        lineHeight = 15.sp
      )
    }
  }
}
