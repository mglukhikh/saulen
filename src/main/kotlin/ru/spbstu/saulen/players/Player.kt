package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Material
import ru.spbstu.saulen.game.Material.*
import ru.spbstu.saulen.game.MaterialAmount
import java.util.*

abstract class Player(val name: String) {
    val materials: MutableMap<Material, Int> = EnumMap<Material, Int>(Material::class.java).apply {
        put(GOLD, 20)
        put(SAND, 0)
        put(WOOD, 0)
        put(STONE, 0)
        put(METAL, 0)
        put(VICTORY_POINTS, 2)
    }

    operator fun plus(amount: MaterialAmount) {
        materials[amount.material] = materials[amount.material]!! + amount.amount
    }

    operator fun minus(amount: MaterialAmount) {
        materials[amount.material] = materials[amount.material]!! - amount.amount
    }

    val craftsmen = mutableListOf(SimpleMortelmischer, SimpleSchreiner, Steinmetz)

    val advantages = mutableListOf<Advantage>()

    operator fun plus(advantage: Advantage) {
        advantages += advantage
    }

    var workers = 12

    val production = mutableListOf<Production>()
}