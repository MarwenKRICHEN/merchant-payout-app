package com.example.androidinterview.payout.ui

import androidx.compose.runtime.Stable
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money

/**
 * State of the Payout screen.
 *
 * @param content What the screen shows: the form, or the result of a payout.
 */
@Stable
data class PayoutUiState(
    val content: PayoutContent = PayoutContent.Editing(),
)

/** What the Payout screen shows: filling the form, or the result of a payout. */
@Stable
sealed interface PayoutContent {

    /**
     * The user is filling the form.
     *
     * @param form What the user typed and whether it can be submitted.
     * @param confirmation The confirmation dialog shown over the form, or null when it is closed.
     */
    data class Editing(
        val form: PayoutFormUi = PayoutFormUi(),
        val confirmation: ConfirmationUi? = null,
    ) : PayoutContent

    /**
     * The payout was accepted.
     *
     * @param amount Amount that was sent.
     */
    data class Completed(val amount: Money) : PayoutContent

    /**
     * The payout could not be created.
     *
     * @param form The inputs the user had typed, restored when they try again.
     * @param reason Why; rendered as text by the composable.
     */
    data class Failed(val form: PayoutFormUi, val reason: Reason) : PayoutContent {

        /** Why the payout failed. */
        @Stable
        sealed interface Reason {
            /** No connectivity or a transport failure. */
            data object Network : Reason

            /** The payout service is temporarily unavailable. */
            data object ServiceUnavailable : Reason

            /** The balance does not cover the amount. */
            data object InsufficientFunds : Reason

            /** The user dismissed the biometric prompt. */
            data object BiometricCancelled : Reason

            /** Biometric authentication is blocked after too many attempts. */
            data object BiometricLockedOut : Reason

            /** The device has no biometric set up; the user can do it in Settings. */
            data object BiometricNotEnrolled : Reason

            /** The device cannot authenticate with biometrics. */
            data object BiometricUnavailable : Reason

            /** Biometric authentication failed. */
            data object BiometricFailed : Reason

            /** Any other failure. */
            data object Unexpected : Reason
        }
    }
}

/**
 * The payout form.
 *
 * @param amountInput Text of the amount field.
 * @param currency Currency selected in the dropdown.
 * @param ibanInput Text of the IBAN field, upper case.
 * @param showIbanError Whether the IBAN field is flagged invalid: it is not blank and not a valid IBAN.
 * @param isConfirmEnabled Whether the amount is positive and the IBAN valid.
 */
@Stable
data class PayoutFormUi(
    val amountInput: String = "",
    val currency: Currency = Currency.GBP,
    val ibanInput: String = "",
    val showIbanError: Boolean = false,
    val isConfirmEnabled: Boolean = false,
)

/**
 * What the confirmation dialog summarises.
 *
 * @param amount Amount to send.
 * @param iban Normalised destination IBAN.
 * @param isSubmitting Whether the request is in flight; the dialog then cannot be dismissed.
 */
@Stable
data class ConfirmationUi(
    val amount: Money,
    val iban: String,
    val isSubmitting: Boolean = false,
)
