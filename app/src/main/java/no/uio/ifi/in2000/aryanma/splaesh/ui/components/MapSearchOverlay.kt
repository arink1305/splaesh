package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import android.content.Context
import android.location.Geocoder
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.aryanma.splaesh.data.LocationRepository
import java.text.Normalizer
import java.util.Locale

private data class SearchSuggestion(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double
)

@Composable
fun MapSearchOverlay(
    context: Context,
    isDarkStyle: Boolean,
    modifier: Modifier = Modifier,
    topOffset: androidx.compose.ui.unit.Dp = 92.dp,
    sidePadding: androidx.compose.ui.unit.Dp = 16.dp,
    onExpandedChange: (Boolean) -> Unit = {},
    onLocationSelected: (name: String, latitude: Double, longitude: Double) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    val suggestions = remember { mutableStateListOf<SearchSuggestion>() }
    val scope = rememberCoroutineScope()
    val localLocations = remember(context) { LocationRepository(context).getLocation() }

    val collapsedBg = if (isDarkStyle) Color(0x8A1B222A) else Color(0xB8FFFFFF)
    val expandedBg = if (isDarkStyle) Color(0xE6161B20) else Color(0xF2FFFFFF)
    val textColor = if (isDarkStyle) Color.White else Color(0xFF101828)
    val secondaryTextColor = textColor
    val accent = if (isDarkStyle) Color(0xFF78D7E2) else Color(0xFF0E7490)
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val searchFieldFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "searchButtonScale"
    )

    LaunchedEffect(expanded) {
        onExpandedChange(expanded)
        if (expanded) {
            searchFieldFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun searchLocations() {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            suggestions.clear()
            errorMessage = "Skriv inn et sted for å søke."
            return
        }

        scope.launch {
            isSearching = true
            errorMessage = null
            val results = withContext(Dispatchers.IO) {
                runCatching {
                    val localMatches = localLocations
                        .filter { location ->
                            location.name.normalizeForSearch().contains(trimmedQuery.normalizeForSearch())
                        }
                        .sortedBy { it.name.length }
                        .take(6)
                        .map { location ->
                            SearchSuggestion(
                                name = location.name,
                                subtitle = "Badeplass",
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }

                    val geocoder = Geocoder(context, Locale.forLanguageTag("nb-NO"))
                    val searchQueries = listOf(
                        trimmedQuery,
                        trimmedQuery.toNorwegianSearchVariant(),
                        "$trimmedQuery, Norge",
                        "${trimmedQuery.toNorwegianSearchVariant()}, Norge",
                        "$trimmedQuery, Norway"
                    ).map { it.trim() }.filter { it.isNotBlank() }.distinct()

                    val geocoderMatches = searchQueries.flatMap { searchQuery ->
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocationName(searchQuery, 6)
                            ?.mapNotNull { address ->
                                val latitude = address.latitude
                                val longitude = address.longitude
                                val title = address.displayName(trimmedQuery)
                                if (title.isBlank()) {
                                    null
                                } else {
                                    SearchSuggestion(
                                        name = title,
                                        subtitle = address.secondaryDescription(title),
                                        latitude = latitude,
                                        longitude = longitude
                                    )
                                }
                            }
                            .orEmpty()
                    }

                    (localMatches + geocoderMatches)
                        .distinctBy { "${it.name}-${it.subtitle}-${it.latitude}-${it.longitude}" }
                }.getOrElse { emptyList() }
            }
            suggestions.clear()
            suggestions.addAll(results)
            if (results.isEmpty()) {
                errorMessage = "Ingen steder funnet. Prøv et annet navn."
            }
            isSearching = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expanded = false
                        suggestions.clear()
                        errorMessage = null
                    }
            )
        }

        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )).togetherWith(
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleOut(
                            targetScale = 0.96f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                ).using(
                    SizeTransform(clip = false)
                )
            },
            label = "searchOverlayTransition"
        ) { isExpanded ->
            if (!isExpanded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = topOffset, start = sidePadding, end = sidePadding)
                        .scale(buttonScale)
                        .size(52.dp)
                        .background(collapsedBg, CircleShape)
                        .clickable(
                            interactionSource = buttonInteractionSource,
                            indication = null
                        ) { expanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Søk etter sted",
                        tint = accent.copy(alpha = 0.92f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = expandedBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = topOffset, start = sidePadding, end = sidePadding)
                        .width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Søk sted",
                                color = textColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = {
                                    expanded = false
                                    query = ""
                                    suggestions.clear()
                                    errorMessage = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Lukk søk",
                                    tint = secondaryTextColor
                                )
                            }
                        }

                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                errorMessage = null
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchLocations() }),
                            label = { Text("By, sted eller område") },
                            trailingIcon = {
                                IconButton(onClick = ::searchLocations) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Start søk",
                                        tint = accent
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accent,
                                unfocusedBorderColor = secondaryTextColor,
                                focusedLabelColor = accent,
                                unfocusedLabelColor = secondaryTextColor,
                                cursorColor = accent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFieldFocusRequester),
                        )

                        when {
                            isSearching -> {
                                Text(
                                    text = "Søker...",
                                    color = secondaryTextColor
                                )
                            }

                            errorMessage != null -> {
                                Text(
                                    text = errorMessage!!,
                                    color = if (isDarkStyle) Color(0xFFFFB4AB) else Color(0xFFB42318)
                                )
                            }

                            suggestions.isNotEmpty() -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    suggestions.forEach { suggestion ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onLocationSelected(
                                                        suggestion.name,
                                                        suggestion.latitude,
                                                        suggestion.longitude
                                                    )
                                                    expanded = false
                                                    query = suggestion.name
                                                    suggestions.clear()
                                                    errorMessage = null
                                                }
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = suggestion.name,
                                                color = textColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (suggestion.subtitle.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = suggestion.subtitle,
                                                    color = secondaryTextColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun android.location.Address.displayName(query: String): String {
    val normalizedQuery = query.normalizeForSearch()
    val candidates = listOfNotNull(
        featureName?.cleanAddressPart(),
        premises?.cleanAddressPart(),
        subLocality?.cleanAddressPart(),
        thoroughfare?.cleanAddressPart(),
        locality?.cleanAddressPart(),
        subAdminArea?.cleanAddressPart(),
        adminArea?.cleanAddressPart()
    ).distinct()

    candidates.firstOrNull { it.normalizeForSearch().contains(normalizedQuery) }?.let { return it }
    return candidates.firstOrNull().orEmpty()
}

private fun android.location.Address.secondaryDescription(primaryName: String): String {
    return listOfNotNull(
        subLocality?.cleanAddressPart(),
        locality?.cleanAddressPart(),
        subAdminArea?.cleanAddressPart(),
        adminArea?.cleanAddressPart(),
        countryName?.cleanAddressPart()
    )
        .distinct()
        .filterNot { it.equals(primaryName, ignoreCase = true) }
        .joinToString(", ")
}

private fun String.cleanAddressPart(): String? {
    val trimmed = trim()
    return trimmed.takeIf { it.isNotBlank() }
}

private fun String.normalizeForSearch(): String {
    val lowered = lowercase(Locale.forLanguageTag("nb-NO"))
        .replace("æ", "ae")
        .replace("ø", "o")
        .replace("å", "a")
    val decomposed = Normalizer.normalize(lowered, Normalizer.Form.NFD)
    return decomposed.replace(Regex("\\p{Mn}+"), "")
}

private fun String.toNorwegianSearchVariant(): String {
    return if (containsNorwegianLetters()) {
        replace("æ", "ae", ignoreCase = true)
            .replace("ø", "o", ignoreCase = true)
            .replace("å", "aa", ignoreCase = true)
    } else {
        replace("ae", "æ", ignoreCase = true)
            .replace("oe", "ø", ignoreCase = true)
            .replace("aa", "å", ignoreCase = true)
    }
}

private fun String.containsNorwegianLetters(): Boolean {
    return contains('æ', ignoreCase = true) ||
        contains('ø', ignoreCase = true) ||
        contains('å', ignoreCase = true)
}
