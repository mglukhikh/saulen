package ru.spbstu.saulen.game

import java.util.*

class Stock : ResourceStorage {
    val resources: MutableMap<Resource, Int> = EnumMap<Resource, Int>(Resource::class.java).apply {
        put(Resource.GOLD, 20)
        put(Resource.SAND, 0)
        put(Resource.WOOD, 0)
        put(Resource.STONE, 0)
        put(Resource.METAL, 0)
        put(Resource.VICTORY_POINTS, 2)
        put(Resource.WORKER, 12)
        put(Resource.MASTER, 2)
    }

    override operator fun plus(amount: ResourceAmount) {
        resources[amount.resource] = this[amount.resource] + amount.amount
    }

    override operator fun minus(amount: ResourceAmount) {
        resources[amount.resource] = this[amount.resource] - amount.amount
    }

    override operator fun get(resource: Resource): Int = resources[resource]!!
}