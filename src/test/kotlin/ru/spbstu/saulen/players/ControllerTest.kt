package ru.spbstu.saulen.players

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.saulen.game.Resource

class ControllerTest {
    @Test
    fun testCardContest() {
        val p1 = SimpleTestPlayer(0)
        val p2 = SimpleTestPlayer(1)
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

        assertTrue(p1[Resource.GOLD] < startGold || p2[Resource.GOLD] < startGold + 1)
        if (p1[Resource.GOLD] < startGold) {
            assertTrue(p1.craftsmen.size > startCraftsmenNumber)
        }
        if (p2[Resource.GOLD] < startGold) {
            assertTrue(p2.craftsmen.size > startCraftsmenNumber)
        }
        assertEquals(startGold, p1[Resource.GOLD] + p1.craftsmen.sumBy { it.cost.amount })
        assertEquals(startGold + 1, p2[Resource.GOLD] + p2.craftsmen.sumBy { it.cost.amount })
    }

    @Test
    fun testMasterSetup() {
        val p1 = SimpleTestPlayer(0)
        val p2 = SimpleTestPlayer(1)
        val startGold = p1[Resource.GOLD]
        assertEquals(startGold + 1, p2[Resource.GOLD])
        val startMasters = p1[Resource.MASTER]
        assertEquals(startMasters, p2[Resource.MASTER])
        val controller = Controller(p1, p2)
        controller.currentRound++
        controller.prepareForRound()
        controller.runMasterSetup()

        assertTrue(p1[Resource.GOLD] < startGold)
        assertTrue(p2[Resource.GOLD] < startGold + 1)
        assertEquals(startMasters, controller.board.positions.entries.count { it.value == p1 })
        assertEquals(startMasters, controller.board.positions.entries.count { it.value == p2 })
        assertEquals(0, controller.board.positions.entries.count { !it.key.masterPosition && it.value != null })
    }

    @Test
    fun testCardContestWithHandling() {
        val p1 = SimpleTestPlayer(0)
        val p2 = SimpleTestPlayer(1)
        val startWorkers = p1[Resource.WORKER]
        assertEquals(startWorkers, p2[Resource.WORKER])
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

        assertEquals(0, p1.buildingResourceCount)
        assertEquals(0, p2.buildingResourceCount)

        controller.runPositionHandling(withEvent = false)
        assertEquals(startWorkers, p1[Resource.WORKER])
        assertEquals(startWorkers, p2[Resource.WORKER])
        assertTrue(Resource.BUILDING_RESOURCES.joinToString(prefix = p1.toString()) {
            it.name + ": " + p1[it]
        }, p1.buildingResourceCount >= 3)
        assertTrue(Resource.BUILDING_RESOURCES.joinToString(prefix = p2.toString()) {
            it.name + ": " + p2[it]
        }, p2.buildingResourceCount >= 3)
        for (resource in Resource.BUILDING_RESOURCES) {
            if (resource == Resource.METAL) continue
            assertNotEquals(1, p1[resource])
            assertNotEquals(1, p2[resource])
        }
    }

    @Test
    fun testCardContestWithCraftsmen() {
        val p1 = SimpleTestPlayer(0)
        p1.buyCraftsmen = false
        val p2 = SimpleTestPlayer(1)
        p2.buyCraftsmen = false
        val startWorkers = p1[Resource.WORKER]
        assertEquals(startWorkers, p2[Resource.WORKER])
        val startWinningPoints = p1[Resource.WINNING_POINT]
        assertEquals(startWinningPoints, p2[Resource.WINNING_POINT])
        val controller = Controller(p1, p2)
        controller.currentRound++
        controller.prepareForRound()
        controller.runCardContest()

        assertTrue(p1[Resource.WORKER] < startWorkers)
        assertTrue(p2[Resource.WORKER] < startWorkers)
        assertTrue(p1.production.isNotEmpty())
        assertTrue(p2.production.isNotEmpty())
        assertEquals(0, p1.buildingResourceCount)
        assertEquals(0, p2.buildingResourceCount)

        controller.runPositionHandling(withEvent = false)
        controller.runCraftsmenWork()
        assertTrue(p1[Resource.WINNING_POINT] > startWinningPoints)
        assertTrue(p2[Resource.WINNING_POINT] > startWinningPoints)
    }

    @Test
    fun smokeTestForTwo() {
        val p1 = SimpleTestPlayer(0)
        val p2 = SimpleTestPlayer(1)
        val controller = Controller(p1, p2)
        controller.silent = false
        controller.runGame()
    }

    @Test
    fun smokeTestForFour() {
        val p1 = SimpleTestPlayer(0)
        val p2 = SimpleTestPlayer(1)
        p2.randomMasters = true
        val p3 = SimpleTestPlayer(2)
        val p4 = SimpleTestPlayer(3)
        p4.randomMasters = true
        val controller = Controller(p1, p2, p3, p4)
        controller.silent = false
        controller.runGame()
    }
}