package ru.spbstu.saulen.cards

import ru.spbstu.saulen.game.Resource.*
import ru.spbstu.saulen.game.ResourceAmount
import ru.spbstu.saulen.players.Player

sealed class Advantage(val kind: AdvantageKind) {

    abstract fun invokeOn(player: Player)

    companion object {
        val regular = listOf(
                Madonna,
                Richard,
                Wollmesse,
                Tom,
                Aliena,
                Toledo,
                Jack,
                Otto,
                Francis,
                PriorPhilip,
                Ellen,
                Steuerfreiheit,
                Thomas,
                Remigius
        )

        val lastRound = listOf(
                Vollendung,
                Feierlichkeiten
        )
    }
}

sealed class IncomeAdvantage(
        immediate: Boolean,
        val income: List<ResourceAmount> = emptyList()
) : Advantage(if (immediate) AdvantageKind.IMMEDIATE else AdvantageKind.CONTINUOUS) {

    override fun invokeOn(player: Player) {
        for (income in this.income) {
            player += income
        }
        if (kind == AdvantageKind.IMMEDIATE) {
            player -= this
        }
    }

    constructor(immediate: Boolean, income: ResourceAmount):
            this(immediate, listOf(income))
}

object Madonna : IncomeAdvantage(immediate = true, income = WINNING_POINT(3))

object Richard : IncomeAdvantage(immediate = false, income = SAND(1))

object Wollmesse : IncomeAdvantage(immediate = true, income = GOLD(8))

object Tom : IncomeAdvantage(immediate = false, income = STONE(1))

object Aliena : IncomeAdvantage(immediate = false, income = WOOD(1))

object Toledo : IncomeAdvantage(immediate = true, income = METAL(2))

object Jack : IncomeAdvantage(immediate = true, income = CRAFTSMEN_LIMIT(1))

object Vollendung : IncomeAdvantage(
        immediate = true,
        income = listOf(WOOD(1), STONE(1))
)

object Feierlichkeiten : IncomeAdvantage(
        immediate = true,
        income = METAL(1)
)

object Otto : IncomeAdvantage(immediate = true, income = WORKER(1))

object Francis : Advantage(AdvantageKind.CONTINUOUS) {
    override fun invokeOn(player: Player) {
        // Implemented manually (fixed tax level of 2 gold)
    }
}

object PriorPhilip : Advantage(AdvantageKind.CONTINUOUS) {
    override fun invokeOn(player: Player) {
        // Implemented manually (+1 WP at winning point position)
    }
}

object Ellen : Advantage(AdvantageKind.CONTINUOUS) {
    // Du darfst dir zu Beginn jeder Runde die kommende Ereigniskarte ansehen
    override fun invokeOn(player: Player) {
        // TODO (probably not here)
    }
}

object Steuerfreiheit : Advantage(AdvantageKind.ONETIME) {
    // Du erhaelst in einer beliebigen Runde Steuerfreiheit (nach dem Wuerfelwurf)
    override fun invokeOn(player: Player) {
        // TODO (probably not here)
    }
}

object Thomas : Advantage(AdvantageKind.ONETIME) {
    // Du erhaelst in einer beliebigen Runde Schutz vor dem Ereignis (nach dem Aufdecken)
    override fun invokeOn(player: Player) {
        // TODO (probably not here)
    }
}

object Remigius : Advantage(AdvantageKind.ONETIME) {
    // Du darfst in einer beliebigen Runde einen eigenen gezogenen Baumeister kostenlos einsetzen
    override fun invokeOn(player: Player) {
        // TODO (probably not here)
    }

}