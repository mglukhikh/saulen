package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.BoardPosition
import ru.spbstu.saulen.cards.Advantage
import ru.spbstu.saulen.cards.ContestCard
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.game.ResourceAmount

sealed class Answer

object PassAnswer : Answer()

data class BuyAnswer(val amount: ResourceAmount) : Answer()

data class SellAnswer(val amount : ResourceAmount) : Answer()

data class ContestCardAnswer(val card: ContestCard) : Answer()

data class SetMasterAnswer(val position: BoardPosition) : Answer()

data class DropBuildingResourceAnswer(val amount: ResourceAmount) : Answer()

data class UseCraftsmanAnswer(val craftsman: Craftsman, val multiplier: Int) : Answer()

data class ChooseCraftsmanAnswer(val craftsman: Craftsman) : Answer()

data class UseAdvantageAnswer(val advantage: Advantage) : Answer()