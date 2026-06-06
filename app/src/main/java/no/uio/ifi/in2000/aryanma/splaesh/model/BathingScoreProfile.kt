package no.uio.ifi.in2000.aryanma.splaesh.model

enum class BathingScoreProfile(
    val storageKey: String,
    val title: String,
    val shortDescription: String
) {
    STANDARD(
        storageKey = "standard",
        title = "Standard",
        shortDescription = "Legger større vekt på lufttemperatur, vann og UV."
    ),
    SOL(
        storageKey = "sol",
        title = "Sol",
        shortDescription = "Legger størst vekt på solforhold og varme."
    ),
    BARNEVENNLIG(
        storageKey = "barnevennlig",
        title = "Barnevennlig",
        shortDescription = "Legger større vekt på bølger og strøm."
    );

    companion object {
        fun fromStorageKey(value: String?): BathingScoreProfile {
            return when (value) {
                SOL.storageKey -> SOL
                BARNEVENNLIG.storageKey -> BARNEVENNLIG
                else -> STANDARD
            }
        }
    }
}
