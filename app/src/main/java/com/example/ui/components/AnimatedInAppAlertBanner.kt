package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RadiantGold

enum class AlertType {
  SOLAH_PRE_WARNING,
  SUHOOR_FASTING,
  IFTAR_FASTING,
  QURAN_RECITE,
  SOLAH_EXACT,
  ADHKAR_REMINDER,
  DAILY_VERSE,
  SURAH_RECITATION
}

data class InAppAlert(
  val id: String,
  val type: AlertType,
  val title: String,
  val message: String,
  val actionLabel: String,
  val icon: ImageVector = Icons.Default.AccessTime,
  val categoryLabel: String = "",
  val arabicSubtitle: String = "",
  val prayerOrdinal: Int = -1,
  val hasStopAudio: Boolean = false
)

data class FloatingAlertPayload(
  val id: String,
  val type: AlertType,
  val categoryLabel: String,
  val title: String,
  val arabicSubtitle: String = "",
  val message: String,
  val actionLabel: String,
  val targetTab: String = "TODAY",
  val prayerOrdinal: Int = -1,
  val hasStopAudio: Boolean = false,
  val openQuranFullPage: Boolean = false
) {
  fun resolveIcon(): ImageVector = when (type) {
    AlertType.SOLAH_PRE_WARNING -> Icons.Default.AccessTime
    AlertType.SOLAH_EXACT -> Icons.Default.NotificationsActive
    AlertType.SUHOOR_FASTING -> Icons.Default.NightsStay
    AlertType.IFTAR_FASTING -> Icons.Default.WbSunny
    AlertType.QURAN_RECITE, AlertType.SURAH_RECITATION -> Icons.AutoMirrored.Filled.MenuBook
    AlertType.ADHKAR_REMINDER, AlertType.DAILY_VERSE -> Icons.Default.AutoAwesome
  }

  fun putIntoIntent(intent: Intent): Intent {
    val payloadType = this.type.name
    return intent.apply {
      putExtra(EXTRA_ALERT_ID, id)
      putExtra(EXTRA_ALERT_TYPE, payloadType)
      putExtra(EXTRA_CATEGORY_LABEL, categoryLabel)
      putExtra(EXTRA_TITLE, title)
      putExtra(EXTRA_ARABIC_SUBTITLE, arabicSubtitle)
      putExtra(EXTRA_MESSAGE, message)
      putExtra(EXTRA_ACTION_LABEL, actionLabel)
      putExtra(EXTRA_TARGET_TAB, targetTab)
      putExtra(EXTRA_PRAYER_ORDINAL, prayerOrdinal)
      putExtra(EXTRA_HAS_STOP_AUDIO, hasStopAudio)
      putExtra(EXTRA_OPEN_QURAN_FULL_PAGE, openQuranFullPage)
    }
  }

  companion object {
    const val EXTRA_ALERT_ID = "extra_floating_alert_id"
    const val EXTRA_ALERT_TYPE = "extra_floating_alert_type"
    const val EXTRA_CATEGORY_LABEL = "extra_floating_category"
    const val EXTRA_TITLE = "extra_floating_title"
    const val EXTRA_ARABIC_SUBTITLE = "extra_floating_arabic"
    const val EXTRA_MESSAGE = "extra_floating_message"
    const val EXTRA_ACTION_LABEL = "extra_floating_action_label"
    const val EXTRA_TARGET_TAB = "extra_floating_target_tab"
    const val EXTRA_PRAYER_ORDINAL = "extra_floating_prayer_ordinal"
    const val EXTRA_HAS_STOP_AUDIO = "extra_floating_has_stop_audio"
    const val EXTRA_OPEN_QURAN_FULL_PAGE = "extra_floating_open_quran_full_page"

    fun fromIntent(intent: Intent?): FloatingAlertPayload? {
      if (intent == null) return null
      val title = intent.getStringExtra(EXTRA_TITLE) ?: return null
      val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
      val id = intent.getStringExtra(EXTRA_ALERT_ID) ?: "ALERT_${System.currentTimeMillis()}"
      val typeName = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: AlertType.SOLAH_EXACT.name
      val parsedType = AlertType.values().firstOrNull { it.name == typeName } ?: AlertType.SOLAH_EXACT
      return FloatingAlertPayload(
        id = id,
        type = parsedType,
        categoryLabel = intent.getStringExtra(EXTRA_CATEGORY_LABEL) ?: "NOOR REMINDER",
        title = title,
        arabicSubtitle = intent.getStringExtra(EXTRA_ARABIC_SUBTITLE) ?: "",
        message = message,
        actionLabel = intent.getStringExtra(EXTRA_ACTION_LABEL) ?: "Open Noor",
        targetTab = intent.getStringExtra(EXTRA_TARGET_TAB) ?: "TODAY",
        prayerOrdinal = intent.getIntExtra(EXTRA_PRAYER_ORDINAL, -1),
        hasStopAudio = intent.getBooleanExtra(EXTRA_HAS_STOP_AUDIO, false),
        openQuranFullPage = intent.getBooleanExtra(EXTRA_OPEN_QURAN_FULL_PAGE, false)
      )
    }

    fun fromInAppAlert(alert: InAppAlert): FloatingAlertPayload {
      val defaultCategory = when (alert.type) {
        AlertType.SOLAH_PRE_WARNING -> "UPCOMING SOLAH"
        AlertType.SOLAH_EXACT -> "ADHAN • PRAYER TIME"
        AlertType.SUHOOR_FASTING -> "FAJR SUHOOR"
        AlertType.IFTAR_FASTING -> "MAGHRIB IFTAR"
        AlertType.QURAN_RECITE -> "QURAN TILAWAH"
        AlertType.ADHKAR_REMINDER -> "ADHKAR REMEMBRANCE"
        AlertType.DAILY_VERSE -> "DAILY TADABBUR"
        AlertType.SURAH_RECITATION -> "SUNNAH RECITATION"
      }
      val defaultTab = when (alert.type) {
        AlertType.QURAN_RECITE, AlertType.SURAH_RECITATION -> "QURAN"
        AlertType.DAILY_VERSE -> "TADABBUR"
        AlertType.ADHKAR_REMINDER -> "TRACKER"
        else -> "TODAY"
      }
      return FloatingAlertPayload(
        id = alert.id,
        type = alert.type,
        categoryLabel = alert.categoryLabel.ifBlank { defaultCategory },
        title = alert.title,
        arabicSubtitle = alert.arabicSubtitle,
        message = alert.message,
        actionLabel = alert.actionLabel,
        targetTab = defaultTab,
        prayerOrdinal = alert.prayerOrdinal,
        hasStopAudio = alert.hasStopAudio,
        openQuranFullPage = alert.type == AlertType.QURAN_RECITE
      )
    }
  }
}

/**
 * Centered floating on-screen popup card shared between inside-app dialogs
 * and outside-app system overlay / translucent modal windows.
 */
@Composable
fun FloatingCenterAlertCard(
  payload: FloatingAlertPayload,
  onPrimaryAction: () -> Unit,
  onDismiss: () -> Unit,
  onStopAudio: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 10.dp,
    shadowElevation = 18.dp,
    border = BorderStroke(1.5.dp, RadiantGold.copy(alpha = 0.85f)),
    modifier = modifier
      .widthIn(min = 280.dp, max = 352.dp)
      .fillMaxWidth()
      .testTag("in_app_alert_banner")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Emerald & Gold Header Strip with subtle geometric watermark
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(EmeraldDark, EmeraldGreen)
            )
          )
          .islamicGeometricWatermark(tint = RadiantGold, alpha = 0.16f)
          .padding(horizontal = 18.dp, vertical = 14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(RadiantGold.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = payload.resolveIcon(),
              contentDescription = null,
              tint = RadiantGold,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = RadiantGold.copy(alpha = 0.22f)
            ) {
              Text(
                text = payload.categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = RadiantGold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
            if (payload.arabicSubtitle.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = payload.arabicSubtitle,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(40.dp)
              .testTag("banner_dismiss_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss notification",
              tint = Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      GoldHairlineDivider()

      // Main Body Copy & Actions
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp)
      ) {
        Text(
          text = payload.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = payload.message,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onPrimaryAction,
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldDark,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("banner_action_btn")
        ) {
          Text(
            text = payload.actionLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (payload.hasStopAudio && onStopAudio != null) {
            Arrangement.SpaceBetween
          } else {
            Arrangement.Center
          },
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (payload.hasStopAudio && onStopAudio != null) {
            OutlinedButton(
              onClick = onStopAudio,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.VolumeOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Silence Audio",
                fontSize = 12.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          TextButton(
            onClick = onDismiss,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "Dismiss",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.outline,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }
    }
  }
}

/**
 * In-app centered modal popup Dialog replacing the old docked top banner.
 */
@Composable
fun AnimatedInAppAlertBanner(
  alert: InAppAlert?,
  onActionClick: (InAppAlert) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (alert != null) {
    val payload = FloatingAlertPayload.fromInAppAlert(alert)
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
      )
    ) {
      Box(
        modifier = modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
      ) {
        FloatingCenterAlertCard(
          payload = payload,
          onPrimaryAction = { onActionClick(alert) },
          onDismiss = onDismiss
        )
      }
    }
  }
}
