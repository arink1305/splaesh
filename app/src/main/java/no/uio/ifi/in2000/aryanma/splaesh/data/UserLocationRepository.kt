package no.uio.ifi.in2000.aryanma.splaesh.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class UserLocationRepository(context: Context) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentPoint(): Point? = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (!continuation.isCompleted) {
                    continuation.resume(
                        location?.let { Point.fromLngLat(it.longitude, it.latitude) }
                    )
                }
            }
            .addOnFailureListener {
                if (!continuation.isCompleted) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastKnownLocation ->
                            if (!continuation.isCompleted) {
                                continuation.resume(
                                    lastKnownLocation?.let {
                                        Point.fromLngLat(it.longitude, it.latitude)
                                    }
                                )
                            }
                        }
                        .addOnFailureListener {
                            if (!continuation.isCompleted) {
                                continuation.resume(null)
                            }
                        }
                }
            }
    }
}
