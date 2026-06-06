package no.uio.ifi.in2000.aryanma.splaesh.api

import no.uio.ifi.in2000.aryanma.splaesh.data.UvResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UvApi {

    @GET("v1/forecast")
    suspend fun getUv(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "uv_index",
        @Query("timezone") timezone: String = "auto"
    ): UvResponse

    @GET("v1/forecast")
    suspend fun getDailyUv(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "uv_index_max",
        @Query("forecast_days") forecastDays: Int = 10,
        @Query("timezone") timezone: String = "auto"
    ): UvResponse
}
