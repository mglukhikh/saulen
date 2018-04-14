package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import java.util.*

class Controller(vararg val players: Player) {

    internal val board = Board()

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

    private fun runRound() {
        prepareForRound()
        runCardContest()
        runMasterSetup()
        val nextStartPlayer = runPositionHandling()
        finishRound(nextStartPlayer)
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
            val player = activePlayers.find { it.playerQueue == queueIndex } ?: continue
            val cardsToChoose = board.contestCards.filter { player.has(it.cost) }
            var answer: Answer
            do {
                answer = if (cardsToChoose.isEmpty()) {
                    PassAnswer
                } else {
                    player.handleRequest(
                            ContestCardRequest(cardsToChoose)
                    )
                }
            } while (answer != PassAnswer && (answer !is ContestCardAnswer || answer.card !in cardsToChoose))
            when (answer) {
                PassAnswer -> {
                    activePlayers -= player
                }
                is ContestCardAnswer -> {
                    val card = answer.card
                    when (card) {
                        is Production -> {
                            player += card
                        }
                        is Craftsman -> {
                            player -= card.cost
                            player += card
                        }
                    }
                    board.contestCards -= card
                }
            }
            queueIndex = (queueIndex + 1) % players.size
        }
    }

    internal fun runMasterSetup() {
        val masters = mutableListOf<Player>()
        for (player in players) {
            for (i in 1..player[Resource.MASTER]) {
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

    // Returns index of next start player, or -1 if next start player should be chosen by default
    internal fun runPositionHandling(withEvent: Boolean = true, manualEvent: Event? = null): Int {
        var returnMastersBack = true
        var nextStartPlayer = -1
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
                is EventInvocationPosition -> if (withEvent) {
                    val event = manualEvent ?: position.event!!
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
                    val activePlayers = players.filter { it.marketQueue != -1 }.toMutableSet()
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
                    nextStartPlayer = players.indexOf(startPlayer)
                }
            }
        }
        for (player in players) {
            if (returnMastersBack) {
                if (player[Resource.MASTER] == 2) {
                    player += Resource.MASTER(1)
                }
            }
        }
        return nextStartPlayer
    }

    internal fun finishRound(nextStartPlayer: Int = -1) {
        for ((index, player) in players.withIndex()) {
            player.endOfRound(players.size)
            if (nextStartPlayer != -1) {
                player.playerQueue = (index + players.size - nextStartPlayer) % players.size
            }
        }
    }

    companion object {
        const val LAST_ROUND = 6

        const val START_MASTER_COST = 7
    }
}