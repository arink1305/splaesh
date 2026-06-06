package no.uio.ifi.in2000.aryanma.splaesh.api

import no.uio.ifi.in2000.aryanma.splaesh.model.MetAlertsResponse
import retrofit2.http.GET

interface MetAlertsApi {

    @GET("weatherapi/metalerts/2.0/current.json")
    suspend fun getCurrentWarnings(): MetAlertsResponse
}
