package com.hamlog.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.ui.theme.NotoSerif
import java.time.Instant
import java.time.ZoneId
import com.hamlog.AppPreferences
import com.hamlog.viewmodel.DateItem
import com.hamlog.viewmodel.MainViewModel
import com.hamlog.ui.component.AlxDatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToLog: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 40.dp
        WindowWidthSizeClass.Medium -> 24.dp
        else -> 12.dp
    }
    val cardSpacing = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 14.dp
        WindowWidthSizeClass.Medium -> 12.dp
        else -> 10.dp
    }
    val uiState by viewModel.uiState.collectAsState()
    val userCallsign by AppPreferences.callsign.collectAsState()

    Scaffold(
        topBar = {
            val title = if (userCallsign.isNotBlank()) "${userCallsign} 的通联日志" else "通联日志"
            TopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NotoSerif),
                        fontWeight = FontWeight.Bold
                    )
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