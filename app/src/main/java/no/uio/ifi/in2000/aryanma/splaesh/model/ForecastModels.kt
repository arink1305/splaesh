package no.uio.ifi.in2000.aryanma.splaesh.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val type: String,
    val coordinates: List<Double> // [lon, lat, altitude]
)

data class Properties(
    val meta: Meta,
    val timeseries: List<TimeSeries>
)

data class Meta(
    @SerializedName("updated_at") val updatedAt: String,
    val units: Units
)

data class Units(
    @SerializedName("air_temperature") val airTemperature: String?,
    @SerializedName("precipitation_amount") val precipitationAmount: String?,
    @SerializedName("wind_speed") val windSpeed: String?,
    @SerializedName("wind_from_direction") val windFromDirection: String?
)


data class TimeSeries(
    val time: String,
    val data: TimeSeriesData
)

data class TimeSeriesData(
    val instant: Instant,
    @SerializedName("next_1_hours") val next1Hours: NextHours?,
    @SerializedName("next_6_hours") val next6Hours: NextHours?,
    @SerializedName("next_12_hours") val next12Hours: NextHours?
)

data class Instant(
    val details: InstantDetails
)

data class InstantDetails(
    @SerializedName("air_temperature") val airTemperature: Double?,
    @SerializedName("wind_speed") val windSpeed: Double?,
    @SerializedName("wind_from_direction") val windFromDirection: Double?
)

data class NextHours(
    val summary: Summary?,
    val details: NextHoursDetails?
)

data class Summary(
    @SerializedName("symbol_code") val symbolCode: String?
)

data class NextHoursDetails(
    @SerializedName("precipitation_amount") val precipitationAmount: Double?,
    @SerializedName("air_temperature_max") val airTemperatureMax: Double?,
    @SerializedName("air_temperature_min") val airTemperatureMin: Double?
)


data class DailyForecast(
    val dateKey: String,
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val precipitation: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val symbolCode: String?,
    val uvMax: Double? = null
)
