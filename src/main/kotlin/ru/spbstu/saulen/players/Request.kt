package ru.spbstu.saulen.players

import ru.spbstu.saulen.cards.ContestCard
import ru.spbstu.saulen.game.Stock

sealed class Request

object FreeResourceRequest : Request()

class TradeRequest(val market: Stock) : Request()

class ContestCardRequest(val cards: List<ContestCard>) : Request()

class SetMasterRequest(val cost: Int) : Request()