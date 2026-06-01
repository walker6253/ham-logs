package com.hamlog.util

import com.hamlog.data.entity.ContactRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AdifExporter {

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormat = SimpleDateFormat("HHmmss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(contacts: List<ContactRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("Ham Log ADIF Export")
        sb.appendLine("<ADIF_VER:5>3.1.1")
        sb.appendLine("<PROGRAMID:7>Ham Log")
        sb.appendLine("<EOH>")

        for (contact in contacts) {
            sb.append(contactToAdif(contact))
        }

        return sb.toString()
    }

    private fun contactToAdif(contact: ContactRecord): String {
        val sb = StringBuilder()
        val date = Date(contact.createdAt)
        val qsoDate = Date(contact.dateEpochDay * 86400000L)

        addField(sb, "QSO_DATE", dateFormat.format(qsoDate))
        addField(sb, "TIME_ON", timeFormat.format(date))
        addField(sb, "CALL", contact.callsign)
        addField(sb, "FREQ", contact.frequencyMHz.toString())
        addField(sb, "MODE", contact.mode)
        addField(sb, "RST_SENT", contact.rstSent)
        addField(sb, "RST_RCVD", contact.rstReceived)

        if (contact.powerTx.isNotBlank()) {
            addField(sb, "TX_PWR", contact.powerTx)
        }
        if (contact.powerRx.isNotBlank()) {
            addField(sb, "RX_PWR", contact.powerRx)
        }
        if (contact.notes.isNotBlank()) {
            addField(sb, "COMMENT", contact.notes)
        }

        sb.appendLine("<EOR>")
        return sb.toString()
    }

    private fun addField(sb: StringBuilder, name: String, value: String) {
        sb.append("<:>")
    }
}
