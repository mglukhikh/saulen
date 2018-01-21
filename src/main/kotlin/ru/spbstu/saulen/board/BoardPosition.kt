package ru.spbstu.saulen.board

import ru.spbstu.saulen.cards.Advantage
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Event
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.players.Player

sealed class BoardPosition(val masterPosition: Boolean = true) {
    abstract fun invokeOn(player: Player)

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
}

object EventProtectionPosition : BoardPosition() {
    override fun invokeOn(player: Player) {
        player.hasEventProtection = true
    }
}

class EventInvocationPosition(var event: Event? = null) : BoardPosition(false) {
    override fun invokeOn(player: Player) {
        val event = event ?: return
        if (!event.negative || !player.hasEventProtection) {
            event.invokeOn(player)
        }
    }
}

object WollManufakturPosition : BoardPosition(false) {
    override fun invokeOn(player: Player) {
        val workers = player[Resource.WORKER]
        player += Resource.GOLD(workers)
    }
}

class AdvantagePosition(var advantage: Advantage? = null) : BoardPosition() {
    override fun invokeOn(player: Player) {
        val advantage = this.advantage ?: return
        player += advantage
        advantage.invokeOn(player)
    }
}

class WinningPointPosition(val amount: Int) : BoardPosition() {
    override fun invokeOn(player: Player) {
        player += Resource.WINNING_POINT(amount)
    }
}

object ProductionPosition : BoardPosition(false) {
    override fun invokeOn(player: Player) {
        player.produce()
    }
}

class TaxFreePosition(val withMetal: Boolean = false) : BoardPosition() {
    override fun invokeOn(player: Player) {
        player.hasTaxFree = true
        if (withMetal) {
            player += Resource.METAL(1)
        }
    }
}

class TaxPosition(var amount: Int = 0) : BoardPosition(false) {
    override fun invokeOn(player: Player) {
        if (!player.hasTaxFree) {
            player -= Resource.GOLD(amount)
        }
    }
}

class CraftsmanPosition(var craftsman: Craftsman? = null) : BoardPosition() {
    override fun invokeOn(player: Player) {
        val craftsman = craftsman ?: return
        player += craftsman
    }
}

object GrayWorkersPosition : BoardPosition() {
    override fun invokeOn(player: Player) {
        player += Resource.WORKER(2)
    }
}

class MarketPosition(val queue: Int) : BoardPosition() {
    override fun invokeOn(player: Player) {
        player.marketQueue = queue
    }
}

object TradePosition : BoardPosition(false) {
    override fun invokeOn(player: Player) {
        // Have to handle in controller, too complex logic
    }
}

object StartPlayerPosition : BoardPosition() {
    override fun invokeOn(player: Player) {
        player.playerQueue = Int.MAX_VALUE
    }
}