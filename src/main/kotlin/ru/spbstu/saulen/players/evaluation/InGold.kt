package ru.spbstu.saulen.players.evaluation

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.game.ResourceStorage
import ru.spbstu.saulen.players.Player

private val wpCostPerRound = arrayOf(7, 6, 6, 5, 5, 4)

fun Resource.evaluateInGold(numberOfRound: Int): Int {
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

fun ResourceAmount.evaluateInGold(numberOfRound: Int): Int =
        resource.evaluateInGold(numberOfRound) * amount

fun ResourceStorage.evaluateInGold(numberOfRound: Int): Int {
    var total = 0
    for (resource in Resource.values()) {
        val amount = this[resource]
        total += amount + resource.evaluateInGold(numberOfRound)
    }
    return total
}

fun Production.evaluateInGold(numberOfRound: Int): Int {
    val cost = workers * Resource.WORKER.evaluateInGold(numberOfRound)
    val income = amount * material.evaluateInGold(numberOfRound)
    return income - cost
}

fun ContestCard.evaluateInGold(numberOfRound: Int): Int {
    return when {
        this is Craftsman -> this.evaluateInGold(numberOfRound)
        this is Production -> this.evaluateInGold(numberOfRound)
        else -> throw AssertionError("Strange contest card: $this")
    }
}

fun BoardPosition.evaluateInGold(numberOfRound: Int): Int {
    return when (this) {
        EventProtectionPosition -> 5
        is EventInvocationPosition -> 0
        WollManufakturPosition -> 0
        is AdvantagePosition -> advantage?.evaluateInGold(numberOfRound) ?: 0
        is WinningPointPosition -> wpCostPerRound[numberOfRound - 1] * amount
        ProductionPosition -> 0
        is TaxFreePosition -> (if (withMetal) Resource.METAL.marketCost else 0) + 4
        is TaxPosition -> 0
        is CraftsmanPosition -> craftsman?.evaluateInGold(numberOfRound) ?: 0
        GrayWorkersPosition -> 2
        is MarketPosition -> 0
        TradePosition -> 0
        StartPlayerPosition -> 2
    }
}

fun Advantage.evaluateInGold(numberOfRound: Int): Int {
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

fun Event.evaluateInGold(numberOfRound: Int): Int {
    return when (this) {

        Koenig -> 5
        William -> -4
        RichardEvent -> Resource.METAL.evaluateInGold(numberOfRound)
        Konflikt -> -2 * Resource.WINNING_POINT.evaluateInGold(numberOfRound)
        StadtMauer -> -5
        Philip -> 2
        KalteWinter -> -6
        Freiwilligen -> 6
        Teile -> -5
        Erzbischof -> 5
    }
}

fun Craftsman.evaluateInGold(numberOfRound: Int): Int {
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

fun Player.evaluateInGold(numberOfRound: Int): Int {
    var total = (this as ResourceStorage).evaluateInGold(numberOfRound)
    if (hasTaxFree) total += 4
    if (hasEventProtection) total += 4
    for (craftsman in craftsmen) {
        total += craftsman.evaluateInGold(numberOfRound)
    }
    for (advantage in advantages) {
        total += advantage.evaluateInGold(numberOfRound)
    }
    return total
}