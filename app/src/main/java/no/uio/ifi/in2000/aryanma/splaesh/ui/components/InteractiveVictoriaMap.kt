@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import no.uio.ifi.in2000.aryanma.splaesh.R
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning

private const val MEPS_HOURS = 60
private const val LONG_RANGE_STEP_HOURS = 6

private data class WmsLayerConfig(
    val layerId: String,
    val sourceId: String,
    val layerName: String,
    val opacity: Double
)

private fun resolveTempLayerConfig(selectedTimeIndex: Int): WmsLayerConfig {
    return if (selectedTimeIndex <= MEPS_HOURS) {
        WmsLayerConfig(
            layerId = "wms-temp-layer",
            sourceId = "wms-temp-source",
            layerName = "air_temperature_2m_meps_det_vdiv_2_5km_calculations",
            opacity = 0.5
        )
    } else {
        WmsLayerConfig(
            layerId = "wms-temp-layer",
            sourceId = "wms-temp-source",
            layerName = "air_temperature_2m_ec_sfc_3h_calculations",
            opacity = 0.5
        )
    }
}

private fun resolveRainLayerConfig(selectedTimeIndex: Int): WmsLayerConfig {
    return if (selectedTimeIndex <= MEPS_HOURS) {
        WmsLayerConfig(
            layerId = "wms-rain-layer",
            sourceId = "wms-rain-source",
            layerName = "precipitation_amount_1h_meps_det_vdiv_2_5km_calculations",
            opacity = 0.6
        )
    } else {
        WmsLayerConfig(
            layerId = "wms-rain-layer",
            sourceId = "wms-rain-source",
            layerName = "precipitation_amount_6h_ec_sfc_3h_calculations",
            opacity = 0.6
        )
    }
}

private fun resolveWindLayerConfig(selectedTimeIndex: Int): WmsLayerConfig {
    return if (selectedTimeIndex <= MEPS_HOURS) {
        WmsLayerConfig(
            layerId = "wms-wind-layer",
            sourceId = "wms-wind-source",
            layerName = "wind_10m_vector_meps_det_vdiv_2_5km_calculations",
            opacity = 0.6
        )
    } else {
        WmsLayerConfig(
            layerId = "wms-wind-layer",
            sourceId = "wms-wind-source",
            layerName = "wind_10m_vector_ec_sfc_3h_calculations",
            opacity = 0.6
        )
    }
}

private fun snapToLongRangeStep(wmsTime: String): String {
    val match = Regex("""T(\d{2}):""").find(wmsTime) ?: return wmsTime
    val hour = match.groupValues[1].toIntOrNull() ?: return wmsTime
    val snappedHour = (hour / LONG_RANGE_STEP_HOURS) * LONG_RANGE_STEP_HOURS
    return wmsTime.replace(
        Regex("""T\d{2}:00:00Z"""),
        "T${snappedHour.toString().padStart(2, '0')}:00:00Z"
    )
}

private fun buildWmsTileUrl(config: WmsLayerConfig, requestedTime: String): String {
    return "https://public-victoria.met.no/wms?" +
        "SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=TRUE" +
        "&STYLES=&LAYERS=${config.layerName}&CRS=EPSG:3857&WIDTH=256&HEIGHT=256" +
        "&BBOX={bbox-epsg-3857}&TIME=$requestedTime"
}

private fun ensureRasterSource(style: Style, config: WmsLayerConfig, tileUrl: String) {
    if (!style.styleSourceExists(config.sourceId)) {
        style.addSource(
            rasterSource(config.sourceId) {
                tiles(listOf(tileUrl))
                tileSize(256)
            }
        )
        return
    }

    style.setStyleSourceProperty(
        config.sourceId,
        "tiles",
        Value(listOf(Value(tileUrl)))
    )
}

private fun ensureRasterLayer(style: Style, config: WmsLayerConfig) {
    if (!style.styleLayerExists(config.layerId)) {
        val symbolLayerId = style.styleLayers.firstOrNull { it.type == "symbol" }?.id
        val layer = rasterLayer(config.layerId, config.sourceId) {
            rasterOpacity(config.opacity)
        }
        if (symbolLayerId != null) {
            style.addLayerBelow(layer, symbolLayerId)
        } else {
            style.addLayer(layer)
        }
    }

    style.setStyleLayerProperty(
        config.layerId,
        "raster-opacity",
        Value(config.opacity)
    )
}

private fun updateRasterLayerVisibility(style: Style, layerId: String, isVisible: Boolean) {
    if (!style.styleLayerExists(layerId)) return

    style.setStyleLayerProperty(
        layerId,
        "visibility",
        Value(if (isVisible) "visible" else "none")
    )
}

@Composable
fun InteractiveVictoriaKart(
    modifier: Modifier = Modifier,
    location: List<Location>,
    warnings: List<Warning>,
    mapViewportState: MapViewportState,
    isDarkStyle: Boolean,
    showTempLayer: Boolean,
    showRainLayer: Boolean,
    showWindLayer: Boolean,
    showFareLayer: Boolean,
    wmsTime: String,
    selectedTimeIndex: Int,
    selectedLocationId: Int? = null,
    isSelectedLocationHighlighted: Boolean = false,
    onLocationClick: (Location) -> Unit,
    onMapPointClick: (Point) -> Unit
) {
    val blueIcon = rememberIconImage("blue", painterResource(R.drawable.badepin3))
    val yellowIcon = rememberIconImage("yellow", painterResource(R.drawable.badepin2))
    val redIcon = rememberIconImage("red", painterResource(R.drawable.badepin1))
    val currentOnMapPointClick = rememberUpdatedState(onMapPointClick)
    val requestedTime = remember(selectedTimeIndex, wmsTime) {
        if (selectedTimeIndex <= MEPS_HOURS) wmsTime else snapToLongRangeStep(wmsTime)
    }
    val tempConfig = remember(selectedTimeIndex) { resolveTempLayerConfig(selectedTimeIndex) }
    val rainConfig = remember(selectedTimeIndex) { resolveRainLayerConfig(selectedTimeIndex) }
    val windConfig = remember(selectedTimeIndex) { resolveWindLayerConfig(selectedTimeIndex) }

    val severityByLocationId = remember(location, warnings) {
        location.associate { location ->
            location.id to mapSeverity(
                getSeverityForBath(location.latitude, location.longitude, warnings)
            )
        }
    }

    MapboxMap(
        modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = {},
        attribution = { Attribution(Modifier.padding(bottom = 40.dp)) },
        style = { MapStyle(style = if (isDarkStyle) Style.DARK else Style.LIGHT) }
    ) {
        UserLocationPuck()

        DisposableMapEffect(Unit) { mapView ->
            val listener = OnMapClickListener { point ->
                currentOnMapPointClick.value(point)
                true
            }
            mapView.gestures.addOnMapClickListener(listener)

            onDispose {
                mapView.gestures.removeOnMapClickListener(listener)
            }
        }

        DisposableMapEffect(showTempLayer, requestedTime, tempConfig, isDarkStyle) { mapView ->
            mapView.mapboxMap.getStyle { style ->
                if (showTempLayer ||
                    style.styleSourceExists(tempConfig.sourceId) ||
                    style.styleLayerExists(tempConfig.layerId)
                ) {
                    ensureRasterSource(style, tempConfig, buildWmsTileUrl(tempConfig, requestedTime))
                    ensureRasterLayer(style, tempConfig)
                    updateRasterLayerVisibility(style, tempConfig.layerId, showTempLayer)
                }
            }
            onDispose { }
        }

        DisposableMapEffect(showRainLayer, requestedTime, rainConfig, isDarkStyle) { mapView ->
            mapView.mapboxMap.getStyle { style ->
                if (showRainLayer ||
                    style.styleSourceExists(rainConfig.sourceId) ||
                    style.styleLayerExists(rainConfig.layerId)
                ) {
                    ensureRasterSource(style, rainConfig, buildWmsTileUrl(rainConfig, requestedTime))
                    ensureRasterLayer(style, rainConfig)
                    updateRasterLayerVisibility(style, rainConfig.layerId, showRainLayer)
                }
            }
            onDispose { }
        }

        DisposableMapEffect(showWindLayer, requestedTime, windConfig, isDarkStyle) { mapView ->
            mapView.mapboxMap.getStyle { style ->
                if (showWindLayer ||
                    style.styleSourceExists(windConfig.sourceId) ||
                    style.styleLayerExists(windConfig.layerId)
                ) {
                    ensureRasterSource(style, windConfig, buildWmsTileUrl(windConfig, requestedTime))
                    ensureRasterLayer(style, windConfig)
                    updateRasterLayerVisibility(style, windConfig.layerId, showWindLayer)
                }
            }
            onDispose { }
        }

        if (showFareLayer) {
            WarningMapLayer(warnings)
        }

        location.filterNot { it.id == selectedLocationId }.forEach { location ->
            val severity = severityByLocationId[location.id] ?: "blue"
            PointAnnotation(point = Point.fromLngLat(location.longitude, location.latitude)) {
                iconImage = when (severity) {
                    "red" -> redIcon
                    "yellow" -> yellowIcon
                    else -> blueIcon
                }
                iconSize = 0.2
                interactionsState.onClicked {
                    onLocationClick(location)
                    true
                }
            }
        }

        location.firstOrNull { it.id == selectedLocationId }?.let { selectedLocation ->
            val severity = severityByLocationId[selectedLocation.id] ?: "blue"
            PointAnnotation(point = Point.fromLngLat(selectedLocation.longitude, selectedLocation.latitude)) {
                iconImage = when (severity) {
                    "red" -> redIcon
                    "yellow" -> yellowIcon
                    else -> blueIcon
                }
                iconSize = if (isSelectedLocationHighlighted) 0.34 else 0.2
                interactionsState.onClicked {
                    onLocationClick(selectedLocation)
                    true
                }
            }
        }
    }
}
