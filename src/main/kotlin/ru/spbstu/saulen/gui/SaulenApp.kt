package ru.spbstu.saulen.gui

import tornadofx.App
import tornadofx.launch

class SaulenApp : App(SaulenView::class)

fun main(args: Array<String>) {
    launch<SaulenApp>(args)
}