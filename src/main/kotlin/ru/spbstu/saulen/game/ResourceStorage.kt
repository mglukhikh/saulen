package ru.spbstu.saulen.game

interface ResourceStorage {
    operator fun plus(amount: ResourceAmount)

    operator fun minus(amount: ResourceAmount)

    operator fun get(resource: Resource): Int
}