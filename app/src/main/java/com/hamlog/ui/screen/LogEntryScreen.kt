package com.hamlog.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamlog.ui.component.ContactListItem
import com.hamlog.ui.component.IndependentFields
import com.hamlog.ui.component.SmartInputField
import com.hamlog.viewmodel.LogEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    dateEpochDay: Long,
    viewModel: LogEntryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(dateEpochDay) {
        viewModel.init(dateEpochDay)
    }

    // Saved toast
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.showSavedToast) {
        if (uiState.showSavedToast) {
            snackbarHostState.showSnackbar("通联记录已保存")
            viewModel.clearSavedToast()
        }
    }

    // Delete confirmation dialog
    uiState.showDeleteConfirm?.let { contact ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("删除记录") },
            text = { Text("确定要删除与 ${contact.callsign} 的通联记录吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.dateString) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top 70%: Input area
            Box(modifier = Modifier.weight(0.7f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (uiState.isSmartMode) {
                        SmartInputField(
                            smartInput = uiState.smartInput,
                            parsedFields = uiState.parsedFields,
                            onInputChange = { viewModel.onSmartInputChanged(it) },
                            onSave = { viewModel.saveContact() },
                            onToggleMode = { viewModel.toggleInputMode() }
                        )
                    } else {
                        IndependentFields(
                            callsign = uiState.callsign,
                            frequency = uiState.frequency,
                            mode = uiState.mode,
                            rstSent = uiState.rstSent,
                            rstReceived = uiState.rstReceived,
                            powerTx = uiState.powerTx,
                            powerRx = uiState.powerRx,
                            notes = uiState.notes,
                            onFieldChange = { field, value -> viewModel.updateField(field, value) },
                            onSave = { viewModel.saveContact() },
                            onToggleMode = { viewModel.toggleInputMode() }
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Bottom 30%: Today's contacts list
            Column(modifier = Modifier.weight(0.3f)) {
                Text(
                    text = "今日通联 (${uiState.contacts.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                if (uiState.contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.contacts, key = { it.id }) { contact ->
                            ContactListItem(
                                contact = contact,
                                onDelete = { viewModel.requestDelete(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}
