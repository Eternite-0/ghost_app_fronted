package com.example.myapplication.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context,
        timeoutMillis: Long = 3500L
    ): Location? {
        if (!hasLocationPermission(context)) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        val bestLastKnown = providers
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { location -> location.time }

        val freshEnough = bestLastKnown?.let { System.currentTimeMillis() - it.time <= 10 * 60 * 1000 } == true
        if (freshEnough) return bestLastKnown

        val requestProvider = when {
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
                LocationManager.NETWORK_PROVIDER
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
                LocationManager.GPS_PROVIDER
            else -> null
        } ?: return bestLastKnown

        val updated = withTimeoutOrNull(timeoutMillis) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (continuation.isActive) continuation.resume(location)
                        manager.removeUpdates(this)
                    }

                    override fun onProviderDisabled(provider: String) {
                        if (continuation.isActive) continuation.resume(null)
                        manager.removeUpdates(this)
                    }
                }

                manager.requestLocationUpdates(
                    requestProvider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    manager.removeUpdates(listener)
                }
            }
        }

        return updated ?: bestLastKnown
    }
}
