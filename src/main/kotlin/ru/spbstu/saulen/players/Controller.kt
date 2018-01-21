package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.StadtMauer
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
                            // Request for one free resource from market
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
                                    if (true /* Pass answered */) {
                                        activePlayers -= player
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

    }

    private fun runMasterSetup() {

    }

    companion object {
        val LAST_ROUND = 6
    }
}