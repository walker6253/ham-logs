package com.hamlog.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hamlog.ui.theme.AlxOnSurface
import com.hamlog.ui.theme.AlxOutline
import com.hamlog.ui.theme.AlxPrimary
import com.hamlog.ui.theme.AlxSurfaceContainerLow
import com.hamlog.ui.theme.AlxTertiary
import com.hamlog.ui.theme.LocalSurfaceContainerHigh
import com.hamlog.ui.theme.LocalSurfaceContainerLow
import com.hamlog.ui.theme.LocalSurfaceContainerLowest
import java.time.LocalDate
import java.time.YearMonth

/**
 * Alexandria-style date picker dialog.
 *
 * Matches the HTML design: rounded-[2rem] card, Noto-Serif month/year headline,
 * circular day cells, primary-filled selected day, tertiary+underline for today,
 * and pill-shaped action buttons.
 */
@Composable
fun AlxDatePickerDialog(
    initialEpochDay: Long,
    onDismiss: () -> Unit,
    onConfirm: (epochDay: Long) -> Unit
) {
    val today = LocalDate.now()
    var selected by remember { mutableStateOf(LocalDate.ofEpochDay(initialEpochDay)) }
    var yearMonth by remember { mutableStateOf(YearMonth.of(selected.year, selected.month)) }

    val surfaceLowest = LocalSurfaceContainerLowest.current
    val surfaceLow    = LocalSurfaceContainerLow.current
    val surfaceHigh   = LocalSurfaceContainerHigh.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 360.dp),
            shape = RoundedCornerShape(32.dp),         // rounded-[2rem]
            color = surfaceLowest,
            shadowElevation = 24.dp
        ) {
            Column {

                // ── Header ────────────────────────────────────────────────────
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month/Year title with dropdown arrow
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = {}) // placeholder for year/month picker
                        ) {
                            Text(
                                text = "${yearMonth.year}年 ${yearMonth.monthValue}月",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AlxOutline,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Prev / Next month buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NavButton(Icons.Default.ChevronLeft) {
                                yearMonth = yearMonth.minusMonths(1)
                            }
                            NavButton(Icons.Default.ChevronRight) {
                                yearMonth = yearMonth.plusMonths(1)
                            }
                        }
                    }

                    // ── Weekday header ────────────────────────────────────────
                    // HTML: 一 二 三 四 五 六 日  (Monday-first)
                    val weekdays = listOf("一","二","三","四","五","六","日")
                    Row(Modifier.fillMaxWidth()) {
                        weekdays.forEach { wd ->
                            Box(Modifier.weight(1f).padding(vertical = 8.dp), Alignment.Center) {
                                Text(
                                    wd,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = AlxOutline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ── Day grid ──────────────────────────────────────────────
                    val firstDay = yearMonth.atDay(1)
                    // dayOfWeek: Mon=1 … Sun=7; offset = (dow - 1) so Mon→0 blanks
                    val startOffset = (firstDay.dayOfWeek.value - 1)
                    val daysInMonth = yearMonth.lengthOfMonth()
                    val totalCells = startOffset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column {
                        repeat(rows) { row ->
                            Row(Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startOffset + 1
                                    Box(
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNum in 1..daysInMonth) {
                                            val date = yearMonth.atDay(dayNum)
                                            val isSelected = date == selected
                                            val isToday = date == today

                                            when {
                                                isSelected -> {
                                                    // bg-primary text-on-primary rounded-full shadow
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(AlxPrimary)
                                                            .clickable { selected = date },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "$dayNum",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                                isToday -> {
                                                    // text-tertiary font-bold underline
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .clickable { selected = date },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "$dayNum",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                textDecoration = TextDecoration.Underline
                                                            ),
                                                            color = AlxTertiary
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    // normal day: rounded-full hover bg
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .clickable { selected = date },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "$dayNum",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Actions ───────────────────────────────────────────────────
                // bg-surface-container-low/50, pill buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceLow.copy(alpha = 0.5f))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 取消
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "取消".uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // 确认 — bg-primary pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AlxPrimary)
                            .clickable { onConfirm(selected.toEpochDay()) }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "确认".uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val surfaceLow = LocalSurfaceContainerLow.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
