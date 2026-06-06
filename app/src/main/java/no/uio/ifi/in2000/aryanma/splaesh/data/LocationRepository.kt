package no.uio.ifi.in2000.aryanma.splaesh.data

import android.content.Context
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.aryanma.splaesh.model.Location

class LocationRepository(private val context: Context) {

    companion object {
        @Volatile
        private var cachedLocation: List<Location>? = null
    }

    fun getLocation(): List<Location> {
        cachedLocation?.let { return it }

        val jsonString = context.assets
            .open("badeplasser.json")
            .bufferedReader()
            .use { it.readText() }

        return Json.decodeFromString<List<Location>>(jsonString).also {
            cachedLocation = it
        }
    }
}
