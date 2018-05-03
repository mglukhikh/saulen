package ru.spbstu.saulen.game

import org.junit.Assert.assertEquals
import org.junit.Test

class StockTest {

    @Test
    fun simpleTest() {
        val stock = Stock()
        stock += Resource.GOLD(20)
        stock -= Resource.GOLD(5)
        assertEquals(15, stock[Resource.GOLD])
    }

    @Test
    fun noNegativeGoldTest() {
        val stock = Stock()
        stock += Resource.WINNING_POINT(2)
        stock += Resource.GOLD(5)
        stock -= Resource.GOLD(2)
        stock += Resource.GOLD(-4)
        assertEquals(0, stock[Resource.GOLD])
        assertEquals(2, stock[Resource.WINNING_POINT])
        stock -= Resource.GOLD(2)
        assertEquals(0, stock[Resource.GOLD])
        assertEquals(1, stock[Resource.WINNING_POINT])
        stock -= Resource.GOLD(4)
        assertEquals(0, stock[Resource.GOLD])
        assertEquals(0, stock[Resource.WINNING_POINT])
    }

    @Test
    fun goldLimitTest() {
        val stock = Stock()
        stock += Resource.GOLD(20)
        stock += Resource.GOLD(7)
        assertEquals(27, stock[Resource.GOLD])
        stock += Resource.GOLD(8)
        assertEquals(Stock.GOLD_LIMIT, stock[Resource.GOLD])
    }
}