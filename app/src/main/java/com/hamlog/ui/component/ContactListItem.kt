package com.hamlog.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import com.hamlog.data.entity.ContactRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).apply { this.timeZone = timezone }
    }
    val timeString = remember(contact.createdAt, timezone) {
        timeFormat.format(Date(contact.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = onDelete),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Callsign + QRZ | spacer | Time + Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        contact.callsign,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val uriHandler = LocalUriHandler.current
                    Text(
                        "QRZ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://www.qrz.com/db/${contact.callsign}") }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        timeString,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    if (contact.mode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                contact.mode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Row 2: Frequency + RST + Power
            val hasDetails = contact.frequencyMHz > 0 ||
                contact.rstSent.isNotBlank() ||
                contact.rstReceived.isNotBlank() ||
                contact.powerTx.isNotBlank() ||
                contact.powerRx.isNotBlank()

            if (hasDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (contact.frequencyMHz > 0) {
                        DetailChip("频率", "${contact.frequencyMHz} MHz")
                    }
                    if (contact.rstSent.isNotBlank()) {
                        DetailChip("RST发", contact.rstSent)
                    }
                    if (contact.rstReceived.isNotBlank()) {
                        DetailChip("RST收", contact.rstReceived)
                    }
                    if (contact.powerTx.isNotBlank()) {
                        DetailChip("发射", contact.powerTx)
                    }
                    if (contact.powerRx.isNotBlank()) {
                        DetailChip("接收", contact.powerRx)
                    }
                }
            }

            // Row 3: Notes
            if (contact.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    contact.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
