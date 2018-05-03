package ru.spbstu.saulen.players

import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount

class HumanPlayer(
        name: String,
        playerQueue: Int
) : Player(name, playerQueue) {
    override fun handleRequest(request: Request): Answer {
        println("For $name: ${request.message}")
        for (answerClass in request.possibleAnswers) {
            when (answerClass) {
                PassAnswer::class -> {
                    println("(P)ass")
                }
                CancelAnswer::class -> {
                    println("c(A)ncel")
                }
                BuyAnswer::class -> {
                    println("(B)uy s(T)one (W)ood (S)and 1-4")
                }
                SellAnswer::class -> {
                    println("(S)ell s(T)one (W)ood (S)and 1-9")
                }
                ContestCardAnswer::class -> {
                    println("(C)ontest card 1-9")
                }
                SetMasterAnswer::class -> {
                    println("(M)aster 11-99")
                }
                DropBuildingResourceAnswer::class -> {
                    println("(D)rop s(T)one (W)ood (S)and 1-9")
                }
                UseCraftsmanAnswer::class -> {
                    println("p(R)oduce by craftsman 1-9")
                }
                ChooseCraftsmanAnswer::class -> {
                    println("cra(F)tsman 1-9")
                }
                UseAdvantageAnswer::class -> {
                    println("(U)se")
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
                    'B', 'S', 'D' -> {
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
                            when (first) {
                                'B' -> BuyAnswer(ResourceAmount(resource, amount))
                                'D' -> DropBuildingResourceAnswer(ResourceAmount(resource, amount))
                                else -> SellAnswer(ResourceAmount(resource, amount))
                            }
                        }
                    }
                    'C' -> {
                        val possibleCards = (request as? ContestCardRequest)?.cards ?: emptyList()
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..possibleCards.size) {
                            null
                        } else {
                            ContestCardAnswer(possibleCards[index - 1])
                        }
                    }
                    'M' -> {
                        val positions = (request as? SetMasterRequest)?.positions ?: emptyList()
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..positions.size) {
                            null
                        } else {
                            SetMasterAnswer(positions[index - 1])
                        }

                    }
                    'F' -> {
                        val craftsmen = (request as? ChooseCraftsmanRequest)?.craftsmen ?: emptyList()
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..craftsmen.size) {
                            null
                        } else {
                            ChooseCraftsmanAnswer(craftsmen[index - 1])
                        }
                    }
                    'R' -> {
                        val craftsmen =
                                (request as? UseCraftsmanRequest)?.craftsmenCapacities?.keys?.toList() ?: emptyList()
                        val index = line.substring(1).toIntOrNull()
                        if (index == null || index !in 1..craftsmen.size) {
                            null
                        } else {
                            UseCraftsmanAnswer(craftsmen[index - 1], 1)
                        }

                    }
                    'A' -> CancelAnswer
                    'U' -> (request as? UseAdvantageRequest)?.advantage?.let { UseAdvantageAnswer(it) }
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