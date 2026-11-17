package com.example.notification

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.FloatingAlertPayload
import com.example.ui.components.FloatingCenterAlertCard
import com.example.ui.theme.NoorTheme

/**
 * Translucent centered floating modal popup Activity used when tapping notifications
 * or when system overlay permission is unavailable. Never occupies full-screen content.
 */
class FloatingAlertActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      val km = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
      km?.requestDismissKeyguard(this, null)
    }

    val payload = FloatingAlertPayload.fromIntent(intent)
    if (payload == null) {
      finish()
      return
    }

    setContent {
      NoorTheme {
        val backdropInteraction = remember { MutableInteractionSource() }
        val cardInteraction = remember { MutableInteractionSource() }
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
              interactionSource = backdropInteraction,
              indication = null
            ) { finish() }
            .padding(horizontal = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier.clickable(
              interactionSource = cardInteraction,
              indication = null
            ) { /* Consume taps inside the card */ }
          ) {
            FloatingCenterAlertCard(
              payload = payload,
              onPrimaryAction = {
                FloatingAlertController.handlePrimaryAction(this@FloatingAlertActivity, payload)
                finish()
              },
              onStopAudio = if (payload.hasStopAudio) {
                {
                  FloatingAlertController.stopActiveAudio(this@FloatingAlertActivity)
                  finish()
                }
              } else null,
              onDismiss = {
                finish()
              }
            )
          }
        }
      }
    }
  }
}
