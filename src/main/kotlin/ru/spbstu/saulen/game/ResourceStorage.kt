package ru.spbstu.saulen.game

interface ResourceStorage {
    operator fun plusAssign(amount: ResourceAmount)

    operator fun minusAssign(amount: ResourceAmount)

    operator fun get(resource: Resource): Int

    val buildingResourceCount: Int get() = Resource.BUILDING_RESOURCES.sumBy { this[it] }
}