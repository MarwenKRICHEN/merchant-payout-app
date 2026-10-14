package com.example.androidinterview.payout.domain

import com.example.androidinterview.authentication.AuthenticationProvider
import com.example.androidinterview.authentication.AuthenticationResult
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.domain.DeviceRepository
import com.example.androidinterview.money.Money
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import javax.inject.Inject

/** Payouts above this amount (1,000.00, in the lowest denomination) need the user to authenticate. */
private const val AUTHENTICATION_THRESHOLD_MINOR_UNITS = 100_000L

/**
 * Creates a payout tagged with this device's identifier.
 *
 * A payout above the authentication threshold is sent only once the user authenticated, and no
 * payout is sent when the device id cannot be obtained.
 *
 * @param payoutRepository Creates the payout.
 * @param deviceRepository Provides the device identifier.
 * @param authenticationProvider Authenticates the user for large payouts.
 */
class CreatePayoutUseCase @Inject constructor(
    private val payoutRepository: PayoutRepository,
    private val deviceRepository: DeviceRepository,
    private val authenticationProvider: AuthenticationProvider,
) {

    /**
     * Sends the payout.
     *
     * @param amount Amount to send.
     * @param iban Normalised destination IBAN.
     */
    suspend operator fun invoke(amount: Money, iban: String): PayoutResult {
        if (amount.minorUnits > AUTHENTICATION_THRESHOLD_MINOR_UNITS) {
            val error = authenticationProvider.authenticate().toPayoutError()
            if (error != null) {
                return PayoutResult.Failure(error)
            }
        }
        return when (val device = deviceRepository.getDeviceId()) {
            is DataResult.Success ->
                payoutRepository.createPayout(PayoutRequest(amount, iban, device.data))
            is DataResult.Failure -> PayoutResult.Failure(device.error.toPayoutError())
        }
    }
}

/** The error to report for this outcome, or null when the user authenticated. */
private fun AuthenticationResult.toPayoutError(): PayoutError? = when (this) {
    AuthenticationResult.Succeeded -> null
    AuthenticationResult.Cancelled -> PayoutError.AuthenticationCancelled
    AuthenticationResult.LockedOut -> PayoutError.AuthenticationLockedOut
    AuthenticationResult.NotEnrolled -> PayoutError.AuthenticationNotEnrolled
    AuthenticationResult.Unavailable -> PayoutError.AuthenticationUnavailable
    AuthenticationResult.Failed -> PayoutError.AuthenticationFailed
}

/** A device id failure is either a connectivity problem or something unexpected. */
private fun DataError.toPayoutError(): PayoutError =
    if (this == DataError.Network) PayoutError.Network else PayoutError.Unknown
