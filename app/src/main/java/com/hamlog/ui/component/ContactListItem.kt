package com.hamlog.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import com.hamlog.data.entity.ContactRecord
import com.hamlog.ui.theme.AlxPrimary
import com.hamlog.ui.theme.AlxOnSurface
import com.hamlog.ui.theme.AlxOnSurfaceVariant
import com.hamlog.ui.theme.AlxOutline
import com.hamlog.ui.theme.LocalSurfaceContainerLowest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ── Accent colour per mode ────────────────────────────────────────────────────
// Matches image: USB→blue, CW→green, FM→amber/gold, others→primary
private fun modeAccentColor(mode: String): Color = when (mode.uppercase()) {
    "USB", "SSB"        -> Color(0xFFC9A227)   // amber/gold
    "LSB"                -> Color(0xFFFF8C00)   // orange
    "CW"                 -> Color(0xFF3DAA6B)   // green
    "FM", "AM"           -> Color(0xFFD93636)   // red
    "FT8", "FT4",
    "RTTY", "PSK31"     -> Color(0xFF8B5CF6)   // purple
    else                -> Color(0xFF094CB2)
}

// Badge colours: bg + text
private data class BadgeColors(val bg: Color, val fg: Color)
private fun modeBadgeColors(mode: String): BadgeColors = when (mode.uppercase()) {
    "USB", "SSB"        -> BadgeColors(Color(0xFFFFF3C4), Color(0xFF7A5C00))
    "LSB"                -> BadgeColors(Color(0xFFFFE0B2), Color(0xFFB26500))
    "CW"                 -> BadgeColors(Color(0xFFCCF0DC), Color(0xFF1E6E45))
    "FM", "AM"           -> BadgeColors(Color(0xFFFFE0E0), Color(0xFF8B0000))
    "FT8", "FT4",
    "RTTY", "PSK31"     -> BadgeColors(Color(0xFFEDE9FE), Color(0xFF5B21B6))
    else                -> BadgeColors(Color(0xFFE8ECFD), Color(0xFF094CB2))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListItem(
    contact: ContactRecord,
    onDelete: () -> Unit,
    timezone: TimeZone = TimeZone.getTimeZone("UTC"),
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormat = remember(timezone) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { this.timeZone = timezone }
    }
    val timeString = remember(contact.createdAt, timezone) {
        timeFormat.format(Date(contact.createdAt))
    }

    val accentColor = remember(contact.mode) { modeAccentColor(contact.mode) }
    val badgeColors  = remember(contact.mode) { modeBadgeColors(contact.mode) }
    val surfaceLowest = LocalSurfaceContainerLowest.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = onDelete),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = surfaceLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

            // ── Left accent bar ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(accentColor)
                    .defaultMinSize(minHeight = 72.dp)
            )

            // ── Content ───────────────────────────────────────────────────────
            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {

                // Row 1: Callsign + QRZ  |  Time + Mode badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Callsign (Serif, deep blue, bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val uriHandler = LocalUriHandler.current
                        Text(
                            contact.callsign,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = AlxPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "QRZ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = AlxOutline,
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://www.qrz.com/db/${contact.callsign}")
                            }
                        )
                    }

                    // Time + Mode badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = AlxOutline.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            timeString,
                            style = MaterialTheme.typography.labelSmall,
                            color = AlxOutline.copy(alpha = 0.7f)
                        )
                        if (contact.mode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(badgeColors.bg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    contact.mode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = badgeColors.fg
                                )
                            }
                        }
                    }
                }

                // Row 2: Frequency + RST sent + RST recv (+ power if present)
                val hasDetails = contact.frequencyMHz > 0 ||
                    contact.rstSent.isNotBlank() ||
                    contact.rstReceived.isNotBlank() ||
                    contact.powerTx.isNotBlank() ||
                    contact.powerRx.isNotBlank()

                if (hasDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Frequency
                        if (contact.frequencyMHz > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "频率 ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlxOutline
                                )
                                Text(
                                    "${contact.frequencyMHz} MHz",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AlxOnSurface
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }

                        // Right: RST发 + RST收 + power
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (contact.rstSent.isNotBlank()) {
                                InlineDetail("Set", contact.rstSent)
                            }
                            if (contact.rstReceived.isNotBlank()) {
                                InlineDetail("Rcv", contact.rstReceived)
                            }
                            if (contact.powerTx.isNotBlank()) {
                                InlineDetail("PS", contact.powerTx)
                            }
                            if (contact.powerRx.isNotBlank()) {
                                InlineDetail("PR", contact.powerRx)
                            }
                        }
                    }
                }

                // Row 3: Notes
                if (contact.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        contact.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AlxOutline.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── InlineDetail ──────────────────────────────────────────────────────────────
// "RST发 59" — label in outline grey, value in onSurface
@Composable
private fun InlineDetail(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label ",
            style = MaterialTheme.typography.labelSmall,
            color = AlxOutline
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = AlxOnSurface
        )
    }
}
