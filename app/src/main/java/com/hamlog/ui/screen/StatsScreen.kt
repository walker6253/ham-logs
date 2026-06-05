package com.hamlog.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.ui.theme.NotoSerif
import com.hamlog.ui.component.charts.DonutChart
import com.hamlog.ui.component.charts.HorizontalBarList
import com.hamlog.ui.component.charts.HourHeatStrip
import com.hamlog.ui.component.charts.TrendBarChart
import com.hamlog.ui.component.charts.TrendLineChart
import com.hamlog.ui.component.charts.colorForIndex
import com.hamlog.viewmodel.CallsignStat
import com.hamlog.viewmodel.StatsUiState
import com.hamlog.viewmodel.StatsViewModel
import com.hamlog.viewmodel.TrendGranularity
import com.hamlog.viewmodel.TrendRange
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 40.dp
        WindowWidthSizeClass.Medium -> 24.dp
        else -> 16.dp
    }
    val contentMaxWidth = if (widthClass == WindowWidthSizeClass.Expanded) 1080.dp else Dp.Unspecified
    val isWide = widthClass != WindowWidthSizeClass.Compact
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Text(
                        "通联统计",
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NotoSerif),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .then(if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth) else Modifier)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                } else if (!uiState.hasAnyData) {
                    EmptyStats()
                } else {
                    OverviewSection(uiState)
                    TrendSection(viewModel, uiState)
                    if (isWide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.weight(1.05f)) { BandDistributionSection(uiState) }
                            Box(Modifier.weight(0.95f)) { ModeDistributionSection(uiState) }
                        }
                    } else {
                        BandDistributionSection(uiState)
                        ModeDistributionSection(uiState)
                    }
                    HourDistributionSection(uiState)
                    TopCallsignsSection(uiState)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Empty state ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyStats() {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Equalizer, null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "暂无统计数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "记录通联后即可查看统计图表",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Overview stats ──────────────────────────────────────────────────────────
@Composable
private fun OverviewSection(state: StatsUiState) {
    val widthClass = LocalWindowSizeClass.current
    val columns = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 6
        WindowWidthSizeClass.Medium -> 4
        else -> 2
    }
    val items = listOf(
        OverviewItem("总通联", state.overview.totalContacts.toString(), Icons.Default.Radio, colorForIndex(0)),
        OverviewItem("独立呼号", state.overview.distinctCallsigns.toString(), Icons.Default.Tag, colorForIndex(2)),
        OverviewItem("活跃天数", state.overview.activeDays.toString(), Icons.Default.CalendarMonth, colorForIndex(5)),
        OverviewItem("波段数", state.overview.bandsUsed.toString(), Icons.AutoMirrored.Filled.ShowChart, colorForIndex(3)),
        OverviewItem("模式数", state.overview.modesUsed.toString(), Icons.Default.WifiTethering, colorForIndex(4)),
        OverviewItem(
            "日均",
            if (state.overview.activeDays > 0)
                String.format("%.1f", state.overview.totalContacts.toFloat() / state.overview.activeDays)
            else "0",
            Icons.Default.AccessTime,
            colorForIndex(1)
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 标题
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionDot(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                "总览",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        // 网格
        val rows = (items.size + columns - 1) / columns
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0 until columns) {
                        val idx = row * columns + col
                        if (idx < items.size) {
                            Box(Modifier.weight(1f)) { OverviewCard(items[idx]) }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionDot(color: Color) {
    Box(
        Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

private data class OverviewItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun OverviewCard(item: OverviewItem) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(item.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon, null,
                        tint = item.color,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                item.value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Trend ───────────────────────────────────────────────────────────────────
@Composable
private fun TrendSection(viewModel: StatsViewModel, state: StatsUiState) {
    val widthClass = LocalWindowSizeClass.current
    val cardRadius = MaterialTheme.shapes.small
    Card(
        shape = cardRadius,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "通联趋势",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                if (state.trend.points.isNotEmpty()) {
                    Text(
                        "峰值 ${state.trend.maxValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 粒度 + 范围 选择
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SegmentedChips(
                    items = TrendGranularity.entries.toList(),
                    selected = state.trendGranularity,
                    label = { it.label },
                    onSelect = { viewModel.setGranularity(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(2.dp))
            FlowRowChips(
                items = TrendRange.entries.toList(),
                selected = state.trendRange,
                label = { it.label },
                onSelect = { viewModel.setRange(it) }
            )
            Spacer(Modifier.height(6.dp))
            // 折线/柱状
            val chartHeight = if (widthClass == WindowWidthSizeClass.Compact) 140.dp else 200.dp
            if (state.trend.points.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(chartHeight), contentAlignment = Alignment.Center) {
                    Text(
                        "该范围内无数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (state.trendGranularity == TrendGranularity.DAILY && state.trendRange == TrendRange.DAYS_7) {
                    TrendBarChart(
                        points = state.trend.points,
                        modifier = Modifier.fillMaxWidth().height(chartHeight)
                    )
                } else {
                    TrendLineChart(
                        points = state.trend.points,
                        modifier = Modifier.fillMaxWidth().height(chartHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SegmentedChips(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            items.forEach { item ->
                val isSel = item == selected
                Surface(
                    onClick = { onSelect(item) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label(item),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSel) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowRowChips(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            val isSel = item == selected
            Surface(
                onClick = { onSelect(item) },
                shape = RoundedCornerShape(50),
                color = if (isSel) primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    label(item),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSel) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Band distribution ───────────────────────────────────────────────────────
@Composable
private fun BandDistributionSection(state: StatsUiState) {
    val widthClass = LocalWindowSizeClass.current
    val isWide = widthClass != WindowWidthSizeClass.Compact
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "波段分布",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${state.bandDistribution.size} 个波段",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.bandDistribution.isEmpty()) {
                Text(
                    "暂无波段数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (isWide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Donut
                        Box(Modifier.size(140.dp)) {
                            DonutChart(
                                items = state.bandDistribution.map { it.band to it.count },
                                modifier = Modifier.fillMaxSize(),
                                centerTitle = "总通联",
                                centerValue = state.overview.totalContacts.toString()
                            )
                        }
                        // 横向条形图
                        Box(Modifier.weight(1f)) {
                            HorizontalBarList(
                                items = state.bandDistribution.map { it.band to it.count },
                                total = state.overview.totalContacts
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxWidth().height(160.dp)) {
                        DonutChart(
                            items = state.bandDistribution.map { it.band to it.count },
                            modifier = Modifier.fillMaxSize(),
                            centerTitle = "总通联",
                            centerValue = state.overview.totalContacts.toString()
                        )
                    }
                    HorizontalBarList(
                        items = state.bandDistribution.map { it.band to it.count },
                        total = state.overview.totalContacts
                    )
                }
            }
        }
    }
}

// ── Mode distribution ───────────────────────────────────────────────────────
@Composable
private fun ModeDistributionSection(state: StatsUiState) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "模式分布",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${state.modeDistribution.size} 种模式",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.modeDistribution.isEmpty()) {
                Text(
                    "暂无模式数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HorizontalBarList(
                    items = state.modeDistribution.map { it.mode to it.count },
                    total = state.overview.totalContacts
                )
            }
        }
    }
}

// ── Hour distribution ───────────────────────────────────────────────────────
@Composable
private fun HourDistributionSection(state: StatsUiState) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "活跃时段",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                val peak = state.hourDistribution.maxByOrNull { it.count }
                if (peak != null && peak.count > 0) {
                    Text(
                        "高峰 ${peak.hour}:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "小时分布（基于通联创建时间）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            HourHeatStrip(
                hours = state.hourDistribution,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Top callsigns ───────────────────────────────────────────────────────────
@Composable
private fun TopCallsignsSection(state: StatsUiState) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "活跃呼号 Top ${state.topCallsigns.size}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (state.topCallsigns.isEmpty()) {
                Text(
                    "暂无呼号数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxCnt = state.topCallsigns.maxOf { it.count }.coerceAtLeast(1)
                state.topCallsigns.forEachIndexed { i, c ->
                    TopCallsignRow(rank = i + 1, stat = c, maxCount = maxCnt)
                }
            }
        }
    }
}

@Composable
private fun TopCallsignRow(rank: Int, stat: CallsignStat, maxCount: Int) {
    val pct = stat.count.toFloat() / maxCount
    Row(verticalAlignment = Alignment.CenterVertically) {
        // rank
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when (rank) {
                        1 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        3 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                rank.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stat.callsign,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${stat.count} 次",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pct)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                )
            }
            if (stat.lastBand.isNotBlank() || stat.lastMode.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (stat.lastBand.isNotBlank()) MiniTag(stat.lastBand)
                    if (stat.lastMode.isNotBlank()) MiniTag(stat.lastMode)
                }
            }
        }
    }
}

@Composable
private fun MiniTag(text: String) {
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}
