package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.CityLocation
import com.example.data.model.GlobalCityDirectory
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean

object GpsLocationHelper {

  private const val LOCATION_TIMEOUT_MS = 10_000L

  fun hasLocationPermission(context: Context): Boolean {
    val coarse = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val fine = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return coarse || fine
  }

  @SuppressLint("MissingPermission")
  fun detectCurrentCity(
    context: Context,
    onResult: (CityLocation?) -> Unit
  ) {
    if (!hasLocationPermission(context)) {
      onResult(null)
      return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
      onResult(null)
      return
    }

    // Try last known location from GPS or Network provider
    val providers = listOf(
      LocationManager.GPS_PROVIDER,
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER
    )

    var bestLocation: Location? = null
    for (provider in providers) {
      try {
        val loc = locationManager.getLastKnownLocation(provider)
        if (loc != null) {
          if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
            bestLocation = loc
          }
        }
      } catch (_: Exception) {}
    }

    if (bestLocation != null) {
      onResult(mapLocationToCity(bestLocation))
      return
    }

    val mainHandler = Handler(Looper.getMainLooper())
    val finished = AtomicBoolean(false)

    val provider = when {
      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
      locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
      else -> null
    }

    if (provider == null) {
      onResult(null)
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val cancellationSignal = CancellationSignal()
      val timeoutRunnable = Runnable {
        if (finished.compareAndSet(false, true)) {
          cancellationSignal.cancel()
          onResult(null)
        }
      }
      mainHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS)

      try {
        locationManager.getCurrentLocation(
          provider,
          cancellationSignal,
          context.mainExecutor
        ) { location ->
          if (finished.compareAndSet(false, true)) {
            mainHandler.removeCallbacks(timeoutRunnable)
            onResult(location?.let { mapLocationToCity(it) })
          }
        }
      } catch (_: Exception) {
        if (finished.compareAndSet(false, true)) {
          mainHandler.removeCallbacks(timeoutRunnable)
          onResult(null)
        }
      }
      return
    }

    // Fallback for Android 10 and below with strict 10s timeout
    lateinit var listener: LocationListener
    val timeoutRunnable = Runnable {
      if (finished.compareAndSet(false, true)) {
        try {
          locationManager.removeUpdates(listener)
        } catch (_: Exception) {}
        onResult(null)
      }
    }

    listener = object : LocationListener {
      override fun onLocationChanged(location: Location) {
        if (finished.compareAndSet(false, true)) {
          mainHandler.removeCallbacks(timeoutRunnable)
          try {
            locationManager.removeUpdates(this)
          } catch (_: Exception) {}
          onResult(mapLocationToCity(location))
        }
      }
      @Deprecated("Deprecated in Java")
      override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
      override fun onProviderEnabled(provider: String) {}
      override fun onProviderDisabled(provider: String) {}
    }

    try {
      mainHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS)
      @Suppress("DEPRECATION")
      locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
    } catch (_: Exception) {
      if (finished.compareAndSet(false, true)) {
        mainHandler.removeCallbacks(timeoutRunnable)
        onResult(null)
      }
    }
  }

  private fun mapLocationToCity(location: Location): CityLocation {
    val lat = location.latitude
    val lng = location.longitude
    val nearest = GlobalCityDirectory.findNearestCity(lat, lng)

    val tz = TimeZone.getDefault()
    val offsetHours = tz.getOffset(System.currentTimeMillis()) / (1000.0 * 60.0 * 60.0)

    return CityLocation(
      name = nearest.name,
      country = nearest.country,
      latitude = lat,
      longitude = lng,
      timezoneOffset = offsetHours,
      region = nearest.region
    )
  }
}
