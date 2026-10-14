package com.example.androidinterview.payout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidinterview.R
import com.example.androidinterview.money.Money
import com.example.androidinterview.money.format
import com.example.androidinterview.payout.ui.PayoutUiState
import com.example.androidinterview.ui.theme.Green
import com.example.androidinterview.ui.theme.Red

/**
 * Centred icon, title, message and action describing the outcome of a payout.
 *
 * @param icon Drawable of the outcome icon.
 * @param tint Colour of the icon, title and message.
 * @param title Outcome title.
 * @param message Outcome details.
 * @param actionLabel Label of the single action.
 * @param onAction Called when the user taps the action.
 * @param modifier Modifier applied to the container.
 * @param secondaryActionLabel Label of an optional action shown above the main one.
 * @param onSecondaryAction Called when the user taps the secondary action.
 */
@Composable
private fun ResultContent(
    icon: Int,
    tint: Color,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryActionLabel: String? = null,
    onSecondaryAction: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(72.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (tint == Green) MaterialTheme.colorScheme.onSurface else tint,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (tint == Green) MaterialTheme.colorScheme.onSurfaceVariant else tint,
                textAlign = TextAlign.Center,
            )
        }
        if (secondaryActionLabel != null) {
            Button(onClick = onSecondaryAction) {
                Text(secondaryActionLabel)
            }
        }
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

/**
 * The payout was accepted.
 *
 * @param amount Amount that was sent.
 * @param onCreateAnother Called when the user wants to send another payout.
 * @param modifier Modifier applied to the container.
 */
@Composable
fun PayoutCompletedContent(
    amount: Money,
    onCreateAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatted = remember(amount) { amount.format() }
    ResultContent(
        icon = R.drawable.ic_check_circle,
        tint = Green,
        title = stringResource(R.string.payout_completed_title),
        message = stringResource(R.string.payout_completed_message, formatted),
        actionLabel = stringResource(R.string.payout_create_another),
        onAction = onCreateAnother,
        modifier = modifier,
    )
}

/**
 * The payout failed.
 *
 * @param reason Why it failed.
 * @param onTryAgain Called when the user wants to go back to the form.
 * @param onOpenSettings Called when the user wants to set up a biometric, offered when none is.
 * @param modifier Modifier applied to the container.
 */
@Composable
fun PayoutFailedContent(
    reason: PayoutUiState.Failed.Reason,
    onTryAgain: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ResultContent(
        icon = R.drawable.ic_close,
        tint = Red,
        title = stringResource(R.string.payout_failed_title),
        message = reason.message(),
        actionLabel = stringResource(R.string.payout_try_again),
        onAction = onTryAgain,
        modifier = modifier,
        secondaryActionLabel = if (reason == PayoutUiState.Failed.Reason.BiometricNotEnrolled) {
            stringResource(R.string.payout_open_settings)
        } else {
            null
        },
        onSecondaryAction = onOpenSettings,
    )
}

/** Localised text for this failure reason. */
@Composable
private fun PayoutUiState.Failed.Reason.message(): String = when (this) {
    PayoutUiState.Failed.Reason.Network -> stringResource(R.string.error_network)
    PayoutUiState.Failed.Reason.ServiceUnavailable -> stringResource(R.string.payout_error_service_unavailable)
    PayoutUiState.Failed.Reason.InsufficientFunds -> stringResource(R.string.payout_error_insufficient_funds)
    PayoutUiState.Failed.Reason.BiometricCancelled -> stringResource(R.string.payout_error_biometric_cancelled)
    PayoutUiState.Failed.Reason.BiometricLockedOut -> stringResource(R.string.payout_error_biometric_locked_out)
    PayoutUiState.Failed.Reason.BiometricNotEnrolled -> stringResource(R.string.payout_error_biometric_not_enrolled)
    PayoutUiState.Failed.Reason.BiometricUnavailable -> stringResource(R.string.payout_error_biometric_unavailable)
    PayoutUiState.Failed.Reason.BiometricFailed -> stringResource(R.string.payout_error_biometric_failed)
    PayoutUiState.Failed.Reason.Unexpected -> stringResource(R.string.error_unexpected)
}
