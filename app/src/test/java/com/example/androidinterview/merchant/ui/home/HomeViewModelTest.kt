package com.example.androidinterview.merchant.ui.home

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.FakeMerchantRepository
import com.example.androidinterview.merchant.domain.model.Activity
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Merchant
import com.example.androidinterview.merchant.domain.model.Money
import com.example.androidinterview.testutil.MainDispatcherRule
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun activity(id: String, amount: Long, description: String) = Activity(
        id = id,
        type = if (amount < 0) ActivityType.PAYOUT else ActivityType.DEPOSIT,
        amount = Money(amount, Currency.GBP),
        date = Instant.parse("2026-05-18T10:23:00Z"),
        description = description,
        status = ActivityStatus.COMPLETED,
    )

    private val merchant = Merchant(
        availableBalance = Money(500000, Currency.GBP),
        pendingBalance = Money(25000, Currency.GBP),
        recentActivity = listOf(
            activity("1", 150000, "Payment from Customer ABC"),
            activity("2", -50000, "Payout to Bank Account ****1234"),
            activity("3", 230000, "Payment from Customer XYZ"),
        ),
    )

    @Test
    fun `loads the merchant and exposes balances and rows`() {
        val viewModel = HomeViewModel(FakeMerchantRepository(DataResult.Success(merchant)))

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(Money(500000, Currency.GBP), state.availableBalance)
        assertEquals(Money(25000, Currency.GBP), state.pendingBalance)
        assertEquals(
            listOf(
                ActivityRowUi("1", "Payment from Customer ABC", Money(150000, Currency.GBP)),
                ActivityRowUi("2", "Payout to Bank Account ****1234", Money(-50000, Currency.GBP)),
                ActivityRowUi("3", "Payment from Customer XYZ", Money(230000, Currency.GBP)),
            ),
            state.recentActivity,
        )
    }

    @Test
    fun `network failure shows the network reason`() {
        val viewModel = HomeViewModel(FakeMerchantRepository(DataResult.Failure(DataError.Network)))

        assertEquals(HomeUiState.Error(HomeUiState.Error.Reason.Network), viewModel.uiState.value)
    }

    @Test
    fun `server and unknown failures show the unexpected reason`() {
        val server = HomeViewModel(FakeMerchantRepository(DataResult.Failure(DataError.Server(503, null))))
        val unknown = HomeViewModel(FakeMerchantRepository(DataResult.Failure(DataError.Unknown)))

        val expected = HomeUiState.Error(HomeUiState.Error.Reason.Unexpected)
        assertEquals(expected, server.uiState.value)
        assertEquals(expected, unknown.uiState.value)
    }

    @Test
    fun `retry reloads and recovers from an error`() {
        val repository = FakeMerchantRepository(DataResult.Failure(DataError.Network))
        val viewModel = HomeViewModel(repository)

        repository.result = DataResult.Success(merchant)
        viewModel.retry()

        assertEquals(2, repository.callCount)
        assertEquals(3, (viewModel.uiState.value as HomeUiState.Success).recentActivity.size)
    }

    @Test
    fun `empty activity gives an empty success state`() {
        val viewModel = HomeViewModel(
            FakeMerchantRepository(DataResult.Success(merchant.copy(recentActivity = emptyList()))),
        )

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(emptyList<ActivityRowUi>(), state.recentActivity)
    }

    @Test
    fun `show more emits a single show activity event`() = runTest {
        val viewModel = HomeViewModel(FakeMerchantRepository(DataResult.Success(merchant)))
        val event = async { viewModel.events.first() }

        viewModel.onShowMoreClick()

        assertEquals(HomeViewModel.Event.ShowActivity, event.await())
    }
}
