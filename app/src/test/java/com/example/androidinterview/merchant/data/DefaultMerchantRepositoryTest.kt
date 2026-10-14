package com.example.androidinterview.merchant.data

import com.example.androidinterview.core.network.ApiErrorDto
import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.data.remote.FakeMerchantRemoteDataSource
import com.example.androidinterview.merchant.domain.model.Activity
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Money
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.Instant

class DefaultMerchantRepositoryTest {

    private val remote = FakeMerchantRemoteDataSource()
    private val repository = DefaultMerchantRepository(remote)

    @Test
    fun `maps the remote response to the domain model`() = runTest {
        val result = repository.getMerchant()

        val merchant = (result as DataResult.Success).data
        assertEquals(Money(500000, Currency.GBP), merchant.availableBalance)
        assertEquals(Money(25000, Currency.GBP), merchant.pendingBalance)
        assertEquals(3, merchant.recentActivity.size)
        assertEquals(
            Activity(
                id = "act_002",
                type = ActivityType.PAYOUT,
                amount = Money(-50000, Currency.GBP),
                date = Instant.parse("2026-05-17T14:30:00Z"),
                description = "Payout to Bank Account ****1234",
                status = ActivityStatus.COMPLETED,
            ),
            merchant.recentActivity[1],
        )
    }

    @Test
    fun `network failure maps to Network error`() = runTest {
        remote.error = IOException("offline")

        assertEquals(DataResult.Failure(DataError.Network), repository.getMerchant())
    }

    @Test
    fun `http failure maps to Server error with the api code`() = runTest {
        remote.error = HttpException(503, ApiErrorDto("Service unavailable", "SERVICE_UNAVAILABLE"))

        assertEquals(
            DataResult.Failure(DataError.Server(503, "SERVICE_UNAVAILABLE")),
            repository.getMerchant(),
        )
    }

    @Test
    fun `http failure without error body maps to Server error without api code`() = runTest {
        remote.error = HttpException(404, null)

        assertEquals(DataResult.Failure(DataError.Server(404, null)), repository.getMerchant())
    }

    @Test
    fun `unexpected payload maps to Unknown error`() = runTest {
        remote.error = SerializationException("bad payload")

        assertEquals(DataResult.Failure(DataError.Unknown), repository.getMerchant())
    }

    @Test
    fun `invalid activity date maps to Unknown error`() = runTest {
        val sample = FakeMerchantRemoteDataSource.sampleResponse()
        remote.response = sample.copy(activity = listOf(sample.activity[0].copy(date = "not-a-date")))

        assertEquals(DataResult.Failure(DataError.Unknown), repository.getMerchant())
    }
}
