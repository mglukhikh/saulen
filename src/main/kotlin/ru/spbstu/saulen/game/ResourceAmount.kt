package ru.spbstu.saulen.game

data class ResourceAmount(val resource: Resource, val amount: Int) {
    operator fun times(multiplier: Int) = ResourceAmount(resource, multiplier * amount)

    override fun toString(): String {
        return "$resource $amount"
    }
}