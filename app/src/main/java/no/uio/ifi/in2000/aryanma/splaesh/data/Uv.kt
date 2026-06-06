package no.uio.ifi.in2000.aryanma.splaesh.data

import com.google.gson.annotations.SerializedName

data class UvResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hourly: Hourly? = null,
    val daily: DailyUv? = null
)


data class Hourly(
    val time: List<String>,
    @SerializedName("uv_index")
    val uvIndex: List<Double>
)

data class DailyUv(
    val time: List<String>,
    @SerializedName("uv_index_max")
    val uvIndexMax: List<Double>
)
