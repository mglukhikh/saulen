package ru.spbstu.saulen.players

import ru.spbstu.saulen.game.Color
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount

class HumanPlayer(
        name: String,
        color: Color,
        playerQueue: Int
) : Player(name, color, playerQueue) {
    override fun handleRequest(request: Request): Answer {
        println("For $name: ${request.message}")
        for (answerClass in request.possibleAnswers) {
            when (answerClass) {
                PassAnswer::class -> {
                    println("(P)ass")
                }
                BuyAnswer::class -> {
                    println("(B)uy s(T)one (W)ood (S)and 1-4")
                }
                SellAnswer::class -> {
                    println("(S)ell s(T)one (W)ood san(D) 1-9")
                }
                ContestCardAnswer::class -> {
                    println("(C)ontest card 1-9")
                }
                SetMasterAnswer::class -> {
                    println("(M)aster 11-99")
                }
            }
        }
        var answer: Answer? = null
        do {
            var correct = true
            val line = readLine()!!
            val first = line.getOrNull(0)
            if (first == null || first !in "PBSCM") {
                correct = false
            } else {
                answer = when (first) {
                    'B', 'S' -> {
                        val second = line.getOrNull(1)
                        val last = line.getOrNull(2)
                        if (second == null || second !in "TWD" || last == null || !last.isDigit()) {
                            null
                        } else {
                            val resource = when (second) {
                                'T' -> Resource.STONE
                                'W' -> Resource.WOOD
                                else -> Resource.SAND
                            }
                            val amount = last - '0'
                            if (first == 'B') {
                                BuyAnswer(ResourceAmount(resource, amount))
                            } else {
                                SellAnswer(ResourceAmount(resource, amount))
                            }
                        }
                    }
                    'C' -> {
                        val possibleCards = (request as ContestCardRequest).cards
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..possibleCards.size) {
                            null
                        } else {
                            ContestCardAnswer(possibleCards[index - 1])
                        }
                    }
                    'M' -> {
                        val positions = (request as SetMasterRequest).positions
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..positions.size) {
                            null
                        } else {
                            SetMasterAnswer(positions[index - 1])
                        }

                    }
                    else -> PassAnswer
                }
                if (answer == null) {
                    correct = false
                }
            }
        } while (!correct)
        return answer!!
    }

}