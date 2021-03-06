package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.*
import ru.spbstu.saulen.game.Resource.*

abstract class Player private constructor(
        val name: String,
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

    constructor(name: String, playerQueue: Int): this(name, Stock(), playerQueue)

    val craftsmen = Craftsman.startingCards.toMutableList()

    operator fun plusAssign(craftsman: Craftsman) {
        craftsmen += craftsman
    }

    operator fun minusAssign(craftsman: Craftsman) {
        craftsmen -= craftsman
    }

    var hasEventProtection = false

    var hasTaxFree = false

    private var hasGrayWorkers = false

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

    fun requirementMatched(craftsman: Craftsman): Boolean {
        val requirement = craftsman.requirement
        return when (requirement) {
            Resource.METAL::class.java -> has(Resource.METAL(1))
            Mortelmischer::class.java -> craftsmen.any { requirement.isInstance(it.template) }
            null -> true
            else -> throw AssertionError("Unknown craftsman requirement: $requirement")
        }
    }

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

    protected var numberOfRound = 1

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
        numberOfRound++
    }

    override fun toString(): String = name

    internal fun resourceDescription(checkNegative: Boolean, vararg resources: Resource) =
            resourceDescription(resources.toList(), checkNegative)

    internal fun resourceDescription(resources: List<Resource>, checkNegative: Boolean = true): String {
        if (checkNegative) {
            resources.forEach {
                val amount = this[it]
                if (amount < 0) {
                    throw AssertionError("Negative amount of $it: $amount")
                }
            }
        }
        return resources.joinToString { "$it: ${this[it]}" }
    }
}