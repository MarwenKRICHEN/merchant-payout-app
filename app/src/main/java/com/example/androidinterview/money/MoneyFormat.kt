package com.example.androidinterview.money

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import java.util.Currency as JavaCurrency

/**
 * Formats this amount with its currency symbol, e.g. `£5,000.00`; the sign is kept for outflows.
 *
 * @param locale Locale that decides grouping and decimal separators; the symbol always follows the money's currency.
 */
fun Money.format(locale: Locale = Locale.getDefault()): String {
    val javaCurrency = JavaCurrency.getInstance(currency.name)
    val amount = BigDecimal.valueOf(minorUnits).movePointLeft(javaCurrency.defaultFractionDigits)
    return NumberFormat.getCurrencyInstance(locale)
        .apply { this.currency = javaCurrency }
        .format(amount)
}
