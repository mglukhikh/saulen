package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Production
import ru.spbstu.saulen.game.Color
import ru.spbstu.saulen.game.Resource

internal class SimpleTestPlayer(
        color: Color,
        playerQueue: Int
) : Player("Test player #$playerQueue", color, playerQueue) {
    override fun handleRequest(request: Request): Answer {
        return when (request) {
            is ContestCardRequest -> {
                val cards = request.cards
                require(cards.all { has(it.cost) })
                cards.filterIsInstance<Production>()
                        .firstOrNull { it.workers <= this[Resource.WORKER] }
                        ?.let { ContestCardAnswer(it) }
                        ?: cards.filterIsInstance<Craftsman>()
                                    .firstOrNull { it.cost.amount <= this[Resource.GOLD] }
                                    ?.let { ContestCardAnswer(it) }
                        ?: PassAnswer
            }
            is SetMasterRequest -> {
                val positions = request.positions
                positions.firstOrNull()?.let { SetMasterAnswer(it) } ?: PassAnswer
            }
            FreeResourceRequest -> TODO()
            is TradeRequest -> TODO()
        }
    }
}