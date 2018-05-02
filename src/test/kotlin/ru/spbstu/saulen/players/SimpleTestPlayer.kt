package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Production
import ru.spbstu.saulen.cards.Schreiner
import ru.spbstu.saulen.cards.Steinmetz
import ru.spbstu.saulen.game.Resource
import java.util.*

internal class SimpleTestPlayer(
        playerQueue: Int
) : Player(names[playerQueue], playerQueue) {

    internal var buyCraftsmen = true

    internal var randomMasters = false

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
                if (!randomMasters) {
                    positions.firstOrNull()?.let { SetMasterAnswer(it) } ?: PassAnswer
                } else {
                    if (positions.isEmpty()) PassAnswer else SetMasterAnswer(positions[random.nextInt(positions.size)])
                }
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
            FreeResourceRequest -> BuyAnswer(Resource.STONE(1))
            is DropCraftsmanRequest -> DropCraftsmanAnswer(request.craftsmen.minBy { it.cost.amount }!!)
            is TradeRequest -> when {
                has(Resource.GOLD(8)) -> {
                    val market = request.market
                    when {
                        market[Resource.STONE] > 0 ->
                            BuyAnswer(Resource.STONE(1))
                        market[Resource.WOOD] > 0 && craftsmen.any { it.template is Schreiner } ->
                            BuyAnswer(Resource.WOOD(1))
                        market[Resource.SAND] > 0 ->
                            BuyAnswer(Resource.SAND(1))
                        else -> PassAnswer
                    }
                }
                !has(Resource.GOLD(2)) -> {
                    when {
                        this[Resource.METAL] > 3 ->
                            SellAnswer(Resource.METAL(1))
                        this[Resource.SAND] > 0 ->
                            SellAnswer(Resource.SAND(1))
                        this[Resource.WOOD] > 0 ->
                            SellAnswer(Resource.WOOD(1))
                        this[Resource.STONE] > 0 && craftsmen.any { it.template === Steinmetz } ->
                            SellAnswer(Resource.STONE(1))
                        else -> PassAnswer
                    }
                }
                else -> PassAnswer
            }
        }
    }

    companion object {
        val names = listOf("Alex", "Bob", "Chris", "Dick")

        val random = Random()
    }
}