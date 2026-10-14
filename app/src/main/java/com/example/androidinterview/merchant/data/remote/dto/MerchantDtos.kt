package com.example.androidinterview.merchant.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/merchant`: the merchant's balances and most recent activity.
 *
 * @param availableBalance Balance available for payouts, in the lowest denomination (pence / cents).
 * @param pendingBalance Balance not yet settled, in the lowest denomination (pence / cents).
 * @param currency Currency of the merchant account.
 * @param activity The 3 most recent activity items.
 */
@Serializable
data class MerchantDataResponseDto(
    @SerialName("available_balance") val availableBalance: Int,
    @SerialName("pending_balance") val pendingBalance: Int,
    val currency: CurrencyDto,
    val activity: List<ActivityItemDto>,
)

/**
 * A single transaction on the merchant account.
 *
 * @param id Unique identifier of the activity, also used as a pagination cursor.
 * @param type Kind of transaction.
 * @param amount Amount in the lowest denomination (pence / cents); negative for outflows.
 * @param currency Currency of [amount].
 * @param date When the activity happened, as an ISO 8601 string.
 * @param description Human-readable label of the transaction.
 * @param status Processing status of the transaction.
 */
@Serializable
data class ActivityItemDto(
    val id: String,
    val type: ActivityTypeDto,
    val amount: Int,
    val currency: CurrencyDto,
    val date: String,
    val description: String,
    val status: ActivityStatusDto,
)

/** Currencies supported by the API. */
@Serializable
enum class CurrencyDto { GBP, EUR }

/** Kind of activity, serialized as its lowercase name. */
@Serializable
enum class ActivityTypeDto {
    @SerialName("payout") PAYOUT,
    @SerialName("deposit") DEPOSIT,
    @SerialName("refund") REFUND,
    @SerialName("fee") FEE,
}

/** Processing status of an activity, serialized as its lowercase name. */
@Serializable
enum class ActivityStatusDto {
    @SerialName("completed") COMPLETED,
    @SerialName("pending") PENDING,
    @SerialName("processing") PROCESSING,
    @SerialName("failed") FAILED,
}
