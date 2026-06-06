package no.uio.ifi.in2000.aryanma.splaesh.model

data class RecommendationPlace(
    val location: Location,
    val distanceKm: Double,
    val score: BathingScore,
    val warningSeverity: String,
    val seaInfo: SeaInfo?,
    val uvValue: Double?,
    val airTemperature: Double?,
    val warningDescription: String?
)
