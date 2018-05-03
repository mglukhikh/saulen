package ru.spbstu.saulen.players.evaluation

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceStorage
import ru.spbstu.saulen.players.Player

fun ResourceStorage.evaluateInGold(roundsRemaining: Int): Int {
    var total = 0
    for (resource in Resource.values()) {
        val amount = this[resource]
        val marketCost = resource.marketCost
        if (marketCost > 0) {
            total += amount * marketCost
        } else {
            val approximateCost = when (resource) {
                Resource.WINNING_POINT -> 5
                Resource.MASTER -> 4
                Resource.CRAFTSMEN_LIMIT -> 6
                else -> throw AssertionError("Unknown resource without cost: $resource")
            }
            total += amount * approximateCost
        }
    }
    return total
}

fun Player.evaluateInGold(roundsRemaining: Int): Int {
    var total = (this as ResourceStorage).evaluateInGold(roundsRemaining)
    if (hasTaxFree) total += 4
    if (hasEventProtection) total += 4
    for (craftsman in craftsmen) {
        total += if (craftsman.cost.amount > 0) {
            craftsman.cost.amount * 2
        } else {
            when (craftsman.template) {
                is Mortelmischer -> 4
                is Schreiner -> 3
                is Steinmetz -> 3
                else -> throw AssertionError("Unknown craftsman without cost: $craftsman")
            }
        }
    }
    for (advantage in advantages) {
        total += when (advantage) {
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
            Thomas -> 4
            Remigius -> 6
        }
    }
    return total
}