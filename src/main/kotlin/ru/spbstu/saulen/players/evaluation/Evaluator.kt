package ru.spbstu.saulen.players.evaluation

import ru.spbstu.saulen.board.BoardPosition
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.game.ResourceStorage

interface Evaluator {
    fun Resource.evaluate(numberOfRound: Int): Int

    fun ResourceAmount.evaluate(numberOfRound: Int): Int

    fun Production.evaluate(numberOfRound: Int): Int

    fun ContestCard.evaluate(numberOfRound: Int): Int

    fun BoardPosition.evaluate(numberOfRound: Int): Int

    fun Advantage.evaluate(numberOfRound: Int): Int

    fun Event.evaluate(numberOfRound: Int): Int

    fun Craftsman.evaluate(numberOfRound: Int): Int
}