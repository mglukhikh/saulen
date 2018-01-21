package ru.spbstu.saulen.board

import ru.spbstu.saulen.cards.ContestCard
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Production
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.Stock
import ru.spbstu.saulen.players.Player
import java.util.*

class Board {

    val masterPositions = mutableMapOf<MasterPosition, Player?>().apply {
        for (position in MasterPosition.positions) {
            this[position] = null
        }
    }

    val contestCards = mutableListOf<ContestCard>()

    val market = Stock()

    fun prepareForRound(craftsmen: MutableList<Craftsman>, r: Random) {
        contestCards.clear()
        contestCards.addAll(Production.cards)
        val first = r.nextInt(craftsmen.size)
        contestCards += craftsmen[first]
        craftsmen.removeAt(first)
        val second = r.nextInt(craftsmen.size)
        contestCards += craftsmen[second]
        craftsmen.removeAt(second)
        for (position in MasterPosition.positions) {
            masterPositions[position] = null
            if (position is CraftsmanPosition) {
                position.craftsman = craftsmen[0]
                craftsmen.removeAt(0)
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