package ru.spbstu.saulen.game

import java.util.*

enum class Resource(
        val marketCost: Int
) : Requirement {
    SAND(2),
    WOOD(3),
    STONE(4),
    METAL(5),
    GOLD(1),
    WINNING_POINT(0),
    WORKER(1),
    MASTER(0),
    CRAFTSMEN_LIMIT(0);

    operator fun invoke(amount: Int) = ResourceAmount(this, amount)

    override fun toString(): String {
        return name.toLowerCase().replace('_', ' ')
    }

    companion object {
        val BUILDING_RESOURCES: EnumSet<Resource> = EnumSet.of(SAND, WOOD, STONE, METAL)
    }
}

