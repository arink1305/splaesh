package no.uio.ifi.in2000.aryanma.splaesh.utils

import no.uio.ifi.in2000.aryanma.splaesh.model.Warning

private enum class WarningSeverityLevel(val rank: Int) {
    NONE(0),
    YELLOW(1),
    ORANGE(2),
    RED(3)
}

private fun parseWarningSeverityLevel(severity: String): WarningSeverityLevel {
    return when {
        severity.contains("red", true) || severity.contains("rød", true) || severity.contains("rod", true) -> WarningSeverityLevel.RED
        severity.contains("orange", true) || severity.contains("oransje", true) -> WarningSeverityLevel.ORANGE
        severity.contains("yellow", true) || severity.contains("gul", true) -> WarningSeverityLevel.YELLOW
        else -> WarningSeverityLevel.NONE
    }
}

fun resolveWarningSeverityForLocation(
    latitude: Double,
    longitude: Double,
    warnings: List<Warning>
): String {
    if (warnings.isEmpty()) return "green"
    var worst = "green"
    for (warning in warnings) {
        for (ring in warning.coordinates) {
            if (farevarselPolygon(latitude, longitude, ring)) {
                if (parseWarningSeverityLevel(warning.severity).rank > parseWarningSeverityLevel(worst).rank) {
                    worst = warning.severity
                }
            }
        }
    }
    return worst
}
