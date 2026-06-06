package no.uio.ifi.in2000.aryanma.splaesh.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    bottomPadding: Dp = 0.dp,
    isDarkStyle: Boolean,
    bathingScoreProfile: BathingScoreProfile,
    onDarkStyleChange: (Boolean) -> Unit,
    onBathingScoreProfileChange: (BathingScoreProfile) -> Unit
) {


    val screenBackground = if (isDarkStyle) Color(0xFF101214) else Color(0xFFF3F6F8)
    val panelBackground = if (isDarkStyle) Color(0xFF191C1F) else Color(0xFFFDFDFD)
    val titleColor = if (isDarkStyle) Color.White else Color(0xFF101828)
    val subTitleColor = titleColor
    val segmentedBg = if (isDarkStyle) Color(0xFF22262A) else Color(0xFFE9EEF1)
    val selectedBg = if (isDarkStyle) Color(0xFF78D7E2) else Color.White
    val selectedText = if (isDarkStyle) Color(0xFF062A31) else Color(0xFF101828)
    val unselectedText = if (isDarkStyle) Color(0xFFE5E7EB) else Color(0xFF475467)
    val infoCardBackground = if (isDarkStyle) Color(0xFF15181B) else Color(0xFFF8FBFC)
    val infoBadgeBackground = if (isDarkStyle) Color(0xFF24363A) else Color(0xFFE8F6F8)
    val appName = "Splæsh"
    val appVersion = "1.0"
    val heroBrush = if (isDarkStyle) {
        Brush.verticalGradient(
            listOf(Color(0xFF10242A), Color(0xFF131A1D), Color(0xFF101214))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFDDF4F8), Color(0xFFF0F9FB), Color(0xFFF3F6F8))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .navigationBarsPadding()
            .padding(bottom = bottomPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isDarkStyle) Color(0x3318B7C9) else Color(0xE6FFFFFF)
            ) {

            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Innstillinger",
                color = titleColor,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Velg skjerminnstilling og tilpass appinnstillinger.",
                color = subTitleColor,
                fontSize = 15.sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = panelBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDarkStyle) Color(0xFF24363A) else Color(0xFFE8F6F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗺", fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = "Skjerminnstilling",
                                style = MaterialTheme.typography.titleLarge,
                                color = titleColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bytt mellom lys og mørk skjermvisning.",
                                fontSize = 13.sp,
                                color = subTitleColor
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(27.dp))
                            .background(segmentedBg)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(23.dp))
                                .background(if (!isDarkStyle) selectedBg else Color.Transparent)
                                .clickable {
                                    onDarkStyleChange(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lys",
                                color = if (!isDarkStyle) selectedText else unselectedText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(23.dp))
                                .background(if (isDarkStyle) selectedBg else Color.Transparent)
                                .clickable {
                                    onDarkStyleChange(true)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mørk",
                                color = if (isDarkStyle) selectedText else unselectedText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = infoCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nåværende valg",
                        color = titleColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isDarkStyle) "Mørk skjerminnstilling er aktiv" else "Lys skjerminnstilling er aktiv",
                        color = titleColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Valget lagres på enheten og brukes videre i appen.",
                        color = titleColor,
                        fontSize = 13.sp
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = infoCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(infoBadgeBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏊", fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = "Badeforhold-score",
                                style = MaterialTheme.typography.titleLarge,
                                color = titleColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Velg hvordan appen skal vurdere badeforhold.",
                                color = subTitleColor,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BathingScoreProfile.entries.forEach { profile ->
                            val isSelected = profile == bathingScoreProfile
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isDarkStyle) Color(0xFF22313A) else Color(0xFFEAF4F8)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable { onBathingScoreProfileChange(profile) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = profile.title,
                                        color = titleColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = profile.shortDescription,
                                        color = subTitleColor,
                                        fontSize = 13.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            if (isSelected) {
                                                if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
                                            } else {
                                                if (isDarkStyle) Color(0x3324363A) else Color(0x1F101828)
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = infoCardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(infoBadgeBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ℹ️", fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = "Om appen",
                                style = MaterialTheme.typography.titleLarge,
                                color = titleColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$appName • versjon $appVersion",
                                color = subTitleColor,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        text = "Appen gjør det enklere å finne badeplasser, vurdere forholdene og få oversikt over vær, sjødata og farevarsler før man drar ut.",
                        color = titleColor,
                        fontSize = 14.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Datakilder",
                            color = subTitleColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        InfoLine(
                            label = "Kart og vær",
                            value = "MET Norway / Victoria WMS",
                            titleColor = titleColor,
                            subTitleColor = subTitleColor
                        )
                        InfoLine(
                            label = "Sjødata",
                            value = "OceanForecast",
                            titleColor = titleColor,
                            subTitleColor = subTitleColor
                        )
                        InfoLine(
                            label = "UV",
                            value = "Open-Meteo",
                            titleColor = titleColor,
                            subTitleColor = subTitleColor
                        )
                        InfoLine(
                            label = "Farevarsler",
                            value = "MET Norway",
                            titleColor = titleColor,
                            subTitleColor = subTitleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String,
    titleColor: Color,
    subTitleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = subTitleColor,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = titleColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
