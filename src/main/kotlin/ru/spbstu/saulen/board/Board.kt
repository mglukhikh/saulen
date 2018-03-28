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

    val positions = linkedMapOf<BoardPosition, Player?>().apply {
        for (position in BoardPosition.positions) {
            this[position] = null
        }
    }

    val contestCards = mutableListOf<ContestCard>()

    val market = Stock()

    private fun taxLevel(r: Random): Int = when (r.nextInt(6)) {
        0 -> 2
        1,2 -> 3
        3,4 -> 4
        else -> 5
    }

    fun prepareForRound(craftsmenList: List<Craftsman>, r: Random, lastRound: Boolean) {
        val craftsmen = craftsmenList.toMutableList()
        contestCards.clear()
        val productionCards = Production.cards.shuffled(r)
        contestCards.addAll(productionCards.subList(0, productionCards.size - 2))
        val first = r.nextInt(craftsmen.size)
        contestCards += craftsmen[first]
        craftsmen.removeAt(first)
        val second = r.nextInt(craftsmen.size)
        contestCards += craftsmen[second]
        craftsmen.removeAt(second)
        val deckOfAdvantages = if (lastRound) lastRoundAdvantages else regularAdvantages
        for (position in BoardPosition.positions) {
            positions[position] = null
            when (position) {
                is CraftsmanPosition -> {
                    position.craftsman = craftsmen[0]
                    craftsmen.removeAt(0)
                }
                is AdvantagePosition -> {
                    val advantageIndex = r.nextInt(deckOfAdvantages.size)
                    position.advantage = deckOfAdvantages[advantageIndex]
                    deckOfAdvantages.removeAt(advantageIndex)
                }
                is EventInvocationPosition -> {
                    val eventIndex = r.nextInt(deckOfEvents.size)
                    position.event = deckOfEvents[eventIndex]
                    deckOfEvents.removeAt(eventIndex)
                }
                is TaxPosition -> {
                    position.amount = taxLevel(r)
                }
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
    }
}