package ru.spbstu.saulen.gui

import javafx.application.Application
import tornadofx.App

class SaulenApp : App(SaulenView::class)

fun main(args: Array<String>) {
    Application.launch(SaulenApp::class.java, *args)
}