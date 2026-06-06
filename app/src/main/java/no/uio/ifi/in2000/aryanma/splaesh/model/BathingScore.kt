package no.uio.ifi.in2000.aryanma.splaesh.model

data class BathingScore(
    val score: Int,
    val label: String,
    val summary: String,
    val primaryReason: String,
    val secondaryReason: String?,
    val isUnavailable: Boolean = false
)
