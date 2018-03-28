package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.BoardPosition
import ru.spbstu.saulen.cards.ContestCard
import ru.spbstu.saulen.game.Stock
import kotlin.reflect.KClass

sealed class Request(val message: String, vararg val possibleAnswers: KClass<out Answer>)

object FreeResourceRequest : Request(
        "Choose free resource from market",
        BuyAnswer::class
)

class TradeRequest(val market: Stock) : Request(
        "Choose action on market",
        PassAnswer::class, BuyAnswer::class, SellAnswer::class
)

private fun List<Any>.render(): String {
    val sb = StringBuilder()
    for ((index, card) in this.withIndex()) {
        sb.appendln("${index+1}. $card")
    }
    return sb.toString()
}

class ContestCardRequest(val cards: List<ContestCard>) : Request(
        "Choose one of contest card to buy\n" + cards.render(),
        PassAnswer::class, ContestCardAnswer::class
)

class SetMasterRequest(val cost: Int, val positions: List<BoardPosition>) : Request(
        "Choose one of free master positions for a cost $cost\n" + positions.render(),
        PassAnswer::class, SetMasterAnswer::class
)