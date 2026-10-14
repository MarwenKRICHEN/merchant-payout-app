package com.example.androidinterview.merchant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.domain.MerchantRepository
import com.example.androidinterview.merchant.domain.model.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads the merchant overview and exposes it as [HomeUiState].
 *
 * @param repository Source of the merchant overview.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MerchantRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    /** Current state of the Home screen. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** One-shot instructions for the UI, each delivered once. */
    sealed interface Event {

        /** The full activity list should be opened. */
        data object ShowActivity : Event
    }

    private val _events = Channel<Event>()
    /** One-shot events to collect while the UI is visible. */
    val events: Flow<Event> = _events.receiveAsFlow()

    init {
        load()
    }

    /** Reloads the merchant overview, e.g. after a failure. */
    fun retry() = load()

    /** The user tapped Show More: asks the UI to open the full activity list. */
    fun onShowMoreClick() {
        viewModelScope.launch { _events.send(Event.ShowActivity) }
    }

    /** Emits [HomeUiState.Loading], then the outcome of fetching the merchant. */
    private fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getMerchant()) {
                is DataResult.Success -> result.data.toUiState()
                is DataResult.Failure -> HomeUiState.Error(result.error.toReason())
            }
        }
    }
}

/** Maps the domain model to the UI state; formatting is left to the composables. */
private fun Merchant.toUiState() = HomeUiState.Success(
    availableBalance = availableBalance,
    pendingBalance = pendingBalance,
    recentActivity = recentActivity.map {
        ActivityRowUi(id = it.id, description = it.description, amount = it.amount)
    },
)

/** Collapses the data-layer error into the reason the screen distinguishes. */
private fun DataError.toReason() = when (this) {
    DataError.Network -> HomeUiState.Error.Reason.Network
    is DataError.Server, DataError.Unknown -> HomeUiState.Error.Reason.Unexpected
}
