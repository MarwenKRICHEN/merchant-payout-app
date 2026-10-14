package com.example.androidinterview.payout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidinterview.R
import com.example.androidinterview.money.Currency
import com.example.androidinterview.payout.ui.components.ConfirmPayoutDialog
import com.example.androidinterview.payout.ui.components.CurrencyDropdown
import com.example.androidinterview.payout.ui.components.PayoutCompletedContent
import com.example.androidinterview.payout.ui.components.PayoutFailedContent

private val CURRENCY_FIELD_WIDTH = 140.dp

/**
 * Stateful entry point of the Payout screen: collects the ViewModel state and delegates to
 * [PayoutScreen].
 *
 * @param modifier Modifier applied to the screen.
 * @param viewModel ViewModel providing the screen state.
 */
@Composable
fun PayoutRoute(
    modifier: Modifier = Modifier,
    viewModel: PayoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PayoutScreen(
        state = state,
        onAmountChange = viewModel::onAmountChange,
        onCurrencyChange = viewModel::onCurrencyChange,
        onIbanChange = viewModel::onIbanChange,
        onConfirmClick = viewModel::onConfirmClick,
        onCancelClick = viewModel::onCancelClick,
        onSubmitClick = viewModel::onSubmitClick,
        onTryAgainClick = viewModel::onTryAgainClick,
        onCreateAnotherClick = viewModel::onCreateAnotherClick,
        modifier = modifier,
    )
}

/**
 * Stateless Payout screen: the form, its confirmation dialog, or the outcome of the payout.
 *
 * @param state What to render.
 * @param onAmountChange Called with the new amount text.
 * @param onCurrencyChange Called with the picked currency.
 * @param onIbanChange Called with the new IBAN text.
 * @param onConfirmClick Called when the user taps Confirm on the form.
 * @param onCancelClick Called when the user dismisses the confirmation dialog.
 * @param onSubmitClick Called when the user confirms in the dialog.
 * @param onTryAgainClick Called when the user taps Try Again on a failure.
 * @param onCreateAnotherClick Called when the user taps Create Another Payout.
 * @param modifier Modifier applied to the screen.
 */
@Composable
fun PayoutScreen(
    state: PayoutUiState,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onIbanChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onTryAgainClick: () -> Unit,
    onCreateAnotherClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        when (state) {
            is PayoutUiState.Completed -> {
                ScreenTitle(stringResource(R.string.payout_result_title))
                PayoutCompletedContent(state.amount, onCreateAnotherClick)
            }
            is PayoutUiState.Failed -> {
                BackHandler(onBack = onTryAgainClick)
                ScreenTitle(stringResource(R.string.payout_result_title))
                PayoutFailedContent(state.reason, onTryAgainClick)
            }
            is PayoutUiState.Editing -> {
                ScreenTitle(stringResource(R.string.payout_title))
                PayoutForm(
                    form = state.form,
                    onAmountChange = onAmountChange,
                    onCurrencyChange = onCurrencyChange,
                    onIbanChange = onIbanChange,
                    onConfirmClick = onConfirmClick,
                )
                state.confirmation?.let { confirmation ->
                    ConfirmPayoutDialog(
                        confirmation = confirmation,
                        onConfirm = onSubmitClick,
                        onCancel = onCancelClick,
                    )
                }
            }
        }
    }
}

/**
 * Large screen title, exposed as a heading to accessibility services.
 *
 * @param text Title text.
 */
@Composable
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 24.dp)
            .semantics { heading() },
    )
}

/**
 * Amount, currency and IBAN inputs with the Confirm button.
 *
 * @param form Current inputs and validation.
 * @param onAmountChange Called with the new amount text.
 * @param onCurrencyChange Called with the picked currency.
 * @param onIbanChange Called with the new IBAN text.
 * @param onConfirmClick Called when the user taps Confirm.
 */
@Composable
private fun PayoutForm(
    form: PayoutFormUi,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onIbanChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel(stringResource(R.string.payout_amount))
                OutlinedTextField(
                    value = form.amountInput,
                    onValueChange = onAmountChange,
                    placeholder = { Text(stringResource(R.string.payout_amount_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel(stringResource(R.string.payout_currency))
                CurrencyDropdown(
                    selected = form.currency,
                    onSelected = onCurrencyChange,
                    modifier = Modifier.width(CURRENCY_FIELD_WIDTH),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(stringResource(R.string.payout_iban))
            OutlinedTextField(
                value = form.ibanInput,
                onValueChange = onIbanChange,
                singleLine = true,
                isError = form.showIbanError,
                supportingText = {
                    Text(
                        stringResource(
                            if (form.showIbanError) R.string.payout_iban_invalid else R.string.payout_iban_hint,
                        ),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onConfirmClick,
            enabled = form.isConfirmEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.payout_confirm))
        }
    }
}

/**
 * Bold label above an input.
 *
 * @param text Label text.
 */
@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
