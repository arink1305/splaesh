package no.uio.ifi.in2000.aryanma.splaesh.ui.favorites

import androidx.compose.animation.AnimatedVisibility
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.setValue

import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.BeachLiveInfoCards
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.getSeverityForBath
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.mapSeverity
import no.uio.ifi.in2000.aryanma.splaesh.utils.farevarselPolygon

@Composable
fun FavoritesScreen(
    favorites: List<Location>,
    warnings: List<Warning>,
    bathingScoreProfile: BathingScoreProfile,
    pendingRemovalIds: List<Int> = emptyList(),
    onPendingRemovalIdsChange: (List<Int>) -> Unit = {},
    onOpenLocationOnMap: (Location) -> Unit = {},
    viewModel: FavoritesViewModel = viewModel()
) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
    val listState = rememberLazyListState()
    val prefs = remember(context) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }
    val isDarkStyle = prefs.getBoolean("map_style_is_dark", false)

    LaunchedEffect(favorites) {
        viewModel.loadDetailsForAll(favorites)
    }

    val detailsById by viewModel.detailsById.collectAsState()

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
    val cardBackground   = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)
    val titleColor       = if (isDarkStyle) Color.White      else Color(0xFF101828)
    val subTitleColor    = titleColor
    val accent           = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
    val defaultHeartColor = Color(0xFF0E7490)
    val overlayBrush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0x24000000), Color(0xD9000000))
    )
    val headerText = if (isDarkStyle) Color.White else Color(0xFF0B1220)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .background(screenBrush),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = if (isLandscape) 10.dp else 16.dp,
            bottom = 100.dp
        )
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
                        .fillMaxWidth(if (isLandscape) 0.78f else 0.96f)
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
                            text = "${favorites.size} lagrede",
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                            color = if (isDarkStyle) Color.White else accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

                    Text(
                        text = "Favoritter",
                        color = headerText,
                        style = if (isLandscape) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Dine favorittbadeplasser, klare med UV, sjøforhold og farestatus.",
                        color = subTitleColor,
                        fontSize = if (isLandscape) 13.sp else 15.sp,
                        lineHeight = if (isLandscape) 17.sp else 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))
        }

        if (favorites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkStyle) Color(0xFF1F2937) else Color(0xFFE6F7FB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("♡", fontSize = 28.sp, color = defaultHeartColor)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Ingen favoritter enda",
                                color = titleColor,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Legg til badeplasser fra kartet for rask tilgang til UV, sjøforhold og farevarsler.",
                                color = subTitleColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(favorites, key = { it.id }) { location ->
                val details = detailsById[location.id]
                val isPendingRemoval = location.id in pendingRemovalIds
                var isExpanded by rememberSaveable(location.id) { mutableStateOf(false) }
                val heartColor by animateColorAsState(
                    targetValue = if (isPendingRemoval) defaultHeartColor else Color(0xFFE53935),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "favoriteHeartColor"
                )
                val heartScale by animateFloatAsState(
                    targetValue = if (isPendingRemoval) 0.94f else 1.08f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "favoriteHeartScale"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(188.dp)
                        ) {
                            AsyncImage(
                                model = location.image,
                                contentDescription = location.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(overlayBrush)
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onPendingRemovalIdsChange(
                                                if (isPendingRemoval) {
                                                    pendingRemovalIds - location.id
                                                } else {
                                                    (pendingRemovalIds + location.id).distinct()
                                                }
                                            )
                                        },
                                    shape = CircleShape,
                                    color = Color(0xE6FFFFFF)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (isPendingRemoval) "♡" else "♥",
                                            modifier = Modifier.scale(heartScale),
                                            color = heartColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(14.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onOpenLocationOnMap(location)
                                    },
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xE6FFFFFF),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "Gå til kart",
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                    color = Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = location.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kredittering: ${location.source}",
                                    color = Color(0xFFF2F4F7),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-22).dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Surface(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isExpanded = !isExpanded
                                    },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isDarkStyle) Color(0xFF0F1F24) else Color(0xFFF7FCFD),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkStyle) Color(0x333DD5E5) else Color(0x1F0E7490)
                                ),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Text(
                                        text = if (isExpanded) "Skjul liveinfo" else "Vis liveinfo",
                                        color = if (isDarkStyle) Color.White else accent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) {
                                            Icons.Default.KeyboardArrowUp
                                        } else {
                                            Icons.Default.KeyboardArrowDown
                                        },
                                        contentDescription = null,
                                        tint = if (isDarkStyle) Color.White else accent
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                    expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Oppdatert oversikt for stedet akkurat nå.",
                                    color = subTitleColor,
                                    fontSize = 13.sp
                                )

                                FavoritesPopupCards(
                                    location = location,
                                    warnings = warnings,
                                    seaInfo = details?.seaInfo,
                                    isDarkStyle = isDarkStyle,
                                    uv = details?.uvValue,
                                    airTemperature = details?.airTemperature,
                                    bathingScoreProfile = bathingScoreProfile
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FavoritesPopupCards(
    location: Location?,
    warnings: List<Warning>,
    seaInfo: SeaInfo?,
    isDarkStyle: Boolean,
    uv: Double?,
    airTemperature: Double?,
    bathingScoreProfile: BathingScoreProfile
) {
    if (location == null) return

    val description = warnings.firstOrNull { warning ->
        warning.coordinates.any { ring ->
            farevarselPolygon(location.latitude, location.longitude, ring)
        }
    }?.description

    val severity = mapSeverity(
        getSeverityForBath(
            location.latitude,
            location.longitude,
            warnings
        )
    )
    val hasWarning = severity == "red" || severity == "yellow"
    val warningText = description ?: "Farevarsel mangler detaljert tekst."

    BeachLiveInfoCards(
        isDarkStyle = isDarkStyle,
        bathingScoreProfile = bathingScoreProfile,
        warningSeverity = severity,
        warningText = if (hasWarning) warningText else null,
        seaInfo = seaInfo,
        uv = uv,
        airTemperature = airTemperature
    )
}
