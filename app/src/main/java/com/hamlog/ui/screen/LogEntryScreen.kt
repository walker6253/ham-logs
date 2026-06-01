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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamlog.ui.component.ContactListItem
import com.hamlog.data.entity.ContactRecord
import com.hamlog.ui.component.IndependentFields
import com.hamlog.ui.component.SmartInputField
import com.hamlog.viewmodel.LogEntryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId as JavaZoneId
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    dateEpochDay: Long,
    viewModel: LogEntryViewModel,
    onNavigateBack: () -> Unit
) {
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
        var editNotes by remember(contact.id) { mutableStateOf(contact.notes) }
        var editDateEpochDay by remember(contact.id) { mutableStateOf(contact.dateEpochDay) }
        var editCreatedAt by remember(contact.id) { mutableStateOf(contact.createdAt) }
        var showEditDatePicker by remember { mutableStateOf(false) }
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
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = editDateEpochDay * 86400000L
            )
            DatePickerDialog(
                onDismissRequest = { showEditDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showEditDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis).atZone(JavaZoneId.of("Asia/Shanghai")).toLocalDate()
                            editDateEpochDay = localDate.toEpochDay()
                        }
                    }) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { showEditDatePicker = false }) { Text("取消") } }
            ) { DatePicker(state = datePickerState) }
        }

        AlertDialog(
            onDismissRequest = { editingContact = null },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("编辑通联", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(editDateStr, {}, Modifier.weight(1f), label = { Text("日期") }, readOnly = true, singleLine = true, enabled = false, textStyle = MaterialTheme.typography.bodyMedium, shape = MaterialTheme.shapes.small, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), trailingIcon = { IconButton(onClick = { showEditDatePicker = true }) { Text("\uD83D\uDCC5") } })
                        Spacer(Modifier.width(8.dp))
                        // Time button
                        var showEditTimePicker by remember { mutableStateOf(false) }
                        val editTimeLabel = remember(editCreatedAt) { val t = Instant.ofEpochMilli(editCreatedAt).atZone(JavaZoneId.of("Asia/Shanghai")); String.format("%02d:%02d:%02d", t.hour, t.minute, t.second) }
                        OutlinedTextField(editTimeLabel, {}, Modifier.weight(1f), label = { Text("时间") }, readOnly = true, singleLine = true, enabled = false, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), shape = MaterialTheme.shapes.small, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), trailingIcon = { IconButton(onClick = { showEditTimePicker = true }) { Text("\uD83D\uDD50") } })
                        if (showEditTimePicker) {
                            val currentTime = Instant.ofEpochMilli(editCreatedAt).atZone(JavaZoneId.of("Asia/Shanghai"))
                            val timePickerState = rememberTimePickerState(
                                initialHour = currentTime.hour,
                                initialMinute = currentTime.minute,
                                is24Hour = true
                            )
                            AlertDialog(
                                onDismissRequest = { showEditTimePicker = false },
                                shape = MaterialTheme.shapes.large,
                                containerColor = MaterialTheme.colorScheme.surface,
                                title = { Text("选择时间") },
                                text = { TimePicker(state = timePickerState) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showEditTimePicker = false
                                        val d = LocalDate.ofEpochDay(editDateEpochDay)
                                        editCreatedAt = d.atTime(timePickerState.hour, timePickerState.minute, 0)
                                            .atZone(JavaZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                                    }) { Text("确定") }
                                },
                                dismissButton = { TextButton(onClick = { showEditTimePicker = false }) { Text("取消") } }
                            )
                        }
                    }
                    OutlinedTextField(editCallsign, { editCallsign = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("呼号") }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), shape = MaterialTheme.shapes.small)
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(editFreq, { editFreq = it }, Modifier.weight(1f), label = { Text("频率 MHz") }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), shape = MaterialTheme.shapes.small)
                        Spacer(Modifier.width(8.dp))
                        ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editMode, onValueChange = {}, readOnly = true,
                                label = { Text("模式") }, singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                shape = MaterialTheme.shapes.small,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                                modeOptions.forEach { opt ->
                                    DropdownMenuItem(text = { Text(opt, fontFamily = FontFamily.Monospace) }, onClick = { editMode = opt; modeExpanded = false })
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(editRstSent, { editRstSent = it }, Modifier.weight(1f), label = { Text("RST 发") }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), shape = MaterialTheme.shapes.small)
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(editRstRecv, { editRstRecv = it }, Modifier.weight(1f), label = { Text("RST 收") }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), shape = MaterialTheme.shapes.small)
                    }
                    OutlinedTextField(editNotes, { editNotes = it }, Modifier.fillMaxWidth(), label = { Text("备注") }, maxLines = 2, textStyle = MaterialTheme.typography.bodyMedium, shape = MaterialTheme.shapes.small)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateContact(contact.copy(
                        dateEpochDay = editDateEpochDay,
                        createdAt = editCreatedAt,
                        callsign = editCallsign.trim(),
                        frequencyMHz = editFreq.toDoubleOrNull() ?: contact.frequencyMHz,
                        mode = editMode.trim(),
                        rstSent = editRstSent.trim(),
                        rstReceived = editRstRecv.trim(),
                        notes = editNotes.trim()
                    ))
                    editingContact = null
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { editingContact = null }) { Text("取消") } }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(uiState.dateString, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                // Input Section
                Column(Modifier.weight(0.6f)) {
                    Box(Modifier.weight(1f)) {
                        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)) {
                            AnimatedContent(
                                targetState = uiState.isSmartMode,
                                transitionSpec = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 6 } togetherWith fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 6 } }
                            ) { smart ->
                                if (smart) {
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
                                        onToggleMode = { viewModel.toggleInputMode() },
                                        qsoTime = uiState.qsoTime,
                                        onSelectSuggestion = { viewModel.selectCallsignSuggestion(it) },
                                        onDismissSuggestions = { viewModel.dismissSuggestions() }
                                    )
                                } else {
                                    IndependentFields(
                                        callsign = uiState.callsign, frequency = uiState.frequency,
                                        mode = uiState.mode, rstSent = uiState.rstSent,
                                        rstReceived = uiState.rstReceived, powerTx = uiState.powerTx,
                                        powerRx = uiState.powerRx, notes = uiState.notes,
                                        suggestions = uiState.callsignSuggestions, showSuggestions = uiState.showSuggestions,
                                        onFieldChange = { f, v -> viewModel.updateField(f, v) },
                                        onSave = { viewModel.saveContact() },
                                        onToggleMode = { viewModel.toggleInputMode() },
                                        onSelectSuggestion = { viewModel.selectCallsignSuggestion(it) },
                                        onDismissSuggestions = { viewModel.dismissSuggestions() }
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.saveContact() },
                        enabled = uiState.callsign.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("保存通联", fontWeight = FontWeight.Medium) }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Contact List Section
                Column(Modifier.weight(0.4f)) {
                    val title = if (isHistorical) "历史  ${uiState.searchCallsign}" else "今日通联"
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        if (!isHistorical) {
                            Text("${displayContacts.size} 条", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    backgroundContent = {},
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

        // Success Banner
        AnimatedVisibility(
            visible = showSuccessBanner,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp),
            enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { -it },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it }
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer, tonalElevation = 0.dp) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("\u2713", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text("保存成功", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}
