package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.*

abstract class Player private constructor(
        val name: String,
        val color: Color,
        val stock: Stock
) : ResourceStorage by stock {

    constructor(name: String, color: Color): this(name, color, Stock())

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