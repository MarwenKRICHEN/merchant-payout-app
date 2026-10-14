package com.example.androidinterview.merchant.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidinterview.R
import com.example.androidinterview.core.format.format
import com.example.androidinterview.merchant.domain.model.Money

/**
 * Available and pending balances side by side.
 *
 * @param availableBalance Balance available for payouts.
 * @param pendingBalance Balance not yet settled.
 * @param modifier Modifier applied to the row.
 */
@Composable
fun BalanceSection(
    availableBalance: Money,
    pendingBalance: Money,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BalanceItem(
            label = stringResource(R.string.balance_available),
            amount = availableBalance,
            modifier = Modifier.weight(1f),
        )
        BalanceItem(
            label = stringResource(R.string.balance_pending),
            amount = pendingBalance,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * A label above an amount, read by accessibility services as one item.
 *
 * @param label What the amount represents.
 * @param amount Amount to format and show.
 * @param modifier Modifier applied to the column.
 */
@Composable
private fun BalanceItem(label: String, amount: Money, modifier: Modifier = Modifier) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = remember(amount) { amount.format() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
