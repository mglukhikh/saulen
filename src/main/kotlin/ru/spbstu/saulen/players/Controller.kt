package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import java.util.*

class Controller(vararg val players: Player) {

    val board = Board()

    var currentRound = 0

    val random = Random()

    fun runGame(): Map<Player, Int> {
        while (currentRound < LAST_ROUND) {
            currentRound++
            runRound()
        }
        val result = mutableMapOf<Player, Int>()
        for (player in players) {
            result[player] = player[Resource.WINNING_POINT]
        }
        return result
    }

    private fun runRound() {
        board.prepareForRound(
                Craftsman.cardsPerRound[currentRound - 1],
                random,
                currentRound == LAST_ROUND
        )
        runCardContest()
        runMasterSetup()
        var returnMastersBack = true
        var startPlayerIndex = -1
        for ((position, ownerPlayer) in board.positions) {
            // Before invocation
            when (position) {
                is TaxPosition -> {
                    players.firstOrNull { Francis in it.advantages && !it.hasTaxFree }?.let {
                        it += Resource.GOLD(position.amount - 2)
                    }
                }
                is WinningPointPosition -> {
                    ownerPlayer?.takeIf { PriorPhilip in it.advantages }?.let {
                        it += Resource.WINNING_POINT(1)
                    }
                }
            }

            // Invocation
            if (position.masterPosition) {
                ownerPlayer?.let { position.invokeOn(it) }
            } else {
                for (player in players) {
                    position.invokeOn(player)
                }
            }

            // After invocation
            when (position) {
                is EventInvocationPosition -> {
                    val event = position.event!!
                    if (event == StadtMauer) {
                        returnMastersBack = false
                    }
                    for (player in players) {
                        if (player.hasEventProtection && !event.negative) {
                            do {
                                val answer = player.handleRequest(FreeResourceRequest)
                                if (answer is BuyAnswer) {
                                    player += answer.amount.resource(1)
                                    board.market -= answer.amount.resource(1)
                                }
                            } while (answer !is BuyAnswer)
                        }
                    }
                }
                is TradePosition -> {
                    var queueIndex = 0
                    val activePlayers = players.toMutableSet()
                    while (activePlayers.isNotEmpty()) {
                        for (player in players) {
                            if (player.marketQueue == queueIndex) {
                                if (player in activePlayers) {
                                    // Request for trade
                                    val answer = player.handleRequest(TradeRequest(board.market))
                                    when (answer) {
                                        is BuyAnswer -> {
                                            player += answer.amount
                                            player -= Resource.GOLD(answer.amount.resource.marketCost * answer.amount.amount)
                                            board.market -= answer.amount
                                        }
                                        is SellAnswer -> {
                                            player += Resource.GOLD(answer.amount.resource.marketCost * answer.amount.amount)
                                            player -= answer.amount
                                        }
                                        PassAnswer -> {
                                            activePlayers -= player
                                        }
                                    }
                                }
                                break
                            }
                        }
                        queueIndex = (queueIndex + 1) % players.size
                    }
                }
                StartPlayerPosition -> {
                    val startPlayer = board.positions[position]
                    startPlayerIndex = players.indexOf(startPlayer)
                }
            }
        }
        for ((index, player) in players.withIndex()) {
            player.endOfRound(players.size)
            if (returnMastersBack) {
                if (player[Resource.MASTER] == 2) {
                    player += Resource.MASTER(1)
                }
            }
            if (startPlayerIndex != -1) {
                player.playerQueue = (index + players.size - startPlayerIndex) % players.size
            }
        }
    }

    private fun runCardContest() {
        var queueIndex = 0
        val activePlayers = players.sortedBy { it.playerQueue }.toMutableSet()
        while (activePlayers.isNotEmpty() && board.contestCards.isNotEmpty()) {
            for (player in players) {
                if (player.marketQueue == queueIndex) {
                    if (player in activePlayers) {
                        val answer = player.handleRequest(ContestCardRequest(board.contestCards))
                        when (answer) {
                            PassAnswer -> {
                                activePlayers -= player
                            }
                            is ContestCardAnswer -> {
                                val card = answer.card
                                when (card) {
                                    is Production -> {
                                        if (player.isAbleToProduce(card)) {
                                            player += card
                                        } else {
                                            activePlayers -= player
                                        }
                                    }
                                    is Craftsman -> {
                                        val cost = card.cost.amount
                                        if (player[Resource.GOLD] >= cost) {
                                            player -= Resource.GOLD(cost)
                                            player += card
                                        } else {
                                            activePlayers -= player
                                        }
                                    }
                                }
                                board.contestCards -= card
                            }
                        }
                    }
                }
            }
            queueIndex = (queueIndex + 1) % players.size
        }

    }

    private fun runMasterSetup() {

    }

    companion object {
        val LAST_ROUND = 6
    }
}