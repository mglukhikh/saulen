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

    @Test
    fun contestWithThreeSimplePlayers() {
        val p1 = GoldBasedPlayer(0)
        val p2 = SimpleTestPlayer(1)
        p2.randomMasters = true
        val p3 = SimpleTestPlayer(2)
        val p4 = SimpleTestPlayer(3)
        p4.randomMasters = true
        val controller = Controller(p1, p2, p3, p4)
        controller.silent = false
        val results = controller.runGame()
        assertTrue(results[p1]!! > results[p2]!!)
        assertTrue(results[p1]!! > results[p3]!!)
        assertTrue(results[p1]!! > results[p4]!!)
    }
}