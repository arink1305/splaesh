package no.uio.ifi.in2000.aryanma.splaesh.ui.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.aryanma.splaesh.api.RetrofitClient
import no.uio.ifi.in2000.aryanma.splaesh.api.RetrofitClient.uvApi
import no.uio.ifi.in2000.aryanma.splaesh.data.UvRepository
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val FAVORITES_VIEW_MODEL_TAG = "FavoritesViewModel"

data class LocationDetails(
    val seaInfo: SeaInfo? = null,
    val uvValue: Double? = null,
    val airTemperature: Double? = null,
)

class FavoritesViewModel : ViewModel() {

    private val uvRepo = UvRepository(uvApi)

    private val _detailsById = MutableStateFlow<Map<Int, LocationDetails>>(emptyMap())
    val detailsById: StateFlow<Map<Int, LocationDetails>> = _detailsById.asStateFlow()

    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun loadDetailsForAll(favorites: List<Location>) {
        val alreadyLoaded = _detailsById.value.keys
        val pendingFavorites = favorites.filter { it.id !in alreadyLoaded }
        if (pendingFavorites.isEmpty()) return

        viewModelScope.launch {
            pendingFavorites.map { beach ->
                async { loadDetailsFor(beach) }
            }.awaitAll()
        }
    }

    private suspend fun loadDetailsFor(beach: Location) {
        val seaInfo = fetchSeaInfo(beach)
        val uvValue = fetchUv(beach)
        val airTemperature = fetchAirTemperature(beach)

        _detailsById.update { current ->
            current + (
                    beach.id to LocationDetails(
                        seaInfo = seaInfo,
                        uvValue = uvValue,
                        airTemperature = airTemperature
                    )
                    )
        }
    }

    private suspend fun fetchSeaInfo(beach: Location): SeaInfo? {
        return runLoggedRequest(
            errorMessage = "Kunne ikke hente sjødata for ${beach.name}"
        ) {
            val response = RetrofitClient.oceanApi.getOceanForecast(
                beach.latitude,
                beach.longitude
            )

            val nowMillis = System.currentTimeMillis()
            val closest = response.properties.timeseries.minByOrNull {
                val parsed = formatter.parse(it.time)
                kotlin.math.abs((parsed?.time ?: Long.MAX_VALUE) - nowMillis)
            }

            closest?.data?.instant?.details?.let { details ->
                SeaInfo(
                    waterTemperature = details.seaWaterTemperature,
                    waveHeight = details.seaSurfaceWaveHeight,
                    currentSpeed = details.seaWaterSpeed,
                    currentDirection = details.seaWaterToDirection,
                    waveDirection = details.seaSurfaceWaveFromDirection
                )
            }
        }
    }

    private suspend fun fetchUv(beach: Location): Double? {
        return runLoggedRequest(
            errorMessage = "Kunne ikke hente UV for ${beach.name}"
        ) {
            uvRepo.getCurrentUv(beach.latitude, beach.longitude)
        }
    }

    private suspend fun fetchAirTemperature(beach: Location): Double? {
        return runLoggedRequest(
            errorMessage = "Kunne ikke hente lufttemperatur for ${beach.name}"
        ) {
            val response = RetrofitClient.api.getForecast(
                lat = beach.latitude,
                lon = beach.longitude
            )

            val nowMillis = System.currentTimeMillis()
            val closest = response.properties.timeseries.minByOrNull {
                val parsed = formatter.parse(it.time)
                kotlin.math.abs((parsed?.time ?: Long.MAX_VALUE) - nowMillis)
            }

            closest?.data?.instant?.details?.airTemperature
        }
    }

    private inline fun <T> runLoggedRequest(
        errorMessage: String,
        block: () -> T?
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(FAVORITES_VIEW_MODEL_TAG, errorMessage, e)
            null
        }
    }
}
