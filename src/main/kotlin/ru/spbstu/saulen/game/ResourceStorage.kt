package ru.spbstu.saulen.game

interface ResourceStorage {
    operator fun plusAssign(amount: ResourceAmount)

    operator fun minusAssign(amount: ResourceAmount)

    operator fun get(resource: Resource): Int
}