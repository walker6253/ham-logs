package com.hamlog.ui.component.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamlog.viewmodel.BandStat
import com.hamlog.viewmodel.HourStat
import com.hamlog.viewmodel.ModeStat
import com.hamlog.viewmodel.TrendPoint
import com.hamlog.viewmodel.TrendRange

// ── Color palette for bands (可重复使用) ────────────────────────────────────
val BandChartPalette = listOf(
    Color(0xFF094CB2), // primary blue
    Color(0xFF6D5E00), // gold
    Color(0xFF4CAF50), // green
    Color(0xFFE53935), // red
    Color(0xFF9C27B0), // purple
    Color(0xFFFF9800), // orange
    Color(0xFF00BCD4), // cyan
    Color(0xFF795548), // brown
    Color(0xFF607D8B), // blue-grey
    Color(0xFF8BC34A), // light green
    Color(0xFFFF5722), // deep orange
    Color(0xFF3F51B5)  // indigo
)

fun colorForIndex(i: Int): Color = BandChartPalette[i % BandChartPalette.size]

// ── Section header ─────────────────────────────────────────────────────────
@Composable
fun ChartSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

// ── Trend Bar Chart (按天/周/月) ─────────────────────────────────────────────
@Composable
fun TrendBarChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showValues: Boolean = true,
    labelStride: Int = 1
) {
    if (points.isEmpty()) {
        EmptyChartHint(modifier)
        return
    }
    val maxV = (points.maxOf { it.value }).coerceAtLeast(1)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 28f
        val bottomPad = 28f
        val topPad = 8f
        val rightPad = 8f
        val plotW = (w - leftPad - rightPad).coerceAtLeast(1f)
        val plotH = (h - topPad - bottomPad).coerceAtLeast(1f)

        // Y 轴 baseline
        drawLine(
            color = axisColor,
            start = Offset(leftPad, h - bottomPad),
            end = Offset(w - rightPad, h - bottomPad),
            strokeWidth = 1.5f
        )

        // 水平网格线 (4 段)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = topPad + plotH * i / gridLines
            drawLine(
                color = axisColor.copy(alpha = 0.4f),
                start = Offset(leftPad, y),
                end = Offset(w - rightPad, y),
                strokeWidth = 0.8f
            )
        }

        val n = points.size
        val slot = plotW / n
        val barW = (slot * 0.6f).coerceAtMost(if (n > 60) 4f else if (n > 30) 8f else 18f)

        points.forEachIndexed { i, p ->
            val barH = (p.value.toFloat() / maxV) * plotH
            val cx = leftPad + slot * i + slot / 2f
            val left = cx - barW / 2f
            val top = (h - bottomPad) - barH
            // Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barW, barH.coerceAtLeast(1f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
    // X 轴 labels (用 Composable 而非 Canvas 文字，避免在 DrawScope 中算文字尺寸)
    RowLabels(
        points = points,
        modifier = Modifier.fillMaxWidth(),
        stride = labelStride
    )
}

@Composable
private fun RowLabels(points: List<TrendPoint>, modifier: Modifier, stride: Int) {
    val total = points.size
    if (total == 0) return
    val s = if (total <= 8) 1 else if (total <= 20) 2 else if (total <= 60) 5 else (total / 10)
    val showIdx = (0 until total).filter { it % s == 0 || it == total - 1 }
    Row(modifier = modifier) {
        Spacer(Modifier.width(28.dp))
        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            showIdx.forEach { i ->
                Text(
                    points[i].label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

// ── Trend Line Chart ───────────────────────────────────────────────────────
@Composable
fun TrendLineChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    if (points.isEmpty()) {
        EmptyChartHint(modifier)
        return
    }
    val maxV = (points.maxOf { it.value }).coerceAtLeast(1)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 28f
        val bottomPad = 28f
        val topPad = 8f
        val rightPad = 8f
        val plotW = (w - leftPad - rightPad).coerceAtLeast(1f)
        val plotH = (h - topPad - bottomPad).coerceAtLeast(1f)

        // baseline + 网格
        drawLine(axisColor, Offset(leftPad, h - bottomPad), Offset(w - rightPad, h - bottomPad), 1.5f)
        for (i in 0..3) {
            val y = topPad + plotH * i / 3
            drawLine(axisColor.copy(alpha = 0.4f), Offset(leftPad, y), Offset(w - rightPad, y), 0.8f)
        }

        val n = points.size
        val step = if (n > 1) plotW / (n - 1) else 0f

        // 构造路径
        val path = Path()
        val fillPath = Path()
        points.forEachIndexed { i, p ->
            val x = leftPad + step * i
            val y = (h - bottomPad) - (p.value.toFloat() / maxV) * plotH
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h - bottomPad)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(leftPad + step * (n - 1), h - bottomPad)
        fillPath.close()

        drawPath(fillPath, color = fillColor)
        drawPath(path, color = lineColor, style = Stroke(width = 2.5f))

        // 数据点
        points.forEachIndexed { i, p ->
            val x = leftPad + step * i
            val y = (h - bottomPad) - (p.value.toFloat() / maxV) * plotH
            drawCircle(color = lineColor, radius = 3.5f, center = Offset(x, y))
        }
    }
    RowLabels(points = points, modifier = Modifier.fillMaxWidth(), stride = 1)
}

// ── Horizontal Bar Chart (波段/模式分布) ────────────────────────────────────
@Composable
fun HorizontalBarList(
    items: List<Pair<String, Int>>,
    total: Int,
    modifier: Modifier = Modifier,
    palette: List<Color> = BandChartPalette
) {
    if (items.isEmpty() || total <= 0) {
        EmptyChartHint(modifier)
        return
    }
    val maxV = items.maxOf { it.second }.coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { i, (label, value) ->
            val pct = value.toFloat() / total
            val color = palette[i % palette.size]
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$value",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pct)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

// ── Donut Chart (饼图) ──────────────────────────────────────────────────────
@Composable
fun DonutChart(
    items: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    palette: List<Color> = BandChartPalette,
    centerTitle: String = "",
    centerValue: String = ""
) {
    if (items.isEmpty()) {
        EmptyChartHint(modifier)
        return
    }
    val total = items.sumOf { it.second }.coerceAtLeast(1)
    val outline = MaterialTheme.colorScheme.surface
    val centerColor = MaterialTheme.colorScheme.onSurface
    val centerVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.minDimension
            val strokeW = w * 0.18f
            val diameter = w - strokeW
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // 背景环
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW)
            )

            // 各扇形
            var startAngle = -90f
            items.forEachIndexed { i, (_, value) ->
                val sweep = value.toFloat() / total * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = palette[i % palette.size],
                        startAngle = startAngle,
                        sweepAngle = sweep - 0.6f, // 扇形间留 0.6 度缝隙
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW)
                    )
                }
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerTitle.isNotBlank()) {
                Text(
                    centerTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = centerVariant
                )
            }
            if (centerValue.isNotBlank()) {
                Text(
                    centerValue,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = centerColor
                )
            }
        }
    }
}

// ── 24-hour heat strip ──────────────────────────────────────────────────────
@Composable
fun HourHeatStrip(
    hours: List<HourStat>,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.primary
) {
    if (hours.isEmpty()) {
        EmptyChartHint(modifier)
        return
    }
    val maxV = (hours.maxOf { it.count }).coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            hours.forEach { h ->
                val ratio = h.count.toFloat() / maxV
                val cellColor = if (h.count == 0)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                else
                    baseColor.copy(alpha = (0.18f + 0.82f * ratio).coerceIn(0f, 1f))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(cellColor),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (h.count > 0) {
                        Text(
                            if (h.count > 99) "99+" else h.count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = if (ratio > 0.55f) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        // 4/8/12/16/20 时刻度
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(0, 4, 8, 12, 16, 20).forEach { h ->
                Text(
                    "$h",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Empty hint ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyChartHint(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "暂无数据",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
