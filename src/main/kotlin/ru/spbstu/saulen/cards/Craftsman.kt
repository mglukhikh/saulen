package ru.spbstu.saulen.cards

data class Craftsman(val template: CraftsmanTemplate, val capacity: Int, val cost: Int = 0) {

    fun up() = Craftsman(template, capacity + 1, cost)

    fun down() = Craftsman(template, capacity - 1, cost)

    companion object {

        val startingCards = listOf(
                SimpleMortelmischer(3),
                SimpleSchreiner(4),
                Steinmetz(4)
        )

        val cardsPerRound = listOf(
                listOf(
                        Maurer(capacity = 2, cost = 5),
                        Werzeugmacher(capacity = 0, cost = 2),
                        AdvancedMortelmischer(capacity = 3, cost = 4),
                        GreedySchreiner(capacity = 2, cost = 3)
                ),
                listOf(
                        Topfer(capacity = 2, cost = 5),
                        Statiker(capacity = 0, cost = 6),
                        Maurer(capacity = 3, cost = 4),
                        Zimmermann(capacity = 2, cost = 3)
                ),
                listOf(
                        Topfer(capacity = 3, cost = 7),
                        Bildhauer(capacity = 1, cost = 6),
                        Zimmermann(capacity = 3, cost = 5),
                        Maurer(capacity = 4, cost = 4)
                ),
                listOf(
                        Maurer(capacity = 5, cost = 5),
                        Bildhauer(capacity = 2, cost = 7),
                        Zimmermann(capacity = 4, cost = 6),
                        Glasblaser(capacity = 1, cost = 8)
                ),
                listOf(
                        Zimmermann(capacity = 5, cost = 6),
                        Goldschmied(capacity = 6, cost = 7),
                        Bildhauer(capacity = 3, cost = 8),
                        Glasblaser(capacity = 2, cost = 9)
                ),
                listOf(
                        Glockengiesser(capacity = 2, cost = 10),
                        Orgelbauer(capacity = 1, cost = 9),
                        Bildhauer(capacity = 4, cost = 8),
                        Goldschmied(capacity = 8, cost = 7)
                )
        )
    }
}