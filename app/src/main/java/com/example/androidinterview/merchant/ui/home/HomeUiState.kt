package com.example.androidinterview.merchant.ui.home

import androidx.compose.runtime.Stable
import com.example.androidinterview.merchant.domain.model.Money

/** What the Home screen shows; exactly one of these at a time. */
@Stable
sealed interface HomeUiState {

    /** The merchant overview is being fetched. */
    data object Loading : HomeUiState

    /**
     * The merchant overview could not be loaded.
     *
     * @param reason Why loading failed.
     */
    data class Error(val reason: Reason) : HomeUiState {

        /** Why loading failed; the composable decides which text to show. */
        @Stable
        sealed interface Reason {
            data object Network : Reason
            data object Unexpected : Reason
        }
    }

    /**
     * The merchant overview was loaded.
     *
     * @param availableBalance Balance available for payouts; the composable formats it.
     * @param pendingBalance Balance not yet settled; the composable formats it.
     * @param recentActivity Most recent activity rows; empty when there is none.
     */
    data class Success(
        val availableBalance: Money,
        val pendingBalance: Money,
        val recentActivity: List<ActivityRowUi>,
    ) : HomeUiState
}

/**
 * One row of the recent activity list.
 *
 * @param id Unique identifier of the activity.
 * @param description Human-readable label of the transaction.
 * @param amount Amount of the transaction, negative for outflows; the composable formats and colours it.
 */
data class ActivityRowUi(
    val id: String,
    val description: String,
    val amount: Money,
)
