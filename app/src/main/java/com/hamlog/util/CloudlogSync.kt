package com.hamlog.util

import com.hamlog.data.entity.ContactRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class StationInfo(
    val stationId: String,
    val stationName: String
)

data class SyncResult(
    val success: Int = 0,
    val failed: Int = 0,
    val skipped: Int = 0,
    val errors: List<String> = emptyList(),
    val lastResponse: String = ""
)

object CloudlogSync {

    private fun frequencyToBand(freqMHz: Double): String = when {
        freqMHz >= 1.8 && freqMHz < 2.0 -> "160m"
        freqMHz >= 3.5 && freqMHz < 4.0 -> "80m"
        freqMHz >= 5.0 && freqMHz < 5.5 -> "60m"
        freqMHz >= 7.0 && freqMHz < 7.3 -> "40m"
        freqMHz >= 10.0 && freqMHz < 10.2 -> "30m"
        freqMHz >= 14.0 && freqMHz < 14.35 -> "20m"
        freqMHz >= 18.0 && freqMHz < 18.2 -> "17m"
        freqMHz >= 21.0 && freqMHz < 21.45 -> "15m"
        freqMHz >= 24.89 && freqMHz < 24.99 -> "12m"
        freqMHz >= 28.0 && freqMHz < 29.7 -> "10m"
        freqMHz >= 50.0 && freqMHz < 54.0 -> "6m"
        freqMHz >= 144.0 && freqMHz < 148.0 -> "2m"
        freqMHz >= 430.0 && freqMHz < 450.0 -> "70cm"
        else -> ""
    }

    private fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMs))
    }

    private fun formatTime(epochMs: Long): String {
        val sdf = SimpleDateFormat("HHmmss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMs))
    }

    private fun adifTag(tag: String, value: String): String {
        if (value.isBlank()) return ""
        return "<$tag:${value.length}>$value"
    }

    suspend fun syncContacts(
        baseUrl: String,
        apiKey: String,
        contacts: List<ContactRecord>,
        callsign: String = "",
        gridSquare: String = "",
        stationProfileId: String = "1",
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): SyncResult = withContext(Dispatchers.IO) {
        var success = 0
        var failed = 0
        var lastResponseBody = ""
        val errors = mutableListOf<String>()

        val url = baseUrl.trimEnd('/') + "/index.php/api/qso"
        val total = contacts.size

        contacts.forEachIndexed { index, contact ->
            try {
                val band = frequencyToBand(contact.frequencyMHz)
                val mode = contact.mode.uppercase().trim()
                val adifParts = buildString {
                    append(adifTag("CALL", contact.callsign.uppercase().trim()))
                    if (band.isNotBlank()) append(adifTag("BAND", band))
                    append(adifTag("MODE", mode))
                    append(adifTag("QSO_DATE", formatDate(contact.createdAt)))
                    append(adifTag("TIME_ON", formatTime(contact.createdAt)))
                    append(adifTag("RST_SENT", contact.rstSent.trim()))
                    append(adifTag("RST_RCVD", contact.rstReceived.trim()))
                    append(adifTag("FREQ", contact.frequencyMHz.toString()))
                    val txPwr = contact.powerTx.trim().trimEnd('W', 'w').trim()
                    if (txPwr.isNotBlank()) append(adifTag("TX_PWR", txPwr))
                    val rxPwr = contact.powerRx.trim().trimEnd('W', 'w').trim()
                    if (rxPwr.isNotBlank()) append(adifTag("RX_PWR", rxPwr))
                    if (contact.notes.isNotBlank()) append(adifTag("QSLMSG", contact.notes.trim()))
                    if (gridSquare.isNotBlank()) append(adifTag("GRIDSQUARE", gridSquare))
                    if (callsign.isNotBlank()) append(adifTag("STATION_CALLSIGN", callsign.uppercase().trim()))
                    append("<EOR>")
                }
                val json = JSONObject().apply {
                    put("key", apiKey)
                    put("station_profile_id", stationProfileId)
                    put("type", "adif")
                    put("string", adifParts)
                }

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }

                val code = connection.responseCode
                val respBody = try { connection.inputStream.bufferedReader().use { it.readText() } } catch (_: Exception) { "" }
                connection.disconnect()

                if (code in 200..299 && !respBody.contains("failed")) {
                    success++
                    lastResponseBody = respBody
                } else {
                    failed++
                    errors.add(contact.callsign + ": HTTP " + code + " " + respBody.take(200))
                }
            } catch (e: Exception) {
                failed++
                errors.add(contact.callsign + ": " + (e.message ?: "unknown"))
            }

            onProgress(index + 1, total)
        }

        SyncResult(success = success, failed = failed, errors = errors, lastResponse = lastResponseBody)
    }

    suspend fun fetchStationInfo(baseUrl: String, apiKey: String): List<StationInfo> = withContext(Dispatchers.IO) {
        try {
            val url = baseUrl.trimEnd('/') + "/index.php/api/station_info/" + apiKey
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val respBody = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            val arr = org.json.JSONArray(respBody)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                StationInfo(
                    stationId = obj.optString("station_id", ""),
                    stationName = obj.optString("station_profile_name", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class ConnectionTestResult(val ok: Boolean, val message: String = "")

    suspend fun testConnection(baseUrl: String, apiKey: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        try {
            val url = baseUrl.trimEnd('/') + "/index.php/api/qso"
            val testJson = JSONObject().apply {
                put("key", apiKey)
                put("type", "adif")
                put("string", "<CALL:6>TEST01 <BAND:3>20m <MODE:3>SSB <QSO_DATE:8>20260101 <TIME_ON:6>000000 <RST_SENT:3>599 <RST_RCVD:3>599 <EOR>")
            }
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            OutputStreamWriter(connection.outputStream).use { it.write(testJson.toString()) }
            val code = connection.responseCode
            val respBody = try { connection.inputStream.bufferedReader().use { it.readText() } } catch (_: Exception) { "" }
            connection.disconnect()
            if (code in 200..299 && !respBody.contains("failed")) {
                ConnectionTestResult(true, respBody.take(200))
            } else if (code in 400..499) {
                ConnectionTestResult(true, respBody.take(200))
            } else {
                ConnectionTestResult(false, "HTTP $code $respBody")
            }
        } catch (e: Exception) {
            ConnectionTestResult(false, e.message ?: "")
        }
    }
}
