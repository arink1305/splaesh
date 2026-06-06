package no.uio.ifi.in2000.aryanma.splaesh.utils

fun farevarselPolygon(
    latitude: Double,
    longitude: Double,
    polygon: List<Pair<Double, Double>>

) :Boolean {
    var inside = false
    var j = polygon.size - 1

    for (i in polygon.indices) {
        val xi = polygon[i].second
        val yi = polygon[i].first
        val xj = polygon[j].second
        val yj = polygon[j].first

        val intersect =
            ((yi > latitude) != (yj > latitude)) &&
                    (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi)

        if (intersect) {
            inside = !inside
        }
        j = i

    }
    return inside

}

