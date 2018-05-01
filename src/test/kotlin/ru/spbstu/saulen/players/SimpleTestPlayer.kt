package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Production
import ru.spbstu.saulen.game.Resource

internal class SimpleTestPlayer(
        playerQueue: Int
) : Player(names[playerQueue], playerQueue) {

    internal var buyCraftsmen = true

    override fun handleRequest(request: Request): Answer {
        return when (request) {
            is ContestCardRequest -> {
                val cards = request.cards
                require(cards.all { has(it.cost) })
                cards.filterIsInstance<Production>()
                        .firstOrNull { it.workers <= this[Resource.WORKER] }
                        ?.let { ContestCardAnswer(it) }
                        ?: run {
                            if (!buyCraftsmen) return PassAnswer
                            cards.filterIsInstance<Craftsman>()
                                    .firstOrNull { it.cost.amount <= this[Resource.GOLD] }
                                    ?.let { ContestCardAnswer(it) }
                                    ?: PassAnswer
                        }
            }
            is SetMasterRequest -> {
                val positions = request.positions
                positions.firstOrNull()?.let { SetMasterAnswer(it) } ?: PassAnswer
            }
            is DropBuildingResourceRequest -> {
                for (resource in Resource.BUILDING_RESOURCES) {
                    if (this[resource] > 0) {
                        return DropBuildingResourceAnswer(resource(minOf(request.toDrop, this[resource])))
                    }
                }
                throw AssertionError("Building resources not found but should be dropped: ${request.toDrop}")
            }
            is UseCraftsmanRequest -> {
                for ((craftsman, capacity) in request.craftsmenCapacities) {
                    if (capacity <= 0) continue
                    val expenses = craftsman.expenses
                    if (!expenses.all { this.has(it) }) continue
                    var toProduce = 1
                    while (toProduce < capacity) {
                        toProduce++
                        if (!expenses.all { this.has(it * toProduce) }) {
                            toProduce--
                            break
                        }
                    }
                    return UseCraftsmanAnswer(craftsman, toProduce)
                }
                PassAnswer
            }
            FreeResourceRequest -> TODO()
            is TradeRequest -> TODO()
        }
    }

    companion object {
        val names = listOf("Alex", "Bob", "Chris", "Dick")
    }
}