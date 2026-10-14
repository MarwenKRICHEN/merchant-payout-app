package com.example.androidinterview.payout.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.ui.theme.AndroidInterviewTheme

private const val PREVIEW_IBAN = "GB29NWBK60161331926819"

/** Sample valid form shared by the previews. */
private val previewForm = PayoutFormUi(
    amountInput = "400",
    currency = Currency.GBP,
    ibanInput = PREVIEW_IBAN,
    isConfirmEnabled = true,
)

/**
 * Renders [PayoutScreen] with no-op callbacks.
 *
 * @param state State to render.
 */
@Composable
private fun PreviewPayoutScreen(state: PayoutUiState) {
    AndroidInterviewTheme {
        PayoutScreen(
            state = state,
            onAmountChange = {},
            onCurrencyChange = {},
            onIbanChange = {},
            onConfirmClick = {},
            onCancelClick = {},
            onSubmitClick = {},
            onTryAgainClick = {},
            onCreateAnotherClick = {},
            onOpenSettingsClick = {},
        )
    }
}

/** Preview of the empty form, Confirm disabled. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenEmptyPreview() {
    PreviewPayoutScreen(PayoutUiState.Editing())
}

/** Preview of a valid form. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenFilledPreview() {
    PreviewPayoutScreen(PayoutUiState.Editing(previewForm))
}

/** Preview of an invalid IBAN. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenIbanErrorPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Editing(
            previewForm.copy(
                ibanInput = "FR1212345123451234567A12310131231231231",
                showIbanError = true,
                isConfirmEnabled = false,
            ),
        ),
    )
}

/** Preview of the confirmation dialog. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenConfirmingPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Editing(
            form = previewForm,
            confirmation = ConfirmationUi(Money(40000, Currency.GBP), PREVIEW_IBAN),
        ),
    )
}

/** Preview of the confirmation dialog while the payout is being sent. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenSubmittingPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Editing(
            form = previewForm,
            confirmation = ConfirmationUi(Money(40000, Currency.GBP), PREVIEW_IBAN, isSubmitting = true),
        ),
    )
}

/** Preview of the success result. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenCompletedPreview() {
    PreviewPayoutScreen(PayoutUiState.Completed(Money(40000, Currency.GBP)))
}

/** Preview of the insufficient funds failure. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenInsufficientFundsPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Failed(previewForm, PayoutUiState.Failed.Reason.InsufficientFunds),
    )
}

/** Preview of the dismissed biometric prompt. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenBiometricCancelledPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Failed(previewForm, PayoutUiState.Failed.Reason.BiometricCancelled),
    )
}

/** Preview of a device without a biometric set up. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenBiometricNotEnrolledPreview() {
    PreviewPayoutScreen(
        PayoutUiState.Failed(previewForm, PayoutUiState.Failed.Reason.BiometricNotEnrolled),
    )
}

/** Preview of the service unavailable failure. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenServiceUnavailablePreview() {
    PreviewPayoutScreen(
        PayoutUiState.Failed(previewForm, PayoutUiState.Failed.Reason.ServiceUnavailable),
    )
}
