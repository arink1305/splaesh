package no.uio.ifi.in2000.aryanma.splaesh.data

import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.aryanma.splaesh.api.RetrofitClient
import no.uio.ifi.in2000.aryanma.splaesh.model.MetAlertFeature
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning

class WarningsRepository {

    suspend fun getWarnings(): List<Warning> = withContext(Dispatchers.IO) {
        try {
            RetrofitClient.metAlertsApi.getCurrentWarnings().features.mapNotNull(::toWarning)
        } catch (e: Exception) {
            android.util.Log.e("FAREVARSEL", "Failed to fetch warnings", e)
            return@withContext emptyList()
        }
    }

    private fun toWarning(feature: MetAlertFeature): Warning? {
        val properties = feature.properties ?: return null
        val coordinates = parseGeometry(feature) ?: return null

        return Warning(
            event = properties.event ?: "ukjent",
            severity = properties.awarenessLevel ?: "ukjent",
            area = properties.area ?: "ukjent",
            coordinates = coordinates,
            description = properties.description.orEmpty()
        )
    }

    private fun parseGeometry(feature: MetAlertFeature): List<List<Pair<Double, Double>>>? {
        val geometry = feature.geometry ?: return null
        val coordinates = geometry.coordinates ?: return null

        return when (geometry.type) {
            "Polygon" -> parsePolygon(coordinates)
            "MultiPolygon" -> parseMultiPolygon(coordinates)
            else -> null
        }?.takeIf { it.isNotEmpty() }
    }

    private fun parsePolygon(coordinates: JsonElement): List<List<Pair<Double, Double>>> {
        if (!coordinates.isJsonArray) return emptyList()

        return coordinates.asJsonArray.mapNotNull(::parseRing)
    }

    private fun parseMultiPolygon(coordinates: JsonElement): List<List<Pair<Double, Double>>> {
        if (!coordinates.isJsonArray) return emptyList()

        return coordinates.asJsonArray.flatMap { polygonElement ->
            if (!polygonElement.isJsonArray) {
                emptyList()
            } else {
                polygonElement.asJsonArray.mapNotNull(::parseRing)
            }
        }
    }

    private fun parseRing(ringElement: JsonElement): List<Pair<Double, Double>>? {
        if (!ringElement.isJsonArray) return null

        val points = ringElement.asJsonArray.mapNotNull(::parsePoint)
        return points.takeIf { it.isNotEmpty() }
    }

    private fun parsePoint(pointElement: JsonElement): Pair<Double, Double>? {
        if (!pointElement.isJsonArray) return null

        val point = pointElement.asJsonArray
        if (point.size() < 2) return null

        val longitude = point.get(0).asDouble
        val latitude = point.get(1).asDouble
        return latitude to longitude
    }
}
