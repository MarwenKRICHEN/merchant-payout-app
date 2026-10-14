package com.example.androidinterview.merchant.ui.activity.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidinterview.R
import com.example.androidinterview.core.format.formatShort
import com.example.androidinterview.merchant.domain.model.ActivityStatus
import com.example.androidinterview.merchant.domain.model.ActivityType
import com.example.androidinterview.merchant.ui.activity.ActivityItemUi
import com.example.androidinterview.money.Currency
import com.example.androidinterview.money.Money
import com.example.androidinterview.money.format
import com.example.androidinterview.ui.theme.AndroidInterviewTheme
import com.example.androidinterview.ui.theme.Green
import com.example.androidinterview.ui.theme.Red
import java.time.LocalDate

/**
 * Activity item: type, description and date on the left; coloured amount and status on the right.
 *
 * @param item Data to render.
 * @param modifier Modifier applied to the row.
 */
@Composable
fun ActivityListItem(item: ActivityItemUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.type.label(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = remember(item.date) { item.date.formatShort() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = remember(item.amount) { item.amount.format() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.amount.minorUnits < 0) Red else Green,
            )
            Text(
                text = item.status.label(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Localised label of the activity type. */
@Composable
private fun ActivityType.label(): String = stringResource(
    when (this) {
        ActivityType.DEPOSIT -> R.string.activity_type_deposit
        ActivityType.PAYOUT -> R.string.activity_type_payout
        ActivityType.REFUND -> R.string.activity_type_refund
        ActivityType.FEE -> R.string.activity_type_fee
    },
)

/** Localised label of the activity status. */
@Composable
private fun ActivityStatus.label(): String = stringResource(
    when (this) {
        ActivityStatus.COMPLETED -> R.string.activity_status_completed
        ActivityStatus.PENDING -> R.string.activity_status_pending
        ActivityStatus.PROCESSING -> R.string.activity_status_processing
        ActivityStatus.FAILED -> R.string.activity_status_failed
    },
)

/** Preview of a completed inflow item: green amount. */
@Preview(showBackground = true)
@Composable
private fun ActivityListItemInflowPreview() {
    AndroidInterviewTheme {
        ActivityListItem(
            item = ActivityItemUi(
                id = "1",
                type = ActivityType.DEPOSIT,
                description = "Payment from Customer MYK",
                amount = Money(72738, Currency.GBP),
                date = LocalDate.of(2026, 1, 23),
                status = ActivityStatus.COMPLETED,
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

/** Preview of a pending outflow item: red amount. */
@Preview(showBackground = true)
@Composable
private fun ActivityListItemOutflowPreview() {
    AndroidInterviewTheme {
        ActivityListItem(
            item = ActivityItemUi(
                id = "2",
                type = ActivityType.PAYOUT,
                description = "Payout to Bank Account ****1234",
                amount = Money(-50000, Currency.GBP),
                date = LocalDate.of(2026, 1, 21),
                status = ActivityStatus.PENDING,
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
