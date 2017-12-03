package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.game.Requirement

sealed class CraftsmanTemplate(
        val income: ResourceAmount,
        val requirement: Class<out Requirement>? = null,
        val expenses: List<ResourceAmount> = listOf(GOLD(0))
) : Requirement {
    constructor(income: ResourceAmount, expense: ResourceAmount):
            this(income, expenses = listOf(expense))

    constructor(victoryPoints: Int, expense: ResourceAmount = GOLD(0)):
            this(WINNING_POINT(victoryPoints), expense)

    operator fun invoke(capacity: Int, cost: Int = 0) =
            Craftsman(this, capacity, cost)
}

sealed class Mortelmischer(sandAmount: Int) : CraftsmanTemplate(victoryPoints = 1, expense = SAND(sandAmount))

object SimpleMortelmischer : Mortelmischer(sandAmount = 3)

sealed class Schreiner(
        income: ResourceAmount,
        woodAmount: Int
) : CraftsmanTemplate(income, expense = WOOD(woodAmount))

object SimpleSchreiner : Schreiner(income = WINNING_POINT(1), woodAmount = 2)

object Steinmetz : CraftsmanTemplate(victoryPoints = 1, expense = STONE(2))

object Maurer : CraftsmanTemplate(
        income = WINNING_POINT(1),
        requirement = Mortelmischer::class.java,
        expenses = listOf(STONE(1))
)

object Werzeugmacher : CraftsmanTemplate(income = GOLD(2), requirement = METAL::class.java)

object GreedySchreiner : Schreiner(income = GOLD(4), woodAmount = 1)

object AdvancedMortelmischer : Mortelmischer(sandAmount = 2)

object Topfer : CraftsmanTemplate(victoryPoints = 1, expense = SAND(1))

object Statiker : CraftsmanTemplate(victoryPoints = 1)

object Zimmermann : CraftsmanTemplate(victoryPoints = 1, expense = WOOD(1))

object Bildhauer : CraftsmanTemplate(victoryPoints = 1, expense = STONE(2))

object Glasblaser : CraftsmanTemplate(
        income = WINNING_POINT(3),
        expenses = listOf(SAND(1), METAL(1))
)

object Goldschmied : CraftsmanTemplate(victoryPoints = 1, expense = GOLD(3))

object Glockengiesser : CraftsmanTemplate(victoryPoints = 4, expense = METAL(2))

object Orgelbauer : CraftsmanTemplate(
        income = WINNING_POINT(6),
        expenses = listOf(WOOD(1), METAL(1))
)