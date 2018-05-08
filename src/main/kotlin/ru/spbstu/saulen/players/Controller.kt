package ru.spbstu.saulen.players

import ru.spbstu.saulen.board.*
import ru.spbstu.saulen.cards.*
import ru.spbstu.saulen.game.Resource
import java.util.*

class Controller(vararg val players: Player) {

    internal val board = Board()

    internal var currentRound = 0

    internal var silent = true

    private var currentEvent: Event? = null

    private val random = Random()

    private fun log(s: String) {
        if (silent) return
        println(s)
    }

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
        log("\n\n")
        log("===== ROUND $currentRound STARTED =====")
        log("Starting player: ${players.find { it.playerQueue == 0 }}")
        prepareForRound()
        for (player in players) {
            log("Player $player resources: ${player.resourceDescription(resourcesToLog)}")
            if (Ellen in player.advantages) {
                val event = board.positions.keys.filterIsInstance<EventInvocationPosition>().first().event!!
                player.handleRequest(EventAcknowledgeRequest(event))
            }
        }
        runCardContest()
        runMasterSetup()
        val nextStartPlayer = runPositionHandling()
        runCraftsmenWork()
        finishRound(nextStartPlayer)
        log("===== ROUND $currentRound FINISHED =====")
    }

    internal fun prepareForRound() {
        board.prepareForRound(
                Craftsman.cardsPerRound[currentRound - 1],
                random,
                currentRound == LAST_ROUND
        )
    }

    internal fun runCardContest() {
        log("=== Card contest started ===")
        log("Contest cards available: ${board.contestCards}")
        var queueIndex = 0
        val activePlayers = players.sortedBy { it.playerQueue }.toMutableSet()
        while (activePlayers.isNotEmpty() && board.contestCards.isNotEmpty()) {
            val player = activePlayers.find { it.playerQueue == queueIndex }
            if (player == null) {
                queueIndex = (queueIndex + 1) % players.size
                continue
            }
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
                    log("Player $player passes")
                    activePlayers -= player
                }
                is ContestCardAnswer -> {
                    val card = answer.card
                    log("Player $player takes $card for ${card.cost}")
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
        log("Card contest finished")
    }

    private fun Player.useAdvantageQuestion(request: UseAdvantageRequest): Boolean {
        val advantage = request.advantage
        if (advantage !in advantages) return false
        val answer = handleRequest(request)
        if (answer is UseAdvantageAnswer && answer.advantage == advantage) {
            advantages -= advantage
            return true
        }
        return false
    }

    internal fun runMasterSetup() {
        log("=== Master contest started ===")
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
            val accessiblePositions = board.accessiblePositions
            val request = SetMasterRequest(
                    cost,
                    accessiblePositions
            )
            var answer = if (currentPlayer[Resource.GOLD] < cost) {
                if (currentPlayer.useAdvantageQuestion(SetFreeMasterAdvantageRequest(Remigius, cost))) {
                    log("Player $currentPlayer uses Remigius to set master for free")
                    currentPlayer += Resource.GOLD(cost)
                    var answer: Answer
                    do {
                        answer = currentPlayer.handleRequest(request)
                    } while (answer == PassAnswer)
                    answer
                } else {
                    PassAnswer
                }
            } else {
                currentPlayer.handleRequest(request)
            }
            do {
                var incorrect = false
                when (answer) {
                    PassAnswer -> {
                        if (cost > 0) {
                            masterCircle[cost] = currentPlayer
                            log("Player $currentPlayer chooses to delay his master")
                        } else {
                            incorrect = true
                        }
                    }
                    is SetMasterAnswer -> {
                        if (answer.position in accessiblePositions) {
                            currentPlayer -= Resource.GOLD(cost)
                            board.positions[answer.position] = currentPlayer
                            if (cost > 0) {
                                log("Player $currentPlayer buys his master for $cost and sets it to ${answer.position}")
                            } else {
                                log("Player $currentPlayer takes his master for free and sets it to ${answer.position}")
                            }
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

        var cancelled = false
        while (masters.isNotEmpty()) {
            val currentPlayer = masters.removeAt(masters.lastIndex)
            val cost = currentCost--
            if (!cancelled) {
                val own = currentPlayer == startingPlayer
                val cancelAnswer = startingPlayer.handleRequest(
                        CancelMasterRequest(own, cost, board.accessiblePositions)
                )
                if (cancelAnswer === CancelAnswer) {
                    log("Starting player $startingPlayer uses his right to cancel master of $currentPlayer")
                    cancelled = true
                    currentCost++
                    val newIndex = random.nextInt(masters.size + 1)
                    masters.add(newIndex, currentPlayer)
                    continue
                }
            }
            log("Player $currentPlayer can buy his master for $cost")
            setMaster(currentPlayer, cost)
        }
        for (currentPlayer in masterCircle.descendingMap().values) {
            setMaster(currentPlayer, 0)
        }
        log("=== Master contest finished ===")
    }

    // Returns index of next start player, or -1 if next start player should be chosen by default
    internal fun runPositionHandling(withEvent: Boolean = true, manualEvent: Event? = null): Int {
        log("=== Position handling started ===")
        var returnMastersBack = true
        var nextStartPlayer = -1
        for ((position, ownerPlayer) in board.positions) {
            // Before invocation
            when (position) {
                is TaxPosition -> {
                    val taxLevel = position.amount
                    players.firstOrNull { Francis in it.advantages && !it.hasTaxFree }?.let {
                        log("Player $it pays minimum tax because of Francis")
                        it += Resource.GOLD(taxLevel - 2)
                    }
                    val taxFreeRequest = TaxFreeAdvantageRequest(Steuerfreiheit, taxLevel)
                    players.forEach {
                        if (it.useAdvantageQuestion(taxFreeRequest)) {
                            log("Player $it uses Steuerfreiheit")
                            it += Resource.GOLD(taxLevel)
                        }
                    }
                }
                is WinningPointPosition -> {
                    ownerPlayer?.takeIf { PriorPhilip in it.advantages }?.let {
                        log("Player $it gets extra winning point because of Prior Philip")
                        it += Resource.WINNING_POINT(1)
                    }
                }
            }

            // Invocation
            if (position.masterPosition) {
                ownerPlayer?.let { position.invokeOn(it, ::log) }
            } else {
                for (player in players) {
                    if (position is EventInvocationPosition) {
                        if (!withEvent) continue
                        val event = manualEvent ?: position.event!!
                        val negative = event.negative
                        if (negative && !player.hasEventProtection) {
                            if (player.useAdvantageQuestion(EventProtectionAdvantageRequest(Thomas, event))) {
                                log("Player $player uses Thomas to get negative event protection")
                                continue
                            }
                        }
                    }
                    position.invokeOn(player, ::log)
                }
            }

            // After invocation
            when (position) {
                is EventInvocationPosition -> if (withEvent) {
                    val event = manualEvent ?: position.event!!
                    currentEvent = event
                    if (event == StadtMauer) {
                        returnMastersBack = false
                    }
                    for (player in players) {
                        if (player.hasEventProtection && !event.negative) {
                            log("Player $player should select a free resource")
                            do {
                                val answer = player.handleRequest(FreeResourceRequest)
                                if (answer is BuyAnswer) {
                                    log("Player $player selects ${answer.amount.resource}")
                                    player += answer.amount.resource(1)
                                    board.market -= answer.amount.resource(1)
                                }
                            } while (answer !is BuyAnswer)
                        }
                    }
                }
                is TradePosition -> {
                    log("= Market trade started")
                    var queueIndex = 0
                    val activePlayers = players.filter { it.marketQueue != -1 }.toMutableSet()
                    log("Active trade players: $activePlayers")
                    while (activePlayers.isNotEmpty()) {
                        for (player in players) {
                            if (player.marketQueue == queueIndex) {
                                if (player in activePlayers) {
                                    log("Player $player trade turn")
                                    // Request for trade
                                    do {
                                        val answer = player.handleRequest(TradeRequest(board.market))
                                        var correctAnswer = true
                                        when (answer) {
                                            is BuyAnswer -> {
                                                val amount = answer.amount
                                                player += amount
                                                val cost = amount.resource.marketCost * amount.amount
                                                if (amount.resource == Resource.WOOD &&
                                                    player.craftsmen.none { it.template is Schreiner }
                                                ) {
                                                    log("Player $player cannot buy wood: no Schreiner")
                                                    correctAnswer = false
                                                } else if (player.has(Resource.GOLD(cost))) {
                                                    log("Player $player buys $amount for $cost")
                                                    player -= Resource.GOLD(cost)
                                                    board.market -= amount
                                                } else {
                                                    log("Player $player has not enough gold to buy $amount")
                                                    correctAnswer = false
                                                }
                                            }
                                            is SellAnswer -> {
                                                val amount = answer.amount
                                                val cost = amount.resource.marketCost * amount.amount
                                                if (amount.resource == Resource.STONE &&
                                                    player.craftsmen.none { it.template === Steinmetz }
                                                ) {
                                                    log("Player $player cannot sell stone: no Steinmetz")
                                                    correctAnswer = false
                                                } else {
                                                    log("Player $player sells $amount for $cost")
                                                    player += Resource.GOLD(cost)
                                                    player -= amount
                                                }
                                            }
                                            PassAnswer -> {
                                                log("Player $player ends trade")
                                                activePlayers -= player
                                            }
                                        }
                                    } while (!correctAnswer)
                                }
                                break
                            }
                        }
                        queueIndex = (queueIndex + 1) % players.size
                    }
                    log("= Market trade finished")
                }
                StartPlayerPosition -> {
                    val startPlayer = board.positions[position]
                    if (startPlayer != null) {
                        log("Start player for next round reset: $startPlayer")
                        nextStartPlayer = players.indexOf(startPlayer)
                    } else {

                    }
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
        log("=== Position handling finished ===")
        return nextStartPlayer
    }

    internal fun runCraftsmenWork() {
        log("=== Craftsmen work started ===")
        for (player in players) {
            log("Player $player production")
            log("Resources before: ${player.resourceDescription(resourcesToLog)}")
            val craftsmen = player.craftsmen
            log("Craftsmen: $craftsmen")
            // Check limit
            val craftsmenLimit = player[Resource.CRAFTSMEN_LIMIT]
            while (craftsmen.size > craftsmenLimit) {
                log("Player $player has too much craftsmen: ${craftsmen.size}/$craftsmenLimit")
                val dropAnswer = player.handleRequest(
                        DropCraftsmanRequest(craftsmen, craftsmenLimit)
                )
                if (dropAnswer is ChooseCraftsmanAnswer && dropAnswer.craftsman in craftsmen) {
                    log("Player $player dropped ${dropAnswer.craftsman}")
                    player.craftsmen -= dropAnswer.craftsman
                }
            }
            // Increase capacity on Erzbischof
            var stimulated: Craftsman? = null
            if (currentEvent === Erzbischof) {
                val craftsmenToStimulate = craftsmen.filter {
                    it.capacity > 0 && it.template != Orgelbauer && it.template != Glockengiesser
                }
                log("Player $player chooses craftsman to increase capacity")
                val chooseAnswer = player.handleRequest(
                        StimulateCraftsmanRequest(craftsmenToStimulate)
                )
                if (chooseAnswer is ChooseCraftsmanAnswer && chooseAnswer.craftsman in craftsmenToStimulate) {
                    log("Player $player increased ${chooseAnswer.craftsman} capacity by 1")
                    stimulated = chooseAnswer.craftsman
                }
            }
            // Produce winning points
            val craftsmenCapacities = mutableMapOf<Craftsman, Int>()
            for (craftsman in craftsmen) {
                if (!player.requirementMatched(craftsman)) {
                    log("Player $player does not match $craftsman requirement and cannot use it")
                    craftsmenCapacities[craftsman] = 0
                    continue
                }
                val capacity = craftsman.capacity
                craftsmenCapacities[craftsman] = if (craftsman == stimulated) capacity + 1 else capacity
                if (capacity == 0) {
                    log("Player $player uses craftsman $craftsman automatically")
                    player += craftsman.income
                }
            }
            do {
                val useAnswer = player.handleRequest(
                        UseCraftsmanRequest(craftsmenCapacities)
                )
                if (useAnswer is UseCraftsmanAnswer) {
                    val craftsman = useAnswer.craftsman
                    if (craftsman !in craftsmen) continue
                    var currentCapacity = craftsmenCapacities[craftsman] ?: 0
                    val multiplier = minOf(currentCapacity, useAnswer.multiplier)
                    if (multiplier == 0) continue
                    log("Player $player uses craftsman $craftsman $multiplier times")
                    for (i in 0 until multiplier) {
                        for (resourceAmount in craftsman.expenses) {
                            player -= resourceAmount
                        }
                        player += craftsman.income
                        currentCapacity--
                        craftsmenCapacities[craftsman] = currentCapacity
                    }
                }
            } while (useAnswer !== PassAnswer)
            log("Resources after: ${player.resourceDescription(resourcesToLog)}")

            // Drop resources above limit
            var buildingResourceCount = player.buildingResourceCount
            while (buildingResourceCount > BUILDING_RESOURCE_LIMIT) {
                val toDrop = buildingResourceCount - BUILDING_RESOURCE_LIMIT
                log("Player $player has too much building resources: $buildingResourceCount, must drop $toDrop")
                val dropAnswer = player.handleRequest(
                        DropBuildingResourceRequest(toDrop)
                ) as? DropBuildingResourceAnswer ?: continue
                val dropped = if (player[dropAnswer.amount.resource] < dropAnswer.amount.amount) {
                    dropAnswer.amount.resource(player[dropAnswer.amount.resource])
                } else {
                    dropAnswer.amount
                }
                player -= dropped
                log("Player $player dropped $dropped")
                buildingResourceCount = player.buildingResourceCount
            }
        }
        log("=== Craftsmen work finished ===")
    }

    private fun finishRound(nextStartPlayer: Int = -1) {
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

        const val BUILDING_RESOURCE_LIMIT = 5

        val resourcesToLog = listOf(
                Resource.WINNING_POINT, Resource.GOLD,
                Resource.SAND, Resource.WOOD,
                Resource.STONE, Resource.METAL
        )
    }
}