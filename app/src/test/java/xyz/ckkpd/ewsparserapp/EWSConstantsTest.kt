package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*
import xyz.ckkpd.ewsparserapp.*

class EWSConstantsTest {
    @Test
    fun EWSSymbol_toInt_test() {
        val o = EWSMark.ZERO
        val l = EWSMark.ONE

        assertEquals(0b0000, listOf(o, o, o, o).toInt())
        assertEquals(0b1, listOf(l).toInt())
        assertEquals(0b11, listOf(l, l).toInt())
        assertEquals(0b1111, listOf(l, l, l, l).toInt())
    }

    @Test
    fun EWSParser_Parse_Test_1() {
        val arr = listOf<Byte>(1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0)
        val parser = EWSParser(arr.toEWSMarks())
        val parseResult = parser.parse()

        assertEquals(EWSSignal.SECOND, parseResult.signal)
        assertEquals(EWSRegionSign.COMMON, parseResult.region)
        assertEquals(6, parseResult.day)
        assertEquals(9, parseResult.month)
        assertEquals(0, parseResult.hour)
        assertEquals(4, parseResult.year)
        assertEquals(false, parseResult.monthDayToday)
        assertEquals(false, parseResult.yearHourToday)
    }

    @Test
    fun EWSParser_Parse_Test_2011_03_11_14() {
        val arr = listOf<Byte>(1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0)
        val parser = EWSParser(arr.toEWSMarks())
        val parseResult = parser.parse()

        assertEquals(EWSSignal.SECOND, parseResult.signal)
        assertEquals(EWSRegionSign.COMMON, parseResult.region)
        assertEquals(11, parseResult.day)
        assertEquals(3, parseResult.month)
        assertEquals(14, parseResult.hour)
        assertEquals(1, parseResult.year)
        assertEquals(false, parseResult.monthDayToday)
        assertEquals(false, parseResult.yearHourToday)
    }

    @Test
    fun EWSParser_Parse_Test_2011_03_11_14_1th() {
        val arr = listOf<Byte>(1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0)
        val parser = EWSParser(arr.toEWSMarks())
        val parseResult = parser.parse()

        assertEquals(EWSSignal.FIRST, parseResult.signal)
        assertEquals(EWSRegionSign.COMMON, parseResult.region)
        assertEquals(11, parseResult.day)
        assertEquals(3, parseResult.month)
        assertEquals(14, parseResult.hour)
        assertEquals(1, parseResult.year)
        assertEquals(false, parseResult.monthDayToday)
        assertEquals(false, parseResult.yearHourToday)
    }
}