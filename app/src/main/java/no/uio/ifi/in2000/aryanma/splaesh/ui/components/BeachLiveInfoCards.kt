package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.Velocity
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.utils.calculateBathingScore
import kotlin.math.abs

@Composable
fun BeachLiveInfoCards(
    isDarkStyle: Boolean,
    bathingScoreProfile: BathingScoreProfile,
    warningSeverity: String,
    warningText: String?,
    seaInfo: SeaInfo?,
    uv: Double?,
    airTemperature: Double?
) {
    var showWave by rememberSaveable { mutableStateOf(true) }
    var showUv by rememberSaveable { mutableStateOf(true) }
    val mappedSeverity = mapSeverity(warningSeverity)

    val statusText = when (mappedSeverity) {
        "red" -> "Fare"
        "yellow" -> "Vær forsiktig"
        else -> "Trygt"
    }
    val hasWarning = mappedSeverity == "red" || mappedSeverity == "yellow"
    val resolvedWarningText = warningText ?: "Farevarsel mangler detaljert tekst."
    val bathingScore = calculateBathingScore(
        profile = bathingScoreProfile,
        warningSeverity = warningSeverity,
        waterTemperature = seaInfo?.waterTemperature,
        waveHeight = seaInfo?.waveHeight,
        currentSpeed = seaInfo?.currentSpeed,
        uvIndex = uv,
        airTemperature = airTemperature
    )

    val waterTempText = seaInfo?.waterTemperature?.let { "$it°C" } ?: "—"
    val uvText = uv?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "—"
    val airTempText = airTemperature?.let { "${String.format(java.util.Locale.US, "%.1f", it)}°C" } ?: "—"
    val waveHeightText = seaInfo?.waveHeight?.let { "$it m" } ?: "—"
    val currentSpeedText = seaInfo?.currentSpeed?.let { "${String.format(java.util.Locale.US, "%.1f", it)} m/s" } ?: "—"

    val sectionBg = if (isDarkStyle) Color(0xFF2A2A2A) else Color(0xFFF5F7FA)
    val highlightedBg = if (isDarkStyle) Color(0xFF22313A) else Color(0xFFEAF4F8)
    val titleColor = if (isDarkStyle) Color.White else Color(0xFF0F172A)
    val labelColor = if (isDarkStyle) Color(0xFFB8C1CC) else Color(0xFF475467)
    val infoCardHeight = 138.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BathingScoreCard(
            score = bathingScore,
            isDarkStyle = isDarkStyle,
            backgroundColor = sectionBg,
            titleColor = titleColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveInfoWarningFlipCard(
                title = statusText,
                warningText = resolvedWarningText,
                hasWarning = hasWarning,
                backgroundColor = sectionBg,
                titleColor = titleColor,
                subtitleColor = labelColor,
                iconTint = if (isDarkStyle) Color(0xFF7DD3FC) else Color(0xFF0E7490),
                modifier = Modifier
                    .weight(1f)
                    .height(infoCardHeight)
            )

            LiveInfoCard(
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveInfoCard(
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

            LiveInfoCard(
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
private fun LiveInfoWarningFlipCard(
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
        label = "liveInfoWarningCardFlip"
    )

    val showingBack = rotation > 90f
    val stretchScaleY = 1f + (abs(warningStretch.value) / 260f)
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
                LiveInfoPageDots(
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
                    LiveInfoPageDots(
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
private fun LiveInfoPageDots(
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
                    .background(
                        color = if (index == currentPage) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun LiveInfoCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    pageIndex: Int? = null,
    pageIndicatorActiveColor: Color = titleColor,
    pageIndicatorInactiveColor: Color = subtitleColor.copy(alpha = 0.35f)
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()

            Text(
                text = title,
                color = titleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = titleColor,
                fontSize = 14.sp
            )

            if (pageIndex != null) {
                LiveInfoPageDots(
                    currentPage = pageIndex,
                    activeColor = pageIndicatorActiveColor,
                    inactiveColor = pageIndicatorInactiveColor
                )
            }
        }
    }
}
