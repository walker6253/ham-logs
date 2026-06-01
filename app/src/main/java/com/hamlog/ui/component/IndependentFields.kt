package com.hamlog.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndependentFields(
    callsign: String,
    frequency: String,
    mode: String,
    rstSent: String,
    rstReceived: String,
    powerTx: String,
    powerRx: String,
    notes: String,
    onFieldChange: (String, String) -> Unit,
    onSave: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeOptions = listOf("SSB", "CW", "FM", "AM", "RTTY", "FT8", "FT4", "USB", "LSB")
    var modeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "独立录入",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onToggleMode) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("切换智能输入", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = callsign,
            onValueChange = { onFieldChange("callsign", it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("呼号") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = frequency,
            onValueChange = { onFieldChange("frequency", it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("频率 (MHz)") },
            singleLine = true,
            placeholder = { Text("如 14.200") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mode dropdown
        ExposedDropdownMenuBox(
            expanded = modeExpanded,
            onExpandedChange = { modeExpanded = it }
        ) {
            OutlinedTextField(
                value = mode,
                onValueChange = { onFieldChange("mode", it) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                label = { Text("模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = modeExpanded,
                onDismissRequest = { modeExpanded = false }
            ) {
                modeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onFieldChange("mode", option)
                            modeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rstSent,
                onValueChange = { onFieldChange("rstSent", it) },
                modifier = Modifier.weight(1f),
                label = { Text("RST 发") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = rstReceived,
                onValueChange = { onFieldChange("rstReceived", it) },
                modifier = Modifier.weight(1f),
                label = { Text("RST 收") },
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = powerTx,
                onValueChange = { onFieldChange("powerTx", it) },
                modifier = Modifier.weight(1f),
                label = { Text("发射功率") },
                singleLine = true,
                placeholder = { Text("如 100W") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = powerRx,
                onValueChange = { onFieldChange("powerRx", it) },
                modifier = Modifier.weight(1f),
                label = { Text("接收功率") },
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { onFieldChange("notes", it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = callsign.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存通联记录")
        }
    }
}
