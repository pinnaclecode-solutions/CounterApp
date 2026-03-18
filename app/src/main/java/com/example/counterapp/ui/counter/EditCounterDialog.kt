package com.example.counterapp.ui.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCounterDialog(
    currentCount: Int,
    currentTimeMs: Long,
    onSave: (count: Int, timeMs: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalSeconds = currentTimeMs / 1000
    var countText by remember { mutableStateOf(currentCount.toString()) }
    var hoursText by remember { mutableStateOf((totalSeconds / 3600).toString()) }
    var minutesText by remember { mutableStateOf(((totalSeconds % 3600) / 60).toString()) }
    var secondsText by remember { mutableStateOf((totalSeconds % 60).toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Edit Counter",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Count field
            OutlinedTextField(
                value = countText,
                onValueChange = { countText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time fields
            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secondsText,
                    onValueChange = { secondsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Sec") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val count = countText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val hours = hoursText.toLongOrNull()?.coerceAtLeast(0) ?: 0
                    val minutes = minutesText.toLongOrNull()?.coerceAtLeast(0) ?: 0
                    val seconds = secondsText.toLongOrNull()?.coerceAtLeast(0) ?: 0
                    val timeMs = (hours * 3600 + minutes * 60 + seconds) * 1000
                    onSave(count, timeMs)
                }) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
