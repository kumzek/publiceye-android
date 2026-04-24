package com.publiceye.app.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * One-shot location + reverse-geocoding helper.
 *
 * We intentionally do NOT use continuous location updates — the report flow only needs the
 * user's current position at the moment they tag the report. CurrentLocationRequest with
 * HIGH_ACCURACY returns a fresh fix (or null if GPS is off).
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Get the device's current location. Returns null if:
     *   - permission is not granted
     *   - location services are disabled
     *   - the request timed out
     */
    @SuppressLint("MissingPermission") // we check permission above
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(30_000) // accept a cached fix up to 30s old
            .build()

        return try {
            fusedClient.getCurrentLocation(request, null).await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reverse-geocode to a human-readable address. Falls back to "lat, lng" on error.
     *
     * Uses the async Geocoder API on Android 13+ (Tiramisu) and blocking API otherwise.
     * Always runs off the main thread.
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String =
        withContext(Dispatchers.IO) {
            val fallback = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
            if (!Geocoder.isPresent()) return@withContext fallback

            val geocoder = Geocoder(context, Locale.getDefault())

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            cont.resume(formatAddress(addresses.firstOrNull()) ?: fallback)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    formatAddress(addresses?.firstOrNull()) ?: fallback
                }
            } catch (e: Exception) {
                fallback
            }
        }

    private fun formatAddress(address: android.location.Address?): String? {
        if (address == null) return null
        val parts = buildList {
            // Street or feature name (e.g. "MG Road")
            address.thoroughfare?.let { add(it) }
            address.subLocality?.let { add(it) }
            address.locality?.let { add(it) }
        }.distinct()
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }
}
