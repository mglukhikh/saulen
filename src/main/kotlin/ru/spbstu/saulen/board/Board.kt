package ru.spbstu.saulen.board

import ru.spbstu.saulen.game.Resource
import ru.spbstu.saulen.game.Stock
import ru.spbstu.saulen.players.Player

class Board {

    val masterPositions = mutableMapOf<MasterPosition, Player>()

    val market = Stock().apply {
        this += Resource.WOOD(4)
        this += Resource.SAND(4)
        this += Resource.METAL(4)
    }


}