package com.hamlog.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hamlog.data.entity.ContactRecord

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListItem(
    contact: ContactRecord,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onDelete
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Row 1: Callsign (prominent) + Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.callsign,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                if (contact.mode.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(contact.mode, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Frequency + RST
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (contact.frequencyMHz > 0) {
                    DetailItem("频率", "${contact.frequencyMHz} MHz")
                }
                if (contact.rstSent.isNotBlank()) {
                    DetailItem("RST发", contact.rstSent)
                }
                if (contact.rstReceived.isNotBlank()) {
                    DetailItem("RST收", contact.rstReceived)
                }
            }

            // Row 3: Power + Notes
            if (contact.powerTx.isNotBlank() || contact.powerRx.isNotBlank() || contact.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (contact.powerTx.isNotBlank()) {
                        DetailItem("发射", contact.powerTx)
                    }
                    if (contact.powerRx.isNotBlank()) {
                        DetailItem("接收", contact.powerRx)
                    }
                }
                if (contact.notes.isNotBlank()) {
                    Text(
                        text = contact.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row {
        Text(
            text = "${label}: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
