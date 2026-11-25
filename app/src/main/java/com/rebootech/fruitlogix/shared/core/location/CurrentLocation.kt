package com.rebootech.fruitlogix.shared.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** A GPS position of the phone. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * Reads the phone's position with the platform LocationManager (no Play Services needed).
 * In the emulator the default position is Mountain View: set it in Extended controls → Location
 * before testing the geofence.
 */
object CurrentLocation {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** A fresh fix (up to [timeoutMs]), or the last known one; null without permission or signal. */
    @SuppressLint("MissingPermission")
    suspend fun get(context: Context, timeoutMs: Long = 10_000): GeoPoint? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        val fresh = provider?.let { withTimeoutOrNull(timeoutMs) { singleFix(context, manager, it) } }
        val location = fresh ?: listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        return location?.let { GeoPoint(it.latitude, it.longitude) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun singleFix(context: Context, manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            }
        }

}
