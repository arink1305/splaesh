package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun TimeScroller(
    visible: Boolean,
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    bottomOffset: Dp = 160.dp,
    compactMode: Boolean = false,
    isDarkStyle: Boolean,
    locationName: String,
    selectedTimeIndex: Int,
    onTimeChange: (Int) -> Unit,
    baseCalendar: Calendar
) {
    val scrollerSub = if (isDarkStyle) Color(0xFFBDBDBD) else MaterialTheme.colorScheme.onSurface
    val cardBg = if (isDarkStyle) Color(0xF0222222) else Color(0xF2FFFFFF)
    val activeBg = if (isDarkStyle) Color(0xFF42A5F5) else Color(0xFF1976D2)
    val activeText = Color.White
    val inactiveBg = if (isDarkStyle) Color(0xFF333333) else Color(0xFFEEEEEE)
    val inactiveBorder = if (isDarkStyle) Color(0xFF555555) else Color(0xFFCCCCCC)

    val scrollState = rememberScrollState()
    val currentDayOffset = remember(selectedTimeIndex) { selectedTimeIndex / 24 }
    val currentHour = remember(selectedTimeIndex) { selectedTimeIndex % 24 }
    var hasInitialized by remember { mutableIntStateOf(0) }
    val isToday = currentDayOffset == 0
    val isTomorrow = currentDayOffset == 1
    val norwegianLocale = remember { Locale.forLanguageTag("nb-NO") }
    val cardHeight = if (compactMode) 112.dp else 130.dp
    val verticalPadding = if (compactMode) 8.dp else 10.dp
    val horizontalPadding = if (compactMode) 10.dp else 12.dp
    val arrowBoxSize = if (compactMode) 28.dp else 32.dp
    val arrowIconSize = if (compactMode) 18.dp else 20.dp
    val headerFontSize = if (compactMode) 13.sp else 14.sp
    val modelFontSize = if (compactMode) 9.sp else 10.sp
    val timeChipHorizontalPadding = if (compactMode) 11.dp else 14.dp
    val timeChipVerticalPadding = if (compactMode) 8.dp else 10.dp
    val timeChipFontSize = if (compactMode) 12.sp else 13.sp
    val sectionSpacing = if (compactMode) 8.dp else 10.dp
    val footerSpacing = if (compactMode) 6.dp else 8.dp
    val hourSpacerWidth = if (compactMode) 6.dp else 8.dp
    val edgeSpacerWidth = if (compactMode) 8.dp else 12.dp

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.padding(bottom = bottomPadding + bottomOffset, start = 8.dp, end = 8.dp),
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(200))
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.height(cardHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding, horizontal = horizontalPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(arrowBoxSize).clip(RoundedCornerShape(8.dp)).background(if (currentDayOffset > 0) { if (isDarkStyle) Color(0xFF333333) else Color(0xFFE0E0E0) } else { Color.Transparent }).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = currentDayOffset > 0) { if (currentDayOffset > 0) { onTimeChange((currentDayOffset - 1) * 24 + currentHour) } }, contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Forrige dag", tint = if (currentDayOffset > 0) { if (isDarkStyle) Color.White else Color(0xFF424242) } else { scrollerSub }, modifier = Modifier.size(arrowIconSize))
                        }
                        val displayDate = remember(selectedTimeIndex, baseCalendar.timeInMillis) {
                            val df = SimpleDateFormat("EEEE dd. MMMM", norwegianLocale).apply { timeZone = TimeZone.getTimeZone("UTC") }
                            val cal = (baseCalendar.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, selectedTimeIndex) }
                            val raw = df.format(cal.time)
                            raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    isToday -> "I dag"
                                    isTomorrow -> "I morgen"
                                    else -> displayDate
                                },
                                fontSize = headerFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkStyle) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            val modelNotice = if (selectedTimeIndex > 60) "ECMWF 6t" else "MEPS 1t"
                            val noticeColor = if (selectedTimeIndex > 60) Color(0xFFFF9800) else activeBg
                            Text(text = modelNotice, fontSize = modelFontSize, fontWeight = FontWeight.Medium, color = noticeColor)
                        }
                        Box(modifier = Modifier.size(arrowBoxSize).clip(RoundedCornerShape(8.dp)).background(if (currentDayOffset < 9) { if (isDarkStyle) Color(0xFF333333) else Color(0xFFE0E0E0) } else { Color.Transparent }).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = currentDayOffset < 9) { if (currentDayOffset < 9) { onTimeChange((currentDayOffset + 1) * 24 + currentHour) } }, contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Neste dag", tint = if (currentDayOffset < 9) { if (isDarkStyle) Color.White else Color(0xFF424242) } else { scrollerSub }, modifier = Modifier.size(arrowIconSize))
                        }
                    }
                    Spacer(Modifier.height(sectionSpacing))

                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(edgeSpacerWidth))
                        for (hour in 0 until 24) {
                            val isSelected = currentHour == hour
                            val timeLabel = remember(hour) { String.format(norwegianLocale, "%02d:00", hour) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) activeBg else inactiveBg)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) activeBg else inactiveBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onTimeChange((currentDayOffset * 24) + hour) }
                                    .padding(horizontal = timeChipHorizontalPadding, vertical = timeChipVerticalPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = timeLabel,
                                    fontSize = timeChipFontSize,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) activeText else { if (isDarkStyle) Color.White else Color(0xFF424242) }
                                )
                            }
                            Spacer(Modifier.width(hourSpacerWidth))
                        }
                        Spacer(Modifier.width(edgeSpacerWidth))
                    }
                    Spacer(Modifier.height(footerSpacing))

                    val timeNow = remember {
                        val df = SimpleDateFormat("HH:00", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                        df.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
                    }
                    Text(text = "Nå: $timeNow", fontSize = if (compactMode) 9.sp else 10.sp, color = scrollerSub)
                }

                Text(
                    text = locationName,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = arrowBoxSize + 6.dp),
                    fontSize = if (compactMode) 9.sp else 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = scrollerSub,
                    maxLines = 1
                )
            }
        }
    }
    LaunchedEffect(visible) {
        if (visible && hasInitialized == 0) {
            hasInitialized = 1
            val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val baseTime = baseCalendar.timeInMillis
            val diffMillis = now.timeInMillis - baseTime
            val diffHours = (diffMillis / (1000 * 60 * 60)).toInt()
            val startHour = diffHours.coerceIn(0, 239)
            onTimeChange(startHour)
            val hourOfDay = startHour % 24
            val scrollPos = hourOfDay * 58
            scrollState.scrollTo(scrollPos)
        }
    }
    LaunchedEffect(currentDayOffset) {
        val scrollPos = currentHour * 58
        scrollState.animateScrollTo(scrollPos)
    }
}
