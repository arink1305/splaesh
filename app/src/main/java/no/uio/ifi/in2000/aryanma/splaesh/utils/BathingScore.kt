package no.uio.ifi.in2000.aryanma.splaesh.utils

import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScore
import no.uio.ifi.in2000.aryanma.splaesh.model.BathingScoreProfile
import kotlin.math.abs
import kotlin.math.roundToInt
//denne filen står bak logikken for scoren hver badeplas får.
private const val MISSING_DATA_NEUTRAL = 0.55

private data class ScoreFactor(
    val name: String,
    val weight: Int,
    val normalized: Double,
    val message: String?
) {
    val contribution: Double = weight * normalized
    val influence: Double = weight * abs(normalized - 0.5)
}

private data class BathingScoreWeights(
    val warning: Int,
    val wave: Int,
    val current: Int,
    val water: Int,
    val air: Int,
    val uv: Int
)

fun calculateBathingScore(
    profile: BathingScoreProfile,
    warningSeverity: String,
    waterTemperature: Double?,
    waveHeight: Double?,
    currentSpeed: Double?,
    uvIndex: Double?,
    airTemperature: Double?
): BathingScore {
    val availableLiveFactorCount = listOf(
        waterTemperature,
        waveHeight,
        currentSpeed,
        uvIndex,
        airTemperature
    ).count { it != null }

    if (availableLiveFactorCount == 0) {
        return BathingScore(
            score = 0,
            label = "Ikke tilgjengelig",
            summary = "Badescoren kan ikke beregnes uten oppdaterte data.",
            primaryReason = "Appen får ikke hentet vær-, sjø- eller UV-data akkurat nå.",
            secondaryReason = "Sjekk nettforbindelsen og prøv igjen senere.",
            isUnavailable = true
        )
    }

    val weights = weightsForProfile(profile)
    val factors = listOf(
        warningFactor(warningSeverity, weights.warning),
        waveHeightFactor(waveHeight, weights.wave),
        currentSpeedFactor(currentSpeed, weights.current),
        waterTemperatureFactor(waterTemperature, weights.water),
        airTemperatureFactor(airTemperature, weights.air),
        uvFactor(uvIndex, weights.uv)
    )

    val score = factors.sumOf { it.contribution }.roundToInt().coerceIn(0, 100)

    val label = when {
        score >= 85 -> "Svært bra"
        score >= 70 -> "Bra"
        score >= 55 -> "Greit"
        score >= 40 -> "Dårlig"
        else -> "Frarådes"
    }

    val summary = when {
        score >= 85 -> "Trygge og komfortable forhold for bading."
        score >= 70 -> "Forholdene ser gode ut for en badetur."
        score >= 55 -> "Forholdene er brukbare, men ikke perfekte."
        score >= 40 -> "Det går an å bade, men forholdene trekker ned."
        else -> "Forholdene tilsier at bading bør vurderes nøye."
    }

    val rankedReasons = factors
        .filter { it.message != null }
        .sortedByDescending { it.influence }

    val warningReason = factors
        .firstOrNull { it.name == "warning" && it.message != null && it.normalized < 1.0 }
        ?.message

    val primaryReason = rankedReasons.firstOrNull()?.message ?: "Lite datagrunnlag ennå."
    val secondaryReason = when {
        warningReason != null && warningReason != primaryReason -> warningReason
        else -> rankedReasons.getOrNull(1)?.message
    }

    return BathingScore(
        score = score,
        label = label,
        summary = summary,
        primaryReason = primaryReason,
        secondaryReason = secondaryReason,
        isUnavailable = false
    )
}

private fun weightsForProfile(profile: BathingScoreProfile): BathingScoreWeights {
    return when (profile) {
        BathingScoreProfile.STANDARD -> BathingScoreWeights(30, 8, 6, 18, 22, 16)
        BathingScoreProfile.SOL -> BathingScoreWeights(25, 4, 4, 17, 25, 25)
        BathingScoreProfile.BARNEVENNLIG -> BathingScoreWeights(32, 26, 22, 12, 4, 4)
    }
}

private fun warningFactor(severity: String, weight: Int): ScoreFactor {
    val normalized = when {
        severity.contains("red", true) || severity.contains("rød", true) || severity.contains("rod", true) -> 0.0
        severity.contains("orange", true) || severity.contains("oransje", true) -> 0.2
        severity.contains("yellow", true) || severity.contains("gul", true) -> 0.45
        else -> 1.0
    }

    val message = when {
        severity.contains("red", true) || severity.contains("rød", true) || severity.contains("rod", true) ->
            "Rødt farevarsel gjør at badeforholdene vurderes som lite trygge."
        severity.contains("orange", true) || severity.contains("oransje", true) ->
            "Oransje farevarsel trekker vurderingen tydelig ned."
        severity.contains("yellow", true) || severity.contains("gul", true) ->
            "Gult farevarsel gjør at badeplassen bør vurderes mer forsiktig."
        else -> "Ingen farevarsler ved badeplassen trekker totalvurderingen opp."
    }

    return ScoreFactor(
        name = "warning",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun waveHeightFactor(value: Double?, weight: Int): ScoreFactor {
    value ?: return neutralFactor("wave", weight)

    val normalized = when {
        value <= 0.2 -> 1.0
        value <= 0.5 -> 0.85
        value <= 1.0 -> 0.55
        value <= 1.5 -> 0.25
        value <= 2.0 -> 0.1
        else -> 0.0
    }

    val message = when {
        value <= 0.2 -> "Svært rolige bølger gir gode og trygge badeforhold."
        value <= 0.5 -> "Rolige bølger gjør badingen tryggere og mer behagelig."
        value <= 1.0 -> "Merkbare bølger trekker badeforholdene noe ned."
        value <= 1.5 -> "Ganske høye bølger gjør badeplassen mindre egnet."
        else -> "Høye bølger trekker badeforholdene tydelig ned."
    }

    return ScoreFactor(
        name = "wave",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun currentSpeedFactor(value: Double?, weight: Int): ScoreFactor {
    value ?: return neutralFactor("current", weight)

    val normalized = when {
        value <= 0.1 -> 1.0
        value <= 0.3 -> 0.85
        value <= 0.5 -> 0.6
        value <= 0.8 -> 0.3
        value <= 1.1 -> 0.1
        else -> 0.0
    }


    val message = when {
        value <= 0.1 -> "Svært svak strøm gir rolige og trygge forhold i vannet."
        value <= 0.3 -> "Lite strøm gir roligere og tryggere badeforhold."
        value <= 0.5 -> "Merkbar strøm trekker forholdene noe ned."
        value <= 0.8 -> "Strøm i vannet gjør badeforholdene mer krevende."
        else -> "Sterk strøm trekker badeforholdene kraftig ned."
    }

    return ScoreFactor(
        name = "current",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun waterTemperatureFactor(value: Double?, weight: Int): ScoreFactor {
    value ?: return neutralFactor("water", weight)

    val normalized = when {
        value >= 22 -> 1.0
        value >= 18 -> 0.9
        value >= 15 -> 0.75
        value >= 12 -> 0.55
        value >= 8 -> 0.3
        else -> 0.1
    }

    val message = when {
        value >= 22 -> "Svært god vanntemperatur gjør badeplassen ekstra innbydende."
        value >= 18 -> "God vanntemperatur trekker badeforholdene opp."
        value >= 15 -> "Vanntemperaturen er brukbar for mange badegjester."
        value >= 12 -> "Vanntemperaturen er grei, men ikke optimal."
        else -> "Kaldt vann trekker badeopplevelsen tydelig ned."
    }

    return ScoreFactor(
        name = "water",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun airTemperatureFactor(value: Double?, weight: Int): ScoreFactor {
    value ?: return neutralFactor("air", weight)

    val normalized = when {
        value in 20.0..26.0 -> 1.0
        value in 16.0..19.9 -> 0.8
        value in 12.0..15.9 -> 0.6
        value in 8.0..11.9 -> 0.35
        value < 8.0 -> 0.1
        value <= 30.0 -> 0.85
        else -> 0.65
    }

    val message = when {
        value in 20.0..26.0 -> "Behagelig lufttemperatur gjør badeturen mer fristende."
        value in 16.0..19.9 -> "Lufttemperaturen er ganske god for et bad."
        value in 12.0..15.9 -> "Lufttemperaturen er brukbar, men ikke helt ideell."
        value < 12.0 -> "Kjølig luft trekker totalopplevelsen noe ned."
        value > 30.0 -> "Svært varm luft trekker litt ned på komforten rundt badingen."
        else -> "Lufttemperaturen er brukbar for bading."
    }

    return ScoreFactor(
        name = "air",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun uvFactor(value: Double?, weight: Int): ScoreFactor {
    value ?: return neutralFactor("uv", weight)

    val normalized = when {
        value < 1.0 -> 0.8
        value < 3.0 -> 0.95
        value < 6.0 -> 1.0
        value < 8.0 -> 0.75
        value < 10.0 -> 0.5
        else -> 0.3
    }

    val rounded = value.roundToInt()
    val message = when {
        value < 1.0 -> "Svært lav UV gir rolige solforhold ved badeplassen."
        value < 3.0 -> "Lav UV gjør oppholdet ved vannet mer behagelig."
        value < 6.0 -> "UV-nivået er håndterbart for en badetur."
        value < 8.0 -> "UV på $rounded krever litt ekstra solbeskyttelse."
        else -> "Høy UV på $rounded trekker komforten ned og krever mer forsiktighet."
    }

    return ScoreFactor(
        name = "uv",
        weight = weight,
        normalized = normalized,
        message = message
    )
}

private fun neutralFactor(name: String, weight: Int): ScoreFactor {
    return ScoreFactor(
        name = name,
        weight = weight,
        normalized = MISSING_DATA_NEUTRAL,
        message = null

    )
}
