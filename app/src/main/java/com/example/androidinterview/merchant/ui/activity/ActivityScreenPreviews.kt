package com.example.androidinterview.merchant.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Money
import com.example.androidinterview.ui.theme.AndroidInterviewTheme
import java.time.LocalDate

/** Sample loaded state shared by the previews: Today, Yesterday and an older day. */
private val previewContent = ActivityUiState.Content(
    sections = listOf(
        ActivitySectionUi(
            date = LocalDate.of(2026, 1, 23),
            header = DateHeader.Today,
            items = listOf(
                previewItem("1", ActivityType.DEPOSIT, "Payment from Customer MYK", 72738, LocalDate.of(2026, 1, 23)),
                previewItem("2", ActivityType.FEE, "Transaction fee", -97974, LocalDate.of(2026, 1, 23)),
            ),
        ),
        ActivitySectionUi(
            date = LocalDate.of(2026, 1, 22),
            header = DateHeader.Yesterday,
            items = listOf(
                previewItem(
                    "3", ActivityType.REFUND, "Refund to Customer TMF", -50989, LocalDate.of(2026, 1, 22),
                    ActivityStatus.PENDING,
                ),
            ),
        ),
        ActivitySectionUi(
            date = LocalDate.of(2026, 1, 21),
            header = DateHeader.Date(LocalDate.of(2026, 1, 21)),
            items = listOf(
                previewItem(
                    "4", ActivityType.PAYOUT, "Payout to Bank Account ****1234", -50000, LocalDate.of(2026, 1, 21),
                ),
            ),
        ),
    ),
    append = AppendState.Loading,
)

/**
 * Builds a GBP item for the previews.
 *
 * @param id Unique identifier of the item.
 * @param type Kind of transaction.
 * @param description Label of the transaction.
 * @param pence Amount in pence, negative for outflows.
 * @param date Local day of the item.
 * @param status Processing status.
 */
private fun previewItem(
    id: String,
    type: ActivityType,
    description: String,
    pence: Long,
    date: LocalDate,
    status: ActivityStatus = ActivityStatus.COMPLETED,
) = ActivityItemUi(id, type, description, Money(pence, Currency.GBP), date, status)

/**
 * Renders [ActivityScreen] in the app theme with no-op callbacks.
 *
 * @param state State to preview.
 */
@Composable
private fun ActivityScreenPreview(state: ActivityUiState) {
    AndroidInterviewTheme {
        ActivityScreen(state, onDone = {}, onLoadMore = {}, onRetry = {})
    }
}

/** Preview of loaded sections while the next page loads. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenContentPreview() = ActivityScreenPreview(previewContent)

/** Preview of loaded sections once every page has been loaded: no footer. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenEndReachedPreview() =
    ActivityScreenPreview(previewContent.copy(append = AppendState.EndReached))

/** Preview of loaded sections after the next page failed. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenAppendErrorPreview() =
    ActivityScreenPreview(previewContent.copy(append = AppendState.Error(ActivityUiState.Reason.Network)))

/** Preview of a merchant without activity. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenEmptyPreview() =
    ActivityScreenPreview(ActivityUiState.Content(emptyList(), AppendState.EndReached))

/** Preview of the first page loading. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenLoadingPreview() = ActivityScreenPreview(ActivityUiState.Loading)

/** Preview of the first page failing. */
@Preview(showBackground = true)
@Composable
private fun ActivityScreenErrorPreview() =
    ActivityScreenPreview(ActivityUiState.Error(ActivityUiState.Reason.Unexpected))
