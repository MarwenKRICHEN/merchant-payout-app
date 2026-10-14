package com.example.androidinterview.payout.domain

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.domain.DeviceRepository
import com.example.androidinterview.money.Money
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import javax.inject.Inject

/**
 * Creates a payout tagged with this device's identifier.
 *
 * The payout is not sent when the device id cannot be obtained.
 *
 * @param payoutRepository Creates the payout.
 * @param deviceRepository Provides the device identifier.
 */
class CreatePayoutUseCase @Inject constructor(
    private val payoutRepository: PayoutRepository,
    private val deviceRepository: DeviceRepository,
) {

    /**
     * Sends the payout.
     *
     * @param amount Amount to send.
     * @param iban Normalised destination IBAN.
     */
    suspend operator fun invoke(amount: Money, iban: String): PayoutResult =
        when (val device = deviceRepository.getDeviceId()) {
            is DataResult.Success ->
                payoutRepository.createPayout(PayoutRequest(amount, iban, device.data))
            is DataResult.Failure -> PayoutResult.Failure(device.error.toPayoutError())
        }
}

/** A device id failure is either a connectivity problem or something unexpected. */
private fun DataError.toPayoutError(): PayoutError =
    if (this == DataError.Network) PayoutError.Network else PayoutError.Unknown
