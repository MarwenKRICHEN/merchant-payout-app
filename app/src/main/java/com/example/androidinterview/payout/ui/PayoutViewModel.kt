package com.example.androidinterview.payout.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.money.Currency
import com.example.androidinterview.payout.domain.AmountParser
import com.example.androidinterview.payout.domain.CreatePayoutUseCase
import com.example.androidinterview.payout.domain.IbanValidator
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_AMOUNT_LENGTH = 12
private const val MAX_AMOUNT_DECIMALS = 2

/**
 * Drives the payout flow: form editing, confirmation, submission and its outcome.
 *
 * @param createPayout Sends the payout with the device identity.
 * @param ibanValidator Normalises and validates the destination IBAN.
 * @param amountParser Turns the amount typed by the user into money.
 */
@HiltViewModel
class PayoutViewModel @Inject constructor(
    private val createPayout: CreatePayoutUseCase,
    private val ibanValidator: IbanValidator,
    private val amountParser: AmountParser,
) : ViewModel() {

    /** One-shot events the UI turns into platform actions. */
    sealed interface Event {
        /** Open the system screen where the user enrols a biometric. */
        data object OpenBiometricSettings : Event
    }

    private val _uiState = MutableStateFlow(PayoutUiState())
    private val _events = Channel<Event>()

    /** One-shot events to collect while the UI is visible. */
    val events: Flow<Event> = _events.receiveAsFlow()

    /** Current state of the Payout screen. */
    val uiState: StateFlow<PayoutUiState> = _uiState.asStateFlow()

    /**
     * The user edited the amount. Characters that cannot be part of an amount are ignored, so a
     * negative number or more than two decimals cannot be typed.
     *
     * @param text New content of the amount field.
     */
    fun onAmountChange(text: String) {
        if (!text.isAcceptableAmountInput()) return
        updateEditing { editing ->
            editing.copy(form = editing.form.copy(amountInput = text).validated())
        }
    }

    /**
     * The user picked a currency.
     *
     * @param currency Selected currency.
     */
    fun onCurrencyChange(currency: Currency) {
        updateEditing { editing ->
            editing.copy(form = editing.form.copy(currency = currency).validated())
        }
    }

    /**
     * The user edited the IBAN. The field is flagged as invalid as soon as it is not blank and not a
     * valid IBAN yet.
     *
     * @param text New content of the IBAN field.
     */
    fun onIbanChange(text: String) {
        updateEditing { editing ->
            editing.copy(form = editing.form.copy(ibanInput = text.uppercase()).validated())
        }
    }

    /** The user tapped Confirm on the form: asks for confirmation when the form is valid. */
    fun onConfirmClick() {
        updateEditing { editing ->
            val form = editing.form
            val amount = amountParser.parse(form.amountInput, form.currency)
            val iban = ibanValidator.normalize(form.ibanInput)
            if (editing.confirmation != null || amount == null || !ibanValidator.isValid(iban)) {
                editing
            } else {
                editing.copy(confirmation = ConfirmationUi(amount, iban))
            }
        }
    }

    /** The user dismissed the confirmation dialog; ignored while the payout is being sent. */
    fun onCancelClick() {
        updateEditing { editing ->
            val confirmation = editing.confirmation
            if (confirmation != null && !confirmation.isSubmitting) editing.copy(confirmation = null) else editing
        }
    }

    /** The user confirmed: sends the payout. A second tap while in flight is ignored. */
    fun onSubmitClick() {
        val editing = _uiState.value.content as? PayoutContent.Editing ?: return
        val confirmation = editing.confirmation ?: return
        if (confirmation.isSubmitting) return
        updateContent { editing.copy(confirmation = confirmation.copy(isSubmitting = true)) }
        viewModelScope.launch {
            val result = createPayout(confirmation.amount, confirmation.iban)
            updateContent {
                when (result) {
                    is PayoutResult.Success -> PayoutContent.Completed(confirmation.amount)
                    is PayoutResult.Failure -> PayoutContent.Failed(editing.form, result.error.toReason())
                }
            }
        }
    }

    /** The user wants to correct a failed payout: back to the form with the inputs kept. */
    fun onTryAgainClick() {
        updateContent { previous ->
            if (previous is PayoutContent.Failed) PayoutContent.Editing(previous.form) else previous
        }
    }

    /** The user wants to set up a biometric after being told none is enrolled. */
    fun onOpenSettingsClick() {
        viewModelScope.launch { _events.send(Event.OpenBiometricSettings) }
    }

    /** The user wants to send another payout: starts from an empty form. */
    fun onCreateAnotherClick() {
        updateContent { PayoutContent.Editing() }
    }

    /** Applies [transform] when the user is editing the form; the other states have nothing to edit. */
    private inline fun updateEditing(crossinline transform: (PayoutContent.Editing) -> PayoutContent.Editing) {
        updateContent { previous ->
            if (previous is PayoutContent.Editing) transform(previous) else previous
        }
    }

    /** Replaces the content of the screen with [transform] applied to the current one. */
    private inline fun updateContent(crossinline transform: (PayoutContent) -> PayoutContent) {
        _uiState.update { previous -> previous.copy(content = transform(previous.content)) }
    }

    /** Recomputes [PayoutFormUi.showIbanError] and [PayoutFormUi.isConfirmEnabled] from the inputs. */
    private fun PayoutFormUi.validated(): PayoutFormUi {
        val iban = ibanValidator.normalize(ibanInput)
        val isIbanValid = ibanValidator.isValid(iban)
        return copy(
            showIbanError = iban.isNotEmpty() && !isIbanValid,
            isConfirmEnabled = amountParser.parse(amountInput, currency) != null && isIbanValid,
        )
    }
}

/** Whether [this] is digits with at most one `.` or `,` and two decimals, within the length limit. */
private fun String.isAcceptableAmountInput(): Boolean {
    if (length > MAX_AMOUNT_LENGTH) return false
    val separators = count { it == '.' || it == ',' }
    if (separators > 1 || any { !it.isDigit() && it != '.' && it != ',' }) return false
    val separatorIndex = indexOfFirst { it == '.' || it == ',' }
    return separatorIndex == -1 || length - separatorIndex - 1 <= MAX_AMOUNT_DECIMALS
}

/** Maps the domain error to the reason the payout screen renders. */
private fun PayoutError.toReason(): PayoutContent.Failed.Reason = when (this) {
    PayoutError.Network -> PayoutContent.Failed.Reason.Network
    PayoutError.InsufficientFunds -> PayoutContent.Failed.Reason.InsufficientFunds
    PayoutError.ServiceUnavailable -> PayoutContent.Failed.Reason.ServiceUnavailable
    PayoutError.AuthenticationCancelled -> PayoutContent.Failed.Reason.BiometricCancelled
    PayoutError.AuthenticationLockedOut -> PayoutContent.Failed.Reason.BiometricLockedOut
    PayoutError.AuthenticationNotEnrolled -> PayoutContent.Failed.Reason.BiometricNotEnrolled
    PayoutError.AuthenticationUnavailable -> PayoutContent.Failed.Reason.BiometricUnavailable
    PayoutError.AuthenticationFailed -> PayoutContent.Failed.Reason.BiometricFailed
    PayoutError.Unknown -> PayoutContent.Failed.Reason.Unexpected
}
