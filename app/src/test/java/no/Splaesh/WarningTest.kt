package no.uio.ifi.in2000.aryanma.splaesh

import no.uio.ifi.in2000.aryanma.splaesh.model.Warning
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.getColor
import no.uio.ifi.in2000.aryanma.splaesh.ui.components.mapSeverity
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class WarningTest {

    //test for forskjellig farge på kart
    @Test
    fun `mapSeverity returns red for rod`() {
        assertEquals("red", mapSeverity("rød"))
    }

    @Test
    fun `mapSeverity returns yellow for gul`() {
        assertEquals("yellow", mapSeverity("gul"))
    }

    @Test
    fun `mapSeverity returns blue for unknown severity`() {
        assertEquals("blue", mapSeverity("ukjent"))
    }

    //sjekke om vi får riktig hexfarge på varsel

    @Test
    fun `getColor returns red color for rod severity`() {
        assertEquals(Color(0xFFD92D20), getColor("rød"))
    }

    @Test
    fun `getColor returns yellow color for gul severity`() {
        assertEquals(Color(0xFFFACC15), getColor("gul"))
    }

    @Test
    fun `getColor returns gray for unknown severity`() {
        assertEquals(Color.Gray, getColor("ukjent"))
    }


    //riktig informasjon lagring
    @Test
    fun `Warning model stores description correctly`() {
        val warning = Warning(
            event = "storm",
            severity = "rød",
            area = "Oslo",
            coordinates = emptyList(),
            description = "Kraftig storm"
        )
        assertEquals("Kraftig storm", warning.description)
    }

    @Test
    fun `Warning severity field is stored correctly`() {
        val warning = Warning(
            event = "storm",
            severity = "gul",
            area = "Bergen",
            coordinates = emptyList(),
            description = "Lett vind"
        )
        assertEquals("gul", warning.severity)
    }
}