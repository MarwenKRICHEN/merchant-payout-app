package com.example.androidinterview.payout.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.androidinterview.R

/**
 * Reminds the user to keep their financial data private after they took a screenshot.
 *
 * @param onDismiss Called when the user acknowledges or dismisses the reminder.
 */
@Composable
fun ScreenshotWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.payout_screenshot_warning_title)) },
        text = { Text(stringResource(R.string.payout_screenshot_warning_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}
