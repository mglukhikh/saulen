package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.players.DropCraftsmanAnswer
import ru.spbstu.saulen.players.DropCraftsmanRequest
import ru.spbstu.saulen.players.Player

sealed class Event(val negative: Boolean) {
    abstract fun invokeOn(player: Player)

    companion object {
        val events = listOf(
                Koenig,
                William,
                RichardEvent,
                Konflikt,
                StadtMauer,
                Philip,
                KalteWinter,
                Freiwilligen,
                Teile,
                Erzbischof
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

object StadtMauer : ResourceEvent(Resource.MASTER(-1))

object Philip : Event(negative = false) {
    override fun invokeOn(player: Player) {
        val workers = player[Resource.WORKER]
        player += Resource.GOLD(minOf(workers, 5))
    }
}

object KalteWinter : Event(negative = true) {
    override fun invokeOn(player: Player) {
        for (production in player.production) {
            player -= production.material(1)
        }
    }
}

object Freiwilligen : Event(negative = false) {
    override fun invokeOn(player: Player) {
        for (production in player.production) {
            player += production.material(1)
        }
    }
}

object Teile : Event(negative = true) {
    // Alle Spieler verlieren einen Handwerker ihrer Wahl
    override fun invokeOn(player: Player) {
        val craftsmen = player.craftsmen
        //log("Craftsmen: $craftsmen")
        //log("Player $player has too much craftsmen: ${craftsmen.size}/$craftsmenLimit")
        do {
            val dropAnswer = player.handleRequest(
                    DropCraftsmanRequest(craftsmen, craftsmen.size - 1)
            )
            var correct = true
            if (dropAnswer is DropCraftsmanAnswer && dropAnswer.craftsman in craftsmen) {
                //log("Player $player dropped ${dropAnswer.craftsman}")
                player.craftsmen -= dropAnswer.craftsman
            } else {
                correct = false
            }
        } while (!correct)
    }
}

object Erzbischof : Event(negative = false) {
    // Fuer jeden Spieler erhoeht sich die Kapazitaet eines eigenes Handwerkers um 1
    // (gilt nicht fuer Orgelbauer und Glockengiesser)
    override fun invokeOn(player: Player) {
        // TODO (probably not here)
    }
}