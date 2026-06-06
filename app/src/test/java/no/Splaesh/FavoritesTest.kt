package no.uio.ifi.in2000.aryanma.splaesh

import org.junit.Assert.*
import org.junit.Test


class FavoritesTest {

    @Test
    fun `adding location to pendingRemoval adds its id`() {
        val pendingRemovalIds = emptyList<Int>()
        val locationId = 5

        val result = (pendingRemovalIds + locationId).distinct()

        assertTrue(result.contains(locationId))
    }

    @Test
    fun `removing location from pendingRemoval removes its id`() {
        val pendingRemovalIds = listOf(1, 2, 3)

        val result = pendingRemovalIds - 2

        assertFalse(result.contains(2))
        assertEquals(2, result.size)
    }

    @Test
    fun `adding same location twice does not duplicate in pendingRemoval`() {
        val pendingRemovalIds = listOf(1)

        val result = (pendingRemovalIds + 1).distinct()

        assertEquals(1, result.size)
    }

    @Test
    fun `location is marked as pending removal when id is in list`() {
        val pendingRemovalIds = listOf(1, 2, 3)

        val isPendingRemoval = 2 in pendingRemovalIds

        assertTrue(isPendingRemoval)
    }
}