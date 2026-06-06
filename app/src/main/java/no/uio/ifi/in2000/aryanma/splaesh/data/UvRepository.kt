package no.uio.ifi.in2000.aryanma.splaesh.data

import no.uio.ifi.in2000.aryanma.splaesh.api.UvApi
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class UvRepository(private val api: UvApi) {


    suspend fun getCurrentUv(
        lat: Double,
        lon: Double,
        nowMillis: Long = System.currentTimeMillis()
    ): Double? {
        return try {
            val response = api.getUv(lat, lon)
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone(response.timezone)
            }
            val hourly = response.hourly ?: return null

            hourly.time
                .zip(hourly.uvIndex)
                .minByOrNull { (time, _) ->
                    val parsedTime = formatter.parse(time)
                    kotlin.math.abs((parsedTime?.time ?: Long.MAX_VALUE) - nowMillis)
                }
                ?.second
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getDailyUv(lat: Double, lon: Double): Map<String, Double> {
        return try {
            val response = api.getDailyUv(lat, lon)
            val daily = response.daily ?: return emptyMap()
            daily.time.zip(daily.uvIndexMax).toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
