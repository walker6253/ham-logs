package com.hamlog.util

import com.hamlog.data.entity.ContactRecord
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AdifImporter {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    )

    data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val errors: List<String>
    )

    fun parse(content: String): List<ContactRecord> {
        val records = mutableListOf<ContactRecord>()
        val lines = content.lines()
        var inHeader = true
        val currentFields = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (inHeader) {
                if (trimmed.uppercase().contains("<EOH>")) {
                    inHeader = false
                }
                continue
            }

            if (trimmed.uppercase().contains("<EOR>")) {
                // End of record - build ContactRecord
                val contact = buildContact(currentFields.toMap())
                if (contact != null) {
                    records.add(contact)
                }
                currentFields.clear()
                continue
            }

            // Parse fields: <NAME:LENGTH>VALUE
            parseFields(trimmed, currentFields)
        }

        return records
    }

    private fun parseFields(line: String, fields: MutableMap<String, String>) {
        var index = 0
        while (index < line.length) {
            if (line[index] != '<') {
                index++
                continue
            }
            val endBracket = line.indexOf('>', index)
            if (endBracket == -1) break

            val tagContent = line.substring(index + 1, endBracket)
            val colonIndex = tagContent.indexOf(':')
            if (colonIndex == -1) {
                index = endBracket + 1
                continue
            }

            val fieldName = tagContent.substring(0, colonIndex).uppercase().trim()
            val lengthStr = tagContent.substring(colonIndex + 1).trim()
            val length = lengthStr.toIntOrNull() ?: 0

            val valueStart = endBracket + 1
            val valueEnd = (valueStart + length).coerceAtMost(line.length)
            val value = line.substring(valueStart, valueEnd).trim()

            if (fieldName.isNotEmpty()) {
                fields[fieldName] = value
            }

            index = valueEnd
        }
    }

    private fun buildContact(fields: Map<String, String>): ContactRecord? {
        val call = fields["CALL"] ?: return null
        if (call.isBlank()) return null

        val qsoDateStr = fields["QSO_DATE"] ?: fields["QSO_DATE_OFF"] ?: ""
        val dateEpochDay = parseDate(qsoDateStr) ?: LocalDate.now().toEpochDay()

        val freqStr = fields["FREQ"] ?: fields["FREQ_RX"] ?: "0"
        val frequencyMHz = freqStr.toDoubleOrNull() ?: 0.0

        val mode = fields["MODE"] ?: ""
        val rstSent = fields["RST_SENT"] ?: fields["RST_S"] ?: ""
        val rstReceived = fields["RST_RCVD"] ?: fields["RST_R"] ?: ""

        val timeStr = fields["TIME_ON"] ?: fields["TIME_OFF"] ?: "000000"
        val createdAt = parseDateTime(dateEpochDay, timeStr)

        val powerTx = fields["TX_PWR"] ?: ""
        val powerRx = fields["RX_PWR"] ?: ""
        val notes = fields["COMMENT"] ?: fields["NOTES"] ?: fields["QSLMSG"] ?: ""

        return ContactRecord(
            dateEpochDay = dateEpochDay,
            callsign = call.uppercase().trim(),
            frequencyMHz = frequencyMHz,
            mode = mode.trim(),
            rstSent = rstSent.trim(),
            rstReceived = rstReceived.trim(),
            powerTx = powerTx.trim(),
            powerRx = powerRx.trim(),
            notes = notes.trim(),
            createdAt = createdAt
        )
    }

    private fun parseDate(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        for (fmt in dateFormats) {
            try {
                return LocalDate.parse(dateStr, fmt).toEpochDay()
            } catch (_: DateTimeParseException) {}
        }
        return null
    }

    private fun parseDateTime(epochDay: Long, timeStr: String): Long {
        val localDate = LocalDate.ofEpochDay(epochDay)
        try {
            val hour = timeStr.substring(0, 2).toIntOrNull() ?: 0
            val minute = timeStr.substring(2, 4).toIntOrNull() ?: 0
            val second = if (timeStr.length >= 6) timeStr.substring(4, 6).toIntOrNull() ?: 0 else 0
            return localDate.atTime(hour, minute, second)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            return localDate.atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli()
        }
    }
}
