package com.example.androidinterview.merchant.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.MerchantRepository
import com.example.androidinterview.merchant.domain.model.Activity
import com.example.androidinterview.merchant.domain.model.ActivityPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Loads the merchant's activity page by page and exposes it grouped by local date.
 *
 * @param repository Source of the activity pages.
 * @param clock Gives "today" and the time zone used for grouping.
 */
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: MerchantRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)

    /** Current state of the activity list. */
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    /** One-shot instructions for the UI, each delivered once. */
    sealed interface Event {

        /** The activity list should close. */
        data object Dismiss : Event
    }

    private val _events = Channel<Event>()

    /** One-shot events to collect while the UI is visible. */
    val events: Flow<Event> = _events.receiveAsFlow()

    /** Cursor of the next page; null when the last page has been loaded. */
    private var nextCursor: String? = null

    /** The page request in flight, if any. */
    private var loadJob: Job? = null

    init {
        loadFirstPage()
    }

    /** The user tapped Done: asks the UI to close the activity list. */
    fun onDoneClick() {
        viewModelScope.launch { _events.send(Event.Dismiss) }
    }

    /** Loads the next page when more are available; ignored while a page is loading or after a failure. */
    fun loadMore() {
        val state = _uiState.value
        if (state is ActivityUiState.Content && state.append == AppendState.Idle) {
            loadNextPage()
        }
    }

    /** Retries whichever request failed: the first page, or the next page. */
    fun retry() {
        when (val state = _uiState.value) {
            is ActivityUiState.Error -> loadFirstPage()
            is ActivityUiState.Content -> if (state.append is AppendState.Error) {
                loadNextPage()
            }

            ActivityUiState.Loading -> Unit
        }
    }

    /** Clears what was loaded and fetches the first page. */
    private fun loadFirstPage() {
        loadJob?.cancel()
        nextCursor = null
        _uiState.value = ActivityUiState.Loading
        loadJob = viewModelScope.launch {
            when (val result = repository.getActivityPage()) {
                is DataResult.Success -> onPageLoaded(result.data)
                is DataResult.Failure -> _uiState.value =
                    ActivityUiState.Error(result.error.toReason())
            }
        }
    }

    /** Fetches the page after [nextCursor], keeping the loaded sections on screen. */
    private fun loadNextPage() {
        val cursor = nextCursor ?: return
        if (loadJob?.isActive == true) return
        _uiState.update {
            (it as ActivityUiState.Content).copy(append = AppendState.Loading)
        }
        loadJob = viewModelScope.launch {
            when (val result = repository.getActivityPage(cursor)) {
                is DataResult.Success -> onPageLoaded(result.data)
                is DataResult.Failure -> _uiState.update {
                    (it as ActivityUiState.Content).copy(append = AppendState.Error(result.error.toReason()))
                }
            }
        }
    }

    /**
     * Adds [page] to the sections already on screen; before the first page there are none.
     *
     * @param page The page that was just loaded.
     */
    private fun onPageLoaded(page: ActivityPage) {
        nextCursor = page.nextCursor
        _uiState.update { previous ->
            val loaded = (previous as? ActivityUiState.Content)?.sections.orEmpty()
            ActivityUiState.Content(
                sections = loaded.withPage(page.items, clock),
                append = if (page.nextCursor == null) AppendState.EndReached else AppendState.Idle,
            )
        }
    }
}

/**
 * Adds a page of activity under the matching local date, in [clock]'s zone, keeping the API order.
 * Items already shown are skipped, and their UI models are reused as they are.
 *
 * @param page Activity of the page that was just loaded, newest first.
 * @param clock Gives "today" and the time zone.
 */
private fun List<ActivitySectionUi>.withPage(
    page: List<Activity>,
    clock: Clock
): List<ActivitySectionUi> {
    val shownIds = flatMap { section -> section.items.map { it.id } }.toSet()
    val itemsByDate = LinkedHashMap<LocalDate, List<ActivityItemUi>>()
    forEach { section ->
        itemsByDate[section.date] = section.items
    }
    page.filter { it.id !in shownIds }
        .distinctBy { it.id }
        .forEach { activity ->
            val date = activity.date.atZone(clock.zone).toLocalDate()
            itemsByDate[date] = itemsByDate[date].orEmpty() + activity.toItemUi(date)
        }
    val today = LocalDate.now(clock)
    return itemsByDate.map { (date, items) ->
        ActivitySectionUi(
            date = date,
            header = when (date) {
                today -> DateHeader.Today
                today.minusDays(1) -> DateHeader.Yesterday
                else -> DateHeader.Date(date)
            },
            items = items,
        )
    }
}

/**
 * Maps the domain model to a display-ready item.
 *
 * @param localDate Local date of the activity, already computed for grouping.
 */
private fun Activity.toItemUi(localDate: LocalDate) = ActivityItemUi(
    id = id,
    type = type,
    description = description,
    amount = amount,
    date = localDate,
    status = status,
)

/** Collapses the data-layer error into the reason the screen distinguishes. */
private fun DataError.toReason() = when (this) {
    DataError.Network -> ActivityUiState.Reason.Network
    is DataError.Server, DataError.Unknown -> ActivityUiState.Reason.Unexpected
}
