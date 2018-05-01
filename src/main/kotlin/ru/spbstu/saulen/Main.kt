package ru.spbstu.saulen

import ru.spbstu.saulen.players.Controller
import ru.spbstu.saulen.players.HumanPlayer

fun main(args: Array<String>) {
    val p1 = HumanPlayer("John", 0)
    val p2 = HumanPlayer("Jack", 1)
    val controller = Controller(p1, p2)
    val result = controller.runGame()
    for ((player, score) in result) {
        println("Player ${player.name} scores $score")
    }
}