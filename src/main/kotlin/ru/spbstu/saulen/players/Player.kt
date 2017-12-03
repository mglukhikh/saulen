package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount
import java.util.*

abstract class Player(val name: String) {
    val resources: MutableMap<Resource, Int> = EnumMap<Resource, Int>(Resource::class.java).apply {
        put(GOLD, 20)
        put(SAND, 0)
        put(WOOD, 0)
        put(STONE, 0)
        put(METAL, 0)
        put(VICTORY_POINTS, 2)
    }

    operator fun plus(amount: ResourceAmount) {
        resources[amount.resource] = resources[amount.resource]!! + amount.amount
    }

    operator fun minus(amount: ResourceAmount) {
        resources[amount.resource] = resources[amount.resource]!! - amount.amount
    }

    val craftsmen = Craftsman.startingCards.toMutableList()

    operator fun plus(craftsman: Craftsman) {
        craftsmen += craftsman
    }

    operator fun minus(craftsman: Craftsman) {
        craftsmen -= craftsman
    }

    val advantages = mutableListOf<Advantage>()

    operator fun plus(advantage: Advantage) {
        advantages += advantage
    }

    val production = mutableListOf<Production>()
}