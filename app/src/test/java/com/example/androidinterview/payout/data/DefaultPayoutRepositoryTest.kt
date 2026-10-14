package com.example.androidinterview.payout.data

import com.example.androidinterview.core.network.ApiErrorDto
import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.payout.data.remote.PayoutRemoteDataSource
import com.example.androidinterview.payout.data.remote.dto.CreatePayoutRequestDto
import com.example.androidinterview.payout.data.remote.dto.PayoutResponseDto
import com.example.androidinterview.payout.domain.model.Payout
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import com.example.androidinterview.payout.domain.model.PayoutStatus
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultPayoutRepositoryTest {

    private val request = PayoutRequest(Money(40000, Currency.GBP), "GB29NWBK60161331926819")

    private class FakeRemote(
        private val outcome: () -> PayoutResponseDto,
    ) : PayoutRemoteDataSource {
        val requests = mutableListOf<CreatePayoutRequestDto>()

        override suspend fun createPayout(request: CreatePayoutRequestDto): PayoutResponseDto {
            requests += request
            return outcome()
        }
    }

    private val response = PayoutResponseDto(
        id = "pay_001",
        status = "pending",
        amount = 40000,
        currency = "GBP",
        iban = "GB29NWBK60161331926819",
        createdAt = "2026-05-18T10:00:00Z",
    )

    @Test
    fun `maps the request to the body and the response to the domain model`() = runTest {
        val remote = FakeRemote { response }

        val result = DefaultPayoutRepository(remote).createPayout(request)

        assertEquals(
            listOf(CreatePayoutRequestDto(40000, "GBP", "GB29NWBK60161331926819")),
            remote.requests,
        )
        assertEquals(
            PayoutResult.Success(
                Payout(
                    id = "pay_001",
                    status = PayoutStatus.PENDING,
                    amount = Money(40000, Currency.GBP),
                    iban = "GB29NWBK60161331926819",
                    createdAt = Instant.parse("2026-05-18T10:00:00Z"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `maps transport failures to network`() = runTest {
        val repository = DefaultPayoutRepository(FakeRemote { throw IOException() })

        assertEquals(PayoutResult.Failure(PayoutError.Network), repository.createPayout(request))
    }

    @Test
    fun `maps the insufficient funds api code`() = runTest {
        val repository = DefaultPayoutRepository(
            FakeRemote { throw HttpException(400, ApiErrorDto("Insufficient funds", "INSUFFICIENT_FUNDS")) },
        )

        assertEquals(PayoutResult.Failure(PayoutError.InsufficientFunds), repository.createPayout(request))
    }

    @Test
    fun `maps a 503 to service unavailable`() = runTest {
        val repository = DefaultPayoutRepository(
            FakeRemote { throw HttpException(503, ApiErrorDto("Service unavailable", "SERVICE_UNAVAILABLE")) },
        )

        assertEquals(PayoutResult.Failure(PayoutError.ServiceUnavailable), repository.createPayout(request))
    }

    @Test
    fun `maps other http failures and unexpected payloads to unknown`() = runTest {
        val server = DefaultPayoutRepository(FakeRemote { throw HttpException(500, null) })
        val payload = DefaultPayoutRepository(FakeRemote { response.copy(status = "teleported") })

        assertEquals(PayoutResult.Failure(PayoutError.Unknown), server.createPayout(request))
        assertEquals(PayoutResult.Failure(PayoutError.Unknown), payload.createPayout(request))
    }
}
