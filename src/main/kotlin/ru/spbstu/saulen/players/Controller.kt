package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import java.util.*

class Controller(vararg val players: Player) {

    private val board = Board()

    internal var currentRound = 0

    private val random = Random()

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

    internal fun runRound() {
        prepareForRound()
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
                                        // TODO: handle incorrect answers
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

    internal fun prepareForRound() {
        board.prepareForRound(
                Craftsman.cardsPerRound[currentRound - 1],
                random,
                currentRound == LAST_ROUND
        )
    }

    internal fun runCardContest() {
        var queueIndex = 0
        val activePlayers = players.sortedBy { it.playerQueue }.toMutableSet()
        while (activePlayers.isNotEmpty() && board.contestCards.isNotEmpty()) {
            for (player in players) {
                if (player.marketQueue == queueIndex) {
                    if (player in activePlayers) {
                        val answer = player.handleRequest(ContestCardRequest(board.contestCards))
                        when (answer) {
                            // TODO: handle incorrect answers
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
        val masters = mutableListOf<Player>()
        for (player in players) {
            for (i in 0..player[Resource.MASTER]) {
                masters += player
            }
        }
        val startingPlayer = players.sortedBy { it.playerQueue }.first()
        masters.shuffle(random)
        val masterCircle = TreeMap<Int, Player>()
        var currentCost = START_MASTER_COST

        fun setMaster(currentPlayer: Player, cost: Int) {
            val request = SetMasterRequest(cost, board.positions.filterValues { it == null }.keys.toList())
            var answer = if (currentPlayer[Resource.GOLD] < cost) {
                // TODO: Use Remigius?
                PassAnswer
            } else {
                currentPlayer.handleRequest(request)
            }
            do {
                var incorrect = false
                when (answer) {
                    PassAnswer -> {
                        if (cost > 0) {
                            masterCircle[cost] = currentPlayer
                        } else {
                            incorrect = true
                        }
                    }
                    is SetMasterAnswer -> {
                        if (board.positions[answer.position] == null) {
                            currentPlayer -= Resource.GOLD(cost)
                            board.positions[answer.position] = currentPlayer
                        } else {
                            incorrect = true
                        }

                    }
                    else -> {
                        incorrect = true
                    }
                }
                if (incorrect) {
                    answer = currentPlayer.handleRequest(request)
                }
            } while (incorrect)
        }

        while (masters.isNotEmpty()) {
            val currentPlayer = masters.removeAt(masters.lastIndex)
            // TODO: allow starting player to put current master back once

            val cost = currentCost--
            setMaster(currentPlayer, cost)
        }
        for (currentPlayer in masterCircle.descendingMap().values) {
            setMaster(currentPlayer, 0)
        }
    }

    companion object {
        const val LAST_ROUND = 6

        const val START_MASTER_COST = 7
    }
}