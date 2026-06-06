package no.uio.ifi.in2000.aryanma.splaesh.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.aryanma.splaesh.data.HomeRepository
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.DailyForecast
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.utils.calculateBathingScore
import no.uio.ifi.in2000.aryanma.splaesh.utils.resolveWarningSeverityForLocation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal const val MAP_CLICK_SOURCE = "map_click"

private const val HOME_VIEW_MODEL_TAG = "HomesScreenViewModel"

data class HomeUiState(
    val locations: List<Location> = emptyList(),
    val selectedLocation: Location? = null,
    val highlightedFavoriteLocationId: Int? = null,
    val locationName: String = "Oslo",
    val forecasts: List<DailyForecast> = emptyList(),
    val seaInfo: SeaInfo? = null,
    val currentUv: Double? = null,
    val airTemperature: Double? = null,
    val placeDataError: String? = null,
    val selectedDayIndex: Int = 0,
    val selectedTimeIndex: Int = 0,
    val showFareLayer: Boolean = false,
    val isLoadingPlaceData: Boolean = false,
    val bathingScoresByLocationId: Map<Int, Int> = emptyMap(),
    val isLoadingBathingScores: Boolean = false
)

class HomesScreenViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = HomeRepository(application.applicationContext)
    private var placeLoadJob: Job? = null

    private val _uiState = MutableStateFlow(
        HomeUiState(locations = repository.getLocations())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val baseCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val timeSteps: List<String> = run {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:00:00'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        (0 until 240).map { index ->
            val calendar = (baseCalendar.clone() as Calendar).apply {
                add(Calendar.HOUR_OF_DAY, index)
            }
            formatter.format(calendar.time)
        }
    }

    val wmsTime: String
        get() = timeSteps[uiState.value.selectedTimeIndex]

    fun setSelectedTimeIndex(index: Int) {
        _uiState.update { it.copy(selectedTimeIndex = index.coerceIn(timeSteps.indices)) }
    }

    fun setSelectedDayIndex(index: Int) {
        _uiState.update { current ->
            val maxIndex = (current.forecasts.size - 1).coerceAtLeast(0)
            current.copy(selectedDayIndex = index.coerceIn(0, maxIndex))
        }
    }

    fun setShowFareLayer(show: Boolean) {
        _uiState.update { it.copy(showFareLayer = show) }
    }

    fun preloadBathingScores(
        warnings: List<Warning>,
        profile: BathingScoreProfile
    ) {
        val locations = _uiState.value.locations
        if (locations.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBathingScores = true) }
            try {
                val semaphore = Semaphore(6)
                val scores = coroutineScope {
                    locations.map { location ->
                        async {
                            semaphore.withPermit {
                                runCatching {
                                    val placeData = repository.getPlaceData(
                                        location.latitude,
                                        location.longitude
                                    )
                                    val warningSeverity = resolveWarningSeverityForLocation(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        warnings = warnings
                                    )
                                    location.id to calculateBathingScore(
                                        profile = profile,
                                        warningSeverity = warningSeverity,
                                        waterTemperature = placeData.seaInfo?.waterTemperature,
                                        waveHeight = placeData.seaInfo?.waveHeight,
                                        currentSpeed = placeData.seaInfo?.currentSpeed,
                                        uvIndex = placeData.currentUv,
                                        airTemperature = placeData.airTemperature
                                    ).score
                                }.onFailure { exception ->
                                    Log.w(
                                        HOME_VIEW_MODEL_TAG,
                                        "Hoppet over badescore for ${location.name}",
                                        exception
                                    )
                                }.getOrNull()
                            }
                        }
                    }.awaitAll().filterNotNull().toMap()
                }
                _uiState.update {
                    it.copy(
                        bathingScoresByLocationId = scores,
                        isLoadingBathingScores = false
                    )
                }
            } catch (exception: Exception) {
                Log.e(HOME_VIEW_MODEL_TAG, "Kunne ikke laste badescorer", exception)
                _uiState.update { it.copy(isLoadingBathingScores = false) }
            }
        }
    }

    fun clearFavoriteHighlight() {
        _uiState.update { it.copy(highlightedFavoriteLocationId = null) }
    }

    fun clearSelectedLocation() {
        placeLoadJob?.cancel()
        _uiState.update {
            it.copy(
                selectedLocation = null,
                highlightedFavoriteLocationId = null,
                seaInfo = null,
                currentUv = null,
                airTemperature = null,
                placeDataError = null,
                forecasts = emptyList(),
                selectedDayIndex = 0,
                isLoadingPlaceData = false
            )
        }
    }

    private fun normalizeLocationName(name: String): String =
        name.trim().takeUnless {
            it.isBlank() || it.equals("Ukjent sted", ignoreCase = true)
        } ?: ""

    fun selectLocation(location: Location, highlightedFavoriteLocationId: Int? = null) {
        placeLoadJob?.cancel()
        val initialLocationName = normalizeLocationName(location.name)
        _uiState.update {
            it.copy(
                selectedLocation = location,
                highlightedFavoriteLocationId = highlightedFavoriteLocationId,
                locationName = initialLocationName,
                forecasts = emptyList(),
                seaInfo = null,
                currentUv = null,
                airTemperature = null,
                placeDataError = null,
                selectedDayIndex = 0,
                isLoadingPlaceData = true
            )
        }

        placeLoadJob = viewModelScope.launch {
            try {
                val data = repository.getPlaceData(location.latitude, location.longitude)
                _uiState.update { current ->
                    val currentLocation = current.selectedLocation
                    if (currentLocation == null ||
                        currentLocation.latitude != location.latitude ||
                        currentLocation.longitude != location.longitude
                    ) {
                        current
                    } else {
                        val resolvedLocationName =
                            normalizeLocationName(data.locationName).ifBlank {
                                current.locationName
                            }
                        current.copy(
                            selectedLocation = if (currentLocation.source == MAP_CLICK_SOURCE) {
                                currentLocation.copy(name = resolvedLocationName)
                            } else {
                                currentLocation
                            },
                            locationName = resolvedLocationName,
                            forecasts = data.forecasts,
                            seaInfo = data.seaInfo,
                            currentUv = data.currentUv,
                            airTemperature = data.airTemperature,
                            placeDataError = null,
                            selectedDayIndex = 0,
                            isLoadingPlaceData = false
                        )
                    }
                }
            } catch (exception: Exception) {
                Log.e(
                    HOME_VIEW_MODEL_TAG,
                    "Kunne ikke laste stedsdata for HomeScreen",
                    exception
                )
                _uiState.update { current ->
                    val currentLocation = current.selectedLocation
                    if (currentLocation == null ||
                        currentLocation.latitude != location.latitude ||
                        currentLocation.longitude != location.longitude
                    ) {
                        current
                    } else {
                        current.copy(
                            placeDataError = "Kunne ikke laste værdata. Sjekk tilkoblingen og prøv igjen.",
                            isLoadingPlaceData = false
                        )
                    }
                }
            }
        }
    }
}
