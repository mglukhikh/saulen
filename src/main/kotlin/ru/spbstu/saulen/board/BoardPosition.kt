package ru.spbstu.saulen.board

import ru.spbstu.saulen.cards.Advantage
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Event
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.players.Player

sealed class BoardPosition(
        private val description: String,
        val masterPosition: Boolean = true
) {
    abstract fun invokeOn(player: Player, log: (String) -> Unit = {})

    companion object {
        val positions = listOf(
                EventProtectionPosition,
                EventInvocationPosition(),
                WollManufakturPosition,
                AdvantagePosition(),
                AdvantagePosition(),
                WinningPointPosition(2),
                WinningPointPosition(1),
                ProductionPosition,
                TaxFreePosition(withMetal = true),
                TaxFreePosition(),
                TaxFreePosition(),
                TaxFreePosition(),
                TaxPosition(),
                CraftsmanPosition(),
                CraftsmanPosition(),
                GrayWorkersPosition,
                MarketPosition(0),
                MarketPosition(1),
                MarketPosition(2),
                MarketPosition(3),
                TradePosition,
                StartPlayerPosition
        )
    }

    override fun toString() = description
}

object EventProtectionPosition : BoardPosition("Protection from events") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        log("Player $player has event protection")
        player.hasEventProtection = true
    }
}

class EventInvocationPosition(
        var event: Event? = null
) : BoardPosition("", false) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        val event = event ?: return
        log("Event ${event.javaClass} activated!")
        if (!event.negative || !player.hasEventProtection) {
            event.invokeOn(player)
        }
    }
}

object WollManufakturPosition : BoardPosition(
        "", false
) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        val workers = player[Resource.WORKER]
        player += Resource.GOLD(workers)
        log("Player $player gets $workers gold on manufacture for a total of ${player[Resource.GOLD]}")
    }
}

class AdvantagePosition(var advantage: Advantage? = null) : BoardPosition("Advantage") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        val advantage = this.advantage ?: return
        log("Player $player gets advantage ${advantage.javaClass}")
        player += advantage
        advantage.invokeOn(player)
    }

    override fun toString(): String {
        return advantage?.toString() ?: "? No advantage ?"
    }
}

class WinningPointPosition(val amount: Int) : BoardPosition("Winning points $amount") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        player += Resource.WINNING_POINT(amount)
        log("Player $player gets $amount winning points for a total of ${player[Resource.WINNING_POINT]}")
    }
}

object ProductionPosition : BoardPosition("", false) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        log("Player $player produces resources")
        // Here we can have negative resources due to kalte winter
        log("Before: ${player.resourceDescription(false, Resource.SAND, Resource.WOOD, Resource.STONE)}")
        player.produce()
        log("After: ${player.resourceDescription(true, Resource.SAND, Resource.WOOD, Resource.STONE)}")
    }
}

class TaxFreePosition(val withMetal: Boolean = false) : BoardPosition(
        if (withMetal) "Tax free with metal" else "Tax free"
) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        player.hasTaxFree = true
        if (withMetal) {
            log("Player $player gets metal on taxes for a total of ${player[Resource.METAL]}")
            player += Resource.METAL(1)
        }
    }
}

class TaxPosition(var amount: Int = 0) : BoardPosition("", false) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        if (!player.hasTaxFree) {
            player -= Resource.GOLD(amount)
            log("Player $player pays $amount tax and now has ${player[Resource.GOLD]} gold")
        }
    }
}

class CraftsmanPosition(var craftsman: Craftsman? = null) : BoardPosition(
        "Craftsman"
) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        val craftsman = craftsman ?: return
        log("Player $player gets craftsman $craftsman")
        player += craftsman
    }

    override fun toString(): String {
        return craftsman?.toString() ?: "? No craftsman ?"
    }
}

object GrayWorkersPosition : BoardPosition("Two gray workers") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        player += Resource.WORKER(2)
        log("Player $player gets gray workers")
    }
}

class MarketPosition(val queue: Int) : BoardPosition("Market queue $queue") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        player.marketQueue = queue
    }
}

object TradePosition : BoardPosition("", false) {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        // Have to handle in controller, too complex logic
    }
}

object StartPlayerPosition : BoardPosition("Starting player") {
    override fun invokeOn(player: Player, log: (String) -> Unit) {
        player.playerQueue = Int.MAX_VALUE
    }
}