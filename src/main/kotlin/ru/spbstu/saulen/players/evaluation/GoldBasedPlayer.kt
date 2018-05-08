package ru.spbstu.saulen.players.evaluation

import ru.spbstu.saulen.cards.Mortelmischer
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.players.*
import java.util.*

class GoldBasedPlayer(
        playerQueue: Int
) : Player(names[playerQueue], playerQueue) {
    override fun handleRequest(request: Request): Answer {
        return when (request) {
            FreeResourceRequest -> BuyAnswer(
                    setOf(Resource.STONE, Resource.WOOD, Resource.SAND).maxBy { it.evaluateInGold(numberOfRound) }!!
            )
            is TradeRequest -> PassAnswer // TODO
            is ContestCardRequest -> {
                val cardToIncome = request.cards.map { it to it.evaluateInGold(numberOfRound) }
                val best = cardToIncome.maxBy { it.second }
                when {
                    best == null -> PassAnswer
                    best.second <= 0 -> PassAnswer
                    else -> ContestCardAnswer(best.first)
                }
            }
            is DropBuildingResourceRequest -> DropBuildingResourceAnswer(
                    Resource.BUILDING_RESOURCES.filter { this[it] > 0 }.minBy { it.evaluateInGold(numberOfRound) }!!
            )
            is UseCraftsmanRequest -> {
                val craftsmenCapacities = request.craftsmenCapacities.filter { (_, value) -> value > 0 }
                val accessibleCraftsmen = craftsmenCapacities.keys.filter {
                    craftsman -> craftsman.expenses.all { amount -> has(amount) }
                }.sortedBy { craftsman ->
                    val cost = craftsman.cost.evaluateInGold(numberOfRound)
                    val income = craftsman.income.evaluateInGold(numberOfRound)
                    income - cost
                }
                if (accessibleCraftsmen.isEmpty()) {
                    PassAnswer
                } else {
                    UseCraftsmanAnswer(accessibleCraftsmen.last(), 1)
                }
            }
            is DropCraftsmanRequest -> {
                val singleMortelmischer = request.craftsmen.singleOrNull { it.template is Mortelmischer }
                val notToDrop = listOfNotNull(singleMortelmischer)
                return ChooseCraftsmanAnswer(
                        request.craftsmen.filter { it !in notToDrop }.minBy { it.evaluateInGold(numberOfRound)}!!
                )
            }
            is StimulateCraftsmanRequest -> {
                val best = request.craftsmen.filter { requirementMatched(it) }
                        .filter { craftsman ->
                            craftsman.expenses.all { amount -> has(amount * (craftsman.capacity + 1)) }
                        }
                        .maxBy {
                    val cost = it.cost.evaluateInGold(numberOfRound)
                    val income = it.income.evaluateInGold(numberOfRound)
                    income - cost
                }
                ChooseCraftsmanAnswer(best ?: request.craftsmen.first())
            }
            is SetFreeMasterAdvantageRequest -> {
                if (request.cost > 5) UseAdvantageAnswer(request.advantage) else PassAnswer
            }
            is TaxFreeAdvantageRequest -> {
                if (request.taxLevel > 3) UseAdvantageAnswer(request.advantage) else PassAnswer
            }
            is EventProtectionAdvantageRequest -> {
                if (request.event.evaluateInGold(numberOfRound) < -5) {
                    UseAdvantageAnswer(request.advantage)
                } else {
                    PassAnswer
                }
            }
            is EventAcknowledgeRequest -> PassAnswer
            is SetMasterRequest -> {
                val bestPosition = request.positions.maxBy { it.evaluateInGold(numberOfRound) }
                return bestPosition?.let { SetMasterAnswer(it) } ?: PassAnswer
            }
            is CancelMasterRequest -> {
                val cost = request.cost
                if (request.own) {
                    when {
                        cost < 4 -> PassAnswer
                        this[Resource.GOLD] > cost + 1 -> PassAnswer
                        else -> CancelAnswer
                    }
                } else {
                    val positionToIncome = request.positions.map {
                        it to it.evaluateInGold(numberOfRound)
                    }
                    val income = positionToIncome.map { it.second }.max() ?: 0
                    when {
                        income - cost < 2 -> PassAnswer
                        positionToIncome.count { it.second >= income - 1 } > 1 -> PassAnswer
                        else -> CancelAnswer
                    }
                }
            }
        }
    }


    companion object {
        val names = listOf("MacDuck", "Rocky", "Midas", "Goldmaster")

        val random = Random()
    }
}