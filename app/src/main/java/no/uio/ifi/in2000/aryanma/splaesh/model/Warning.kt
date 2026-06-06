package no.uio.ifi.in2000.aryanma.splaesh.model

data class Warning(
    val event: String, //type være
    val severity: String, //gul/oransje/rød
    val area: String, //område
    val coordinates: List<List<Pair<Double, Double>>>, // koordinater
    val description: String
)