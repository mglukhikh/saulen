package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.players.Player

sealed class Event(val negative: Boolean) {
    abstract fun invokeOn(player: Player)

    companion object {
        val events = listOf(
                Koenig,
                William,
                RichardEvent,
                Konflikt
        )
    }
}

sealed class ResourceEvent(val amount: ResourceAmount) : Event(negative = amount.amount < 0) {
    override fun invokeOn(player: Player) {
        player += amount
    }
}

object Koenig : ResourceEvent(Resource.GOLD(5))

object William : ResourceEvent(Resource.GOLD(-4))

object RichardEvent : ResourceEvent(Resource.METAL(1))

object Konflikt : ResourceEvent(Resource.WINNING_POINT(-2))

