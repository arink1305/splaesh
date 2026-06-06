package no.uio.ifi.in2000.aryanma.splaesh

import no.uio.ifi.in2000.aryanma.splaesh.model.Location
import org.junit.Assert.*
import org.junit.Test

class LocationRepositoryTest {


    private fun makeLocation(
        id: Int = 1,
        name: String = "Huk",
        longitude: Double = 10.68,
        latitude: Double = 59.88,
        image: String = "huk.jpg",
        source: String = "bath"
    ) = Location(id, name, longitude, latitude, image, source)


    // lage location model

    @Test
    fun `Location is created with correct fields`() {
        val location = makeLocation(id = 5, name = "Huk")

        assertEquals(5, location.id)
        assertEquals("Huk", location.name)
    }


    // kordinat lagring

    @Test
    fun `Location stores longitude and latitude correctly`() {
        val location = makeLocation(longitude = 10.68, latitude = 59.88)

        assertEquals(10.68, location.longitude, 0.0001)
        assertEquals(59.88, location.latitude, 0.0001)
    }

    // finn location med id

    @Test
    fun `find location by id returns correct location`() {
        val locations = listOf(
            makeLocation(id = 1, name = "Huk"),
            makeLocation(id = 2, name = "Sørenga"),
            makeLocation(id = 3, name = "Tjuvholmen")
        )

        val result = locations.find { it.id == 2 }

        assertNotNull(result)
        assertEquals("Sørenga", result?.name)
    }
}