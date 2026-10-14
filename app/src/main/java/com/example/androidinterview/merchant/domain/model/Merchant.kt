package com.example.androidinterview.merchant.domain.model

import java.time.Instant

/** Currencies a merchant account or payout can use. */
enum class Currency { GBP, EUR }

/**
 * An amount of money.
 *
 * @param minorUnits Amount in the currency's lowest denomination (pence / cents); negative for outflows.
 * @param currency Currency of the amount.
 */
data class Money(val minorUnits: Long, val currency: Currency)

/** Kind of transaction on the account. */
enum class ActivityType { PAYOUT, DEPOSIT, REFUND, FEE }

/** Processing status of a transaction. */
enum class ActivityStatus { COMPLETED, PENDING, PROCESSING, FAILED }

/**
 * A single transaction on the merchant account.
 *
 * @param id Unique identifier of the activity.
 * @param type Kind of transaction.
 * @param amount Amount of the transaction; negative for outflows.
 * @param date When the activity happened.
 * @param description Human-readable label of the transaction.
 * @param status Processing status of the transaction.
 */
data class Activity(
    val id: String,
    val type: ActivityType,
    val amount: Money,
    val date: Instant,
    val description: String,
    val status: ActivityStatus,
)

/**
 * The merchant's financial overview.
 *
 * @param availableBalance Balance available for payouts.
 * @param pendingBalance Balance not yet settled.
 * @param recentActivity The most recent transactions.
 */
data class Merchant(
    val availableBalance: Money,
    val pendingBalance: Money,
    val recentActivity: List<Activity>,
)
