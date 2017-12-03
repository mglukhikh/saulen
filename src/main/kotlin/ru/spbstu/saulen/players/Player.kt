package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.*
import ru.spbstu.saulen.game.Resource.GOLD
import ru.spbstu.saulen.game.Resource.WORKER

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

    fun isAbleToProduce(production: Production) = this[WORKER] >= production.workers

    operator fun plus(production: Production) {
        this -= WORKER(production.workers)
        this.production += production
    }

    fun produce() {
        this += GOLD(this[WORKER])
        for (production in this.production) {
            this += WORKER(production.workers)
            this += production()
        }
        production.clear()
    }
}