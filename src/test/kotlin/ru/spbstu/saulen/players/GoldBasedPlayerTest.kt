package ru.spbstu.saulen.players

import org.junit.Assert.assertTrue
import org.junit.Test
import ru.spbstu.saulen.players.evaluation.GoldBasedPlayer

class GoldBasedPlayerTest {

    @Test
    fun contestWithOneSimplePlayer() {
        val p1 = GoldBasedPlayer(0)
        val p2 = SimpleTestPlayer(1)
        val controller = Controller(p1, p2)
        controller.silent = false
        val results = controller.runGame()
        assertTrue(results[p1]!! > results[p2]!!)
    }
}