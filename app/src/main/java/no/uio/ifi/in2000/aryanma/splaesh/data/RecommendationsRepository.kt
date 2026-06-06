package no.uio.ifi.in2000.aryanma.splaesh.data

import android.content.Context
import android.util.Log
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import no.uio.ifi.in2000.aryanma.splaesh.model.RecommendationPlace
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.utils.calculateBathingScore
import no.uio.ifi.in2000.aryanma.splaesh.utils.farevarselPolygon
import no.uio.ifi.in2000.aryanma.splaesh.utils.resolveWarningSeverityForLocation

private const val RECOMMENDATIONS_REPOSITORY_TAG = "RecommendationsRepository"

class RecommendationsRepository(
    context: Context,
    private val homeRepository: HomeRepository = HomeRepository(context),
    private val userLocationRepository: UserLocationRepository = UserLocationRepository(context)
) {
    fun getLocations(): List<Location> = homeRepository.getLocations()

    suspend fun getCurrentUserLocation() = userLocationRepository.getCurrentPoint()

    suspend fun getRecommendations(
        userLatitude: Double,
        userLongitude: Double,
        radiusKm: Int,
        warnings: List<Warning>,
        profile: BathingScoreProfile
    ): List<RecommendationPlace> = coroutineScope {
        val semaphore = Semaphore(6)
        getLocations()
            .map { location ->
                async {
                    semaphore.withPermit {
                        buildRecommendation(
                            userLatitude = userLatitude,
                            userLongitude = userLongitude,
                            radiusKm = radiusKm,
                            location = location,
                            warnings = warnings,
                            profile = profile
                        )
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .sortedWith(
                compareByDescending<RecommendationPlace> { it.score.score }
                    .thenBy { it.distanceKm }
            )
    }

    private suspend fun buildRecommendation(
        userLatitude: Double,
        userLongitude: Double,
        radiusKm: Int,
        location: Location,
        warnings: List<Warning>,
        profile: BathingScoreProfile
    ): RecommendationPlace? {
        val distanceKm = haversineKm(
            userLatitude,
            userLongitude,
            location.latitude,
            location.longitude
        )
        if (distanceKm > radiusKm) return null

        return try {
            val placeData = homeRepository.getPlaceData(location.latitude, location.longitude)
            val warningSeverity = resolveWarningSeverityForLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                warnings = warnings
            )
            val score = calculateBathingScore(
                profile = profile,
                warningSeverity = warningSeverity,
                waterTemperature = placeData.seaInfo?.waterTemperature,
                waveHeight = placeData.seaInfo?.waveHeight,
                currentSpeed = placeData.seaInfo?.currentSpeed,
                uvIndex = placeData.currentUv,
                airTemperature = placeData.airTemperature
            )

            RecommendationPlace(
                location = location,
                distanceKm = distanceKm,
                score = score,
                warningSeverity = warningSeverity,
                seaInfo = placeData.seaInfo,
                uvValue = placeData.currentUv,
                airTemperature = placeData.airTemperature,
                warningDescription = resolveWarningDescription(location, warnings)
            )
        } catch (exception: Exception) {
            Log.e(
                RECOMMENDATIONS_REPOSITORY_TAG,
                "Kunne ikke bygge anbefaling for ${location.name}",
                exception
            )
            null
        }
    }

    private fun resolveWarningDescription(
        location: Location,
        warnings: List<Warning>
    ): String? {
        return warnings.firstOrNull { warning ->
            warning.coordinates.any { ring ->
                farevarselPolygon(location.latitude, location.longitude, ring)
            }
        }?.description
    }

    private fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val originLat = Math.toRadians(lat1)
        val targetLat = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
            cos(originLat) * cos(targetLat) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }
}
