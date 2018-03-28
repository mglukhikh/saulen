package ru.spbstu.saulen.game

data class ResourceAmount(val resource: Resource, val amount: Int) {
    override fun toString(): String {
        return "$resource $amount"
    }
}