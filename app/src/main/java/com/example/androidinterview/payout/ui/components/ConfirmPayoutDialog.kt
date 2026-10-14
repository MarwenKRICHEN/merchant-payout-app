package com.example.androidinterview.payout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.androidinterview.R
import com.example.androidinterview.money.format
import com.example.androidinterview.payout.ui.ConfirmationUi
import com.example.androidinterview.payout.ui.maskIban

/**
 * Dialog summarising the payout before it is sent.
 *
 * @param confirmation What is being confirmed and whether the request is in flight.
 * @param onConfirm Called when the user taps Confirm.
 * @param onCancel Called when the user taps Cancel or dismisses the dialog.
 */
@Composable
fun ConfirmPayoutDialog(
    confirmation: ConfirmationUi,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val amount = remember(confirmation.amount) { confirmation.amount.format() }
    val iban = remember(confirmation.iban) { confirmation.iban.maskIban() }
    val sending = stringResource(R.string.payout_sending)
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = !confirmation.isSubmitting,
            dismissOnClickOutside = !confirmation.isSubmitting,
        ),
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.payout_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() },
                )
                SummaryRow(stringResource(R.string.payout_confirm_amount), amount)
                SummaryRow(stringResource(R.string.payout_confirm_currency), confirmation.amount.currency.name)
                SummaryRow(stringResource(R.string.payout_confirm_iban), iban)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalButton(
                        onClick = onCancel,
                        enabled = !confirmation.isSubmitting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.payout_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !confirmation.isSubmitting,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (confirmation.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .semantics { contentDescription = sending },
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.payout_confirm))
                        }
                    }
                }
            }
        }
    }
}

/**
 * One label/value line of the summary, followed by a divider.
 *
 * @param label What the value is.
 * @param value The value, shown in bold.
 */
@Composable
private fun SummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
