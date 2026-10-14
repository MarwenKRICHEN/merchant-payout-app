package com.example.androidinterview.merchant.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidinterview.merchant.domain.model.Currency
import com.example.androidinterview.merchant.domain.model.Money
import com.example.androidinterview.ui.theme.AndroidInterviewTheme

/** Sample loaded state shared by the previews. */
private val previewSuccess = HomeUiState.Success(
    availableBalance = Money(500000, Currency.GBP),
    pendingBalance = Money(25000, Currency.GBP),
    recentActivity = listOf(
        ActivityRowUi("1", "Payment from Customer ABC", Money(150000, Currency.GBP)),
        ActivityRowUi("2", "Payout to Bank Account ****1234", Money(-50000, Currency.GBP)),
        ActivityRowUi("3", "Payment from Customer XYZ", Money(230000, Currency.GBP)),
    ),
)

/** Preview of the loaded state. */
@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
    AndroidInterviewTheme { HomeScreen(previewSuccess, onRetry = {}, onShowMoreClick = {}) }
}

/** Preview of the loaded state without activity. */
@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    AndroidInterviewTheme {
        HomeScreen(previewSuccess.copy(recentActivity = emptyList()), onRetry = {}, onShowMoreClick = {})
    }
}

/** Preview of the loading state. */
@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    AndroidInterviewTheme { HomeScreen(HomeUiState.Loading, onRetry = {}, onShowMoreClick = {}) }
}

/** Preview of the error state. */
@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    AndroidInterviewTheme {
        HomeScreen(HomeUiState.Error(HomeUiState.Error.Reason.Network), onRetry = {}, onShowMoreClick = {})
    }
}
