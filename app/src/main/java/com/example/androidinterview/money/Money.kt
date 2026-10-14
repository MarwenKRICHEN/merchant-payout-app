package com.example.androidinterview.money

/** Currencies a merchant account or payout can use. */
enum class Currency { GBP, EUR }

/**
 * An amount of money.
 *
 * @param minorUnits Amount in the currency's lowest denomination (pence / cents); negative for outflows.
 * @param currency Currency of the amount.
 */
data class Money(val minorUnits: Long, val currency: Currency)
