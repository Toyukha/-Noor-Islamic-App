package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.ai.configureAppCheck
import com.example.data.repository.NoorRepository
import com.example.notification.NotificationHelper
import com.example.ui.navigation.NoorApp
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.theme.NoorTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    configureAppCheck(this, intent)
    NotificationHelper.createNotificationChannels(this)
    enableEdgeToEdge()
    setContent {
      NoorTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppContent()
        }
      }
    }
  }
}

@Composable
fun AppContent() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  // Attempt silent auto-sign-in on app startup for multi-device sync continuity
  LaunchedEffect(Unit) {
    if (currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {
          currentUser = Firebase.auth.currentUser
        },
        onUnauthenticated = {
          // Keep showing AuthScreen
        },
        scope = scope
      )
    }
  }

  if (currentUser == null) {
    AuthScreen(
      onAuthSuccess = {
        currentUser = Firebase.auth.currentUser
      }
    )
  } else {
    val repository = remember(currentUser?.uid) {
      NoorRepository(context)
    }
    NoorApp(repository = repository)
  }
}
