package no.uio.ifi.in2000.aryanma.splaesh.ui.recommendations

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.ui.home.HomeMapFilters
import no.uio.ifi.in2000.aryanma.splaesh.ui.home.filterRecommendations

@Composable
fun RecommendationsScreen(
    warnings: List<Warning>,
    bathingScoreProfile: BathingScoreProfile,
    isDarkStyle: Boolean,
    favorites: List<Location>,
    homeFilters: HomeMapFilters,
    bottomPadding: Dp = 0.dp,
    onToggleFavorite: (Location) -> Unit = {},
    onOpenLocationOnMap: (Location) -> Unit = {},
    viewModel: RecommendationsViewModel = viewModel()
) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    val uiState by viewModel.uiState.collectAsState()
    val filteredRecommendations = remember(uiState.recommendations, favorites, homeFilters) {
        filterRecommendations(
            recommendations = uiState.recommendations,
            favorites = favorites,
            filters = homeFilters
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission, uiState.selectedRadiusKm, bathingScoreProfile, warnings) {
        if (hasLocationPermission) {
            viewModel.loadRecommendations(
                warnings = warnings,
                profile = bathingScoreProfile
            )
        }
    }

    val screenBackground = if (isDarkStyle) Color(0xFF0E1114) else Color(0xFFF5FAFB)
    val screenBrush = if (isDarkStyle) {
        Brush.verticalGradient(
            listOf(Color(0xFF0E1114), Color(0xFF101820), Color(0xFF0E1114))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFEAF8FA), Color(0xFFF6FAFB), Color(0xFFF3F6F8))
        )
    }
    val cardBackground = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)
    val titleColor = if (isDarkStyle) Color.White else Color(0xFF101828)
    val subTitleColor = titleColor
    val accent = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
    val radiusOptions = listOf(2, 5, 10, 20)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .background(screenBrush),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = if (isLandscape) 10.dp else 16.dp,
            bottom = bottomPadding + 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 2.dp,
                        end = 2.dp,
                        top = if (isLandscape) 4.dp else 8.dp,
                        bottom = if (isLandscape) 6.dp else 14.dp
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.82f else 0.96f)
                        .padding(
                            horizontal = if (isLandscape) 4.dp else 6.dp,
                            vertical = if (isLandscape) 6.dp else 10.dp
                        )
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDarkStyle) Color(0xCC1B2A30) else Color(0xEFFFFFFF),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkStyle) Color(0x3378D7E2) else Color(0x180E7490)
                        )
                    ) {
                        Text(
                            text = "Scoreprofil: ${bathingScoreProfile.title}",
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            color = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

                    Text(
                        text = "Anbefalinger",
                        color = titleColor,
                        style = if (isLandscape) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "De beste badeplassene nær deg, sortert etter badeforhold-score og avstand.",
                        color = subTitleColor,
                        fontSize = if (isLandscape) 13.sp else 15.sp,
                        lineHeight = if (isLandscape) 17.sp else 20.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Avstand",
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        radiusOptions.forEach { radiusKm ->
                            val selected = uiState.selectedRadiusKm == radiusKm
                            val chipBackground by animateColorAsState(
                                targetValue = if (selected) {
                                    if (isDarkStyle) Color(0xFF243845) else Color(0xFFE6F7FB)
                                } else {
                                    if (isDarkStyle) Color(0xFF1E2429) else Color(0xFFF7F9FB)
                                },
                                label = "recommendationRadiusBackground"
                            )
                            val chipBorder by animateColorAsState(
                                targetValue = if (selected) {
                                    if (isDarkStyle) Color(0x6678D7E2) else Color(0x330E7490)
                                } else {
                                    if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000)
                                },
                                label = "recommendationRadiusBorder"
                            )
                            val chipTextColor by animateColorAsState(
                                targetValue = if (selected) accent else subTitleColor,
                                label = "recommendationRadiusText"
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = chipBackground,
                                border = BorderStroke(
                                    1.dp,
                                    chipBorder
                                ),
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.setRadiusKm(radiusKm)
                                }
                            ) {
                                Text(
                                    text = "$radiusKm km",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = chipTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!hasLocationPermission) {
            item {
                LocationPermissionCard(
                    isDarkStyle = isDarkStyle,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        } else if (uiState.isLoading) {
            item {
                LoadingCard(isDarkStyle = isDarkStyle)
            }
        } else if (!uiState.hasLocationData) {
            item {
                LocationUnavailableCard(isDarkStyle = isDarkStyle)
            }
        } else if (filteredRecommendations.isEmpty()) {
            item {
                EmptyRecommendationsCard(
                    radiusKm = uiState.selectedRadiusKm,
                    isDarkStyle = isDarkStyle
                )
            }
        } else {
            items(filteredRecommendations, key = { it.location.id }) { recommendation ->
                RecommendationPlaceCard(
                    recommendation = recommendation,
                    isDarkStyle = isDarkStyle,
                    bathingScoreProfile = bathingScoreProfile,
                    isFavorite = favorites.any { it.id == recommendation.location.id },
                    onToggleFavorite = onToggleFavorite,
                    onOpenLocationOnMap = onOpenLocationOnMap
                )
            }
        }
    }
}

@Composable
private fun LocationPermissionCard(
    isDarkStyle: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "For å få anbefalinger trenger vi posisjonen din.",
                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tillat posisjon for å finne de beste badeplassene nær deg.",
                color = if (isDarkStyle) Color(0xFFD0D5DD) else Color(0xFF475467),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("Gi tilgang", color = if (isDarkStyle) Color(0xFF0E1114) else Color.White)
            }
        }
    }
}

@Composable
private fun LocationUnavailableCard(isDarkStyle: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "For å få anbefalinger trenger appen posisjonen din.",
                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prøv igjen når telefonen eller emulatoren deler en gyldig posisjon.",
                color = if (isDarkStyle) Color(0xFFD0D5DD) else Color(0xFF475467),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmptyRecommendationsCard(radiusKm: Int, isDarkStyle: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ingen treff innenfor ${radiusKm} km",
                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prøv en større radius for å få anbefalte badeplasser.",
                color = if (isDarkStyle) Color(0xFFD0D5DD) else Color(0xFF475467),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LoadingCard(isDarkStyle: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Leter etter gode badeplasser nær deg ...",
                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
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
