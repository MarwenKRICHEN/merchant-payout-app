package com.example.androidinterview.payout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.androidinterview.money.Currency

/**
 * Read-only field that opens a menu of the supported currencies.
 *
 * @param selected Currently selected currency.
 * @param onSelected Called with the currency the user picked.
 * @param modifier Modifier applied to the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    selected: Currency,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.name) },
                    onClick = {
                        onSelected(currency)
                        expanded = false
                    },
                )
            }
        }
    }
}
