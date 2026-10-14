package com.example.androidinterview.payout.domain

import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {

    private val parser = AmountParser()

    @Test
    fun `converts a whole amount to minor units`() {
        assertEquals(Money(40000, Currency.GBP), parser.parse("400", Currency.GBP))
    }

    @Test
    fun `accepts a dot or a comma as decimal separator`() {
        assertEquals(Money(99999, Currency.GBP), parser.parse("999.99", Currency.GBP))
        assertEquals(Money(1250, Currency.GBP), parser.parse("12,5", Currency.GBP))
    }

    @Test
    fun `keeps the selected currency`() {
        assertEquals(Money(100, Currency.EUR), parser.parse("1", Currency.EUR))
    }

    @Test
    fun `rejects empty, zero, negative and malformed input`() {
        listOf("", " ", ".", "0", "0.00", "-5", "abc", "1.2.3", "1.234").forEach { input ->
            assertNull("'$input' should be rejected", parser.parse(input, Currency.GBP))
        }
    }
}
