package no.uio.ifi.in2000.aryanma.splaesh.api

import no.uio.ifi.in2000.aryanma.splaesh.model.OceanForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OceanForecastApi {

    @GET("weatherapi/oceanforecast/2.0/complete")
    suspend fun getOceanForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): OceanForecastResponse
}