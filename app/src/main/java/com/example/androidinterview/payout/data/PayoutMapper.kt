package com.example.androidinterview.payout.data

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.payout.data.remote.dto.CreatePayoutRequestDto
import com.example.androidinterview.payout.data.remote.dto.PayoutResponseDto
import com.example.androidinterview.payout.domain.model.Payout
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutStatus
import java.time.Instant
import java.util.Locale

private const val INSUFFICIENT_FUNDS_CODE = "INSUFFICIENT_FUNDS"
private const val HTTP_SERVICE_UNAVAILABLE = 503

/** Maps the domain request to the API body. */
internal fun PayoutRequest.toDto() = CreatePayoutRequestDto(
    amount = amount.minorUnits,
    currency = amount.currency.name,
    iban = iban,
)

/** Maps the API response to the domain model; an unknown status or currency is an unexpected payload. */
internal fun PayoutResponseDto.toDomain() = Payout(
    id = id,
    status = PayoutStatus.valueOf(status.uppercase(Locale.ROOT)),
    amount = Money(amount, Currency.valueOf(currency.uppercase(Locale.ROOT))),
    iban = iban,
    createdAt = Instant.parse(createdAt),
)

/** Translates a transport failure into the reasons a payout can fail for, using the API's contract. */
internal fun DataError.toPayoutError(): PayoutError = when (this) {
    DataError.Network -> PayoutError.Network
    is DataError.Server -> when {
        apiCode == INSUFFICIENT_FUNDS_CODE -> PayoutError.InsufficientFunds
        code == HTTP_SERVICE_UNAVAILABLE -> PayoutError.ServiceUnavailable
        else -> PayoutError.Unknown
    }
    DataError.Unknown -> PayoutError.Unknown
}
