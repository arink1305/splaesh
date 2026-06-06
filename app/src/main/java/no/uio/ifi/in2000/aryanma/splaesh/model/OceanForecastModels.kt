package no.uio.ifi.in2000.aryanma.splaesh.model

import com.google.gson.annotations.SerializedName

data class OceanForecastResponse(
    val type: String,
    val geometry: OceanGeometry,
    val properties: OceanProperties
)

data class OceanGeometry(
    val type: String,
    val coordinates: List<Double>
)

data class OceanProperties(
    val meta: OceanMeta,
    val timeseries: List<OceanTimeSeries>
)

data class OceanMeta(
    @SerializedName("updated_at")
    val updatedAt: String,
    val units: OceanUnits
)

data class OceanUnits(
    @SerializedName("sea_water_temperature")
    val seaWaterTemperature: String? = null,

    @SerializedName("sea_surface_wave_height")
    val seaSurfaceWaveHeight: String? = null,

    @SerializedName("sea_water_speed")
    val seaWaterSpeed: String? = null,

    @SerializedName("sea_water_to_direction")
    val seaWaterToDirection: String? = null,

    @SerializedName("sea_surface_wave_from_direction")
    val seaSurfaceWaveFromDirection: String? = null
)

data class OceanTimeSeries(
    val time: String,
    val data: OceanData
)

data class OceanData(
    val instant: OceanInstant
)

data class OceanInstant(
    val details: OceanInstantDetails
)

data class OceanInstantDetails(
    @SerializedName("sea_water_temperature")
    val seaWaterTemperature: Double? = null,

    @SerializedName("sea_surface_wave_height")
    val seaSurfaceWaveHeight: Double? = null,

    @SerializedName("sea_water_speed")
    val seaWaterSpeed: Double? = null,

    @SerializedName("sea_water_to_direction")
    val seaWaterToDirection: Double? = null,

    @SerializedName("sea_surface_wave_from_direction")
    val seaSurfaceWaveFromDirection: Double? = null
)