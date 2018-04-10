package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Production
import ru.spbstu.saulen.game.Color
import ru.spbstu.saulen.game.Resource

class SimpleTestPlayer(
        color: Color,
        playerQueue: Int
) : Player("Test player #$playerQueue", color, playerQueue) {
    override fun handleRequest(request: Request): Answer {
        return when (request) {
            is ContestCardRequest -> {
                val cards = request.cards
                cards.filterIsInstance<Production>()
                        .firstOrNull { it.workers <= this[Resource.WORKER] }
                        ?.let { ContestCardAnswer(it) }
                        ?: cards.filterIsInstance<Craftsman>()
                                    .firstOrNull { it.cost.amount <= this[Resource.GOLD] }
                                    ?.let { ContestCardAnswer(it) }
                        ?: PassAnswer
            }
            FreeResourceRequest -> TODO()
            is TradeRequest -> TODO()
            is SetMasterRequest -> TODO()
        }
    }
}