package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.players.Player

sealed class Advantage(val kind: AdvantageKind) {

    abstract fun invokeOn(player: Player)

    companion object {
        val regular = listOf(
                Madonna,
                Richard,
                Wollmesse,
                Tom,
                Aliena,
                Toledo,
                Jack,
                Otto,
                Francis,
                PriorPhilip
        )

        val lastRound = listOf(
                Vollendung,
                Feierlichkeiten
        )
    }
}

sealed class IncomeAdvantage(
        immediate: Boolean,
        val income: List<ResourceAmount> = emptyList()
) : Advantage(if (immediate) AdvantageKind.IMMEDIATE else AdvantageKind.CONTINUOUS) {

    override fun invokeOn(player: Player) {
        for (income in this.income) {
            player += income
        }
        if (kind == AdvantageKind.IMMEDIATE) {
            player -= this
        }
    }

    constructor(immediate: Boolean, income: ResourceAmount):
            this(immediate, listOf(income))
}

object Madonna : IncomeAdvantage(immediate = true, income = WINNING_POINT(3))

object Richard : IncomeAdvantage(immediate = false, income = SAND(1))

object Wollmesse : IncomeAdvantage(immediate = true, income = GOLD(8))

object Tom : IncomeAdvantage(immediate = false, income = STONE(1))

object Aliena : IncomeAdvantage(immediate = false, income = WOOD(1))

object Toledo : IncomeAdvantage(immediate = true, income = METAL(2))

object Jack : IncomeAdvantage(immediate = true, income = CRAFTSMEN_LIMIT(1))

object Vollendung : IncomeAdvantage(
        immediate = true,
        income = listOf(WOOD(1), STONE(1))
)

object Feierlichkeiten : IncomeAdvantage(
        immediate = true,
        income = METAL(1)
)

object Otto : IncomeAdvantage(immediate = true, income = WORKER(1))

object Francis : Advantage(AdvantageKind.CONTINUOUS) {
    override fun invokeOn(player: Player) {

    }
}

object PriorPhilip : Advantage(AdvantageKind.CONTINUOUS) {
    override fun invokeOn(player: Player) {

    }
}