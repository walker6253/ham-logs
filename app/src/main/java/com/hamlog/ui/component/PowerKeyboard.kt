package com.hamlog.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamlog.ui.theme.AlxPrimary

/**
 * Custom power keyboard.
 * Layout: 1-2-3 / 4-5-6 / 7-8-9 / 0 + Clear + Delete + Done
 * Max 4 digits. Popup scrim for outside-click dismiss.
 */
@Composable
fun PowerKeyboard(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onDone: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible) onValueChange("")
    }

AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PowerKeyRow(listOf("1","2","3")) { appendDigit(value, onValueChange, it) }
                PowerKeyRow(listOf("4","5","6")) { appendDigit(value, onValueChange, it) }
                PowerKeyRow(listOf("7","8","9")) { appendDigit(value, onValueChange, it) }
                // Row 4: 0 + Clear + Delete + Done
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = { appendDigit(value, onValueChange, "0") },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("0", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    OutlinedButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("清空", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium) }
                    OutlinedButton(
                        onClick = { if (value.isNotEmpty()) onValueChange(value.dropLast(1)) },
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("删除", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium) }
                    FilledTonalButton(
                        onClick = {
                            if (value.isEmpty()) onValueChange("100")
                            onDone()
                        },
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AlxPrimary.copy(alpha = 0.15f), contentColor = AlxPrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("完成", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

private fun appendDigit(value: String, onChange: (String) -> Unit, digit: String) {
    if (value.length < 4) onChange(value + digit)
}

@Composable
private fun PowerKeyRow(digits: List<String>, onDigit: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        digits.forEach { d ->
            Surface(
                onClick = { onDigit(d) },
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(d, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
