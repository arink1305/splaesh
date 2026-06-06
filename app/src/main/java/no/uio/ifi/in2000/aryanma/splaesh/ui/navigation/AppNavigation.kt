@file:Suppress("UNUSED_VALUE", "AssignedValueIsNeverRead", "UnusedEquals")

package no.uio.ifi.in2000.aryanma.splaesh.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.aryanma.splaesh.data.WarningsRepository
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.ui.favorites.FavoritesScreen
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.ui.home.HomeScreen
import no.uio.ifi.in2000.aryanma.splaesh.ui.home.HomeMapFilters
import no.uio.ifi.in2000.aryanma.splaesh.ui.recommendations.RecommendationsScreen
import no.uio.ifi.in2000.aryanma.splaesh.ui.settings.SettingsScreen
import com.mapbox.geojson.Point
import androidx.core.content.edit

private val LocationListStateSaver = listSaver<MutableState<List<Location>>, Any>(
    save = { state ->
        state.value.flatMap { location ->
            listOf(
                location.id,
                location.name,
                location.longitude,
                location.latitude,
                location.image,
                location.source
            )
        }
    },
    restore = { saved ->
        mutableStateOf(
            saved.chunked(6).mapNotNull { values ->
                if (values.size != 6) return@mapNotNull null
                Location(
                    id = values[0] as Int,
                    name = values[1] as String,
                    longitude = values[2] as Double,
                    latitude = values[3] as Double,
                    image = values[4] as String,
                    source = values[5] as String
                )
            }
        )
    }
)

@Composable
fun AppNavigation() {
    val windowInfo = LocalWindowInfo.current
    val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
    var cameraLongitude by rememberSaveable { mutableDoubleStateOf(10.75) }
    var cameraLatitude by rememberSaveable { mutableDoubleStateOf(59.91) }
    var cameraZoom by rememberSaveable { mutableDoubleStateOf(11.0) }
    val cameraCenter = Point.fromLngLat(cameraLongitude, cameraLatitude)
    val navController = rememberNavController()
    val prefs = remember(navController.context) {
        navController.context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var favorites by rememberSaveable(saver = LocationListStateSaver) {
        mutableStateOf(emptyList())
    }
    var pendingFavoriteRemovalIds by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var focusedFavoriteRequestId by rememberSaveable { mutableIntStateOf(0) }
    var focusedFavoriteLocation by remember { mutableStateOf<Location?>(null) }
    var previousRoute by remember { mutableStateOf<String?>(null) }
    var warnings by remember { mutableStateOf<List<Warning>>(emptyList()) }
    var isDarkStyle by rememberSaveable {
        mutableStateOf(prefs.getBoolean("map_style_is_dark", false))
    }
    var homeFilters by remember { mutableStateOf(HomeMapFilters()) }
    var bathingScoreProfile by rememberSaveable {
        mutableStateOf(
            BathingScoreProfile.fromStorageKey(
                prefs.getString("bathing_score_profile", BathingScoreProfile.STANDARD.storageKey)
            )
        )
    }
    val tabBarBg = if (isDarkStyle) Color(0xFF1A1D21) else Color(0xFFFFFFFF)
    val tabIndicator = if (isDarkStyle) Color(0x3378D7E2) else Color(0x1403778A)
    val selectedColor = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF03778A)
    val unselectedColor = if (isDarkStyle) Color(0xFF9CA3AF) else Color(0xFF475467)

    // Keeps weather map layers active while the app is running.
    var showTempLayer by rememberSaveable { mutableStateOf(false) }
    var showRainLayer by rememberSaveable { mutableStateOf(false) }
    var showWindLayer by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val repo = WarningsRepository()
        warnings = repo.getWarnings()
    }

    LaunchedEffect(currentRoute) {
        if (previousRoute == "favorites" && currentRoute != "favorites" && pendingFavoriteRemovalIds.isNotEmpty()) {
            favorites = favorites.filterNot { it.id in pendingFavoriteRemovalIds }
            pendingFavoriteRemovalIds = emptyList()
        }
        previousRoute = currentRoute
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    @Suppress("UNUSED_VALUE")
    fun clearFocusedFavoriteLocation() {
        focusedFavoriteLocation = null
    }

    @Suppress("UNUSED_VALUE")
    fun updateShowSheet(visible: Boolean) {
        showSheet = visible
    }

    @Suppress("UNUSED_VALUE")
    fun updateCamera(center: Point, zoom: Double) {
        cameraLongitude = center.longitude()
        cameraLatitude = center.latitude()
        cameraZoom = zoom
    }

    @Suppress("UNUSED_VALUE")
    fun focusFavoriteLocation(location: Location, zoom: Double) {
        focusedFavoriteLocation = location
        focusedFavoriteRequestId += 1
        cameraLongitude = location.longitude
        cameraLatitude = location.latitude
        cameraZoom = zoom
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !showSheet,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    color = tabBarBg
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .then(
                                if (isLandscape) Modifier.height(72.dp) else Modifier
                            )
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        containerColor = Color.Transparent
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "favorites",
                            onClick = { navigateTo("favorites") },
                            alwaysShowLabel = true,
                            label = {
                                Text(
                                    text = "Favoritter",
                                    fontSize = if (isLandscape) 9.sp else 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "Favoritter",
                                    modifier = if (isLandscape) {
                                        Modifier
                                            .padding(top = 6.dp)
                                            .height(16.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedColor,
                                selectedTextColor = selectedColor,
                                unselectedIconColor = unselectedColor,
                                unselectedTextColor = unselectedColor,
                                indicatorColor = tabIndicator
                            )
                        )

                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = {
                                clearFocusedFavoriteLocation()
                                navigateTo("home")
                            },
                            alwaysShowLabel = true,
                            label = {
                                Text(
                                    text = "Hjem",
                                    fontSize = if (isLandscape) 9.sp else 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Home,
                                    contentDescription = "Hjem",
                                    modifier = if (isLandscape) { //if landscape er at man endrer på posisjonen til ikonet hvis det er rotert eller ikke

                                        Modifier
                                            .padding(top = 6.dp)
                                            .height(16.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedColor,
                                selectedTextColor = selectedColor,
                                unselectedIconColor = unselectedColor,
                                unselectedTextColor = unselectedColor,
                                indicatorColor = tabIndicator
                            )
                        )

                        NavigationBarItem(
                            selected = currentRoute == "recommendations",
                            onClick = { navigateTo("recommendations") },
                            alwaysShowLabel = true,
                            label = {
                                Text(
                                    text = "Anbefalinger",
                                    fontSize = if (isLandscape) 8.sp else 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Anbefalinger",
                                    modifier = if (isLandscape) {
                                        Modifier
                                            .padding(top = 6.dp)
                                            .height(16.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedColor,
                                selectedTextColor = selectedColor,
                                unselectedIconColor = unselectedColor,
                                unselectedTextColor = unselectedColor,
                                indicatorColor = tabIndicator
                            )
                        )

                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = { navigateTo("settings") },
                            alwaysShowLabel = true,
                            label = {
                                Text(
                                    text = "Innstillinger",
                                    fontSize = if (isLandscape) 9.sp else 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Innstillinger",
                                    modifier = if (isLandscape) {
                                        Modifier
                                            .padding(top = 6.dp)
                                            .height(16.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedColor,
                                selectedTextColor = selectedColor,
                                unselectedIconColor = unselectedColor,
                                unselectedTextColor = unselectedColor,
                                indicatorColor = tabIndicator
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("home") {
                HomeScreen(
                    context = navController.context,
                    bottomPadding = innerPadding.calculateBottomPadding(),

                    onSheetVisibilityChange = ::updateShowSheet,
                    favorites = favorites,
                    warnings = warnings,
                    homeFilters = homeFilters,
                    onHomeFiltersChange = { homeFilters = it },
                    onToggleFavorite = { location ->
                        favorites = if (favorites.contains(location)) {
                            favorites - location
                        } else {
                            favorites + location
                        }
                    },
                    cameraCenter = cameraCenter,
                    cameraZoom = cameraZoom,
                    focusedLocation = focusedFavoriteLocation,
                    focusedLocationRequestId = focusedFavoriteRequestId,
                    onFocusedLocationHandled = ::clearFocusedFavoriteLocation,
                    onCameraChange = ::updateCamera,
                    // Passes map layer state down to the home map.
                    showTempLayer = showTempLayer,
                    onTempLayerChange = { showTempLayer = it },
                    showRainLayer = showRainLayer,
                    onRainLayerChange = { showRainLayer = it },
                    showWindLayer = showWindLayer,
                    onWindLayerChange = { showWindLayer = it },
                    bathingScoreProfile = bathingScoreProfile
                )
            }

            composable(route = "favorites") {
                FavoritesScreen(
                    favorites = favorites,
                    warnings = warnings,
                    bathingScoreProfile = bathingScoreProfile,
                    pendingRemovalIds = pendingFavoriteRemovalIds,
                    onPendingRemovalIdsChange = { pendingFavoriteRemovalIds = it },
                    onOpenLocationOnMap = { location ->
                        val focusZoom = maxOf(cameraZoom, 14.0)
                        focusFavoriteLocation(location, focusZoom)
                        navigateTo("home")
                    }
                )
            }

            composable("recommendations") {
                RecommendationsScreen(
                    warnings = warnings,
                    bathingScoreProfile = bathingScoreProfile,
                    isDarkStyle = isDarkStyle,
                    favorites = favorites,
                    homeFilters = homeFilters,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onToggleFavorite = { location ->
                        favorites = if (favorites.contains(location)) {
                            favorites - location
                        } else {
                            favorites + location
                        }
                    },
                    onOpenLocationOnMap = { location ->
                        val focusZoom = maxOf(cameraZoom, 14.0)
                        focusFavoriteLocation(location, focusZoom)
                        navigateTo("home")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    isDarkStyle = isDarkStyle,
                    bathingScoreProfile = bathingScoreProfile,
                    onDarkStyleChange = { newValue ->
                        isDarkStyle = newValue
                        prefs.edit { putBoolean("map_style_is_dark", newValue) }
                    },
                    onBathingScoreProfileChange = { newProfile ->
                        bathingScoreProfile = newProfile
                        prefs.edit { putString("bathing_score_profile", newProfile.storageKey) }
                    }
                )
            }
        }
    }
}
