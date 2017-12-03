package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount

sealed class Advantage(val kind: AdvantageKind, val lastRound: Boolean = false)

sealed class IncomeAdvantage(
        immediate: Boolean,
        lastRound: Boolean = false,
        val income: List<ResourceAmount> = emptyList()
) : Advantage(if (immediate) AdvantageKind.IMMEDIATE else AdvantageKind.CONTINUOUS) {
    constructor(immediate: Boolean, income: ResourceAmount, lastRound: Boolean = false):
            this(immediate, lastRound, listOf(income))
}

object Madonna : IncomeAdvantage(immediate = true, income = WINNING_POINT(3))

object Richard : IncomeAdvantage(immediate = false, income = SAND(1))

object Wollmesse : IncomeAdvantage(immediate = true, income = GOLD(8))

object Tom : IncomeAdvantage(immediate = false, income = STONE(1))

object Aliena : IncomeAdvantage(immediate = false, income = WOOD(1))

object Toledo : IncomeAdvantage(immediate = true, income = METAL(2))

object Vollendung : IncomeAdvantage(
        immediate = true,
        income = listOf(WOOD(1), STONE(1)),
        lastRound = true
)

object Feierlichkeiten : IncomeAdvantage(
        immediate = true,
        income = METAL(1),
        lastRound = true
)