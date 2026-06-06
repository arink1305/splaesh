package no.uio.ifi.in2000.aryanma.splaesh.ui.recommendations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.RecommendationPlace
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.BeachLiveInfoCards

@Composable
fun RecommendationPlaceCard(
    recommendation: RecommendationPlace,
    isDarkStyle: Boolean,
    bathingScoreProfile: BathingScoreProfile,
    isFavorite: Boolean,
    onToggleFavorite: (Location) -> Unit,
    onOpenLocationOnMap: (Location) -> Unit
) {
    var isExpanded by rememberSaveable(recommendation.location.id) { mutableStateOf(false) }
    val overlayBrush = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0x24000000), Color(0xD9000000))
    )
    val cardBackground = if (isDarkStyle) Color(0xFF171717) else Color(0xFFFCFCFD)
    val accent = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
    val scoreChipTextColor = Color(0xFF0F172A)
    val defaultHeartColor = Color(0xFF0E7490)
    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFFE53935) else defaultHeartColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "recommendationHeartColor"
    )
    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "recommendationHeartScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, if (isDarkStyle) Color(0x22FFFFFF) else Color(0x12000000))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
            ) {
                AsyncImage(
                    model = recommendation.location.image,
                    contentDescription = recommendation.location.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(188.dp)
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(overlayBrush)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onToggleFavorite(recommendation.location)
                            },
                        shape = CircleShape,
                        color = Color(0xE6FFFFFF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isFavorite) "♥" else "♡",
                                modifier = Modifier.scale(heartScale),
                                color = heartColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xE6FFFFFF)
                    ) {
                        Text(
                            text = "${recommendation.score.score}/100",
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                            color = scoreChipTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                            onOpenLocationOnMap(recommendation.location)
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
                        text = recommendation.location.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatDistanceKm(recommendation.distanceKm)} unna • ${recommendation.location.source}",
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
                    modifier = Modifier.clickable(
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
                            color = if (isDarkStyle) Color.White else Color(0xFF0E7490),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.material3.Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = if (isDarkStyle) Color.White else Color(0xFF0E7490)
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
                    BeachLiveInfoCards(
                        isDarkStyle = isDarkStyle,
                        bathingScoreProfile = bathingScoreProfile,
                        warningSeverity = recommendation.warningSeverity,
                        warningText = recommendation.warningDescription,
                        seaInfo = recommendation.seaInfo,
                        uv = recommendation.uvValue,
                        airTemperature = recommendation.airTemperature
                    )
                }
            }
        }
    }
}

private fun formatDistanceKm(distanceKm: Double): String {
    return when {
        distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m"
        distanceKm < 10 -> String.format(java.util.Locale.US, "%.1f km", distanceKm)
        else -> "${distanceKm.toInt()} km"
    }
}
