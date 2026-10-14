package com.example.androidinterview.payout.domain

import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import javax.inject.Inject

private const val FRACTION_DIGITS = 2

/** Turns the amount typed by the user into [Money]. */
class AmountParser @Inject constructor() {

    /**
     * Parses [input] as a payout amount.
     *
     * Accepts `.` or `,` as the decimal separator and at most two decimals. The amount field only lets
     * through digits and one separator, so [input] is expected to be of that shape and short enough
     * to fit in minor units.
     *
     * @param input Text of the amount field.
     * @param currency Currency selected for the payout.
     * @return The amount, or null when [input] is not a number, has too many decimals, or is not
     * strictly positive.
     */
    fun parse(input: String, currency: Currency): Money? {
        val value = input.replace(',', '.').toBigDecimalOrNull() ?: return null
        if (value.scale() > FRACTION_DIGITS || value.signum() <= 0) return null
        return Money(value.movePointRight(FRACTION_DIGITS).longValueExact(), currency)
    }
}
