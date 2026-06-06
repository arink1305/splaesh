package no.uio.ifi.in2000.aryanma.splaesh.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class MetAlertsResponse(
    val features: List<MetAlertFeature> = emptyList()
)

data class MetAlertFeature(
    val properties: MetAlertProperties? = null,
    val geometry: MetAlertGeometry? = null
)

data class MetAlertProperties(
    val event: String? = null,
    val description: String? = null,
    val area: String? = null,
    @SerializedName("awareness_level")
    val awarenessLevel: String? = null
)

data class MetAlertGeometry(
    val type: String? = null,
    val coordinates: JsonElement? = null
)
