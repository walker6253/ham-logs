package com.hamlog.ui.screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.ui.theme.NotoSerif
import com.hamlog.AppPreferences
import com.hamlog.viewmodel.SettingsViewModel
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 40.dp
        WindowWidthSizeClass.Medium -> 24.dp
        else -> 16.dp
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userCallsign by AppPreferences.callsign.collectAsState()
    var callsignText by remember { mutableStateOf(userCallsign) }

    LaunchedEffect(userCallsign) { callsignText = userCallsign }

    LaunchedEffect(uiState.exportComplete, uiState.exportUri) {
        if (uiState.exportComplete && uiState.exportUri != null) {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uiState.exportUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "导出 ADIF 日志"))
            viewModel.resetExportState()
        }
    }

    val timezoneOptions = listOf(
        "UTC" to "UTC",
        "Asia/Shanghai" to "UTC+8 北京",
        "Asia/Tokyo" to "UTC+9 东京",
        "Asia/Singapore" to "UTC+8 新加坡",
        "Europe/London" to "UTC+0 伦敦",
        "Europe/Berlin" to "UTC+1 柏林",
        "America/New_York" to "UTC-5 纽约",
        "America/Los_Angeles" to "UTC-8 洛杉矶"
    )
    var tzExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "设置",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Callsign ──
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person, null, Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("我的呼号", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "设置后主页显示「XXX 的通联日志」",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = callsignText,
                            onValueChange = { val v = it.uppercase(); callsignText = v; AppPreferences.setCallsign(v.trim()) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = NotoSerif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            shape = MaterialTheme.shapes.small,
                            colors = hamFieldColors()
                        )
                    }
                }

                // ── Smart Mode Toggle ──
                SettingsCard {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("默认输入模式", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (uiState.isSmartMode) "智能输入" else "独立字段",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isSmartMode,
                            onCheckedChange = { viewModel.toggleSmartMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // ── Timezone ──
                SettingsCard {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule, null, Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("时区", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "通联记录时间显示（默认 UTC）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        ExposedDropdownMenuBox(
                            expanded = tzExpanded,
                            onExpandedChange = { tzExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = timezoneOptions.find { it.first == uiState.selectedTimezone.id }?.second ?: "UTC",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = tzExpanded)
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                colors = hamFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = tzExpanded,
                                onDismissRequest = { tzExpanded = false }
                            ) {
                                timezoneOptions.forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setTimezone(ZoneId.of(id))
                                            tzExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Stats ──
                SettingsCard {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Text("通联总数", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${uiState.totalContacts} 条",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = NotoSerif
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Export ──
                Button(
                    onClick = { viewModel.exportAdif() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isExporting && uiState.totalContacts > 0,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isExporting) "导出中..." else "导出 ADIF 日志",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Footer ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        "通联日志 v1.0 · 业余无线电通联日志",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun hamFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
