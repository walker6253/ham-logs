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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamlog.ui.theme.AlxPrimary

@Composable
fun RstKeyboard(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onDone: () -> Unit
) {
    LaunchedEffect(visible) {
        if (visible) onValueChange("")
    }

    // Inline keyboard
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
                RstKeyRow((1..5).toList(), 5) { appendDigit(value, onValueChange, it) }
                RstKeyRow((1..5).toList(), 5) { appendDigit(value, onValueChange, it) }
                RstKeyRow((6..9).toList(), 4) { appendDigit(value, onValueChange, it) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            if (value.isEmpty()) onValueChange("59")
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
    if (value.length < 2) onChange(value + digit)
}

@Composable
private fun RstKeyRow(digits: List<Int>, total: Int, onDigit: (String) -> Unit) {
    val red = Color(0xFFE53935)
    val green = Color(0xFF43A047)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        digits.forEachIndexed { i, d ->
            val fraction = if (total > 1) i.toFloat() / (total - 1) else 0f
            val keyColor = lerp(red, green, fraction)
            Surface(
                onClick = { onDigit(d.toString()) },
                shape = RoundedCornerShape(6.dp),
                color = keyColor.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(d.toString(), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold), color = keyColor)
                }
            }
        }
    }
}
