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
 * @param content Content to render.
 * @param isScreenshotWarningVisible Whether the screenshot reminder is open.
 */
@Composable
private fun PreviewPayoutScreen(content: PayoutContent, isScreenshotWarningVisible: Boolean = false) {
    AndroidInterviewTheme {
        PayoutScreen(
            state = PayoutUiState(content, isScreenshotWarningVisible),
            onAmountChange = {},
            onCurrencyChange = {},
            onIbanChange = {},
            onConfirmClick = {},
            onCancelClick = {},
            onSubmitClick = {},
            onTryAgainClick = {},
            onCreateAnotherClick = {},
            onOpenSettingsClick = {},
            onScreenshotWarningDismiss = {},
        )
    }
}

/** Preview of the empty form, Confirm disabled. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenEmptyPreview() {
    PreviewPayoutScreen(PayoutContent.Editing())
}

/** Preview of the reminder shown after a screenshot. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenScreenshotWarningPreview() {
    PreviewPayoutScreen(PayoutContent.Editing(), isScreenshotWarningVisible = true)
}

/** Preview of a valid form. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenFilledPreview() {
    PreviewPayoutScreen(PayoutContent.Editing(previewForm))
}

/** Preview of an invalid IBAN. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenIbanErrorPreview() {
    PreviewPayoutScreen(
        PayoutContent.Editing(
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
        PayoutContent.Editing(
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
        PayoutContent.Editing(
            form = previewForm,
            confirmation = ConfirmationUi(Money(40000, Currency.GBP), PREVIEW_IBAN, isSubmitting = true),
        ),
    )
}

/** Preview of the success result. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenCompletedPreview() {
    PreviewPayoutScreen(PayoutContent.Completed(Money(40000, Currency.GBP)))
}

/** Preview of the insufficient funds failure. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenInsufficientFundsPreview() {
    PreviewPayoutScreen(
        PayoutContent.Failed(previewForm, PayoutContent.Failed.Reason.InsufficientFunds),
    )
}

/** Preview of the dismissed biometric prompt. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenBiometricCancelledPreview() {
    PreviewPayoutScreen(
        PayoutContent.Failed(previewForm, PayoutContent.Failed.Reason.BiometricCancelled),
    )
}

/** Preview of a device without a biometric set up. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenBiometricNotEnrolledPreview() {
    PreviewPayoutScreen(
        PayoutContent.Failed(previewForm, PayoutContent.Failed.Reason.BiometricNotEnrolled),
    )
}

/** Preview of the service unavailable failure. */
@Preview(showBackground = true)
@Composable
private fun PayoutScreenServiceUnavailablePreview() {
    PreviewPayoutScreen(
        PayoutContent.Failed(previewForm, PayoutContent.Failed.Reason.ServiceUnavailable),
    )
}
