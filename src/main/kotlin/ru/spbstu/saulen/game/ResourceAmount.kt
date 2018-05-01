package ru.spbstu.saulen.game

data class ResourceAmount(val resource: Resource, val amount: Int) {
    operator fun times(multiplier: Int) = ResourceAmount(resource, multiplier * amount)

    operator fun unaryMinus() = times(-1)

    override fun toString(): String {
        return "$resource $amount"
    }
}