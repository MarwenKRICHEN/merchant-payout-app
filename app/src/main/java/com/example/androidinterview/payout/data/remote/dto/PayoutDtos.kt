package com.example.androidinterview.payout.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of `POST /api/payouts`.
 *
 * @param amount Amount in the lowest denomination (pence / cents).
 * @param currency Currency of [amount], serialized as `GBP` / `EUR`.
 * @param iban Destination IBAN.
 * @param deviceId Stable identifier of the device sending the payout.
 */
@Serializable
data class CreatePayoutRequestDto(
    val amount: Long,
    val currency: String,
    val iban: String,
    @SerialName("device_id") val deviceId: String,
)

/**
 * Response of `POST /api/payouts`.
 *
 * @param id Identifier of the payout.
 * @param status Processing status, lowercase (`pending`, `processing`, `completed`, `failed`).
 * @param amount Amount in the lowest denomination (pence / cents).
 * @param currency Currency of [amount].
 * @param iban Destination IBAN.
 * @param createdAt When the payout was created, as an ISO 8601 string.
 */
@Serializable
data class PayoutResponseDto(
    val id: String,
    val status: String,
    val amount: Long,
    val currency: String,
    val iban: String,
    @SerialName("created_at") val createdAt: String,
)
