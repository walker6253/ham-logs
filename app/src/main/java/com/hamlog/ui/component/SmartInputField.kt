package com.hamlog.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.hamlog.util.BandUtil

private val rstOptions = listOf("59","58","57","56","55","54","53","52","51","49","48","47",
    "46","45","44","43","42","41","39","38","37","36","35","34","33")
private val powerOptions = listOf("100W","50W","10W","5W","1000W","200W","300W","400W","500W",
    "15W","20W","25W","30W","35W","40W","45W","60W","70W","75W","80W","85W","90W","95W",
    "600W","700W","800W","900W","1500W","2000W")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartInputField(
    smartInput: String, callsign: String, frequency: String, mode: String,
    rstSent: String, rstReceived: String, powerTx: String, powerRx: String, notes: String,
    suggestions: List<String>, showSuggestions: Boolean,
    onInputChange: (String) -> Unit, onFieldChange: (String, String) -> Unit,
    onCommitNext: () -> Unit, onSave: () -> Unit, onToggleMode: () -> Unit,
    qsoTime: String = "",
    onSelectSuggestion: (String) -> Unit, onDismissSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val band = remember(frequency) {
        val mhz = frequency.toDoubleOrNull() ?: 0.0
        BandUtil.getBand(mhz)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                "智能录入",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(
                onClick = onToggleMode,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("独立", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(2.dp))

        // Band info bar
        Row(
            Modifier.fillMaxWidth().height(28.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BandChip(
                text = if (frequency.isNotBlank()) "$frequency MHz" else "频率 --",
                active = frequency.isNotBlank(),
                textColor = MaterialTheme.colorScheme.primary,
                bgColor = MaterialTheme.colorScheme.primaryContainer
            )
            BandChip(
                text = if (mode.isNotBlank()) mode else "模式 --",
                active = mode.isNotBlank(),
                textColor = MaterialTheme.colorScheme.secondary,
                bgColor = MaterialTheme.colorScheme.secondaryContainer
            )
            BandChip(
                text = if (band.isNotBlank()) band else "波段 --",
                active = band.isNotBlank(),
                textColor = MaterialTheme.colorScheme.tertiary,
                bgColor = MaterialTheme.colorScheme.tertiaryContainer
            )
            Spacer(Modifier.weight(1f))
            if (qsoTime.isNotBlank()) {
                Text(
                    qsoTime,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Smart input field — handles callsign + frequency parsing
        Box {
            OutlinedTextField(
                value = smartInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) onDismissSuggestions() },
                placeholder = {
                    Text(
                        "呼号 频率 模式...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                },
                singleLine = false,
                maxLines = 2,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { onCommitNext() }),
                shape = MaterialTheme.shapes.small,
                colors = hamFieldColors()
            )

            if (smartInput.isNotBlank()) {
                IconButton(
                    onClick = onCommitNext,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardReturn,
                        "确认",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Suggestions
        if (showSuggestions && suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column {
                    suggestions.forEach { s ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSuggestion(s)
                                    onDismissSuggestions()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                s,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        // RST 发 + RST 收
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                RstCompact(rstSent, { onFieldChange("rstSent", it) }, Modifier.fillMaxWidth(), "RST发")
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("59", "58", "57", "56", "55").forEach { v ->
                        val sel = v == rstSent
                        SuggestionChip(
                            onClick = { onFieldChange("rstSent", v) },
                            label = { Text(v, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                RstCompact(rstReceived, { onFieldChange("rstReceived", it) }, Modifier.fillMaxWidth(), "RST收")
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("59", "58", "57", "56", "55").forEach { v ->
                        val sel = v == rstReceived
                        SuggestionChip(
                            onClick = { onFieldChange("rstReceived", v) },
                            label = { Text(v, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        )
                    }
                }
            }
        }

        // 发射功率 + 接收功率
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                PowerDropdown(powerTx, { onFieldChange("powerTx", it) }, Modifier.fillMaxWidth(), "发射功率（W）")
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("5", "10", "50", "100", "1000").forEach { v ->
                        val sel = (v + "W") == powerTx
                        SuggestionChip(
                            onClick = { onFieldChange("powerTx", v + "W") },
                            label = { Text(v, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                PowerDropdown(powerRx, { onFieldChange("powerRx", it) }, Modifier.fillMaxWidth(), "接收功率（W）")
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("5", "10", "50", "100", "1000").forEach { v ->
                        val sel = (v + "W") == powerRx
                        SuggestionChip(
                            onClick = { onFieldChange("powerRx", v + "W") },
                            label = { Text(v, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        )
                    }
                }
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { onFieldChange("notes", it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
            maxLines = 2,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.small,
            colors = hamFieldColors()
        )

        Spacer(Modifier.height(6.dp))

        // Quick chips
        NotesChips(notes) { newNotes ->
            onFieldChange("notes", newNotes)
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BandChip(
    text: String,
    active: Boolean,
    textColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (active) bgColor.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (active) textColor
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RstCompact(
    value: String,
    onSelect: (String) -> Unit,
    modifier: Modifier,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onSelect(it) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            shape = MaterialTheme.shapes.small,
            colors = hamFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rstOptions.forEach { o ->
                DropdownMenuItem(
                    text = {
                        Text(
                            o,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (o == value) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(o); expanded = false },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PowerDropdown(
    value: String,
    onSelect: (String) -> Unit,
    modifier: Modifier,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value.replace("W", ""),
            onValueChange = { onSelect(it) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            shape = MaterialTheme.shapes.small,
            colors = hamFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            powerOptions.forEach { o ->
                DropdownMenuItem(
                    text = {
                        Text(
                            o.replace("W",""),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (o == value) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(o); expanded = false },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesChips(currentNotes: String, onNotesChange: (String) -> Unit) {
    val selectedAntenna = remember { mutableStateOf("") }
    val selectedRig = remember { mutableStateOf("") }
    val equipmentCategories = listOf(
        "ICOM" to listOf("IC-7300", "IC-705", "IC-9700", "IC-7610", "IC-9100"),
        "八重洲" to listOf("FT-891", "FT-710", "FT-818", "FT-991", "FTdx10", "FT-857", "FT-817"),
        "协谷" to listOf("G90", "X6100", "X5105", "X108G"),
        "其他" to listOf("KX3", "DX-10", "QRP Labs", "uSDX")
    )

    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        // 天馈
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("天馈：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                listOf("GP天线", "八木", "倒V", "正V", "长线", "DP", "端馈", "天调", "磁环").forEach { tag ->
                    val isSelected = tag == selectedAntenna.value
                    AssistChip(
                        onClick = {
                            selectedAntenna.value = tag
                            var result = currentNotes
                            for (t in listOf("GP天线", "八木", "倒V", "正V", "长线", "DP", "端馈", "天调", "磁环")) { result = result.replace(t, "") }
                            result = result.replace("  ", " ").replace(" ,", ",").replace(", ,", ",").trim().trim(',').trim()
                            val newNotes = if (result.isBlank()) tag else "$result, $tag"
                            onNotesChange(newNotes)
                        },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                    )
                }
            }
        }
        // 设备分类
        equipmentCategories.forEach { (category, models) ->
            Column {
                Text("$category：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 0.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(36.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        models.forEach { model ->
                            val isSelected = model == selectedRig.value
                            AssistChip(
                                onClick = {
                                    selectedRig.value = model
                                    var result = currentNotes
                                    // Remove any existing rig model from all categories
                                    for ((_, allModels) in equipmentCategories) { for (m in allModels) { result = result.replace(m, "") } }
                                    result = result.replace("  ", " ").replace(" ,", ",").replace(", ,", ",").trim().trim(',').trim()
                                    val newNotes = if (result.isBlank()) model else "$result, $model"
                                    onNotesChange(newNotes)
                                },
                                label = { Text(model, style = MaterialTheme.typography.labelSmall) },
                                shape = MaterialTheme.shapes.extraSmall,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun hamFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)