package com.example.androidinterview.merchant.domain.model

import com.example.androidinterview.money.Money
import java.time.Instant

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
 * One page of the merchant's activity.
 *
 * @param items Activity in this page, newest first.
 * @param nextCursor Cursor to load the next page; null when this is the last page.
 */
data class ActivityPage(
    val items: List<Activity>,
    val nextCursor: String?,
)
