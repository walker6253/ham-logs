package com.hamlog.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import com.hamlog.ui.component.ContactListItem
import com.hamlog.ui.component.AlxDatePickerDialog
import com.hamlog.data.entity.ContactRecord
import com.hamlog.ui.component.SmartInputField
import com.hamlog.viewmodel.LogEntryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId as JavaZoneId
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import java.util.TimeZone
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.util.CallSignUtils
import com.hamlog.ui.theme.NotoSerif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    dateEpochDay: Long,
    viewModel: LogEntryViewModel,
    onNavigateBack: () -> Unit
) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 24.dp
        WindowWidthSizeClass.Medium -> 16.dp
        else -> 12.dp
    }
    val isWide = widthClass != WindowWidthSizeClass.Compact
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(dateEpochDay) { viewModel.init(dateEpochDay) }

    var showSuccessBanner by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactRecord?>(null) }

    LaunchedEffect(uiState.showSavedToast) {
        if (uiState.showSavedToast) {
            showSuccessBanner = true
            kotlinx.coroutines.delay(2000)
            showSuccessBanner = false
            viewModel.clearSavedToast()
        }
    }

    // Delete confirmation
    uiState.showDeleteConfirm?.let { contact ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("删除记录", fontWeight = FontWeight.SemiBold) },
            text = { Text("确定删除 ${contact.callsign} 的通联？", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete() }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("取消") } }
        )
    }

    val tz = remember(uiState.timezone) { TimeZone.getTimeZone(uiState.timezone) }
    val historical = uiState.historicalContacts
    val displayContacts = historical ?: uiState.contacts
    val isHistorical = historical != null

    // Edit dialog
    editingContact?.let { contact ->
        var editCallsign by remember(contact.id) { mutableStateOf(contact.callsign) }
        var editFreq by remember(contact.id) { mutableStateOf(if (contact.frequencyMHz > 0) contact.frequencyMHz.toString() else "") }
        var editMode by remember(contact.id) { mutableStateOf(contact.mode) }
        var editRstSent by remember(contact.id) { mutableStateOf(contact.rstSent) }
        var editRstRecv by remember(contact.id) { mutableStateOf(contact.rstReceived) }
        var editPowerTx by remember(contact.id) { mutableStateOf(contact.powerTx.trimEnd('W', 'w').trim()) }
        var editPowerRx by remember(contact.id) { mutableStateOf(contact.powerRx.trimEnd('W', 'w').trim()) }
        var editNotes by remember(contact.id) { mutableStateOf(contact.notes) }
        var editDateEpochDay by remember(contact.id) { mutableStateOf(contact.dateEpochDay) }
        var editCreatedAt by remember(contact.id) { mutableStateOf(contact.createdAt) }
        var showEditDatePicker by remember { mutableStateOf(false) }
        var showEditTimePicker by remember { mutableStateOf(false) }
        val editTimeLabel = remember(editCreatedAt) { val t = Instant.ofEpochMilli(editCreatedAt).atZone(JavaZoneId.of("Asia/Shanghai")); String.format("%02d:%02d:%02d", t.hour, t.minute, t.second) }
        var modeExpanded by remember { mutableStateOf(false) }
        val modeOptions = listOf("USB", "LSB", "FM")
        val editDateStr = remember(editDateEpochDay) {
            val d = LocalDate.ofEpochDay(editDateEpochDay)
            "${d.year}-${d.monthValue.toString().padStart(2, '0')}-${d.dayOfMonth.toString().padStart(2, '0')}"
        }
        val editTimeStr = remember(editCreatedAt) {
            val t = Instant.ofEpochMilli(editCreatedAt).atZone(JavaZoneId.of("Asia/Shanghai"))
            String.format("%02d:%02d:%02d", t.hour, t.minute, t.second)
        }

        if (showEditDatePicker) {
            AlxDatePickerDialog(
                initialEpochDay = editDateEpochDay,
                onDismiss = { showEditDatePicker = false },
                onConfirm = { epochDay ->
                    showEditDatePicker = false
                    editDateEpochDay = epochDay
                }
            )
        }

        Dialog(
    onDismissRequest = { editingContact = null },
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(onClick = { editingContact = null }), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.widthIn(max = 448.dp).padding(16.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 24.dp) {
            Column {
                Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 14.dp)) {
                    Text("编辑通联", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("QSO Editor", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("日期", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(editDateStr, {}, Modifier.fillMaxWidth(), readOnly = true, singleLine = true, enabled = false, textStyle = MaterialTheme.typography.bodySmall, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = Color.Transparent, disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow), trailingIcon = { IconButton(onClick = { showEditDatePicker = true }) { Text("\uD83D\uDCC5", style = MaterialTheme.typography.bodySmall) } })
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("时间", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editTimeLabel, {}, Modifier.fillMaxWidth(), readOnly = true, singleLine = true, enabled = false, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = Color.Transparent, disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow), trailingIcon = { IconButton(onClick = { showEditTimePicker = true }) { Text("\uD83D\uDD50", style = MaterialTheme.typography.bodySmall) } })
                        }
                    }

                    if (showEditTimePicker) {
                        val ct = Instant.ofEpochMilli(editCreatedAt).atZone(JavaZoneId.of("Asia/Shanghai"))
                        val tp = rememberTimePickerState(initialHour = ct.hour, initialMinute = ct.minute, is24Hour = true)
                        AlertDialog(onDismissRequest = { showEditTimePicker = false }, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text("选择时间") }, text = { TimePicker(state = tp) }, confirmButton = { TextButton(onClick = { showEditTimePicker = false; val d = LocalDate.ofEpochDay(editDateEpochDay); editCreatedAt = d.atTime(tp.hour, tp.minute, 0).atZone(JavaZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli() }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showEditTimePicker = false }) { Text("取消") } })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("呼号", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                        OutlinedTextField(editCallsign, { editCallsign = it.uppercase() }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = NotoSerif, fontWeight = FontWeight.Bold), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow), trailingIcon = { val p = CallSignUtils.getProvince(editCallsign); if (p != null) Text(p, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("频率 MHz", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editFreq, { editFreq = it }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("模式", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = editMode, onValueChange = {}, readOnly = true, singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) }, colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth())
                                ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) { modeOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt, fontFamily = NotoSerif) }, onClick = { editMode = opt; modeExpanded = false }) } }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("信号报告-发", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editRstSent, { editRstSent = it }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("信号报告-收", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editRstRecv, { editRstRecv = it }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("我的功率 (W)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editPowerTx, { editPowerTx = it }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("对方功率 (W)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                            OutlinedTextField(editPowerRx, { editPowerRx = it }, Modifier.fillMaxWidth(), singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("备注", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.tertiary)
                        OutlinedTextField(editNotes, { editNotes = it }, Modifier.fillMaxWidth(), maxLines = 3, textStyle = MaterialTheme.typography.bodySmall, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow, focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow))
                    }

                    Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { editingContact = null }) { Text("取消", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = { viewModel.updateContact(contact.copy(dateEpochDay = editDateEpochDay, createdAt = editCreatedAt, callsign = editCallsign.trim(), frequencyMHz = editFreq.toDoubleOrNull() ?: contact.frequencyMHz, mode = editMode.trim(), rstSent = editRstSent.trim(), rstReceived = editRstRecv.trim(), powerTx = editPowerTx.trim().trimEnd('W', 'w').trim(), powerRx = editPowerRx.trim().trimEnd('W', 'w').trim(), notes = editNotes.trim())); editingContact = null }, shape = RoundedCornerShape(50), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)) { Text("保存", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.onPrimary) }
                    }
                }
            }
        }
    }
}

    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(uiState.dateString, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp, fontFamily = NotoSerif), fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            if (isWide) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Input Section (left, ~55%)
                    Column(Modifier.weight(0.55f).fillMaxHeight()) {
                        Box(Modifier.weight(1f)) {
                            Column(Modifier.fillMaxSize().padding(horizontal = hPadding, vertical = 6.dp)) {
                                SmartInputField(
                                    smartInput = uiState.smartInput, callsign = uiState.callsign,
                                    frequency = uiState.frequency, mode = uiState.mode,
                                    rstSent = uiState.rstSent, rstReceived = uiState.rstReceived,
                                    powerTx = uiState.powerTx, powerRx = uiState.powerRx, notes = uiState.notes,
                                    suggestions = uiState.callsignSuggestions, showSuggestions = uiState.showSuggestions,
                                    onInputChange = { viewModel.onSmartInputChanged(it) },
                                    onFieldChange = { f, v -> viewModel.updateField(f, v) },
                                    onCommitNext = { viewModel.commitNext() },
                                    onSave = { viewModel.saveContact() },
                                    qsoTime = uiState.qsoTime,
                                    onSelectSuggestion = { viewModel.selectCallsignSuggestion(it) },
                                    onDismissSuggestions = { viewModel.dismissSuggestions() },
                                    dismissKeyboards = uiState.dismissKeyboards
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.saveContact() },
                            enabled = uiState.callsign.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = hPadding, vertical = 6.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("保存通联", fontWeight = FontWeight.Medium) }
                    }

                    VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    // Contact List Section (right, ~45%)
                    Column(Modifier.weight(0.45f).fillMaxHeight()) {
                        val title = if (isHistorical) "历史  ${uiState.searchCallsign}" else "今日通联"
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = hPadding, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = NotoSerif, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            if (!isHistorical) {
                                Text("${displayContacts.size} 条", style = MaterialTheme.typography.labelSmall, fontFamily = NotoSerif, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (displayContacts.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text(if (isHistorical) "无历史记录" else "暂无记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayContacts, key = { it.id }) { c ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                                viewModel.requestDelete(c)
                                            }
                                            false
                                        }
                                    )
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            Row(
                                                Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "删除",
                                                    tint = Color(0xFFFF4444),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "删除",
                                                    tint = Color(0xFFFF4444),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        },
                                        enableDismissFromStartToEnd = true,
                                        enableDismissFromEndToStart = true
                                    ) {
                                        ContactListItem(c, { viewModel.requestDelete(c) }, tz, { editingContact = c })
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    // Input Section
                    Column(Modifier.weight(0.6f)) {
                        Box(Modifier.weight(1f)) {
                            Column(Modifier.fillMaxSize().padding(horizontal = hPadding, vertical = 6.dp)) {
                                SmartInputField(
                                    smartInput = uiState.smartInput, callsign = uiState.callsign,
                                    frequency = uiState.frequency, mode = uiState.mode,
                                    rstSent = uiState.rstSent, rstReceived = uiState.rstReceived,
                                    powerTx = uiState.powerTx, powerRx = uiState.powerRx, notes = uiState.notes,
                                    suggestions = uiState.callsignSuggestions, showSuggestions = uiState.showSuggestions,
                                    onInputChange = { viewModel.onSmartInputChanged(it) },
                                    onFieldChange = { f, v -> viewModel.updateField(f, v) },
                                    onCommitNext = { viewModel.commitNext() },
                                    onSave = { viewModel.saveContact() },
                                    qsoTime = uiState.qsoTime,
                                    onSelectSuggestion = { viewModel.selectCallsignSuggestion(it) },
                                    onDismissSuggestions = { viewModel.dismissSuggestions() },
                                    dismissKeyboards = uiState.dismissKeyboards
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.saveContact() },
                            enabled = uiState.callsign.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = hPadding, vertical = 6.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("保存通联", fontWeight = FontWeight.Medium) }
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    // Contact List Section
                    Column(Modifier.weight(0.4f)) {
                        val title = if (isHistorical) "历史  ${uiState.searchCallsign}" else "今日通联"
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = hPadding, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = NotoSerif, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            if (!isHistorical) {
                                Text("${displayContacts.size} 条", style = MaterialTheme.typography.labelSmall, fontFamily = NotoSerif, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (displayContacts.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text(if (isHistorical) "无历史记录" else "暂无记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayContacts, key = { it.id }) { c ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                                viewModel.requestDelete(c)
                                            }
                                            false
                                        }
                                    )
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                        Row(
                                            Modifier.fillMaxSize().padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "删除",
                                                tint = Color(0xFFFF4444),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "删除",
                                                tint = Color(0xFFFF4444),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    },
                                        enableDismissFromStartToEnd = true,
                                        enableDismissFromEndToStart = true
                                    ) {
                                        ContactListItem(c, { viewModel.requestDelete(c) }, tz, { editingContact = c })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Success Banner
        AnimatedVisibility(
            visible = showSuccessBanner,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp),
            enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { -it },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it }
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), tonalElevation = 0.dp) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("\u2713", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text("保存成功", style = MaterialTheme.typography.labelLarge, color = Color(0xFF00E50B))
                }
            }
        }
    }
}
