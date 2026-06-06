package no.uio.ifi.in2000.aryanma.splaesh.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.aryanma.splaesh.api.RetrofitClient
import no.uio.ifi.in2000.aryanma.splaesh.model.DailyForecast
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.SeaInfo
import no.uio.ifi.in2000.aryanma.splaesh.utils.ForecastAggregator
import java.util.Locale

data class HomePlaceData(
    val locationName: String,
    val forecasts: List<DailyForecast>,
    val seaInfo: SeaInfo?,
    val currentUv: Double?,
    val airTemperature: Double?
)

class HomeRepository(
    context: Context,
    private val uvRepository: UvRepository = UvRepository(RetrofitClient.uvApi)
) {
    private val appContext = context.applicationContext
    private val locationRepository = LocationRepository(appContext)
    private val placeCache = mutableMapOf<String, HomePlaceData>()

    fun getLocations(): List<Location> = locationRepository.getLocation()

    suspend fun getPlaceData(latitude: Double, longitude: Double): HomePlaceData {
        val cacheKey = "${latitude},${longitude}"
        return placeCache[cacheKey] ?: fetchPlaceData(latitude, longitude).also {
            placeCache[cacheKey] = it
        }
    }

    private suspend fun fetchPlaceData(
        latitude: Double,
        longitude: Double
    ): HomePlaceData = withContext(Dispatchers.IO) {
        coroutineScope {
            val geocoderDeferred = async {
                val geocoder = Geocoder(appContext, Locale.ENGLISH)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.locality
                    ?: addresses?.firstOrNull()?.subAdminArea
                    ?: addresses?.firstOrNull()?.adminArea
                    ?: "Ukjent sted"
            }

            val forecastDeferred = async { RetrofitClient.api.getForecast(latitude, longitude, 0) }
            val oceanDeferred = async {
                runCatching { RetrofitClient.oceanApi.getOceanForecast(latitude, longitude) }
                    .getOrNull()
            }
            val dailyUvDeferred = async { uvRepository.getDailyUv(latitude, longitude) }
            val currentUvDeferred = async { uvRepository.getCurrentUv(latitude, longitude) }

            val forecastResponse = forecastDeferred.await()
            val fetchedDailyUv = dailyUvDeferred.await()
            val forecasts = ForecastAggregator.aggregate(forecastResponse.properties.timeseries)
                .map { forecast ->
                    forecast.copy(uvMax = fetchedDailyUv[forecast.dateKey])
                }

            val seaDetails = oceanDeferred.await()?.properties?.timeseries?.firstOrNull()
                ?.data?.instant?.details

            HomePlaceData(
                locationName = geocoderDeferred.await(),
                forecasts = forecasts,
                seaInfo = seaDetails?.let { details ->
                    SeaInfo(
                        waterTemperature = details.seaWaterTemperature,
                        waveHeight = details.seaSurfaceWaveHeight,
                        currentSpeed = details.seaWaterSpeed,
                        currentDirection = details.seaWaterToDirection,
                        waveDirection = details.seaSurfaceWaveFromDirection
                    )
                },
                currentUv = currentUvDeferred.await(),
                airTemperature = forecastResponse.properties.timeseries.firstOrNull()
                    ?.data?.instant?.details?.airTemperature
            )
        }
    }
}
