package com.example.androidinterview.merchant.ui.activity

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.FakeMerchantRepository
import com.example.androidinterview.merchant.domain.model.Activity
import com.example.androidinterview.merchant.domain.model.ActivityPage
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Money
import com.example.androidinterview.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** "Now" is 18 May 2026, 12:00 in London (BST, UTC+1). */
    private val clock = Clock.fixed(Instant.parse("2026-05-18T11:00:00Z"), ZoneId.of("Europe/London"))

    private fun activity(id: String, date: String, amount: Long = 10000) = Activity(
        id = id,
        type = if (amount < 0) ActivityType.PAYOUT else ActivityType.DEPOSIT,
        amount = Money(amount, Currency.GBP),
        date = Instant.parse(date),
        description = "Activity $id",
        status = ActivityStatus.COMPLETED,
    )

    private val firstPage = ActivityPage(
        items = listOf(
            activity("1", "2026-05-18T09:00:00Z"),
            activity("2", "2026-05-17T23:30:00Z", amount = -50000), // 18 May 00:30 in London -> Today
            activity("3", "2026-05-17T10:00:00Z"),
        ),
        nextCursor = "3",
    )

    private val secondPage = ActivityPage(
        items = listOf(
            activity("4", "2026-05-17T08:00:00Z"),
            activity("5", "2026-05-16T08:00:00Z"),
        ),
        nextCursor = null,
    )

    private fun repository() = FakeMerchantRepository(
        activityResults = mutableMapOf(
            null to DataResult.Success(firstPage),
            "3" to DataResult.Success(secondPage),
        ),
    )

    private val ActivityViewModel.content get() = uiState.value as ActivityUiState.Content

    @Test
    fun `first page is grouped by local date`() {
        val viewModel = ActivityViewModel(repository(), clock)

        val state = viewModel.content
        assertEquals(AppendState.Idle, state.append)
        assertEquals(listOf(DateHeader.Today, DateHeader.Yesterday), state.sections.map { it.header })
        assertEquals(listOf("1", "2"), state.sections[0].items.map { it.id })
        assertEquals(
            ActivityItemUi(
                id = "2",
                type = ActivityType.PAYOUT,
                description = "Activity 2",
                amount = Money(-50000, Currency.GBP),
                date = LocalDate.of(2026, 5, 18),
                status = ActivityStatus.COMPLETED,
            ),
            state.sections[0].items[1],
        )
    }

    @Test
    fun `loadMore appends the next page into existing and new sections`() {
        val repository = repository()
        val viewModel = ActivityViewModel(repository, clock)

        viewModel.loadMore()

        val state = viewModel.content
        assertEquals(listOf(null, "3"), repository.activityRequests)
        assertEquals(AppendState.EndReached, state.append)
        assertEquals(
            listOf(DateHeader.Today, DateHeader.Yesterday, DateHeader.Date(LocalDate.of(2026, 5, 16))),
            state.sections.map { it.header },
        )
        assertEquals(listOf("3", "4"), state.sections[1].items.map { it.id })
    }

    @Test
    fun `loadMore is ignored while a page is loading and after the last page`() {
        val repository = repository()
        val viewModel = ActivityViewModel(repository, clock)
        val gate = CompletableDeferred<Unit>()
        repository.activityGate = gate

        viewModel.loadMore()
        assertEquals(AppendState.Loading, viewModel.content.append)
        viewModel.loadMore()
        gate.complete(Unit)
        viewModel.loadMore()

        assertEquals(listOf(null, "3"), repository.activityRequests)
        assertEquals(AppendState.EndReached, viewModel.content.append)
    }

    @Test
    fun `first page failure shows the reason and retry recovers`() {
        val repository = repository()
        repository.activityResults[null] = DataResult.Failure(DataError.Network)
        val viewModel = ActivityViewModel(repository, clock)

        assertEquals(ActivityUiState.Error(ActivityUiState.Reason.Network), viewModel.uiState.value)

        repository.activityResults[null] = DataResult.Success(firstPage)
        viewModel.retry()

        assertEquals(2, viewModel.content.sections.size)
    }

    @Test
    fun `next page failure keeps the sections and retry resumes from the same cursor`() {
        val repository = repository()
        repository.activityResults["3"] = DataResult.Failure(DataError.Server(503, null))
        val viewModel = ActivityViewModel(repository, clock)

        viewModel.loadMore()
        assertEquals(AppendState.Error(ActivityUiState.Reason.Unexpected), viewModel.content.append)
        assertEquals(2, viewModel.content.sections.size)

        viewModel.loadMore() // Scrolling does not retry on its own.
        repository.activityResults["3"] = DataResult.Success(secondPage)
        viewModel.retry()

        assertEquals(listOf(null, "3", "3"), repository.activityRequests)
        assertEquals(AppendState.EndReached, viewModel.content.append)
        assertEquals(3, viewModel.content.sections.size)
    }

    @Test
    fun `empty first page reaches the end with no sections`() {
        val repository = FakeMerchantRepository(
            activityResults = mutableMapOf(null to DataResult.Success(ActivityPage(emptyList(), null))),
        )

        val viewModel = ActivityViewModel(repository, clock)

        assertEquals(ActivityUiState.Content(emptyList(), AppendState.EndReached), viewModel.uiState.value)
    }

    @Test
    fun `done emits a single dismiss event`() = runTest {
        val viewModel = ActivityViewModel(repository(), clock)
        val event = async { viewModel.events.first() }

        viewModel.onDoneClick()

        assertEquals(ActivityViewModel.Event.Dismiss, event.await())
    }

    @Test
    fun `items repeated across pages are shown once`() {
        val repository = repository()
        repository.activityResults["3"] =
            DataResult.Success(secondPage.copy(items = listOf(firstPage.items[2]) + secondPage.items))
        val viewModel = ActivityViewModel(repository, clock)

        viewModel.loadMore()

        assertEquals(
            listOf("1", "2", "3", "4", "5"),
            viewModel.content.sections.flatMap { section -> section.items.map { it.id } },
        )
    }
}
