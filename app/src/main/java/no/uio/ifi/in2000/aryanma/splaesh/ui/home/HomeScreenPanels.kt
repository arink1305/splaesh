package no.uio.ifi.in2000.aryanma.splaesh.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.BathingScoreCard
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.getSeverityForBath
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.mapSeverity
import no.uio.ifi.in2000.aryanma.splaesh.utils.calculateBathingScore
import no.uio.ifi.in2000.aryanma.splaesh.utils.farevarselPolygon
import java.util.Locale



@Composable
fun LayerControlMenu(
    expanded: Boolean,
    isLandscape: Boolean,
    isDarkStyle: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showTempLayer: Boolean,
    onTempLayerChange: (Boolean) -> Unit,
    showRainLayer: Boolean,
    onRainLayerChange: (Boolean) -> Unit,
    showWindLayer: Boolean,
    onWindLayerChange: (Boolean) -> Unit,
    showFareLayer: Boolean,
    onFareLayerChange: (Boolean) -> Unit
) {
    val menuButtonInteractionSource = remember { MutableInteractionSource() }
    val menuButtonScale by animateFloatAsState(
        targetValue = if (expanded) 0.97f else 1f,
        animationSpec = spring(),
        label = "layerMenuButtonScale"
    )
    val buttonContainer by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0xFF243845)
            expanded && !isDarkStyle -> Color(0xFFE4F5F8)
            isDarkStyle -> Color(0xD9232A32)
            else -> Color(0xCCFFFFFF)
        },
        label = "layerMenuButtonContainer"
    )
    val buttonBorder by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0x6678D7E2)
            expanded && !isDarkStyle -> Color(0x4D0E7490)
            isDarkStyle -> Color(0x335E6A75)
            else -> Color(0x1F101828)
        },
        label = "layerMenuButtonBorder"
    )
    val buttonIconTint by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0xFF9AE9F2)
            expanded && !isDarkStyle -> Color(0xFF0E7490)
            isDarkStyle -> Color(0xFF78D7E2)
            else -> Color(0xFF0E7490)
        },
        label = "layerMenuButtonIconTint"
    )

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(
                top = if (isLandscape) 136.dp else 168.dp,
                start = if (isLandscape) 14.dp else 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .scale(menuButtonScale)
                .clickable(
                    interactionSource = menuButtonInteractionSource,
                    indication = null
                ) { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = buttonContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, buttonBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = if (expanded) Icons.Default.ArrowDownward else Icons.Default.Add,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) + scaleIn(
                            initialScale = 0.82f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )).togetherWith(
                            fadeOut(animationSpec = tween(120)) + scaleOut(
                                targetScale = 0.9f,
                                animationSpec = tween(140)
                            )
                        )
                    },
                    label = "layerMenuButtonIcon"
                ) { icon: ImageVector ->
                    Icon(
                        imageVector = icon,
                        contentDescription = if (expanded) "Lukk kartlag" else "Kartlag",
                        tint = buttonIconTint,
                        modifier = Modifier
                            .width(26.dp)
                            .height(26.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = spring()
            ) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = tween(240)
            ),
            exit = fadeOut(animationSpec = tween(170)) + scaleOut(
                targetScale = 0.97f,
                animationSpec = tween(180)
            ) + slideOutVertically(
                targetOffsetY = { -it / 5 },
                animationSpec = tween(180)
            )
        ) {
            if (isLandscape) {

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeatherLayerChip(
                            label = "Temp",
                            icon = Icons.Default.Thermostat,
                            selected = showTempLayer,
                            isDarkStyle = isDarkStyle,
                            onClick = { onTempLayerChange(!showTempLayer) }
                        )
                        WeatherLayerChip(
                            label = "Nedbør",
                            icon = Icons.Default.InvertColors,
                            selected = showRainLayer,
                            isDarkStyle = isDarkStyle,
                            onClick = { onRainLayerChange(!showRainLayer) }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeatherLayerChip(
                            label = "Vind",
                            icon = Icons.Default.Waves,
                            selected = showWindLayer,
                            isDarkStyle = isDarkStyle,
                            onClick = { onWindLayerChange(!showWindLayer) }
                        )
                        WeatherLayerChip(
                            label = "Farevarsler",
                            icon = Icons.Default.Shield,
                            selected = showFareLayer,
                            isDarkStyle = isDarkStyle,
                            onClick = { onFareLayerChange(!showFareLayer) }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WeatherLayerChip(
                        label = "Temp",
                        icon = Icons.Default.Thermostat,
                        selected = showTempLayer,
                        isDarkStyle = isDarkStyle,
                        onClick = { onTempLayerChange(!showTempLayer) }
                    )
                    WeatherLayerChip(
                        label = "Nedbør",
                        icon = Icons.Default.InvertColors,
                        selected = showRainLayer,
                        isDarkStyle = isDarkStyle,
                        onClick = { onRainLayerChange(!showRainLayer) }
                    )
                    WeatherLayerChip(
                        label = "Vind",
                        icon = Icons.Default.Waves,
                        selected = showWindLayer,
                        isDarkStyle = isDarkStyle,
                        onClick = { onWindLayerChange(!showWindLayer) }
                    )
                    WeatherLayerChip(
                        label = "Farevarsler",
                        icon = Icons.Default.Shield,
                        selected = showFareLayer,
                        isDarkStyle = isDarkStyle,
                        onClick = { onFareLayerChange(!showFareLayer) }
                    )
                }
            }
        }
    }
}

@Composable
fun BeachDetailsSheet(
    showSheet: Boolean,
    selectedLocation: Location?,
    warnings: List<Warning>,
    seaInfo: SeaInfo?,
    uvValue: Double?,
    airTemperature: Double?,
    loadErrorMessage: String?,
    isFavorite: Boolean,
    isDarkStyle: Boolean,
    bathingScoreProfile: BathingScoreProfile,
    onClose: () -> Unit,
    onToggleFavorite: (Location) -> Unit,
) {
    AnimatedVisibility(
        visible = showSheet,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            val sheetBg = if (isDarkStyle) Color(0xFF1E1E1E) else Color(0xFFF9F9FB)
            val sheetText = if (isDarkStyle) Color.White else MaterialTheme.colorScheme.onSurface

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = sheetBg),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.LightGray, RoundedCornerShape(2.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val favoriteColor by animateColorAsState(
                        targetValue = if (isFavorite) Color(0xFFE53935) else Color(0xFF0E7490),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "homePopupFavoriteHeartColor"
                    )
                    val favoriteScale by animateFloatAsState(
                        targetValue = if (isFavorite) 1.08f else 0.94f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "homePopupFavoriteHeartScale"
                    )
                    val overlayBrush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x24000000), Color(0xD9000000))
                    )

                    selectedLocation?.image?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(188.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Bilde av ${selectedLocation.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(overlayBrush)
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(14.dp)
                                        .size(34.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onToggleFavorite(selectedLocation)
                                        },
                                    shape = CircleShape,
                                    color = Color(0xE6FFFFFF)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isFavorite) "♥" else "♡",
                                            modifier = Modifier.scale(favoriteScale),
                                            color = favoriteColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = selectedLocation.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Kredittering: ${selectedLocation.source}",
                                        color = Color(0xFFF2F4F7),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                        }
                    } ?: Text(
                        text = selectedLocation?.name ?: "Badeplass",
                        style = MaterialTheme.typography.titleMedium,
                        color = sheetText
                    )

                    loadErrorMessage?.let { message ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkStyle) Color(0xFF3A1F22) else Color(0xFFFFE4E6)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = message,
                                color = if (isDarkStyle) Color(0xFFFFD5D8) else Color(0xFF9F1239),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    HomePopupCards(
                        location = selectedLocation,
                        warnings = warnings,
                        seaInfo = seaInfo,
                        isDarkStyle = isDarkStyle,
                        uv = uvValue,
                        airTemperature = airTemperature,
                        bathingScoreProfile = bathingScoreProfile
                    )

                    Spacer(Modifier.height(2.dp))

                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val isClosePressed by closeInteractionSource.collectIsPressedAsState()
                    var isClosingSheet by remember { mutableStateOf(false) }
                    val closeCoroutineScope = rememberCoroutineScope()
                    val closeButtonScale by animateFloatAsState(
                        targetValue = if (isClosingSheet || isClosePressed) 0.88f else 1f,
                        label = ""
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (!isClosingSheet) {
                                    closeCoroutineScope.launch {
                                        isClosingSheet = true
                                        delay(140)
                                        onClose()
                                        isClosingSheet = false
                                    }
                                }
                            },
                            interactionSource = closeInteractionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.scale(closeButtonScale),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Lukk")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherLayerChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    isDarkStyle: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            if (isDarkStyle) Color(0xFF243845) else Color(0xFFE4F5F8)
        } else {
            if (isDarkStyle) Color(0xD9232A32) else Color(0xCCFFFFFF)
        },
        label = "layerChipContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            if (isDarkStyle) Color(0xFF9AE9F2) else Color(0xFF0E7490)
        } else {
            if (isDarkStyle) Color(0xFFF8FAFC) else Color(0xFF344054)
        },
        label = "layerChipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            if (isDarkStyle) Color(0x6678D7E2) else Color(0x4D0E7490)
        } else {
            if (isDarkStyle) Color(0x335E6A75) else Color(0x1F101828)
        },
        label = "layerChipBorder"
    )

    Card(
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor
            )
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HomePopupCards(
    location: Location?,
    warnings: List<Warning>,
    seaInfo: SeaInfo?,
    isDarkStyle: Boolean,
    uv: Double?,
    airTemperature: Double?,
    bathingScoreProfile: BathingScoreProfile
) {
    if (location == null) return

    var showWave by rememberSaveable { mutableStateOf(true) }
    var showUv by rememberSaveable { mutableStateOf(true) }

    val description = warnings.firstOrNull { warning ->
        warning.coordinates.any { ring ->
            farevarselPolygon(location.latitude, location.longitude, ring)
        }
    }?.description

    val rawWarningSeverity = getSeverityForBath(
        location.latitude,
        location.longitude,
        warnings
    )
    val severity = mapSeverity(rawWarningSeverity)

    val statusText = when (severity) {
        "red" -> "Fare"
        "yellow" -> "Farevarsel!"
        else -> "Trygt"
    }
    val hasWarning = severity == "red" || severity == "yellow"
    val warningText = description ?: "Farevarsel mangler detaljert tekst."

    val waterTempText = seaInfo?.waterTemperature?.let { "${it}°C" } ?: "—"
    val uvText = uv?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
    val airTempText = airTemperature?.let { "${String.format(Locale.US, "%.1f", it)}°C" } ?: "—"
    val waveHeightText = seaInfo?.waveHeight?.let { "$it m" } ?: "—"
    val currentSpeedText = seaInfo?.currentSpeed?.let { "${String.format(Locale.US, "%.1f", it)} m/s" } ?: "—"
    val bathingScore = calculateBathingScore(
        profile = bathingScoreProfile,
        warningSeverity = rawWarningSeverity,
        waterTemperature = seaInfo?.waterTemperature,
        waveHeight = seaInfo?.waveHeight,
        currentSpeed = seaInfo?.currentSpeed,
        uvIndex = uv,
        airTemperature = airTemperature
    )

    val sectionBg = if (isDarkStyle) Color(0xFF2A2A2A) else Color(0xFFF5F7FA)
    val highlightedBg = if (isDarkStyle) Color(0xFF22313A) else Color(0xFFEAF4F8)
    val titleColor = if (isDarkStyle) Color.White else Color(0xFF0F172A)
    val labelColor = if (isDarkStyle) Color(0xFFB8C1CC) else Color(0xFF475467)
    val infoCardHeight = 138.dp

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        BathingScoreCard(
            score = bathingScore,
            isDarkStyle = isDarkStyle,
            backgroundColor = sectionBg,
            titleColor = titleColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeWarningFlipCard(
                title = statusText,
                warningText = warningText,
                hasWarning = hasWarning,
                backgroundColor = sectionBg,
                titleColor = titleColor,
                subtitleColor = labelColor,
                iconTint = if (isDarkStyle) Color(0xFF7DD3FC) else Color(0xFF0E7490),
                modifier = Modifier
                    .weight(1f)
                    .height(infoCardHeight)
            )

            HomeInfoCard(
                title = waterTempText,
                subtitle = "Vanntemperatur",
                backgroundColor = highlightedBg,
                titleColor = titleColor,
                subtitleColor = labelColor,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Vanntemperatur",
                        tint = Color(0xFF4C9FD6)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(infoCardHeight)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeInfoCard(
                title = if (showUv) uvText else airTempText,
                subtitle = if (showUv) "UV" else "Lufttemperatur",
                backgroundColor = sectionBg,
                titleColor = titleColor,
                subtitleColor = labelColor,
                icon = {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = if (showUv) "UV" else "Lufttemperatur",
                        tint = Color(0xFFE0B12D)
                    )
                },
                pageIndex = if (showUv) 0 else 1,
                pageIndicatorActiveColor = Color(0xFFE0B12D),
                pageIndicatorInactiveColor = labelColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .weight(1f)
                    .height(infoCardHeight)
                    .clickable { showUv = !showUv }
            )

            HomeInfoCard(
                title = if (showWave) waveHeightText else currentSpeedText,
                subtitle = if (showWave) "Bølgehøyde" else "Strøm",
                backgroundColor = sectionBg,
                titleColor = titleColor,
                subtitleColor = labelColor,
                icon = {
                    Icon(
                        imageVector = if (showWave) Icons.Default.Waves else Icons.Default.Water,
                        contentDescription = if (showWave) "Bølgehøyde" else "Strøm",
                        tint = Color(0xFF5AA7D9)
                    )
                },
                pageIndex = if (showWave) 0 else 1,
                pageIndicatorActiveColor = Color(0xFF5AA7D9),
                pageIndicatorInactiveColor = labelColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .weight(1f)
                    .height(infoCardHeight)
                    .clickable { showWave = !showWave }
            )
        }
    }
}

@Composable
private fun HomeWarningFlipCard(
    title: String,
    warningText: String,
    hasWarning: Boolean,
    backgroundColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }
    val warningScrollState = rememberScrollState()
    val warningStretch = remember { Animatable(0f) }
    val warningStretchScope = rememberCoroutineScope()
    val warningNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val atTop = warningScrollState.value == 0
                val atBottom = warningScrollState.value == warningScrollState.maxValue
                val draggingPastTop = available.y > 0 && atTop
                val draggingPastBottom = available.y < 0 && atBottom

                if (source == NestedScrollSource.UserInput && (draggingPastTop || draggingPastBottom)) {
                    warningStretchScope.launch {
                        val nextStretch = (warningStretch.value + (available.y * 0.22f))
                            .coerceIn(-22f, 22f)
                        warningStretch.snapTo(nextStretch)
                    }
                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = available

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                warningStretch.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.88f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                return Velocity.Zero
            }
        }
    }
    val rotation by animateFloatAsState(
        targetValue = if (showDetails) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "homeWarningCardFlip"
    )

    val showingBack = rotation > 90f
    val stretchScaleY = 1f + (kotlin.math.abs(warningStretch.value) / 260f)
    val stretchOrigin = if (warningStretch.value >= 0f) {
        TransformOrigin(0.5f, 0f)
    } else {
        TransformOrigin(0.5f, 1f)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = hasWarning) { showDetails = !showDetails }
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showingBack && hasWarning) {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .graphicsLayer {
                            scaleY = stretchScaleY
                            translationY = warningStretch.value * 0.28f
                            transformOrigin = stretchOrigin
                        }
                        .nestedScroll(warningNestedScrollConnection)
                        .verticalScroll(warningScrollState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = warningText,
                        color = titleColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(4.dp))
                HomePageDots(
                    currentPage = 1,
                    activeColor = iconTint,
                    inactiveColor = subtitleColor.copy(alpha = 0.35f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (hasWarning) 3.dp else 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Status",
                    tint = iconTint
                )
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = if (hasWarning) 14.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Status",
                    color = titleColor,
                    fontSize = if (hasWarning) 11.sp else 14.sp,
                    textAlign = TextAlign.Center
                )
                if (hasWarning) {
                    HomePageDots(
                        currentPage = 0,
                        activeColor = iconTint,
                        inactiveColor = subtitleColor.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePageDots(
    currentPage: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) activeColor else inactiveColor)
            )
        }
    }
}

@Composable
fun HomeInfoCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 20.sp,
    subtitleFontSize: TextUnit = 14.sp,
    verticalPadding: Dp = 12.dp,
    contentSpacing: Dp = 6.dp,
    pageIndex: Int? = null,
    pageIndicatorActiveColor: Color = titleColor,
    pageIndicatorInactiveColor: Color = subtitleColor.copy(alpha = 0.35f)
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            icon()

            Text(
                text = title,
                color = titleColor,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = titleColor,
                fontSize = subtitleFontSize
            )

            pageIndex?.let { index ->
                HomePageDots(
                    currentPage = index,
                    activeColor = pageIndicatorActiveColor,
                    inactiveColor = pageIndicatorInactiveColor
                )
            }
        }
    }
}
