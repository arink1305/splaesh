package no.uio.ifi.in2000.aryanma.splaesh.ui.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.InteractiveVictoriaKart
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.MapSearchOverlay
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.TimeScroller
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.UserLocationButton
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.WeatherData
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.ZoomButton

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen(
    context: Context,
    bottomPadding: Dp = 0.dp,
    onSheetVisibilityChange: (Boolean) -> Unit = {},
    favorites: List<Location>,
    warnings: List<Warning>,
    homeFilters: HomeMapFilters,
    onHomeFiltersChange: (HomeMapFilters) -> Unit,
    onToggleFavorite: (Location) -> Unit,
    cameraCenter: Point,
    cameraZoom: Double,
    focusedLocation: Location? = null,
    focusedLocationRequestId: Int = 0,
    onFocusedLocationHandled: () -> Unit = {},
    onCameraChange: (Point, Double) -> Unit,
    showTempLayer: Boolean,
    onTempLayerChange: (Boolean) -> Unit,
    showRainLayer: Boolean,
    onRainLayerChange: (Boolean) -> Unit,
    showWindLayer: Boolean,
    onWindLayerChange: (Boolean) -> Unit,
    bathingScoreProfile: BathingScoreProfile,
    viewModel: HomesScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locations = uiState.locations
    val selectedLocation = uiState.selectedLocation

    val prefs = remember(context) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var pendingMapPointSheet by rememberSaveable { mutableStateOf(false) }
    var animateMapPointSheetSwap by rememberSaveable { mutableStateOf(false) }
    var showTimeScroller by rememberSaveable { mutableStateOf(false) }
    var showLayerMenu by rememberSaveable { mutableStateOf(false) }
    var showFilterMenu by rememberSaveable { mutableStateOf(false) }
    var showWeather by rememberSaveable { mutableStateOf(false) }
    var isDarkStyle by remember { mutableStateOf(prefs.getBoolean("map_style_is_dark", false)) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(cameraZoom)
            center(cameraCenter)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { mapViewportState.cameraState }
            .debounce(300)
            .collect { state ->
                state?.let { onCameraChange(it.center, it.zoom) }
            }
    }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
    val favoriteFocusBottomPadding = with(density) {
        if (isLandscape) 170.dp.toPx().toDouble() else 420.dp.toPx().toDouble() //
    }

    LaunchedEffect(focusedLocationRequestId) {
        focusedLocation?.let { location ->
            pendingMapPointSheet = false
            animateMapPointSheetSwap = false
            viewModel.selectLocation(location, highlightedFavoriteLocationId = location.id)
            showSheet = true
            val focusZoom = maxOf(mapViewportState.cameraState?.zoom ?: cameraZoom, 14.0)
            val focusCenter = Point.fromLngLat(location.longitude, location.latitude)
            val focusPadding = EdgeInsets(70.0, 0.0, favoriteFocusBottomPadding, 0.0)
            onCameraChange(focusCenter, focusZoom)
            mapViewportState.easeTo(
                cameraOptions {
                    center(focusCenter)
                    zoom(focusZoom)
                    padding(focusPadding)
                }
            )
            delay(150)
            mapViewportState.easeTo(
                cameraOptions {
                    center(focusCenter)
                    zoom(focusZoom)
                    padding(focusPadding)
                }
            )
            onFocusedLocationHandled()
        }
    }

    LaunchedEffect(showSheet, selectedLocation?.source) {
        onSheetVisibilityChange(
            showSheet &&
                selectedLocation != null &&
                selectedLocation.source != MAP_CLICK_SOURCE
        )
    }

    LaunchedEffect(Unit) {
        isDarkStyle = prefs.getBoolean("map_style_is_dark", false)
    }

    LaunchedEffect(showTempLayer, showRainLayer, showWindLayer) {
        if (!showTempLayer && !showRainLayer && !showWindLayer) {
            showTimeScroller = false
        }
    }

    val popupTextColor = if (isDarkStyle) Color.White else Color.Black

    val isFavorite by remember(selectedLocation?.id, favorites) {
        derivedStateOf {
            selectedLocation?.let { selected ->
                selected.source != MAP_CLICK_SOURCE && favorites.any { it.id == selected.id }
            } == true
        }
    }

    val filteredLocations by remember(
        locations,
        favorites,
        warnings,
        homeFilters,
        uiState.bathingScoresByLocationId
    ) {
        derivedStateOf {
            filterHomeLocations(
                locations = locations,
                favorites = favorites,
                warnings = warnings,
                filters = homeFilters,
                bathingScoresByLocationId = uiState.bathingScoresByLocationId
            )
        }
    }

    LaunchedEffect(locations, warnings, bathingScoreProfile) {
        if (locations.isNotEmpty()) {
            viewModel.preloadBathingScores(warnings, bathingScoreProfile)
        }
    }

    val todayForecast by remember(uiState.forecasts, uiState.selectedDayIndex) {
        derivedStateOf { uiState.forecasts.getOrNull(uiState.selectedDayIndex) }
    }

    suspend fun showPendingMapPointSheetIfNeeded() {
        if (!pendingMapPointSheet) return
        if (animateMapPointSheetSwap) {
            delay(250)
        }
        showSheet = true
        pendingMapPointSheet = false
        animateMapPointSheetSwap = false
    }

    LaunchedEffect(selectedLocation?.name, selectedLocation?.source, uiState.isLoadingPlaceData) {
        if (
            pendingMapPointSheet &&
            selectedLocation?.source == MAP_CLICK_SOURCE &&
            !uiState.isLoadingPlaceData
        ) {
            showPendingMapPointSheetIfNeeded()
        }
    }

    val isBeachPopupVisible = showSheet &&
        selectedLocation != null &&
        selectedLocation.source != MAP_CLICK_SOURCE

    LaunchedEffect(isLandscape, isBeachPopupVisible) {
        if (isLandscape && isBeachPopupVisible) {
            showWeather = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        InteractiveVictoriaKart(
            modifier = Modifier.fillMaxSize(),
            location = filteredLocations,
            warnings = warnings,
            mapViewportState = mapViewportState,
            isDarkStyle = isDarkStyle,
            showTempLayer = showTempLayer,
            showRainLayer = showRainLayer,
            showWindLayer = showWindLayer,
            showFareLayer = uiState.showFareLayer,
            wmsTime = viewModel.wmsTime,
            selectedTimeIndex = uiState.selectedTimeIndex,
            selectedLocationId = uiState.highlightedFavoriteLocationId,
            isSelectedLocationHighlighted = showSheet && uiState.highlightedFavoriteLocationId != null,
            onLocationClick = { clickedLocation ->
                viewModel.clearFavoriteHighlight()
                pendingMapPointSheet = false
                animateMapPointSheetSwap = false
                viewModel.selectLocation(clickedLocation)
                showSheet = true
            },
            onMapPointClick = { point ->
                viewModel.clearFavoriteHighlight()
                animateMapPointSheetSwap = showSheet
                pendingMapPointSheet = true
                showSheet = false
                viewModel.selectLocation(
                    Location(
                        id = -1,
                        name = "",
                        longitude = point.longitude(),
                        latitude = point.latitude(),
                        image = "",
                        source = MAP_CLICK_SOURCE
                    )
                )
            }
        )

        TimeScroller(
            visible = (showTempLayer || showRainLayer || showWindLayer) && showTimeScroller,
            modifier = if (isLandscape) {
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.5f)
                    .offset(x = 240.dp)
            } else {
                Modifier
                    .align(Alignment.BottomCenter)
            },
            bottomPadding = bottomPadding,
            bottomOffset = if (isLandscape) 18.dp else 160.dp,
            compactMode = isLandscape,
            isDarkStyle = isDarkStyle,
            locationName = uiState.locationName,
            selectedTimeIndex = uiState.selectedTimeIndex,
            onTimeChange = viewModel::setSelectedTimeIndex,
            baseCalendar = viewModel.baseCalendar
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = showTempLayer || showRainLayer || showWindLayer,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isLandscape) 60.dp else 154.dp),
            enter = androidx.compose.animation.fadeIn() +
                androidx.compose.animation.slideInVertically { -it },
            exit = androidx.compose.animation.fadeOut() +
                androidx.compose.animation.slideOutVertically { -it }
        ) {
            val clockBg = if (showTimeScroller) Color(0xFF1976D2) else {
                if (isDarkStyle) Color(0xF0222222) else Color(0xF2FFFFFF)
            }
            val clockTint = if (showTimeScroller) Color.White else {
                if (isDarkStyle) Color.White else Color(0xFF424242)
            }

            Surface(
                onClick = { showTimeScroller = !showTimeScroller },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                color = clockBg,
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = clockTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (showTimeScroller) "Skjul tidsvelger" else "Vis tidsvelger",
                        color = clockTint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        LayerControlMenu(
            expanded = showLayerMenu,
            isLandscape = isLandscape,
            isDarkStyle = isDarkStyle,
            onExpandedChange = {
                showLayerMenu = it
                if (it) showFilterMenu = false
            },
            showTempLayer = showTempLayer,
            onTempLayerChange = onTempLayerChange,
            showRainLayer = showRainLayer,
            onRainLayerChange = onRainLayerChange,
            showWindLayer = showWindLayer,
            onWindLayerChange = onWindLayerChange,
            showFareLayer = uiState.showFareLayer,
            onFareLayerChange = viewModel::setShowFareLayer
        )

        HomeFilterMenu(
            expanded = showFilterMenu,
            filters = homeFilters,
            totalCount = locations.size,
            visibleCount = filteredLocations.size,
            isLoadingBathingScores = uiState.isLoadingBathingScores,
            bathingScoreProfile = bathingScoreProfile,
            isLandscape = isLandscape,
            isDarkStyle = isDarkStyle,
            onExpandedChange = {
                showFilterMenu = it
                if (it) showLayerMenu = false
            },
            onFiltersChange = onHomeFiltersChange
        )

        if (!(isLandscape && isBeachPopupVisible)) {
            MapSearchOverlay(
                context = context,
                isDarkStyle = isDarkStyle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(3f),
                topOffset = if (isLandscape) 4.dp else 6.dp,
                sidePadding = if (isLandscape) 14.dp else 16.dp,
                onExpandedChange = { expanded ->
                    if (expanded) {
                        showLayerMenu = false
                        showFilterMenu = false
                    }
                },
                onLocationSelected = { _, latitude, longitude ->
                    viewModel.clearSelectedLocation()
                    showSheet = false
                    val targetCenter = Point.fromLngLat(longitude, latitude)
                    val targetZoom = maxOf(mapViewportState.cameraState?.zoom ?: cameraZoom, 11.5)
                    onCameraChange(targetCenter, targetZoom)
                    mapViewportState.easeTo(
                        cameraOptions {
                            center(targetCenter)
                            zoom(targetZoom)
                        }
                    )
                }
            )
        }

        if (!(isLandscape && isBeachPopupVisible)) {
            WeatherData(
                showWeather = showWeather,
                onToggleWeather = { showWeather = it },
                todayForecast = todayForecast,
                currentUv = uiState.currentUv,
                selectedDayIndex = uiState.selectedDayIndex,
                onDayChange = viewModel::setSelectedDayIndex,
                allForecastsSize = uiState.forecasts.size,
                locationName = uiState.locationName,
                errorMessage = uiState.placeDataError,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = if (isLandscape) 56.dp else 0.dp)
                    .zIndex(1f),
                compactMode = isLandscape,
                alignToStart = false,
                popupTextColor = popupTextColor,
                popupSubTextColor = popupTextColor
            )
        }

        UserLocationButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(
                    if (isLandscape && (showLayerMenu || showFilterMenu)) {
                        -1f
                    } else {
                        0f
                    }
                ),
            isDarkStyle = isDarkStyle,
            bottomPadding = bottomPadding,
            onLocationResolved = { longitude, latitude ->
                val targetCenter = Point.fromLngLat(longitude, latitude)
                val targetZoom = maxOf(mapViewportState.cameraState?.zoom ?: cameraZoom, 13.5)
                onCameraChange(targetCenter, targetZoom)
                mapViewportState.easeTo(
                    cameraOptions {
                        center(targetCenter)
                        zoom(targetZoom)
                    }
                )
            }
        )

        ZoomButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            mapViewportState = mapViewportState,
            bottomPadding = bottomPadding
        )

        BeachDetailsSheet(
            showSheet = isBeachPopupVisible,
            selectedLocation = selectedLocation?.takeIf { it.source != MAP_CLICK_SOURCE },
            warnings = warnings,
            seaInfo = uiState.seaInfo,
            uvValue = uiState.currentUv,
            airTemperature = uiState.airTemperature,
            loadErrorMessage = uiState.placeDataError,
            isFavorite = isFavorite,
            isDarkStyle = isDarkStyle,
            bathingScoreProfile = bathingScoreProfile,
            onClose = { showSheet = false },
            onToggleFavorite = onToggleFavorite
        )
    }
}
