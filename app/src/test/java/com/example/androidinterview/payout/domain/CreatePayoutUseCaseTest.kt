package com.example.androidinterview.payout.domain

import com.example.androidinterview.authentication.AuthenticationResult
import com.example.androidinterview.authentication.FakeAuthenticationProvider
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.domain.FakeDeviceRepository
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.payout.domain.model.Payout
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import com.example.androidinterview.payout.domain.model.PayoutStatus
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePayoutUseCaseTest {

    private val amount = Money(40000, Currency.GBP)
    private val iban = "GB29NWBK60161331926819"
    private val payout = Payout("pay_001", PayoutStatus.PENDING, amount, iban, Instant.EPOCH)
    private val payoutRepository = FakePayoutRepository(PayoutResult.Success(payout))
    private val deviceRepository = FakeDeviceRepository()
    private val authenticationProvider = FakeAuthenticationProvider()
    private val createPayout = CreatePayoutUseCase(payoutRepository, deviceRepository, authenticationProvider)

    @Test
    fun `sends the payout with the device id`() = runTest {
        val result = createPayout(amount, iban)

        assertEquals(PayoutResult.Success(payout), result)
        assertEquals(listOf(PayoutRequest(amount, iban, "device_1")), payoutRepository.requests)
    }

    @Test
    fun `a network failure on the device id fails the payout without sending it`() = runTest {
        deviceRepository.result = DataResult.Failure(DataError.Network)

        val result = createPayout(amount, iban)

        assertEquals(PayoutResult.Failure(PayoutError.Network), result)
        assertTrue(payoutRepository.requests.isEmpty())
    }

    @Test
    fun `any other device id failure is unknown`() = runTest {
        deviceRepository.result = DataResult.Failure(DataError.Server(500, null))

        assertEquals(PayoutResult.Failure(PayoutError.Unknown), createPayout(amount, iban))
        assertTrue(payoutRepository.requests.isEmpty())
    }

    @Test
    fun `the payout error is passed through`() = runTest {
        payoutRepository.result = PayoutResult.Failure(PayoutError.InsufficientFunds)

        assertEquals(PayoutResult.Failure(PayoutError.InsufficientFunds), createPayout(amount, iban))
    }

    @Test
    fun `a payout of exactly 1,000 is sent without authenticating`() = runTest {
        createPayout(Money(100_000, Currency.GBP), iban)

        assertEquals(0, authenticationProvider.calls)
        assertEquals(1, payoutRepository.requests.size)
    }

    @Test
    fun `a payout above 1,000 is sent after the user authenticated`() = runTest {
        createPayout(Money(100_001, Currency.GBP), iban)
        createPayout(Money(100_001, Currency.EUR), iban)

        assertEquals(2, authenticationProvider.calls)
        assertEquals(2, payoutRepository.requests.size)
    }

    @Test
    fun `an authentication that does not succeed fails the payout without sending it`() = runTest {
        val cases = mapOf(
            AuthenticationResult.Cancelled to PayoutError.AuthenticationCancelled,
            AuthenticationResult.LockedOut to PayoutError.AuthenticationLockedOut,
            AuthenticationResult.NotEnrolled to PayoutError.AuthenticationNotEnrolled,
            AuthenticationResult.Unavailable to PayoutError.AuthenticationUnavailable,
            AuthenticationResult.Failed to PayoutError.AuthenticationFailed,
        )
        cases.forEach { (outcome, error) ->
            authenticationProvider.result = outcome

            val result = createPayout(Money(150_000, Currency.GBP), iban)

            assertEquals(PayoutResult.Failure(error), result)
        }
        assertTrue(payoutRepository.requests.isEmpty())
    }
}
