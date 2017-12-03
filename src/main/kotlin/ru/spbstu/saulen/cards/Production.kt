package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount

data class Production(val material: Resource, val workers: Int, val amount: Int) {

    constructor(materialAmount: ResourceAmount, workers: Int):
            this(materialAmount.resource, workers, materialAmount.amount)

    operator fun invoke() = material(amount)

    fun up() = Production(material, workers, amount + 1)

    companion object {
        val cards = listOf(
                Production(SAND(2), workers = 2),
                Production(SAND(3), workers = 4),
                Production(SAND(4), workers = 7),
                Production(WOOD(2), workers = 3),
                Production(WOOD(3), workers = 6),
                Production(WOOD(4), workers = 9),
                Production(STONE(2), workers = 5),
                Production(STONE(3), workers = 8),
                Production(STONE(4), workers = 10)
        )
    }
}