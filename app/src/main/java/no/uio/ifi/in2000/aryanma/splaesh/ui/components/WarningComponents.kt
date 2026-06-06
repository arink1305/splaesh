package no.uio.ifi.in2000.aryanma.splaesh.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.utils.farevarselPolygon

@Composable
fun WarningMapLayer(warnings: List<Warning>) { // lager en layer med farevarsler

    warnings.forEach { varsel ->

        varsel.coordinates.forEach { ring -> // for hver ring i hver farevarsel

            val points = ring.map {
                Point.fromLngLat(it.second, it.first) // lager en liste med punkter for hver ring
            }

            PolygonAnnotation(
                points = listOf(points) // lager en polygon med punktene
            ) {
                fillColor = getColor(varsel.severity) // setter farge basert på severity
                fillOpacity = 0.4
            }
        }
    }
}

private enum class WarningSeverity(val rank: Int) {
    NONE(0),
    YELLOW(1),
    ORANGE(2),
    RED(3)
}

private fun parseWarningSeverity(severity: String): WarningSeverity {
    return when {
        severity.contains("red", true) || severity.contains("rød", true) || severity.contains("rod", true) -> WarningSeverity.RED
        severity.contains("orange", true) || severity.contains("oransje", true) -> WarningSeverity.ORANGE
        severity.contains("yellow", true) || severity.contains("gul", true) -> WarningSeverity.YELLOW
        else -> WarningSeverity.NONE
    }
}

fun getColor(severity: String): Color {
    return when (parseWarningSeverity(severity)) {
        WarningSeverity.RED -> Color(0xFFD92D20)
        WarningSeverity.ORANGE -> Color(0xFFF79009)
        WarningSeverity.YELLOW -> Color(0xFFFACC15)
        WarningSeverity.NONE -> Color.Gray
    }
}


fun getSeverityForBath(
    latitude: Double,
    longitude: Double,
    warnings: List<Warning>
): String {
    if (warnings.isEmpty()) return "green"
    var worst = "green"
    for (f in warnings) {
        for (ring in f.coordinates) {
            if (farevarselPolygon(latitude, longitude, ring)) {
                if (parseWarningSeverity(f.severity).rank > parseWarningSeverity(worst).rank) {
                    worst = f.severity
                }
            }
        }
    }
    return worst
}

fun mapSeverity(severity: String): String {
    return when (parseWarningSeverity(severity)) {
        WarningSeverity.RED -> "red"
        // We do not have an orange map pin asset, so orange maps to red for clearer emphasis.
        WarningSeverity.ORANGE -> "red"
        WarningSeverity.YELLOW -> "yellow"
        WarningSeverity.NONE -> "blue"
    }
}
