package ru.spbstu.saulen.game

import java.util.*

class Stock : ResourceStorage {
    val resources: MutableMap<Resource, Int> = EnumMap<Resource, Int>(Resource::class.java).apply {
        put(Resource.GOLD, 20)
        put(Resource.SAND, 0)
        put(Resource.WOOD, 0)
        put(Resource.STONE, 0)
        put(Resource.METAL, 0)
        put(Resource.WINNING_POINT, 2)
        put(Resource.WORKER, 12)
        put(Resource.MASTER, 2)
        put(Resource.CRAFTSMEN_LIMIT, 5)
    }

    override operator fun plusAssign(amount: ResourceAmount) {
        resources[amount.resource] = this[amount.resource] + amount.amount
    }

    override operator fun minusAssign(amount: ResourceAmount) {
        if (this[amount.resource] < amount.amount) {
            throw IllegalStateException("Not enough resources of type ${amount.resource.name}: " +
                    "required ${amount.amount}, available ${this[amount.resource]}")
        }
        resources[amount.resource] = this[amount.resource] - amount.amount
    }

    override operator fun get(resource: Resource): Int = resources[resource]!!
}