package ru.spbstu.saulen.game

enum class Resource(
        val marketCost: Int,
        val purchaseAllowed: Boolean = true,
        val saleAllowed: Boolean = true
) : Requirement {
    SAND(2),
    WOOD(3),
    STONE(4),
    METAL(5, purchaseAllowed = false, saleAllowed = true),
    GOLD(1, purchaseAllowed = false, saleAllowed = false),
    WINNING_POINT(0, purchaseAllowed = false, saleAllowed = false),
    WORKER(1, purchaseAllowed = false, saleAllowed = false),
    MASTER(0, purchaseAllowed = false, saleAllowed = false),
    CRAFTSMEN_LIMIT(0, purchaseAllowed = false, saleAllowed = false);

    operator fun invoke(amount: Int) = ResourceAmount(this, amount)
}