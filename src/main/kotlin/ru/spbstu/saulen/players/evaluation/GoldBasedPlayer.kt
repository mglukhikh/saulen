package ru.spbstu.saulen.players.evaluation

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.players.*
import java.util.*

class GoldBasedPlayer(
        playerQueue: Int
) : Player(names[playerQueue], playerQueue), Evaluator {
    override fun handleRequest(request: Request): Answer {
        return when (request) {
            FreeResourceRequest -> BuyAnswer(
                    setOf(Resource.STONE, Resource.WOOD, Resource.SAND).maxBy { it.evaluate(numberOfRound) }!!
            )
            is TradeRequest -> PassAnswer // TODO
            is ContestCardRequest -> {
                val cardToIncome = request.cards.map { it to it.evaluate(numberOfRound) }
                val best = cardToIncome.maxBy { it.second }
                when {
                    best == null -> PassAnswer
                    best.second <= 0 -> PassAnswer
                    else -> ContestCardAnswer(best.first)
                }
            }
            is DropBuildingResourceRequest -> DropBuildingResourceAnswer(
                    Resource.BUILDING_RESOURCES.filter { this[it] > 0 }.minBy { it.evaluate(numberOfRound) }!!
            )
            is UseCraftsmanRequest -> {
                val craftsmenCapacities = request.craftsmenCapacities.filter { (_, value) -> value > 0 }
                val accessibleCraftsmen = craftsmenCapacities.keys.filter {
                    craftsman -> craftsman.expenses.all { amount -> has(amount) }
                }.sortedBy { craftsman ->
                    val cost = craftsman.cost.evaluate(numberOfRound)
                    val income = craftsman.income.evaluate(numberOfRound)
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
                        request.craftsmen.filter { it !in notToDrop }.minBy { it.evaluate(numberOfRound)}!!
                )
            }
            is StimulateCraftsmanRequest -> {
                val best = request.craftsmen.filter { requirementMatched(it) }
                        .filter { craftsman ->
                            craftsman.expenses.all { amount -> has(amount * (craftsman.capacity + 1)) }
                        }
                        .maxBy {
                    val cost = it.cost.evaluate(numberOfRound)
                    val income = it.income.evaluate(numberOfRound)
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
                if (request.event.evaluate(numberOfRound) < -5) {
                    UseAdvantageAnswer(request.advantage)
                } else {
                    PassAnswer
                }
            }
            is EventAcknowledgeRequest -> PassAnswer
            is SetMasterRequest -> {
                val bestPosition = request.positions.maxBy { it.evaluate(numberOfRound) }
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
                        it to it.evaluate(numberOfRound)
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

    override fun Resource.evaluate(numberOfRound: Int): Int {
        return if (this.marketCost > 0) {
            this.marketCost
        } else {
            val approximateCost = when (this) {
                Resource.WINNING_POINT -> wpCostPerRound[numberOfRound - 1]
                Resource.MASTER -> 4
                Resource.CRAFTSMEN_LIMIT -> 6
                else -> throw AssertionError("Unknown resource without cost: $this")
            }
            approximateCost
        }
    }

    override fun ResourceAmount.evaluate(numberOfRound: Int): Int =
            resource.evaluate(numberOfRound) * amount

    override fun Production.evaluate(numberOfRound: Int): Int {
        val cost = workers * Resource.WORKER.evaluate(numberOfRound)
        val income = amount * material.evaluate(numberOfRound)
        return income - cost
    }

    override fun ContestCard.evaluate(numberOfRound: Int): Int {
        return when {
            this is Craftsman -> this.evaluate(numberOfRound)
            this is Production -> this.evaluate(numberOfRound)
            else -> throw AssertionError("Strange contest card: $this")
        }
    }

    override fun BoardPosition.evaluate(numberOfRound: Int): Int {
        return when (this) {
            EventProtectionPosition -> 5
            is EventInvocationPosition -> 0
            WollManufakturPosition -> 0
            is AdvantagePosition -> advantage?.evaluate(numberOfRound) ?: 0
            is WinningPointPosition -> wpCostPerRound[numberOfRound - 1] * amount
            ProductionPosition -> 0
            is TaxFreePosition -> (if (withMetal) Resource.METAL.marketCost else 0) + 4
            is TaxPosition -> 0
            is CraftsmanPosition -> craftsman?.evaluate(numberOfRound) ?: 0
            GrayWorkersPosition -> 2
            is MarketPosition -> 0
            TradePosition -> 0
            StartPlayerPosition -> 2
        }
    }

    override fun Advantage.evaluate(numberOfRound: Int): Int {
        val roundsRemaining = 7 - numberOfRound
        return when (this) {
            Madonna -> 15
            Richard -> 2 * roundsRemaining
            Wollmesse -> 8
            Tom -> 4 * roundsRemaining
            Aliena -> 3 * roundsRemaining
            Toledo -> 10
            Jack -> 6
            Vollendung -> 7
            Feierlichkeiten -> 5
            Otto -> roundsRemaining
            Francis -> roundsRemaining
            PriorPhilip -> 3 * roundsRemaining
            Ellen -> 1
            Steuerfreiheit -> 4
            Thomas -> 5
            Remigius -> 6
        }
    }

    override fun Event.evaluate(numberOfRound: Int): Int {
        return when (this) {

            Koenig -> 5
            William -> -4
            RichardEvent -> Resource.METAL.evaluate(numberOfRound)
            Konflikt -> -2 * Resource.WINNING_POINT.evaluate(numberOfRound)
            StadtMauer -> -5
            Philip -> 2
            KalteWinter -> -6
            Freiwilligen -> 6
            Teile -> -5
            Erzbischof -> 5
        }
    }

    override fun Craftsman.evaluate(numberOfRound: Int): Int {
        return if (cost.amount > 0) {
            cost.amount * 2
        } else {
            when (template) {
                is Mortelmischer -> 4
                is Schreiner -> 3
                is Steinmetz -> 3
                else -> throw AssertionError("Unknown craftsman without cost: $this")
            }
        }
    }

    companion object {
        val names = listOf("MacDuck", "Rocky", "Midas", "Goldmaster")

        private val wpCostPerRound = arrayOf(7, 6, 6, 5, 5, 4)
    }
}