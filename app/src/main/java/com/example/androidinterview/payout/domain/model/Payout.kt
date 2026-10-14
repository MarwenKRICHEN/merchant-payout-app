package com.example.androidinterview.payout.domain.model

import com.example.androidinterview.money.Money
import java.time.Instant

/** Processing status of a payout. */
enum class PayoutStatus { PENDING, PROCESSING, COMPLETED, FAILED }

/**
 * A payout the merchant asks to send to a bank account.
 *
 * @param amount Amount to send; always positive.
 * @param iban Normalised destination IBAN (no spaces, upper case).
 * @param deviceId Stable identifier of the device sending the payout.
 */
data class PayoutRequest(
    val amount: Money,
    val iban: String,
    val deviceId: String,
)

/**
 * A payout accepted by the API.
 *
 * @param id Identifier assigned by the API.
 * @param status Processing status when the payout was created.
 * @param amount Amount being sent.
 * @param iban Destination IBAN.
 * @param createdAt When the payout was created.
 */
data class Payout(
    val id: String,
    val status: PayoutStatus,
    val amount: Money,
    val iban: String,
    val createdAt: Instant,
)
