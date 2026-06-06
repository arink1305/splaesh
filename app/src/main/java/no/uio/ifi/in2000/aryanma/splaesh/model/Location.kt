package no.uio.ifi.in2000.aryanma.splaesh.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val image: String,
    val source: String
)