package ru.spbstu.saulen.board

import ru.spbstu.saulen.cards.Advantage
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.players.Player

sealed class MasterPosition {
    abstract fun invokeOn(player: Player)

    companion object {
        val positions = listOf(
                EventProtectionPosition,
                AdvantagePosition(),
                AdvantagePosition(),
                WinningPointPosition(2),
                WinningPointPosition(1),
                TaxFreePosition(withMetal = true),
                TaxFreePosition(),
                TaxFreePosition(),
                TaxFreePosition(),
                CraftsmanPosition(),
                CraftsmanPosition(),
                GrayWorkersPosition,
                MarketPosition(0),
                MarketPosition(1),
                MarketPosition(2),
                MarketPosition(3),
                StartPlayerPosition
        )
    }
}

object EventProtectionPosition : MasterPosition() {
    override fun invokeOn(player: Player) {
        player.hasEventProtection = true
    }
}

class AdvantagePosition(var advantage: Advantage? = null) : MasterPosition() {
    override fun invokeOn(player: Player) {
        val advantage = this.advantage ?: return
        player += advantage
    }
}

class WinningPointPosition(val amount: Int) : MasterPosition() {
    override fun invokeOn(player: Player) {
        player += Resource.WINNING_POINT(amount)
    }
}

class TaxFreePosition(val withMetal: Boolean = false) : MasterPosition() {
    override fun invokeOn(player: Player) {
        player.hasTaxFree = true
        if (withMetal) {
            player += Resource.METAL(1)
        }
    }
}

class CraftsmanPosition(var craftsman: Craftsman? = null) : MasterPosition() {
    override fun invokeOn(player: Player) {
        val craftsman = craftsman ?: return
        player += craftsman
    }
}

object GrayWorkersPosition : MasterPosition() {
    override fun invokeOn(player: Player) {
        player += Resource.WORKER(2)
    }
}

class MarketPosition(val queue: Int) : MasterPosition() {
    override fun invokeOn(player: Player) {
        player.marketQueue = queue
    }
}

object StartPlayerPosition : MasterPosition() {
    override fun invokeOn(player: Player) {
        player.playerQueue = Int.MAX_VALUE
    }
}