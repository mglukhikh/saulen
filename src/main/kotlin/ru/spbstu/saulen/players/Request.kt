package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.BoardPosition
import ru.spbstu.saulen.cards.Advantage
import ru.spbstu.saulen.cards.ContestCard
import ru.spbstu.saulen.cards.Craftsman
import ru.spbstu.saulen.cards.Event
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

class DropBuildingResourceRequest(val toDrop: Int) : Request(
        "Choose sand / wood / stone to drop, total $toDrop units",
        DropBuildingResourceAnswer::class
)

class UseCraftsmanRequest(val craftsmenCapacities: Map<Craftsman, Int>) : Request(
        "Use one of your craftsmenCapacities to produce winning points\n" + craftsmenCapacities.entries.joinToString {
            (craftsman, capacity) -> craftsman.toString() + " x$capacity"
        }, PassAnswer::class, UseCraftsmanAnswer::class
)

sealed class ChooseCraftsmanRequest(message: String, val craftsmen: List<Craftsman>) : Request(
        message,
        ChooseCraftsmanAnswer::class
)

class DropCraftsmanRequest(craftsmen: List<Craftsman>, limit: Int) : ChooseCraftsmanRequest(
        "Craftsmen limit exceeded (${craftsmen.size}/$limit), drop crafsman" + craftsmen.joinToString(),
        craftsmen
)

class StimulateCraftsmanRequest(craftsmen: List<Craftsman>) : ChooseCraftsmanRequest(
        "Choose craftsman to increase capacity by 1" + craftsmen.joinToString(),
        craftsmen
)

sealed class UseAdvantageRequest(message: String, val advantage: Advantage) : Request(
        message,
        PassAnswer::class, UseAdvantageAnswer::class
)

class BasicUseAdvantageRequest(advantage: Advantage) : UseAdvantageRequest(
        "Choose to use advantage $advantage or not",
        advantage
)

class TaxFreeAdvantageRequest(advantage: Advantage, val taxLevel: Int) : UseAdvantageRequest(
        "Choose to use advantage $advantage or not, tax level is $taxLevel",
        advantage
)

class EventProtectionAdvantageRequest(advantage: Advantage, val event: Event) : UseAdvantageRequest(
        "Choose to use advantage $advantage or not, ongoing negative event is $event",
        advantage
)

class EventAcknowledgeRequest(val event: Event) : Request(
        "Look at the upcoming event: $event (${if (event.negative) "negative" else "positive"})",
        PassAnswer::class
)

class CancelMasterRequest(val own: Boolean, val cost: Int) : Request(
        "Choose to cancel upcoming ${if (own) "own" else "alien"} master with cost $cost",
        CancelAnswer::class, PassAnswer::class
)