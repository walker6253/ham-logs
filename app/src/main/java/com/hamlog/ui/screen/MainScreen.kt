package com.hamlog.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.ui.theme.NotoSerif
import java.time.Instant
import java.time.ZoneId
import com.hamlog.AppPreferences
import com.hamlog.viewmodel.DateItem
import com.hamlog.viewmodel.MainViewModel
import com.hamlog.ui.component.AlxDatePickerDialog
import com.hamlog.util.UpdateChecker
import com.hamlog.util.UpdateInfo
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToLog: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 48.dp
        WindowWidthSizeClass.Medium -> 24.dp
        else -> 12.dp
    }
    val cardSpacing = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 14.dp
        WindowWidthSizeClass.Medium -> 12.dp
        else -> 10.dp
    }
    val useTwoColumns = widthClass == WindowWidthSizeClass.Expanded
    val uiState by viewModel.uiState.collectAsState()
    val userCallsign by AppPreferences.callsign.collectAsState()

    // Auto update check
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lastCheckDate by AppPreferences.lastUpdateCheckDate.collectAsState()
    val ignoredDate by AppPreferences.updateIgnoredDate.collectAsState()
    var autoUpdateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showAutoUpdateDialog by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var downloadingProgress by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        val today = LocalDate.now().toString()
        if (lastCheckDate != today) {
            val info = UpdateChecker.checkForUpdate(context)
            AppPreferences.setLastUpdateCheckDate(today)
            if (info.hasUpdate && ignoredDate != today) {
                autoUpdateInfo = info
                showAutoUpdateDialog = true
            }
        }
    }

    // Auto update dialog
    if (showAutoUpdateDialog && autoUpdateInfo != null) {
        val info = autoUpdateInfo!!
        AlertDialog(
            onDismissRequest = { showAutoUpdateDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("发现新版本 v${info.latestVersion}", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("当前版本: v${info.currentVersion}", style = MaterialTheme.typography.bodyMedium)
                    if (info.body.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(info.body.take(300), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isDownloadingUpdate && downloadingProgress != null) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { downloadingProgress!! }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text("下载中 ${(downloadingProgress!! * 100).toInt()}%", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                if (!isDownloadingUpdate) {
                    TextButton(onClick = {
                        if (info.apkDownloadUrl.isNotBlank()) {
                            isDownloadingUpdate = true
                            downloadingProgress = 0f
                            scope.launch {
                                try {
                                    val apkFile = withContext(Dispatchers.IO) {
                                        val file = File(context.cacheDir, "hamlog_auto_update.apk")
                                        if (file.exists()) file.delete()
                                        val url = URL(info.apkDownloadUrl)
                                        val conn = url.openConnection() as HttpURLConnection
                                        conn.connectTimeout = 15000
                                        conn.readTimeout = 60000
                                        val totalBytes = conn.contentLength.toLong()
                                        var downloaded = 0L
                                        conn.inputStream.use { input ->
                                            file.outputStream().use { output ->
                                                val buffer = ByteArray(8192)
                                                var bytesRead = input.read(buffer)
                                                while (bytesRead != -1) {
                                                    output.write(buffer, 0, bytesRead)
                                                    downloaded += bytesRead
                                                    downloadingProgress = if (totalBytes > 0) {
                                                        (downloaded.toFloat() / totalBytes).coerceIn(0f, 1f)
                                                    } else -1f
                                                    bytesRead = input.read(buffer)
                                                }
                                            }
                                        }
                                        conn.disconnect()
                                        file
                                    }
                                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
                                    } else {
                                        android.net.Uri.fromFile(apkFile)
                                    }
                                    val install = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/vnd.android.package-archive")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(install)
                                    showAutoUpdateDialog = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_LONG).show()
                                    isDownloadingUpdate = false
                                    downloadingProgress = null
                                } finally {
                                    isDownloadingUpdate = false
                                    downloadingProgress = null
                                }
                            }
                        }
                    }) {
                        Text("立即更新")
                    }
                }
            },
            dismissButton = {
                if (!isDownloadingUpdate) {
                    Row {
                        TextButton(onClick = {
                            AppPreferences.setUpdateIgnoredDate(LocalDate.now().toString())
                            showAutoUpdateDialog = false
                        }) {
                            Text("忽略", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = { showAutoUpdateDialog = false }) {
                            Text("关闭")
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            val title = if (userCallsign.isNotBlank()) "${userCallsign} 的通联日志" else "业余无线电通联日志"
            TopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NotoSerif),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = "统计",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            var showDatePicker by remember { mutableStateOf(false) }
            if (showDatePicker) {
                val todayEpoch = java.time.LocalDate.now().toEpochDay()
                AlxDatePickerDialog(
                    initialEpochDay = todayEpoch,
                    onDismiss = { showDatePicker = false },
                    onConfirm = { epochDay ->
                        showDatePicker = false
                        onNavigateToLog(epochDay)
                    }
                )
            }
            FloatingActionButton(
                onClick = { showDatePicker = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Default.Add, "添加", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = hPadding)
        ) {
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (uiState.dates.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-40).dp)
                    ) {
                        Text(
                            "暂无通联记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "点击右下角按钮开始记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else if (useTwoColumns) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing),
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    gridItemsIndexed(
                        uiState.dates,
                        key = { _, item -> item.dateEpochDay }
                    ) { index, dateItem ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(300, delayMillis = index * 50)
                            ) + slideInVertically(
                                animationSpec = tween(300, delayMillis = index * 50),
                                initialOffsetY = { it / 4 }
                            )
                        ) {
                            DateCard(dateItem) { onNavigateToLog(dateItem.dateEpochDay) }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    itemsIndexed(
                        uiState.dates,
                        key = { _, item -> item.dateEpochDay }
                    ) { index, dateItem ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(300, delayMillis = index * 50)
                            ) + slideInVertically(
                                animationSpec = tween(300, delayMillis = index * 50),
                                initialOffsetY = { it / 4 }
                            )
                        ) {
                            DateCard(dateItem) { onNavigateToLog(dateItem.dateEpochDay) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCard(dateItem: DateItem, onClick: () -> Unit) {
    val isToday = dateItem.isToday
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isToday) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    dateItem.dateString,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    "${dateItem.contactCount} 条",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = NotoSerif
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}