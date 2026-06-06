package no.uio.ifi.in2000.aryanma.splaesh.api

import no.uio.ifi.in2000.aryanma.splaesh.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MetWeatherApi {

     // Docs: https://api.met.no/weatherapi/locationforecast/2.0/documentation

    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("altitude") altitude: Int = 0
    ): ForecastResponse
}
