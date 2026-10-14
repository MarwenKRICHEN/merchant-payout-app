package com.example.androidinterview.merchant.ui.activity

import androidx.compose.runtime.Stable
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.money.Money
import java.time.LocalDate

/** What the activity list shows; exactly one of these at a time. */
@Stable
sealed interface ActivityUiState {

    /** The first page is being fetched. */
    data object Loading : ActivityUiState

    /**
     * The first page could not be loaded.
     *
     * @param reason Why loading failed.
     */
    data class Error(val reason: Reason) : ActivityUiState

    /**
     * At least the first page was loaded.
     *
     * @param sections Loaded activity grouped by local date, newest first; empty when there is none.
     * @param append State of loading the next page.
     */
    data class Content(
        val sections: List<ActivitySectionUi>,
        val append: AppendState,
    ) : ActivityUiState

    /** Why a page failed to load; the composable decides which text to show. */
    @Stable
    sealed interface Reason {
        data object Network : Reason
        data object Unexpected : Reason
    }
}

/** State of loading the next page, shown at the bottom of the list. */
@Stable
sealed interface AppendState {

    /** More pages are available and none is loading. */
    data object Idle : AppendState

    /** The next page is being fetched. */
    data object Loading : AppendState

    /**
     * The next page could not be loaded.
     *
     * @param reason Why loading failed.
     */
    data class Error(val reason: ActivityUiState.Reason) : AppendState

    /** Every page has been loaded. */
    data object EndReached : AppendState
}

/**
 * Activity that happened on the same local date.
 *
 * @param date Local date shared by the items; also the section key.
 * @param header Header shown above the items.
 * @param items Activity of that day, newest first.
 */
data class ActivitySectionUi(
    val date: LocalDate,
    val header: DateHeader,
    val items: List<ActivityItemUi>,
)

/** Header of a date section; the composable renders relative days as text. */
@Stable
sealed interface DateHeader {

    /** The section is today. */
    data object Today : DateHeader

    /** The section is yesterday. */
    data object Yesterday : DateHeader

    /**
     * Any other day.
     *
     * @param date The day; the composable formats it.
     */
    data class Date(val date: LocalDate) : DateHeader
}

/**
 * One item of the activity list.
 *
 * @param id Unique identifier of the activity.
 * @param type Kind of transaction; the composable renders its label.
 * @param description Human-readable label of the transaction.
 * @param amount Amount of the transaction, negative for outflows; the composable formats and colours it.
 * @param date Local day of the activity, in the zone used for grouping; the composable formats it.
 * @param status Processing status; the composable renders its label.
 */
data class ActivityItemUi(
    val id: String,
    val type: ActivityType,
    val description: String,
    val amount: Money,
    val date: LocalDate,
    val status: ActivityStatus,
)
