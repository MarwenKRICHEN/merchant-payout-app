package com.example.androidinterview.core.format

import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyFormatTest {

    private val locale = Locale.UK

    @Test
    fun `formats pence as pounds with grouping`() {
        assertEquals("£5,000.00", Money(500000, Currency.GBP).format(locale))
    }

    @Test
    fun `formats amounts below one pound`() {
        assertEquals("£0.05", Money(5, Currency.GBP).format(locale))
    }

    @Test
    fun `keeps the sign of outflows`() {
        assertEquals("-£500.00", Money(-50000, Currency.GBP).format(locale))
    }

    @Test
    fun `uses the currency of the amount rather than the locale`() {
        assertEquals("€1,234.56", Money(123456, Currency.EUR).format(locale))
    }
}
