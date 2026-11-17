package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
  private var latestIntentState by mutableStateOf<Intent?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    latestIntentState = intent
    configureAppCheck(this, intent)
    NotificationHelper.createNotificationChannels(this)
    enableEdgeToEdge()
    setContent {
      NoorTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppContent(
            launchIntent = latestIntentState,
            onConsumeLaunchIntent = { latestIntentState = null }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    latestIntentState = intent
  }
}

@Composable
fun AppContent(
  launchIntent: Intent? = null,
  onConsumeLaunchIntent: () -> Unit = {}
) {
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

  // Automatic startup permission prompt for Location and Notifications
  val permissionsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    // Permissions handled gracefully
  }

  LaunchedEffect(Unit) {
    val neededPermissions = mutableListOf<String>()

    val hasFineLoc = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val hasCoarseLoc = androidx.core.content.ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    if (!hasFineLoc && !hasCoarseLoc) {
      neededPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
      neededPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      val hasNotification = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
      ) == android.content.pm.PackageManager.PERMISSION_GRANTED
      if (!hasNotification) {
        neededPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    if (neededPermissions.isNotEmpty()) {
      permissionsLauncher.launch(neededPermissions.toTypedArray())
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
    NoorApp(
      repository = repository,
      launchIntent = launchIntent,
      onConsumeLaunchIntent = onConsumeLaunchIntent
    )
  }
}
