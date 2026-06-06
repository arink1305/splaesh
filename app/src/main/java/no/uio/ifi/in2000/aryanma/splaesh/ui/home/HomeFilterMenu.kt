package no.uio.ifi.in2000.aryanma.splaesh.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.RecommendationPlace
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.getSeverityForBath
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.mapSeverity

data class HomeMapFilters(
    val favoritesOnly: Boolean = false,
    val safeOnly: Boolean = false,
    val warningOnly: Boolean = false,
    val minimumBathingScore: Int = 0
) {
    val hasActiveFilters: Boolean
        get() = favoritesOnly || safeOnly || warningOnly || minimumBathingScore > 0
}

fun filterHomeLocations(
    locations: List<Location>,
    favorites: List<Location>,
    warnings: List<Warning>,
    filters: HomeMapFilters,
    bathingScoresByLocationId: Map<Int, Int>
): List<Location> {
    if (!filters.hasActiveFilters) return locations

    val favoriteIds = favorites.map { it.id }.toSet()

    return locations.filter { location ->
        val severity = mapSeverity(
            getSeverityForBath(location.latitude, location.longitude, warnings)
        )
        val isFavorite = location.id in favoriteIds
        val hasWarning = severity == "red" || severity == "yellow"
        val isSafe = severity == "blue"
        val bathingScore = bathingScoresByLocationId[location.id]

        (!filters.favoritesOnly || isFavorite) &&
            (!filters.safeOnly || isSafe) &&
            (!filters.warningOnly || hasWarning) &&
            (filters.minimumBathingScore == 0 ||
                (bathingScore != null && bathingScore >= filters.minimumBathingScore))
    }
}

fun filterRecommendations(
    recommendations: List<RecommendationPlace>,
    favorites: List<Location>,
    filters: HomeMapFilters
): List<RecommendationPlace> {
    if (!filters.hasActiveFilters) return recommendations

    val favoriteIds = favorites.map { it.id }.toSet()

    return recommendations.filter { recommendation ->
        val severity = mapSeverity(recommendation.warningSeverity)
        val isFavorite = recommendation.location.id in favoriteIds
        val hasWarning = severity == "red" || severity == "yellow"
        val isSafe = severity == "blue"

        (!filters.favoritesOnly || isFavorite) &&
            (!filters.safeOnly || isSafe) &&
            (!filters.warningOnly || hasWarning) &&
            (filters.minimumBathingScore == 0 ||
                recommendation.score.score >= filters.minimumBathingScore)
    }
}

@Composable
fun HomeFilterMenu(
    expanded: Boolean,
    filters: HomeMapFilters,
    totalCount: Int,
    visibleCount: Int,
    isLoadingBathingScores: Boolean,
    bathingScoreProfile: BathingScoreProfile,
    isLandscape: Boolean,
    isDarkStyle: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFiltersChange: (HomeMapFilters) -> Unit
) {
    val menuButtonInteractionSource = remember { MutableInteractionSource() }
    val filterAccent = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
    val menuButtonScale by animateFloatAsState(
        targetValue = if (expanded) 0.97f else 1f,
        animationSpec = spring(),
        label = "filterMenuButtonScale"
    )
    val buttonContainer by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0xFF243845)
            expanded && !isDarkStyle -> Color(0xFFE4F5F8)
            isDarkStyle -> Color(0xD9232A32)
            else -> Color(0xCCFFFFFF)
        },
        label = "filterMenuButtonContainer"
    )
    val buttonBorder by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0x6678D7E2)
            expanded && !isDarkStyle -> Color(0x4D0E7490)
            isDarkStyle -> Color(0x335E6A75)
            else -> Color(0x1F101828)
        },
        label = "filterMenuButtonBorder"
    )
    val buttonIconTint by animateColorAsState(
        targetValue = when {
            expanded && isDarkStyle -> Color(0xFF9AE9F2)
            expanded && !isDarkStyle -> Color(0xFF0E7490)
            isDarkStyle -> Color(0xFF78D7E2)
            else -> Color(0xFF0E7490)
        },
        label = "filterMenuButtonIconTint"
    )

    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onExpandedChange(false) }
        )
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(
                top = if (isLandscape) 82.dp else 110.dp,
                start = 16.dp,
                end = 0.dp
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
            border = BorderStroke(1.dp, buttonBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = if (expanded) Icons.Default.ArrowDownward else Icons.Default.Tune,
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
                    label = "homeFilterMenuIcon"
                ) { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = if (expanded) "Lukk filter" else "Filtrer badeplasser",
                        tint = buttonIconTint,
                        modifier = Modifier
                            .width(26.dp)
                            .size(26.dp)
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
            val scrollState = rememberScrollState()
            Card(
                modifier = if (isLandscape) {
                    Modifier
                        .width(300.dp)
                        .heightIn(max = 360.dp)
                } else {
                    Modifier.width(320.dp)
                },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkStyle) Color(0xEE11181F) else Color(0xF7FFFFFF)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDarkStyle) Color(0x2478D7E2) else Color(0x120E7490)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column {
                    Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = if (isLandscape) 300.dp else 420.dp)
                        .verticalScroll(scrollState)
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Filtrer badeplasser",
                                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$visibleCount av $totalCount vises",
                                color = if (isDarkStyle) Color(0xFFB8C1CC) else Color(0xFF475467),
                                fontSize = 12.sp
                            )
                        }

                        FilterChipRow(
                            label = "Favoritter",
                            icon = Icons.Default.Favorite,
                            selected = filters.favoritesOnly,
                            activeColor = filterAccent,
                            isDarkStyle = isDarkStyle
                        ) {
                            onFiltersChange(filters.copy(favoritesOnly = !filters.favoritesOnly))
                        }

                        FilterChipRow(
                            label = "Trygge",
                            icon = Icons.Default.Security,
                            selected = filters.safeOnly,
                            activeColor = filterAccent,
                            isDarkStyle = isDarkStyle
                        ) {
                            onFiltersChange(
                                filters.copy(
                                    safeOnly = !filters.safeOnly,
                                    warningOnly = false,
                                    minimumBathingScore = filters.minimumBathingScore
                                )
                            )
                        }

                        FilterChipRow(
                            label = "Farevarsel",
                            icon = Icons.Default.Warning,
                            selected = filters.warningOnly,
                            activeColor = filterAccent,
                            isDarkStyle = isDarkStyle
                        ) {
                            val nextWarningOnly = !filters.warningOnly
                            onFiltersChange(
                                filters.copy(
                                    warningOnly = nextWarningOnly,
                                    safeOnly = false,
                                    minimumBathingScore = filters.minimumBathingScore
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Minimum badescore",
                                color = if (isDarkStyle) Color.White else Color(0xFF101828),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isLoadingBathingScores) {
                                    "Laster scoredata for badeplassene..."
                                } else {
                                    "Bruker profilen ${bathingScoreProfile.title.lowercase()}."
                                },
                                color = if (isDarkStyle) Color(0xFFB8C1CC) else Color(0xFF475467),
                                fontSize = 12.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0 to "Alle", 40 to "40+", 60 to "60+", 80 to "80+").forEach { (score, label) ->
                                    ScoreFilterChip(
                                        label = label,
                                        selected = filters.minimumBathingScore == score,
                                        activeColor = filterAccent,
                                        isDarkStyle = isDarkStyle,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        onFiltersChange(filters.copy(minimumBathingScore = score))
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ResetFilterChip(
                                enabled = filters.hasActiveFilters,
                                isDarkStyle = isDarkStyle,
                                onClick = { onFiltersChange(HomeMapFilters()) }
                            )
                        }

                        Spacer(
                            modifier = Modifier.size(
                                width = 1.dp,
                                height = if (isLandscape) 96.dp else 24.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    activeColor: Color,
    isDarkStyle: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val baseSurfaceColor = when {
        selected && isDarkStyle -> activeColor.copy(alpha = 0.22f)
        selected && !isDarkStyle -> activeColor.copy(alpha = 0.14f)
        isDarkStyle -> Color(0xFF172028)
        else -> Color(0xFFF7FAFC)
    }
    val chipColor by animateColorAsState(
        targetValue = if (isPressed) {
            if (isDarkStyle) baseSurfaceColor.copy(alpha = 0.96f) else Color(0xFFEFF7FA)
        } else {
            baseSurfaceColor
        },
        label = "homeFilterChipColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> activeColor.copy(alpha = 0.45f)
            isDarkStyle -> Color(0x1FFFFFFF)
            else -> Color(0x14000000)
        },
        label = "homeFilterChipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> activeColor
            isDarkStyle -> Color.White
            else -> Color(0xFF344054)
        },
        label = "homeFilterChipText"
    )
    val chipScale by animateFloatAsState(
        targetValue = if (isPressed) 0.955f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "homeFilterChipScale"
    )
    val chipVerticalPadding by animateFloatAsState(
        targetValue = if (isPressed) 9f else 10f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "homeFilterChipPadding"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(chipScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = chipColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = chipVerticalPadding.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScoreFilterChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    isDarkStyle: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val baseSurfaceColor = when {
        selected && isDarkStyle -> activeColor.copy(alpha = 0.18f)
        selected && !isDarkStyle -> activeColor.copy(alpha = 0.10f)
        isDarkStyle -> Color(0xFF172028)
        else -> Color(0xFFF7FAFC)
    }
    val chipColor by animateColorAsState(
        targetValue = if (isPressed) {
            if (isDarkStyle) baseSurfaceColor.copy(alpha = 0.96f) else Color(0xFFEFF7FA)
        } else {
            baseSurfaceColor
        },
        label = "homeScoreFilterChipColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> activeColor.copy(alpha = 0.36f)
            isDarkStyle -> Color(0x1FFFFFFF)
            else -> Color(0x14000000)
        },
        label = "homeScoreFilterChipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> activeColor
            isDarkStyle -> Color.White
            else -> Color(0xFF344054)
        },
        label = "homeScoreFilterChipText"
    )

    Surface(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = chipColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResetFilterChip(
    enabled: Boolean,
    isDarkStyle: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val chipScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.955f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "homeResetChipScale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            enabled && isDarkStyle -> Color(0x1828B4C7)
            enabled && !isDarkStyle -> Color(0x140E7490)
            isDarkStyle -> Color(0x10FFFFFF)
            else -> Color(0x080E7490)
        },
        label = "homeResetChipColor"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            enabled && isDarkStyle -> Color(0xFF9AE9F2)
            enabled && !isDarkStyle -> Color(0xFF0E7490)
            isDarkStyle -> Color(0xFF667085)
            else -> Color(0xFF98A2B3)
        },
        label = "homeResetChipText"
    )

    Surface(
        modifier = Modifier
            .scale(chipScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() },
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor
    ) {
        Text(
            text = "Nullstill",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
