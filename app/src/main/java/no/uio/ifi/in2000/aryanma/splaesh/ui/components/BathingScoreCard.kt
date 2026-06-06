package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScore

@Composable
fun BathingScoreCard(
    score: BathingScore,
    isDarkStyle: Boolean,
    backgroundColor: Color,
    titleColor: Color,
    modifier: Modifier = Modifier,
    alwaysExpanded: Boolean = false
) {
    var showDetails by rememberSaveable(score.score, score.label, score.summary, score.primaryReason, score.secondaryReason, score.isUnavailable) {
        mutableStateOf(false)
    }
    val hasExpandableDetails = remember(score.primaryReason, score.secondaryReason, score.isUnavailable) {
        !score.isUnavailable && (score.primaryReason.isNotBlank() || !score.secondaryReason.isNullOrBlank())
    }
    val accentColor = when {
        score.isUnavailable -> if (isDarkStyle) Color(0xFFB8C1CC) else Color(0xFF475467)
        score.score >= 85 -> if (isDarkStyle) Color(0xFF9AE9F2) else Color(0xFF0E7490)
        score.score >= 70 -> if (isDarkStyle) Color(0xFF86EFAC) else Color(0xFF15803D)
        score.score >= 55 -> if (isDarkStyle) Color(0xFFFDE68A) else Color(0xFFA16207)
        else -> if (isDarkStyle) Color(0xFFFCA5A5) else Color(0xFFB42318)
    }
    val badgeBackground = accentColor.copy(alpha = if (isDarkStyle) 0.18f else 0.12f)
    val metaTextColor = if (isDarkStyle) Color.White else Color(0xD9000000)

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .then(
                if (hasExpandableDetails && !alwaysExpanded) {
                    Modifier.clickable { showDetails = !showDetails }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Badeforhold-score",
                        color = metaTextColor,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (score.isUnavailable) "—" else score.score.toString(),
                            color = accentColor,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (score.isUnavailable) "" else "/100",
                            color = metaTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(badgeBackground, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = score.label,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = score.summary,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            val detailText = score.secondaryReason?.let { "${score.primaryReason} $it" } ?: score.primaryReason

            if (alwaysExpanded || score.isUnavailable) {
                Text(
                    text = detailText,
                    color = titleColor,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                if (hasExpandableDetails) {
                    Text(
                        text = if (showDetails) "Skjul detaljer" else "Vis detaljer",
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(
                    visible = showDetails,
                    enter = fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
                ) {
                    Text(
                        text = detailText,
                        color = titleColor,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
