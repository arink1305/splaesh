package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

@SuppressLint("MissingPermission")
@Composable
fun UserLocationPuck(
    onUserLocationResolved: ((longitude: Double, latitude: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    var hasCenteredOnUser by rememberSaveable {
        mutableStateOf(false)
    }
    var hasRequestedPermission by rememberSaveable {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission, hasRequestedPermission) {
        if (!hasLocationPermission && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableMapEffect(hasLocationPermission) { mapView ->
        val locationPlugin = mapView.location
        locationPlugin.locationPuck = createDefault2DPuck(withBearing = true)
        locationPlugin.puckBearing = PuckBearing.HEADING
        locationPlugin.puckBearingEnabled = hasLocationPermission
        locationPlugin.enabled = hasLocationPermission

        if (hasLocationPermission && !hasCenteredOnUser) {
            context.centerMapOnUser(
                onLocationResolved = { longitude, latitude ->
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(longitude, latitude))
                            .zoom(13.5)
                            .build()
                    )
                    onUserLocationResolved?.invoke(longitude, latitude)
                    hasCenteredOnUser = true
                }
            )
        }

        onDispose {
            locationPlugin.enabled = false
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun UserLocationButton(
    modifier: Modifier = Modifier,
    isDarkStyle: Boolean,
    bottomPadding: Dp,
    onLocationResolved: (longitude: Double, latitude: Double) -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    val containerColor = if (isDarkStyle) {
        androidx.compose.ui.graphics.Color(0xD9232A32)
    } else {
        androidx.compose.ui.graphics.Color(0xCCFFFFFF)
    }
    val borderColor = if (isDarkStyle) {
        androidx.compose.ui.graphics.Color(0x335E6A75)
    } else {
        androidx.compose.ui.graphics.Color(0x1F101828)
    }
    val iconTint = if (isDarkStyle) {
        androidx.compose.ui.graphics.Color(0xFF78D7E2)
    } else {
        androidx.compose.ui.graphics.Color(0xFF0E7490)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            context.centerMapOnUser(onLocationResolved)
        }
    }

    Surface(
        modifier = modifier
            .padding(start = 16.dp, bottom = bottomPadding + 16.dp)
            .size(42.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (hasLocationPermission) {
                    context.centerMapOnUser(onLocationResolved)
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Min posisjon",
                tint = iconTint
            )
        }
    }
}

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun Context.centerMapOnUser(
    onLocationResolved: (longitude: Double, latitude: Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { currentLocation ->
            if (currentLocation != null) {
                onLocationResolved(currentLocation.longitude, currentLocation.latitude)
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastKnownLocation ->
                        if (lastKnownLocation != null) {
                            onLocationResolved(lastKnownLocation.longitude, lastKnownLocation.latitude)
                        }
                    }
            }
        }
}

private fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
