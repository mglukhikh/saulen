package ru.spbstu.saulen.players

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.saulen.game.Color
import ru.spbstu.saulen.game.Resource

class ControllerTest {
    @Test
    fun testCardContest() {
        val p1 = SimpleTestPlayer(Color.BLUE, 0)
        val p2 = SimpleTestPlayer(Color.GREEN, 1)
        val startWorkers = p1[Resource.WORKER]
        val startGold = p1[Resource.GOLD]
        val startCraftsmenNumber = p1.craftsmen.size
        assertEquals(startWorkers, p2[Resource.WORKER])
        assertEquals(startGold + 1, p2[Resource.GOLD])
        assertEquals(startCraftsmenNumber, p2.craftsmen.size)
        assertTrue(startCraftsmenNumber > 0)
        val controller = Controller(p1, p2)
        controller.currentRound++
        controller.prepareForRound()
        controller.runCardContest()

        assertTrue(p1[Resource.WORKER] < startWorkers)
        assertTrue(p2[Resource.WORKER] < startWorkers)
        assertTrue(p1.production.isNotEmpty())
        assertTrue(p2.production.isNotEmpty())
        assertEquals(startWorkers, p1[Resource.WORKER] + p1.production.sumBy { it.workers })
        assertEquals(startWorkers, p2[Resource.WORKER] + p2.production.sumBy { it.workers })

        assertTrue(p1[Resource.GOLD] < startGold)
        assertTrue(p2[Resource.GOLD] < startGold + 1)
        assertTrue(p1.craftsmen.size > startCraftsmenNumber)
        assertTrue(p2.craftsmen.size > startCraftsmenNumber)
        assertEquals(startGold, p1[Resource.GOLD] + p1.craftsmen.sumBy { it.cost.amount })
        assertEquals(startGold + 1, p2[Resource.GOLD] + p2.craftsmen.sumBy { it.cost.amount })
    }
}