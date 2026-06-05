package com.hamlog.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardVoice
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamlog.ui.theme.AlxPrimary
import com.hamlog.ui.theme.AlxPrimaryFixed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.hamlog.ui.theme.LocalSurfaceContainer
import com.hamlog.ui.theme.LocalSurfaceContainerHigh
import com.hamlog.ui.theme.LocalSurfaceContainerLow
import com.hamlog.ui.theme.LocalSurfaceContainerLowest
import com.hamlog.ui.theme.NotoSerif
import com.hamlog.util.BandUtil
import com.hamlog.util.CallSignUtils
import com.hamlog.util.EquipmentManager

private val rstOptions = listOf(
    "59","58","57","56","55","54","53","52","51",
    "49","48","47","46","45","44","43","42","41",
    "39","38","37","36","35","34","33"
)
private val powerOptions = listOf(
    "100W","50W","10W","5W","1000W","200W","300W","400W","500W",
    "15W","20W","25W","30W","35W","40W","45W","60W","70W","75W",
    "80W","85W","90W","95W","600W","700W","800W","900W","1500W","2000W"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartInputField(
    smartInput: String, callsign: String, frequency: String, mode: String,
    rstSent: String, rstReceived: String, powerTx: String, powerRx: String, notes: String,
    suggestions: List<String>, showSuggestions: Boolean,
    onInputChange: (String) -> Unit, onFieldChange: (String, String) -> Unit,
    onCommitNext: () -> Unit, onSave: () -> Unit,
    qsoTime: String = "",
    onSelectSuggestion: (String) -> Unit, onDismissSuggestions: () -> Unit,
    dismissKeyboards: Int = 0, modifier: Modifier = Modifier
) {
    val band = remember(frequency) {
        val mhz = frequency.toDoubleOrNull() ?: 0.0
        BandUtil.getBand(mhz)
    }

    val surfaceContainer       = LocalSurfaceContainer.current
    val surfaceContainerLow    = LocalSurfaceContainerLow.current
    val surfaceContainerHigh   = LocalSurfaceContainerHigh.current
    val surfaceContainerLowest = LocalSurfaceContainerLowest.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 三栏信息卡：频率 | 模式 | 波段 ──────────────────────────────────
        // bg-surface-container rounded-xl
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,   // rounded-xl ≈ 12dp
            color = surfaceContainer
        ) {
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPanel(
                    label = "频率",
                    value = if (frequency.isNotBlank()) "$frequency" else "--",
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
                InfoPanel(
                    label = "模式",
                    value = if (mode.isNotBlank()) mode else "--",
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
                InfoPanel(
                    label = "波段",
                    value = if (band.isNotBlank()) band else "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 主输入框 ──────────────────────────────────────────────────────────
        // bg-surface-container-lowest border-2 border-primary/20 focus:border-primary rounded-xl
        Box {
            OutlinedTextField(
                value = smartInput,
                onValueChange = { onInputChange(it.uppercase()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onDismissSuggestions() },
                placeholder = {
                    Text(
                        "呼号 频率 模式...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val province = CallSignUtils.getProvince(smartInput.split(" ").firstOrNull() ?: "")
                        if (province != null) {
                            Text(
                                province,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        if (smartInput.isNotBlank()) {
                            IconButton(
                                onClick = onCommitNext,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardReturn,
                                    contentDescription = "确认",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = NotoSerif,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { onCommitNext() }),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    focusedContainerColor   = surfaceContainerLowest,
                    unfocusedContainerColor = surfaceContainerLowest
                )
            )
        }

        // Suggestions dropdown
        if (showSuggestions && suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column {
                    suggestions.forEach { s ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSuggestion(s); onDismissSuggestions() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(s,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = NotoSerif,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // ── RST 发 + RST 收 ───────────────────────────────────────────────────
        var showSentKb by remember { mutableStateOf(false) }
        var showRecvKb by remember { mutableStateOf(false) }
        var showPtxKb by remember { mutableStateOf(false) }
        var showPrxKb by remember { mutableStateOf(false) }
        LaunchedEffect(dismissKeyboards) {
            if (dismissKeyboards > 0) {
                showSentKb = false; showRecvKb = false
                showPtxKb = false; showPrxKb = false
                focusManager.clearFocus()
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlxLabel("我的信号报告")
                Surface(
                    modifier = Modifier.fillMaxWidth().height(36.dp).focusable().onFocusChanged { if (!it.isFocused) showSentKb = false }.clickable { if (showSentKb) showSentKb = false else { showSentKb = true; showRecvKb = false; showPtxKb = false; showPrxKb = false } },
                    shape = MaterialTheme.shapes.small,
                    color = surfaceContainerLow
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(rstSent, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                RstKeyboard(
                    value = rstSent,
                    onValueChange = { onFieldChange("rstSent", it) },
                    visible = showSentKb,
                    onDone = { showSentKb = false }
                )
                QuickChipRow(
                    items = listOf("59", "58", "57", "56"),
                    selected = rstSent,
                    containerHigh = surfaceContainerHigh,
                    onSelect = { onFieldChange("rstSent", it); showSentKb = false },
                    gradientStart = Color(0xFF01D00D), gradientEnd = Color(0xFFF9A825)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlxLabel("对方信号报告")
                Surface(
                    modifier = Modifier.fillMaxWidth().height(36.dp).focusable().onFocusChanged { if (!it.isFocused) showRecvKb = false }.clickable { if (showRecvKb) showRecvKb = false else { showRecvKb = true; showSentKb = false; showPtxKb = false; showPrxKb = false } },
                    shape = MaterialTheme.shapes.small,
                    color = surfaceContainerLow
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(rstReceived, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                RstKeyboard(
                    value = rstReceived,
                    onValueChange = { onFieldChange("rstReceived", it) },
                    visible = showRecvKb,
                    onDone = { showRecvKb = false }
                )
                QuickChipRow(
                    items = listOf("59", "58", "57", "56"),
                    selected = rstReceived,
                    containerHigh = surfaceContainerHigh,
                    onSelect = { onFieldChange("rstReceived", it); showRecvKb = false },
                    gradientStart = Color(0xFF01D00D), gradientEnd = Color(0xFFF9A825)
                )
            }
        }

        // ── 我的功率 + 对方功率 ───────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlxLabel("我的功率 (W)")
                Surface(
                    modifier = Modifier.fillMaxWidth().height(36.dp).focusable().onFocusChanged { if (!it.isFocused) showPtxKb = false }.clickable { if (showPtxKb) showPtxKb = false else { showPtxKb = true; showPrxKb = false; showSentKb = false; showRecvKb = false } },
                    shape = MaterialTheme.shapes.small,
                    color = surfaceContainerLow
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(powerTx, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                PowerKeyboard(
                    value = powerTx,
                    onValueChange = { onFieldChange("powerTx", it) },
                    visible = showPtxKb,
                    onDone = { showPtxKb = false }
                )
                QuickChipRow(
                    items = listOf("5", "10", "50", "100"),
                    selected = powerTx,
                    containerHigh = surfaceContainerHigh,
                    onSelect = { onFieldChange("powerTx", it); showPtxKb = false },
                    gradientStart = Color(0xFF01D00D), gradientEnd = Color(0xFFC62828)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlxLabel("对方功率 (W)")
                Surface(
                    modifier = Modifier.fillMaxWidth().height(36.dp).focusable().onFocusChanged { if (!it.isFocused) showPrxKb = false }.clickable { if (showPrxKb) showPrxKb = false else { showPrxKb = true; showPtxKb = false; showSentKb = false; showRecvKb = false } },
                    shape = MaterialTheme.shapes.small,
                    color = surfaceContainerLow
                ) {
                    Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(powerRx, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                PowerKeyboard(
                    value = powerRx,
                    onValueChange = { onFieldChange("powerRx", it) },
                    visible = showPrxKb,
                    onDone = { showPrxKb = false }
                )
                QuickChipRow(
                    items = listOf("5", "10", "50", "100"),
                    selected = powerRx,
                    containerHigh = surfaceContainerHigh,
                    onSelect = { onFieldChange("powerRx", it); showPrxKb = false },
                    gradientStart = Color(0xFF01D00D), gradientEnd = Color(0xFFC62828)
                )
            }
        }

        // ── 备注 ──────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = notes,
            onValueChange = { onFieldChange("notes", it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
            maxLines = 2,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                focusedContainerColor   = surfaceContainerLowest,
                unfocusedContainerColor = surfaceContainerLowest,
                focusedLabelColor   = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Quick note chips
        NotesChips(notes, surfaceContainerHigh) { onFieldChange("notes", it) }

        Spacer(Modifier.height(4.dp))
    }
}

// ── InfoPanel ─────────────────────────────────────────────────────────────────
// Replicates HTML: text-center, label font-label text-[9px] uppercase tracking-widest
//                  value font-headline text-lg font-bold text-primary
@Composable
private fun InfoPanel(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

// ── AlxLabel ─────────────────────────────────────────────────────────────────
// font-label text-[10px] uppercase tracking-widest text-on-surface-variant
@Composable
private fun AlxLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 2.dp)
    )
}

// ── AlxDropdown ──────────────────────────────────────────────────────────────
// Compact height matching HTML `py-1.5` select:
// Surface with fixed height 36dp, no border, rounded-lg (shapes.small)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlxDropdown(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    realOptions: List<String>? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val resolvedOptions = realOptions ?: options

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .menuAnchor(),
            shape = MaterialTheme.shapes.small,
            color = containerColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    trailingIcon?.invoke()
                    Icon(
                        imageVector = if (expanded)
                            androidx.compose.material.icons.Icons.Default.KeyboardArrowUp
                        else
                            androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { i, display ->
                val real = resolvedOptions.getOrElse(i) { display }
                DropdownMenuItem(
                    text = {
                        Text(
                            display,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (display == value) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(real); expanded = false },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ── QuickChipRow ─────────────────────────────────────────────────────────────
// HTML: flex gap-1, each chip px-2 py-1 rounded-lg, evenly fills row width
// selected: bg-primary-container/20 text-primary font-semibold
// default:  bg-surface-container-high text-on-surface-variant
@Composable
private fun QuickChipRow(
    items: List<String>,
    selected: String,
    containerHigh: androidx.compose.ui.graphics.Color,
    onSelect: (String) -> Unit,
    gradientStart: Color? = null,
    gradientEnd: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { i, v ->
            val isSel = v == selected
            val chipColor = if (gradientStart != null && gradientEnd != null) {
                lerp(gradientStart, gradientEnd, i.toFloat() / (items.size - 1).coerceAtLeast(1))
            } else AlxPrimary
            val bg = if (isSel) chipColor.copy(alpha = 0.5f) else containerHigh
            val fg = if (isSel) chipColor else MaterialTheme.colorScheme.onSurfaceVariant
            Surface(
                onClick = { onSelect(v) },
                shape = MaterialTheme.shapes.small,
                color = bg,
                border = if (isSel) BorderStroke(1.dp, chipColor.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1f).height(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        v,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = NotoSerif,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                        color = fg
                    )
                }
            }
        }
    }
}

// ── NotesChips ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesChips(
    currentNotes: String,
    containerHigh: androidx.compose.ui.graphics.Color,
    onNotesChange: (String) -> Unit
) {
    val selectedAntenna = remember { mutableStateOf("") }
    val selectedRig     = remember { mutableStateOf("") }
    val equipmentCategories = EquipmentManager.getRigs().map { it.brand to it.models }

    @Composable
    fun noteChip(tag: String, isSelected: Boolean, onClick: () -> Unit) {
        val bg = if (isSelected) AlxPrimaryFixed.copy(alpha = 0.45f) else containerHigh
        val fg = if (isSelected) AlxPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            color = bg,
            modifier = Modifier.height(24.dp)
        ) {
            Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Text(tag, style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = fg)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            Text("天馈", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EquipmentManager.getAntennas().forEach { tag ->
                    noteChip(tag, tag == selectedAntenna.value) {
                        var r = currentNotes
                        EquipmentManager.getAntennas().forEach { r = r.replace(it, "") }
                        r = r.replace("  "," ").replace(" ,",",").replace(", ,",",").trim().trim(',').trim()
                        if (tag == selectedAntenna.value) {
                            selectedAntenna.value = ""
                            onNotesChange(r)
                        } else {
                            selectedAntenna.value = tag
                            onNotesChange(if (r.isBlank()) tag else r + ", " + tag)
                        }
                    }
                }
            }
        }
        Column {
            Text("设备", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            equipmentCategories.forEach { (cat, models) ->
                Column {
                    Text(cat, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(2.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        models.forEach { model ->
                            noteChip(model, model == selectedRig.value) {
                                var r = currentNotes
                                equipmentCategories.forEach { (_, all) -> all.forEach { r = r.replace(it, "") } }
                                r = r.replace("  "," ").replace(" ,",",").replace(", ,",",").trim().trim(',').trim()
                                if (model == selectedRig.value) {
                                    selectedRig.value = ""
                                    onNotesChange(r)
                                } else {
                                    selectedRig.value = model
                                    onNotesChange(if (r.isBlank()) model else r + ", " + model)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
