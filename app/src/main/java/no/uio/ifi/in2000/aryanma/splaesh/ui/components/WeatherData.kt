package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.aryanma.splaesh.model.DailyForecast
import no.uio.ifi.in2000.aryanma.splaesh.utils.ForecastAggregator
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun WeatherData(
    modifier: Modifier = Modifier,
    showWeather: Boolean,
    onToggleWeather: (Boolean) -> Unit,
    todayForecast: DailyForecast?,
    currentUv: Double?,
    selectedDayIndex: Int,
    onDayChange: (Int) -> Unit,
    allForecastsSize: Int,
    locationName: String,
    errorMessage: String? = null,
    compactMode: Boolean = false,
    alignToStart: Boolean = false,
    popupTextColor: Color = Color.White,
    popupSubTextColor: Color = Color.LightGray
) {
    val displayLocationName = locationName.trim().takeUnless {
        it.isBlank() || it.equals("Ukjent sted", ignoreCase = true)
    } ?: ""
    var stableLocationName by remember { mutableStateOf(displayLocationName) }
    LaunchedEffect(displayLocationName) {
        if (displayLocationName.isNotBlank()) {
            stableLocationName = displayLocationName
        }
    }
    val displayedUv = if (selectedDayIndex == 0) {
        currentUv ?: todayForecast?.uvMax
    } else {
        todayForecast?.uvMax
    }
    val horizontalAlignment = if (alignToStart) Alignment.Start else Alignment.End
    val buttonTopPadding = if (compactMode) 8.dp else 8.dp
    val buttonStartPadding = if (alignToStart) 12.dp else 0.dp
    val buttonEndPadding = if (alignToStart) 0.dp else if (compactMode) 10.dp else 16.dp
    val buttonContentMinWidth = if (compactMode) 136.dp else 184.dp
    val buttonContentMaxWidth = if (compactMode) 166.dp else 214.dp
    val buttonLocationFontSize = if (compactMode) 10.sp else 14.sp
    val buttonLocationLineHeight = if (compactMode) 12.sp else 17.sp
    val buttonTemperatureFontSize = if (compactMode) 13.sp else 16.sp
    val buttonSubtitleFontSize = if (compactMode) 9.sp else 10.sp
    val popupSpacing = if (compactMode) 3.dp else 8.dp
    val dateFontSize = if (compactMode) 9.sp else 12.sp
    val iconFontSize = if (compactMode) 16.sp else 32.sp
    val tempFontSize = if (compactMode) 13.sp else 20.sp
    val metricIconFontSize = if (compactMode) 10.sp else 18.sp
    val metricTextFontSize = if (compactMode) 7.sp else 12.sp
    val metricLabelFontSize = if (compactMode) 7.sp else 10.sp
    val metricHorizontalPadding = if (compactMode) 4.dp else 8.dp
    val metricVerticalPadding = if (compactMode) 5.dp else 9.dp
    val metricSpacing = if (compactMode) 1.dp else 3.dp
    val cardSecondarySurface = if (popupTextColor == Color.White) {
        Color(0x1439D2E3)
    } else {
        Color(0x140E7490)
    }
    val dividerColor = popupSubTextColor.copy(alpha = 0.14f)
    val navChipColor = if (popupTextColor == Color.White) {
        Color(0x1839D2E3)
    } else {
        Color(0x140E7490)
    }
    val weatherButtonSurfaceColor = if (popupTextColor == Color.White) {
        Color(0xFF20262D)
    } else {
        Color(0xFFFFFFFF)
    }
    val weatherButtonBorderColor = if (popupTextColor == Color.White) {
        Color(0x2CFFFFFF)
    } else {
        Color(0x140F172A)
    }
    val weatherButtonAccentSurface = if (popupTextColor == Color.White) {
        Color(0x1FFFFFFF)
    } else {
        Color(0xFFEAF5FB)
    }
    val weatherButtonChevronColor = if (popupTextColor == Color.White) {
        Color(0xFFD2EAF0)
    } else {
        Color(0xFF0E7490)
    }
    val navArrowFontSize = if (compactMode) 12.sp else 19.sp
    val weatherSymbol = when {
        displayedUv != null && displayedUv >= 5.0 -> "☀️"
        (todayForecast?.precipitation ?: 0.0) > 0.0 -> "🌧️"
        else -> "🌤️"
    }
    val uvValueText = displayedUv?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
    val weatherButtonInteractionSource = remember { MutableInteractionSource() }
    val weatherPopupInteractionSource = remember { MutableInteractionSource() }
    val previousDayInteractionSource = remember { MutableInteractionSource() }
    val nextDayInteractionSource = remember { MutableInteractionSource() }
    val isWeatherButtonPressed = weatherButtonInteractionSource.collectIsPressedAsState().value
    val isPreviousDayPressed = previousDayInteractionSource.collectIsPressedAsState().value
    val isNextDayPressed = nextDayInteractionSource.collectIsPressedAsState().value
    val weatherButtonBackgroundAlpha = animateFloatAsState(
        targetValue = if (isWeatherButtonPressed) {
            if (popupTextColor == Color.White) 0.16f else 0.12f
        } else {
            0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "weatherButtonBackgroundAlpha"
    )
    val previousDayScale = animateFloatAsState(
        targetValue = if (isPreviousDayPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "previousDayScale"
    )
    val nextDayScale = animateFloatAsState(
        targetValue = if (isNextDayPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nextDayScale"
    )
    val previousDayBackground = if (isPreviousDayPressed) {
        navChipColor.copy(alpha = 0.28f)
    } else {
        navChipColor
    }
    val nextDayBackground = if (isNextDayPressed) {
        navChipColor.copy(alpha = 0.28f)
    } else {
        navChipColor
    }

    fun Modifier.dayArrowClickable(
        interactionSource: MutableInteractionSource,
        onClick: () -> Unit
    ): Modifier = clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .align(horizontalAlignment)
                .statusBarsPadding()
                .widthIn(min = buttonContentMinWidth, max = buttonContentMaxWidth)
                .padding(
                    top = buttonTopPadding,
                    start = buttonStartPadding,
                    end = buttonEndPadding
                )
                .then(
                    if (showWeather) {
                        Modifier.clickable(
                            interactionSource = weatherPopupInteractionSource,
                            indication = null
                        ) { }
                    } else {
                        Modifier
                    }
                )
                .background(
                    color = weatherButtonSurfaceColor,
                    shape = RoundedCornerShape(if (compactMode) 24.dp else 28.dp)
                )
                .border(
                    width = 1.dp,
                    color = weatherButtonBorderColor,
                    shape = RoundedCornerShape(if (compactMode) 24.dp else 28.dp)
                )
                .background(
                    color = popupTextColor.copy(alpha = weatherButtonBackgroundAlpha.value),
                    shape = RoundedCornerShape(if (compactMode) 24.dp else 28.dp)
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compactMode) 10.dp else 12.dp,
                        vertical = if (compactMode) 8.dp else 9.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(if (stableLocationName.isNotBlank()) 5.dp else 0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = weatherButtonInteractionSource,
                            indication = null
                        ) { onToggleWeather(!showWeather) },
                    verticalArrangement = Arrangement.spacedBy(if (stableLocationName.isNotBlank()) 5.dp else 0.dp)
                ) {
                    if (stableLocationName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stableLocationName,
                                modifier = Modifier.weight(1f),
                                fontSize = buttonLocationFontSize,
                                lineHeight = buttonLocationLineHeight,
                                fontWeight = FontWeight.SemiBold,
                                color = popupTextColor,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (showWeather) "▴" else "▾",
                                modifier = Modifier.padding(start = 6.dp),
                                fontSize = if (compactMode) 11.sp else 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = weatherButtonChevronColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (compactMode) 7.dp else 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(weatherButtonAccentSurface, CircleShape)
                                .padding(
                                    horizontal = if (compactMode) 8.dp else 9.dp,
                                    vertical = if (compactMode) 6.dp else 7.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = weatherSymbol,
                                fontSize = if (compactMode) 17.sp else 18.sp
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = todayForecast?.let { "${it.tempMax.toInt()}°C" } ?: "...",
                                fontSize = buttonTemperatureFontSize,
                                fontWeight = FontWeight.Bold,
                                color = popupTextColor
                            )
                            AnimatedVisibility(visible = !showWeather) {
                                Text(
                                    text = "Trykk for detaljer",
                                    fontSize = buttonSubtitleFontSize,
                                    fontWeight = FontWeight.Medium,
                                    color = popupSubTextColor
                                )
                            }

                        }
                        if (stableLocationName.isBlank()) {
                            Text(
                                text = if (showWeather) "▴" else "▾",
                                fontSize = if (compactMode) 12.sp else 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = weatherButtonChevronColor
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showWeather,
                    enter = fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) +
                        expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                    exit = fadeOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) +
                        shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(top = if (compactMode) 3.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(popupSpacing)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dividerColor, RoundedCornerShape(999.dp))
                                .padding(vertical = 0.5.dp)
                        ) {
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .scale(previousDayScale.value)
                                    .background(previousDayBackground, CircleShape)
                                    .dayArrowClickable(previousDayInteractionSource) {
                                        val newIndex =
                                            if (selectedDayIndex > 0) selectedDayIndex - 1
                                            else allForecastsSize - 1
                                        onDayChange(newIndex)
                                    }
                                    .padding(
                                        horizontal = if (compactMode) 8.dp else 10.dp,
                                        vertical = if (compactMode) 5.dp else 6.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "←",
                                    fontSize = navArrowFontSize,
                                    color = popupTextColor
                                )
                            }

                            Text(
                                todayForecast?.date ?: "...",
                                fontSize = dateFontSize,
                                color = popupSubTextColor
                            )

                            Box(
                                modifier = Modifier
                                    .scale(nextDayScale.value)
                                    .background(nextDayBackground, CircleShape)
                                    .dayArrowClickable(nextDayInteractionSource) {
                                        val newIndex =
                                            if (selectedDayIndex < allForecastsSize - 1) selectedDayIndex + 1
                                            else 0
                                        onDayChange(newIndex)
                                    }
                                    .padding(
                                        horizontal = if (compactMode) 8.dp else 10.dp,
                                        vertical = if (compactMode) 5.dp else 6.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    fontSize = navArrowFontSize,
                                    color = popupTextColor
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (compactMode) 8.dp else 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (compactMode) 42.dp else 54.dp)
                                    .background(cardSecondarySurface, RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(weatherSymbol, fontSize = iconFontSize)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = todayForecast?.let {
                                        "${it.tempMin.toInt()}° / ${it.tempMax.toInt()}°C"
                                    } ?: "...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = tempFontSize,
                                    color = popupTextColor
                                )

                            }
                        }

                        errorMessage?.let { message ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (popupTextColor == Color.White) {
                                        Color(0xFF4A2327)
                                    } else {
                                        Color(0xFFFFE7E6)
                                    }
                                )
                            ) {
                                Text(
                                    text = message,
                                    color = if (popupTextColor == Color.White) Color(0xFFFFD5D2) else Color(0xFFB42318),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherMetricCard(
                                emoji = "💧",
                                value = todayForecast?.let { "${it.precipitation}mm" } ?: "...",
                                label = "Nedbør",
                                backgroundColor = cardSecondarySurface,
                                valueColor = popupTextColor,
                                labelColor = popupSubTextColor,
                                iconFontSize = metricIconFontSize,
                                valueFontSize = metricTextFontSize,
                                labelFontSize = metricLabelFontSize,
                                horizontalPadding = metricHorizontalPadding,
                                verticalPadding = metricVerticalPadding,
                                contentSpacing = metricSpacing,
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricCard(
                                emoji = "💨",
                                value = todayForecast?.let {
                                    "${it.windSpeed}m/s ${ForecastAggregator.degreesToCompass(it.windDirection)}"
                                } ?: "...",
                                label = "Vind",
                                backgroundColor = cardSecondarySurface,
                                valueColor = popupTextColor,
                                labelColor = popupSubTextColor,
                                iconFontSize = metricIconFontSize,
                                valueFontSize = metricTextFontSize,
                                labelFontSize = metricLabelFontSize,
                                horizontalPadding = metricHorizontalPadding,
                                verticalPadding = metricVerticalPadding,
                                contentSpacing = metricSpacing,
                                modifier = Modifier.weight(1f)
                            )
                            WeatherMetricCard(
                                emoji = "☀",
                                value = uvValueText,
                                label = "UV",
                                backgroundColor = cardSecondarySurface,
                                valueColor = popupTextColor,
                                labelColor = popupSubTextColor,
                                iconFontSize = metricIconFontSize,
                                valueFontSize = metricTextFontSize,
                                labelFontSize = metricLabelFontSize,
                                horizontalPadding = metricHorizontalPadding,
                                verticalPadding = metricVerticalPadding,
                                contentSpacing = metricSpacing,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricCard(
    emoji: String,
    value: String,
    label: String,
    backgroundColor: Color,
    valueColor: Color,
    labelColor: Color,
    iconFontSize: androidx.compose.ui.unit.TextUnit,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    verticalPadding: androidx.compose.ui.unit.Dp,
    contentSpacing: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(18.dp))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(contentSpacing)
    ) {
        Text(text = emoji, fontSize = iconFontSize)
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            text = label,
            fontSize = labelFontSize,
            color = labelColor
        )
    }
}
