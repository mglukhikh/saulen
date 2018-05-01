package ru.spbstu.saulen.game

import java.util.*

class Stock : ResourceStorage {
    private val resources: MutableMap<Resource, Int> = EnumMap<Resource, Int>(Resource::class.java).apply {
        for (resource in Resource.values()) {
            put(resource, 0)
        }
    }

    override operator fun plusAssign(amount: ResourceAmount) {
        if (amount.amount < 0) {
            minusAssign(-amount)
            return
        }
        resources[amount.resource] = this[amount.resource] + amount.amount
    }

    override operator fun minusAssign(amount: ResourceAmount) {
        if (amount.amount < 0) {
            plusAssign(-amount)
            return
        }
        if (this[amount.resource] < amount.amount) {
            if (amount.resource == Resource.GOLD) {
                val remainder = amount.amount - this[amount.resource]
                resources[amount.resource] = 0
                this -= ResourceAmount(Resource.WINNING_POINT, minOf(this[Resource.WINNING_POINT], remainder / 2))
                return
            }
            // This check is necessary e.g. for kalte winter
            else if (amount.amount != 1) {
                throw IllegalStateException("Not enough resources of type ${amount.resource.name}: " +
                        "required ${amount.amount}, available ${this[amount.resource]}")
            }
        }
        resources[amount.resource] = this[amount.resource] - amount.amount
    }

    override operator fun get(resource: Resource): Int = resources[resource]!!

    fun clear() {
        for (resource in Resource.values()) {
            resources.put(resource, 0)
        }
    }
}