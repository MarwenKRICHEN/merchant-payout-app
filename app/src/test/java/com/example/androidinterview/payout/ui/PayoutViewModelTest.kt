package com.example.androidinterview.payout.ui

import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.authentication.AuthenticationResult
import com.example.androidinterview.authentication.FakeAuthenticationProvider
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.domain.FakeDeviceRepository
import com.example.androidinterview.payout.domain.AmountParser
import com.example.androidinterview.payout.domain.CreatePayoutUseCase
import com.example.androidinterview.payout.domain.FakePayoutRepository
import com.example.androidinterview.payout.domain.IbanValidator
import com.example.androidinterview.payout.domain.model.Payout
import com.example.androidinterview.payout.domain.model.PayoutError
import com.example.androidinterview.payout.domain.model.PayoutRequest
import com.example.androidinterview.payout.domain.model.PayoutResult
import com.example.androidinterview.payout.domain.model.PayoutStatus
import com.example.androidinterview.testutil.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PayoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val validIban = "GB29NWBK60161331926819"

    private val payout = Payout(
        id = "pay_001",
        status = PayoutStatus.PENDING,
        amount = Money(40000, Currency.GBP),
        iban = validIban,
        createdAt = Instant.parse("2026-05-18T10:00:00Z"),
    )

    private val repository = FakePayoutRepository(PayoutResult.Success(payout))
    private val deviceRepository = FakeDeviceRepository()
    private val authenticationProvider = FakeAuthenticationProvider()
    private val viewModel = PayoutViewModel(
        CreatePayoutUseCase(repository, deviceRepository, authenticationProvider),
        IbanValidator(),
        AmountParser(),
    )

    private val content get() = viewModel.uiState.value.content
    private val editing get() = content as PayoutContent.Editing

    private fun fillValidForm(amount: String = "400") {
        viewModel.onAmountChange(amount)
        viewModel.onIbanChange(validIban)
    }

    @Test
    fun `confirm is disabled for an empty or zero amount`() {
        viewModel.onIbanChange(validIban)
        assertFalse(editing.form.isConfirmEnabled)

        viewModel.onAmountChange("0")
        assertFalse(editing.form.isConfirmEnabled)
    }

    @Test
    fun `confirm is disabled for an invalid IBAN and enabled once the form is valid`() {
        viewModel.onAmountChange("400")
        viewModel.onIbanChange("FR1212345123451234567A12310131231231231")
        assertFalse(editing.form.isConfirmEnabled)

        viewModel.onIbanChange(validIban)
        assertTrue(editing.form.isConfirmEnabled)
    }

    @Test
    fun `amount input rejects characters that cannot be part of an amount`() {
        viewModel.onAmountChange("12")
        viewModel.onAmountChange("-12")
        viewModel.onAmountChange("12a")
        viewModel.onAmountChange("12.345")
        viewModel.onAmountChange("1.2.3")

        assertEquals("12", editing.form.amountInput)
    }

    @Test
    fun `amount input is capped so it always fits in minor units`() {
        viewModel.onAmountChange("999999999999")
        viewModel.onAmountChange("9999999999999")

        assertEquals("999999999999", editing.form.amountInput)
    }

    @Test
    fun `iban error shows as soon as the user types and clears once the iban is valid`() {
        viewModel.onIbanChange("G")
        assertTrue(editing.form.showIbanError)

        viewModel.onIbanChange("GB29NWBK6016133192681")
        assertTrue(editing.form.showIbanError)

        viewModel.onIbanChange(validIban)
        assertFalse(editing.form.showIbanError)
    }

    @Test
    fun `blank iban shows no error`() {
        viewModel.onIbanChange("   ")

        assertFalse(editing.form.showIbanError)
    }

    @Test
    fun `iban input is upper-cased`() {
        viewModel.onIbanChange("gb29")

        assertEquals("GB29", editing.form.ibanInput)
    }

    @Test
    fun `confirm asks for confirmation with the parsed amount and normalised IBAN`() {
        viewModel.onAmountChange("400")
        viewModel.onIbanChange("gb29 nwbk 6016 1331 9268 19")

        viewModel.onConfirmClick()

        assertEquals(ConfirmationUi(Money(40000, Currency.GBP), validIban), editing.confirmation)
    }

    @Test
    fun `confirm is ignored while the form is invalid`() {
        viewModel.onAmountChange("400")

        viewModel.onConfirmClick()

        assertNull(editing.confirmation)
    }

    @Test
    fun `cancel returns to the form`() {
        fillValidForm()
        viewModel.onConfirmClick()

        viewModel.onCancelClick()

        assertNull(editing.confirmation)
    }

    @Test
    fun `submit sends the request and completes with the amount`() {
        fillValidForm()
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()

        assertEquals(listOf(PayoutRequest(Money(40000, Currency.GBP), validIban, "device_1")), repository.requests)
        assertEquals(PayoutContent.Completed(Money(40000, Currency.GBP)), content)
    }

    @Test
    fun `currency selection is sent with the payout`() {
        fillValidForm("10")
        viewModel.onCurrencyChange(Currency.EUR)
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()

        assertEquals(Currency.EUR, repository.requests.single().amount.currency)
    }

    @Test
    fun `while submitting cancel and a second submit are ignored`() {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        fillValidForm()
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()
        assertEquals(
            ConfirmationUi(Money(40000, Currency.GBP), validIban, isSubmitting = true),
            editing.confirmation,
        )
        viewModel.onCancelClick()
        viewModel.onSubmitClick()
        gate.complete(Unit)

        assertEquals(1, repository.requests.size)
        assertTrue(content is PayoutContent.Completed)
    }

    @Test
    fun `insufficient funds failure shows the insufficient funds reason`() {
        repository.result = PayoutResult.Failure(PayoutError.InsufficientFunds)
        fillValidForm("888.88")
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()

        assertEquals(PayoutContent.Failed.Reason.InsufficientFunds, (content as PayoutContent.Failed).reason)
    }

    @Test
    fun `failures map to the matching reasons`() {
        val cases = mapOf(
            PayoutError.ServiceUnavailable to PayoutContent.Failed.Reason.ServiceUnavailable,
            PayoutError.Network to PayoutContent.Failed.Reason.Network,
            PayoutError.InsufficientFunds to PayoutContent.Failed.Reason.InsufficientFunds,
            PayoutError.Unknown to PayoutContent.Failed.Reason.Unexpected,
        )
        cases.forEach { (error, reason) ->
            val vm = PayoutViewModel(
                CreatePayoutUseCase(
                    FakePayoutRepository(PayoutResult.Failure(error)),
                    FakeDeviceRepository(),
                    FakeAuthenticationProvider(),
                ),
                IbanValidator(),
                AmountParser(),
            )
            vm.onAmountChange("400")
            vm.onIbanChange(validIban)
            vm.onConfirmClick()

            vm.onSubmitClick()

            assertEquals(reason, (vm.uiState.value.content as PayoutContent.Failed).reason)
        }
    }

    @Test
    fun `a device id failure shows the network reason and sends no payout`() {
        deviceRepository.result = DataResult.Failure(DataError.Network)
        fillValidForm()
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()

        assertEquals(PayoutContent.Failed.Reason.Network, (content as PayoutContent.Failed).reason)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `a large payout the user did not authenticate fails with the biometric reason and sends nothing`() {
        val cases = mapOf(
            AuthenticationResult.Cancelled to PayoutContent.Failed.Reason.BiometricCancelled,
            AuthenticationResult.LockedOut to PayoutContent.Failed.Reason.BiometricLockedOut,
            AuthenticationResult.NotEnrolled to PayoutContent.Failed.Reason.BiometricNotEnrolled,
            AuthenticationResult.Unavailable to PayoutContent.Failed.Reason.BiometricUnavailable,
            AuthenticationResult.Failed to PayoutContent.Failed.Reason.BiometricFailed,
        )
        cases.forEach { (outcome, reason) ->
            authenticationProvider.result = outcome
            viewModel.onCreateAnotherClick()
            fillValidForm("1500")
            viewModel.onConfirmClick()

            viewModel.onSubmitClick()

            assertEquals(reason, (content as PayoutContent.Failed).reason)
        }
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `an authenticated large payout completes`() {
        fillValidForm("1500")
        viewModel.onConfirmClick()

        viewModel.onSubmitClick()

        assertEquals(1, authenticationProvider.calls)
        assertTrue(content is PayoutContent.Completed)
    }

    @Test
    fun `open settings sends the settings event`() = runTest {
        viewModel.onOpenSettingsClick()

        assertEquals(PayoutViewModel.Event.OpenBiometricSettings, viewModel.events.first())
    }

    @Test
    fun `try again keeps the form and create another resets it`() {
        repository.result = PayoutResult.Failure(PayoutError.Network)
        fillValidForm()
        viewModel.onConfirmClick()
        viewModel.onSubmitClick()

        assertTrue(content is PayoutContent.Failed)
        assertEquals("400", (content as PayoutContent.Failed).form.amountInput)

        viewModel.onTryAgainClick()
        assertTrue(content is PayoutContent.Editing)
        assertNull(editing.confirmation)
        assertEquals("400", editing.form.amountInput)
        assertTrue(editing.form.isConfirmEnabled)

        repository.result = PayoutResult.Success(payout)
        viewModel.onConfirmClick()
        viewModel.onSubmitClick()
        viewModel.onCreateAnotherClick()

        assertEquals(PayoutContent.Editing(), content)
    }

    @Test
    fun `a screenshot shows the reminder over the form until it is dismissed`() {
        fillValidForm()
        val form = content

        viewModel.onScreenshotTaken()
        assertTrue(viewModel.uiState.value.isScreenshotWarningVisible)
        assertEquals(form, content)

        viewModel.onScreenshotWarningDismiss()
        assertFalse(viewModel.uiState.value.isScreenshotWarningVisible)
        assertEquals(form, content)
    }

    @Test
    fun `a screenshot shows the reminder over a result too`() {
        fillValidForm()
        viewModel.onConfirmClick()
        viewModel.onSubmitClick()

        viewModel.onScreenshotTaken()

        assertTrue(viewModel.uiState.value.isScreenshotWarningVisible)
        assertTrue(content is PayoutContent.Completed)
    }

    @Test
    fun `the reminder is hidden initially`() {
        assertFalse(viewModel.uiState.value.isScreenshotWarningVisible)
    }
}
