package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.*
import ru.spbstu.saulen.game.Resource.*

abstract class Player private constructor(
        val name: String,
        val color: Color,
        private val stock: Stock,
        var playerQueue: Int
) : ResourceStorage by stock {

    init {
        listOf(
                GOLD(20 + playerQueue),
                WINNING_POINT(2),
                WORKER(12),
                MASTER(3),
                CRAFTSMEN_LIMIT(5)
        ).forEach { this.plusAssign(it) }
    }

    abstract fun handleRequest(request: Request): Answer

    constructor(name: String, color: Color, playerQueue: Int): this(name, color, Stock(), playerQueue)

    val craftsmen = Craftsman.startingCards.toMutableList()

    operator fun plusAssign(craftsman: Craftsman) {
        craftsmen += craftsman
    }

    operator fun minusAssign(craftsman: Craftsman) {
        craftsmen -= craftsman
    }

    var hasEventProtection = false

    var hasTaxFree = false

    var hasGrayWorkers = false

    var marketQueue = -1

    val advantages = mutableListOf<Advantage>()

    operator fun plusAssign(advantage: Advantage) {
        advantages += advantage
    }

    operator fun minusAssign(advantage: Advantage) {
        advantages -= advantage
    }

    val production = mutableListOf<Production>()

    fun has(amount: ResourceAmount) = this[amount.resource] >= amount.amount

    operator fun plusAssign(production: Production) {
        this -= WORKER(production.workers)
        this.production += production
    }

    fun produce() {
        for (production in this.production) {
            this += WORKER(production.workers)
            this += production()
        }
        production.clear()
    }

    fun endOfRound(numberOfPlayers: Int) {
        hasEventProtection = false
        hasTaxFree = false
        if (hasGrayWorkers) {
            hasGrayWorkers = false
            this -= WORKER(2)
        }
        else if (this[WORKER] > 13) {
            hasGrayWorkers = true
        }
        marketQueue = -1
        playerQueue = when (playerQueue) {
            Int.MAX_VALUE -> -1
            numberOfPlayers - 1 -> 0
            else -> playerQueue + 1
        }
    }

    override fun toString(): String = name
}