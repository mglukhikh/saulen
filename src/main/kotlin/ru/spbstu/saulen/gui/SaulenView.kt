package ru.spbstu.saulen.gui

import javafx.scene.layout.BorderPane
import tornadofx.*
import ru.spbstu.saulen.players.Controller as SaulenController

class SaulenView : View() {
    private val gameController = SaulenController()

    override val root = BorderPane()

    init {
        with (root) {
            center {

            }
            top {

            }
            bottom {

            }
            left {

            }
            right {

            }
        }
    }
}