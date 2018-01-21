package ru.spbstu.saulen.board

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.Stock
import ru.spbstu.saulen.players.Player
import java.util.*

class Board {

    private val deckOfEvents = Event.events.toMutableList()

    private val regularAdvantages = Advantage.regular.toMutableList()

    private val lastRoundAdvantages = Advantage.lastRound.toMutableList()

    val masterPositions = mutableMapOf<MasterPosition, Player?>().apply {
        for (position in MasterPosition.positions) {
            this[position] = null
        }
    }

    val contestCards = mutableListOf<ContestCard>()

    val market = Stock()

    var currentEvent: Event? = null

    fun prepareForRound(craftsmen: MutableList<Craftsman>, r: Random, lastRound: Boolean) {
        contestCards.clear()
        contestCards.addAll(Production.cards)
        val first = r.nextInt(craftsmen.size)
        contestCards += craftsmen[first]
        craftsmen.removeAt(first)
        val second = r.nextInt(craftsmen.size)
        contestCards += craftsmen[second]
        craftsmen.removeAt(second)
        val deckOfAdvantages = if (lastRound) lastRoundAdvantages else regularAdvantages
        for (position in MasterPosition.positions) {
            masterPositions[position] = null
            if (position is CraftsmanPosition) {
                position.craftsman = craftsmen[0]
                craftsmen.removeAt(0)
            }
            if (position is AdvantagePosition) {
                val advantageIndex = r.nextInt(deckOfAdvantages.size)
                position.advantage = deckOfAdvantages[advantageIndex]
                deckOfAdvantages.removeAt(advantageIndex)
            }
        }
        market.apply {
            this += Resource.WOOD(4)
            this += Resource.SAND(4)
            this += Resource.METAL(4)
        }
        if (craftsmen.isNotEmpty()) {
            throw AssertionError("All craftsmen should be spent during round preparation")
        }
        val eventIndex = r.nextInt(deckOfEvents.size)
        currentEvent = deckOfEvents[eventIndex]
        deckOfEvents.removeAt(eventIndex)
    }
}