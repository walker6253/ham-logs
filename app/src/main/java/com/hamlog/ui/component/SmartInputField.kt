package com.hamlog.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamlog.util.ParsedFields

@Composable
fun SmartInputField(
    smartInput: String,
    parsedFields: ParsedFields,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "智能录入",
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
                Text("切换独立输入", style = MaterialTheme.typography.labelSmall)
            }
        }

        OutlinedTextField(
            value = smartInput,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入呼号 频率 模式 RST... (空格分隔)") },
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (parsedFields.callsign.isNotBlank()) {
                        onSave()
                    }
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = onSave,
                    enabled = parsedFields.callsign.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "保存",
                        tint = if (parsedFields.callsign.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ParsedFieldRow("呼号", parsedFields.callsign)
                ParsedFieldRow("频率", if (parsedFields.frequencyMHz.isNotBlank()) "${parsedFields.frequencyMHz} MHz" else "")
                ParsedFieldRow("模式", parsedFields.mode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ParsedFieldRow("RST 发", parsedFields.rstSent, Modifier.weight(1f))
                    ParsedFieldRow("RST 收", parsedFields.rstReceived, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ParsedFieldRow("发射功率", parsedFields.powerTx, Modifier.weight(1f))
                    ParsedFieldRow("接收功率", parsedFields.powerRx, Modifier.weight(1f))
                }
                if (parsedFields.notes.isNotBlank()) {
                    ParsedFieldRow("备注", parsedFields.notes)
                }
            }
        }
    }
}

@Composable
private fun ParsedFieldRow(label: String, value: String, modifier: Modifier = Modifier) {
    if (value.isNotBlank()) {
        Row(modifier = modifier.padding(vertical = 2.dp)) {
            Text(
                text = "${label}: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = if (label == "呼号") FontFamily.Monospace else FontFamily.Default,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
