package com.example.notification

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.components.FloatingAlertPayload
import com.example.ui.components.FloatingCenterAlertCard
import com.example.ui.theme.NoorTheme

/**
 * Lightweight system-overlay Service that presents a centered floating modal popup card
 * over the user's screen outside Noor (never full screen) when "Appear on Top" is granted.
 */
class FloatingAlertOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

  private val lifecycleRegistry = LifecycleRegistry(this)
  override val lifecycle: Lifecycle get() = lifecycleRegistry

  override val viewModelStore: ViewModelStore = ViewModelStore()

  private val savedStateRegistryController = SavedStateRegistryController.create(this)
  override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

  private var windowManager: WindowManager? = null
  private var overlayView: ComposeView? = null
  private val autoDismissHandler = Handler(Looper.getMainLooper())
  private val autoDismissRunnable = Runnable { stopSelf() }

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performRestore(null)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
      stopSelf()
      return START_NOT_STICKY
    }

    val payload = FloatingAlertPayload.fromIntent(intent)
    if (payload == null) {
      stopSelf()
      return START_NOT_STICKY
    }

    showOrUpdateOverlay(payload)
    autoDismissHandler.removeCallbacks(autoDismissRunnable)
    autoDismissHandler.postDelayed(autoDismissRunnable, AUTO_DISMISS_DELAY_MS)
    return START_NOT_STICKY
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun showOrUpdateOverlay(payload: FloatingAlertPayload) {
    removeOverlayView()

    val wm = windowManager ?: return
    val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }

    val params = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      windowType,
      WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
        WindowManager.LayoutParams.FLAG_DIM_BEHIND or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.CENTER
      dimAmount = 0.45f
    }

    val composeView = ComposeView(this).apply {
      setViewTreeLifecycleOwner(this@FloatingAlertOverlayService)
      setViewTreeViewModelStoreOwner(this@FloatingAlertOverlayService)
      setViewTreeSavedStateRegistryOwner(this@FloatingAlertOverlayService)
      setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
          stopSelf()
          true
        } else {
          false
        }
      }
      setContent {
        NoorTheme {
          Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
          ) {
            FloatingCenterAlertCard(
              payload = payload,
              onPrimaryAction = {
                FloatingAlertController.handlePrimaryAction(this@FloatingAlertOverlayService, payload)
                stopSelf()
              },
              onStopAudio = if (payload.hasStopAudio) {
                {
                  FloatingAlertController.stopActiveAudio(this@FloatingAlertOverlayService)
                  stopSelf()
                }
              } else null,
              onDismiss = {
                stopSelf()
              }
            )
          }
        }
      }
    }

    try {
      wm.addView(composeView, params)
      overlayView = composeView
    } catch (e: Exception) {
      Log.w(TAG, "Unable to attach floating center overlay window", e)
      stopSelf()
    }
  }

  private fun removeOverlayView() {
    autoDismissHandler.removeCallbacks(autoDismissRunnable)
    val view = overlayView ?: return
    try {
      windowManager?.removeViewImmediate(view)
    } catch (_: Exception) {
      try {
        windowManager?.removeView(view)
      } catch (_: Exception) {}
    }
    overlayView = null
  }

  override fun onDestroy() {
    removeOverlayView()
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    viewModelStore.clear()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    private const val TAG = "FloatingAlertOverlay"
    private const val AUTO_DISMISS_DELAY_MS = 45_000L
  }
}
