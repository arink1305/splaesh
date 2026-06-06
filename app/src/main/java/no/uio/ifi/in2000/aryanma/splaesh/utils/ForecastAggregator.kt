package no.uio.ifi.in2000.aryanma.splaesh.utils

import no.uio.ifi.in2000.aryanma.splaesh.model.DailyForecast
import no.uio.ifi.in2000.aryanma.splaesh.model.TimeSeries
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.round

object ForecastAggregator {

    private val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputFmt = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun aggregate(timeSeries: List<TimeSeries>): List<DailyForecast> {

        data class Entry(
            val timeMs: Long,
            val dateKey: String,
            val ts: TimeSeries
        )

        val entries = timeSeries.mapNotNull { ts ->
            try {
                val date = inputFmt.parse(ts.time) ?: return@mapNotNull null
                Entry(date.time, dayKeyFmt.format(date), ts)
            } catch (_: Exception) {
                null
            }
        }

        val byDay = entries.groupBy { it.dateKey }

        return byDay.entries
            .sortedBy { it.key }
            .take(10)
            .mapNotNull { (dateKey, dayEntries) ->

                // ── Temperature ──────────────────────────────────────────
                val temps = dayEntries.flatMap { e ->
                    val d6  = e.ts.data.next6Hours?.details
                    val d12 = e.ts.data.next12Hours?.details
                    val instant = e.ts.data.instant.details.airTemperature
                    listOfNotNull(
                        d12?.airTemperatureMax, d12?.airTemperatureMin,
                        d6?.airTemperatureMax,  d6?.airTemperatureMin,
                        instant
                    )
                }
                if (temps.isEmpty()) return@mapNotNull null

                val tempMin = temps.min()
                val tempMax = temps.max()

                // ── Precipitation ─────────────────────────────────────────
                val precipitation = dayEntries.sumOf { e ->
                    e.ts.data.next1Hours?.details?.precipitationAmount
                        ?: e.ts.data.next6Hours?.details?.precipitationAmount
                        ?: 0.0
                }


                val noonMs = run {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    inputFmt.parse(dayEntries.first().ts.time)?.let { cal.time = it }
                    cal.set(Calendar.HOUR_OF_DAY, 12)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis
                }
                val middayEntry = dayEntries.minByOrNull { abs(it.timeMs - noonMs) }
                val windSpeed = middayEntry?.ts?.data?.instant?.details?.windSpeed ?: 0.0
                val windDir   = middayEntry?.ts?.data?.instant?.details?.windFromDirection ?: 0.0

                val symbol = middayEntry?.ts?.data?.next6Hours?.summary?.symbolCode
                    ?: middayEntry?.ts?.data?.next1Hours?.summary?.symbolCode
                    ?: dayEntries.firstOrNull()?.ts?.data?.next6Hours?.summary?.symbolCode

                val dateLabel = try {
                    val d = dayKeyFmt.parse(dateKey)
                    if (d != null) outputFmt.format(d) else dateKey
                } catch (_: Exception) { dateKey }

                DailyForecast(
                    dateKey       = dateKey,
                    date          = dateLabel,
                    tempMin       = round(tempMin * 10) / 10.0,
                    tempMax       = round(tempMax * 10) / 10.0,
                    precipitation = round(precipitation * 10) / 10.0,
                    windSpeed     = round(windSpeed * 10) / 10.0,
                    windDirection = windDir,
                    symbolCode    = symbol
                )
            }
    }

    fun degreesToCompass(degrees: Double): String {
        val directions = listOf(
            "N","NNE","NE","ENE","E","ESE","SE","SSE",
            "S","SSW","SW","WSW","W","WNW","NW","NNW"
        )
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }
}
