package com.hamlog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.AppPreferences
import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.repository.LogRepository
import com.hamlog.util.SmartInputParser
import com.hamlog.util.BandUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class LogEntryUiState(
    val dateEpochDay: Long = LocalDate.now(ZoneId.of("Asia/Shanghai")).toEpochDay(),
    val dateString: String = "",
    val contacts: List<ContactRecord> = emptyList(),
    val historicalContacts: List<ContactRecord>? = null,
    val searchCallsign: String = "",
    val isSmartMode: Boolean = true,
    val smartInput: String = "",
    val callsign: String = "",
    val frequency: String = "",
    val mode: String = "",
    val rstSent: String = "59",
    val rstReceived: String = "59",
    val powerTx: String = "100W",
    val powerRx: String = "100W",
    val notes: String = "",
    val showSavedToast: Boolean = false,
    val showDeleteConfirm: ContactRecord? = null,
    val callsignSuggestions: List<String> = emptyList(),
    val showSuggestions: Boolean = false,
    val timezone: ZoneId = ZoneId.of("UTC"),
    val qsoTime: String = ""
)

class LogEntryViewModel(application: Application) : AndroidViewModel(application) {

    // MUST be before init block
    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    private val repository: LogRepository

    private var searchJob: Job? = null
    private var historyJob: Job? = null

    private var preEditFrequency: String = ""
    private var preEditMode: String = ""

    init {
        val db = try { AppDatabase.getInstance(application) }
        catch (e: Exception) { Log.e("LogEntryVM", "DB init", e); AppDatabase.getInstance(application) }
        repository = LogRepository(db)

        viewModelScope.launch {
            try {
                AppPreferences.timezone.collect { tz ->
                    _uiState.value = _uiState.value.copy(timezone = tz)
                }
            } catch (e: Exception) {
                Log.e("LogEntryVM", "timezone collect error", e)
            }
        }
    }

    fun init(dateEpochDay: Long) {
        val localDate = LocalDate.ofEpochDay(dateEpochDay)
        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        val dateString = when {
            localDate == today -> "${localDate.year}年 今天"
            localDate == today.minusDays(1) -> "${localDate.year}年 昨天"
            else -> "${localDate.year}年${localDate.monthValue}月${localDate.dayOfMonth}日"
        }
        _uiState.value = _uiState.value.copy(dateEpochDay = dateEpochDay, dateString = dateString)
        restoreFormState(dateEpochDay)
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            try {
                repository.getContactsByDate(_uiState.value.dateEpochDay)
                    .catch { e -> Log.e("LogEntryVM", "loadContacts", e) }
                    .collect { _uiState.value = _uiState.value.copy(contacts = it) }
            } catch (e: Exception) { Log.e("LogEntryVM", "loadContacts fail", e) }
        }
    }

    fun toggleInputMode() {
        _uiState.value = _uiState.value.copy(isSmartMode = !_uiState.value.isSmartMode)
        persistFormState(_uiState.value)
    }

    fun onSmartInputChanged(input: String) {
        try {
            val parsed = SmartInputParser.parse(input)
            val s = _uiState.value
            // Capture pre-edit values when starting new input sequence
            if (s.smartInput.isBlank() && input.isNotBlank()) {
                preEditFrequency = s.frequency
                preEditMode = s.mode
            }

            _uiState.value = s.copy(
                smartInput = input,
                callsign = if (parsed.callsign.isNotBlank()) parsed.callsign else s.callsign,
                frequency = if (parsed.frequencyMHz.isNotBlank()) parsed.frequencyMHz else if (input.isBlank()) preEditFrequency else s.frequency,
                mode = if (parsed.mode.isNotBlank()) parsed.mode else if (parsed.frequencyMHz.isNotBlank()) BandUtil.autoMode(parsed.frequencyMHz.toDoubleOrNull() ?: 0.0).ifBlank { s.mode } else if (input.isBlank()) preEditMode else s.mode,
                rstSent = if (parsed.rstSent.isNotBlank()) parsed.rstSent else s.rstSent,
                rstReceived = if (parsed.rstReceived.isNotBlank()) parsed.rstReceived else s.rstReceived,
                powerTx = if (parsed.powerTx.isNotBlank()) parsed.powerTx else s.powerTx,
                powerRx = if (parsed.powerRx.isNotBlank()) parsed.powerRx else s.powerRx,
                notes = if (parsed.notes.isNotBlank()) parsed.notes else s.notes
            )
            // Capture QSO time when rstReceived is first set via smart input
            val newState = _uiState.value
            val finalState = if (parsed.rstReceived.isNotBlank() && s.rstReceived.isBlank()) {
                val now = java.time.LocalDateTime.now()
                val date = java.time.LocalDate.ofEpochDay(s.dateEpochDay)
                newState.copy(qsoTime = String.format("%02d-%02d %02d:%02d", date.monthValue, date.dayOfMonth, now.hour, now.minute))
            } else newState
            _uiState.value = finalState
            persistFormState(finalState)
            if (parsed.callsign.isNotBlank()) searchCallsigns(parsed.callsign)
            else {
                for (token in input.trim().split(Regex("\\s+"))) {
                    if (token.length >= 3 && token.any { it.isDigit() } && token.any { it.isLetter() }) {
                        searchCallsigns(token); return
                    }
                }
                clearSearch()
            }
        } catch (_: Exception) {}
    }

    fun updateField(field: String, value: String) {
        try {
            val v = if (field == "callsign") value.uppercase() else value
            val newState = when (field) {
                "callsign" -> { searchCallsigns(v); _uiState.value.copy(callsign = v) }
                "frequency" -> _uiState.value.copy(frequency = v)
                "mode" -> _uiState.value.copy(mode = v)
                "rstSent" -> _uiState.value.copy(rstSent = v)
                "rstReceived" -> _uiState.value.copy(rstReceived = v)
                "powerTx" -> _uiState.value.copy(powerTx = v)
                "powerRx" -> _uiState.value.copy(powerRx = v)
                "notes" -> _uiState.value.copy(notes = v)
                else -> _uiState.value
            }
            _uiState.value = newState
            // Capture QSO time when rstReceived is first set
            val finalState = if (field == "rstReceived" && v.isNotBlank() && newState.qsoTime.isBlank()) {
                val now = java.time.LocalDateTime.now()
                val date = java.time.LocalDate.ofEpochDay(newState.dateEpochDay)
                newState.copy(qsoTime = String.format("%02d-%02d %02d:%02d", date.monthValue, date.dayOfMonth, now.hour, now.minute))
            } else newState
            _uiState.value = finalState
            persistFormState(finalState)
            if (field == "frequency") autoModeIfNeeded()
        } catch (_: Exception) {}
    }

    private fun searchCallsigns(query: String) {
        searchJob?.cancel(); historyJob?.cancel()
        val q = query.uppercase().trim()
        if (q.isBlank() || q.length < 2) { clearSearch(); return }
        _uiState.value = _uiState.value.copy(searchCallsign = q)

        searchJob = viewModelScope.launch {
            try {
                delay(200)
                val s = repository.searchCallsigns(q)
                _uiState.value = _uiState.value.copy(callsignSuggestions = s, showSuggestions = s.isNotEmpty())
            } catch (_: Exception) {}
        }
        historyJob?.cancel()
        if (q.length >= 3) {
            historyJob = viewModelScope.launch {
                try {
                    repository.searchContactsByCallsignPrefix(q).catch {}.collect {
                        _uiState.value = _uiState.value.copy(historicalContacts = it.ifEmpty { null })
                    }
                } catch (_: Exception) {}
            }
        } else {
            _uiState.value = _uiState.value.copy(historicalContacts = null)
        }
    }

    private fun clearSearch() {
        searchJob?.cancel(); historyJob?.cancel()
        _uiState.value = _uiState.value.copy(
            callsignSuggestions = emptyList(), showSuggestions = false,
            historicalContacts = null, searchCallsign = "")
    }

    private fun clearHistorySearch() {
        historyJob?.cancel()
        _uiState.value = _uiState.value.copy(historicalContacts = null, searchCallsign = "")
    }

    fun selectCallsignSuggestion(callsign: String) {
        val s = _uiState.value
        val newSmartInput = if (s.smartInput.isBlank()) {
            callsign
        } else {
            val tokens = s.smartInput.trim().split("\\s+".toRegex())
            if (tokens.isNotEmpty()) (listOf(callsign) + tokens.drop(1)).joinToString(" ")
            else callsign
        }
        _uiState.value = s.copy(callsign = callsign, showSuggestions = false, smartInput = newSmartInput)
        viewModelScope.launch {
            try {
                val last = repository.getLastContactByCallsign(callsign) ?: return@launch
                val s2 = _uiState.value
                val newState = s2.copy(
                    frequency = if (s2.frequency.isBlank()) last.frequencyMHz.toString() else s2.frequency,
                    mode = if (s2.mode.isBlank()) last.mode else s2.mode)
                _uiState.value = newState
                persistFormState(newState)
                autoModeIfNeeded()
            } catch (_: Exception) {}
        }
    }

    fun dismissSuggestions() { _uiState.value = _uiState.value.copy(showSuggestions = false) }

    fun commitNext() {
        val text = _uiState.value.smartInput.trim()
        if (text.isBlank()) return
        val s = _uiState.value

        // Determine next empty field
        if (s.callsign.isBlank()) {
            // Try to parse callsign from text
            val tokens = text.split(Regex("\\s+"))
            for (token in tokens) {
                if (token.length in 3..6 && token.any { it.isDigit() } && token.any { it.isLetter() }) {
                    updateField("callsign", token)
                    _uiState.value = _uiState.value.copy(smartInput = "")
                    return
                }
            }
            // Just use first token as callsign
            updateField("callsign", tokens.first())
            _uiState.value = _uiState.value.copy(smartInput = "")
        } else if (s.frequency.isBlank()) {
            val parsed = SmartInputParser.parse(text)
            if (parsed.frequencyMHz.isNotBlank()) {
                updateField("frequency", parsed.frequencyMHz)
            } else {
                // Try to format as frequency
                val digits = text.filter { it.isDigit() }
                if (digits.length in 4..7) {
                    val formatted = digits.let {
                        when (it.length) {
                            4 -> "${it[0]}.${it.substring(1)}"
                            5 -> "${it.substring(0, 2)}.${it.substring(2)}"
                            6 -> "${it.substring(0, 3)}.${it.substring(3)}"
                            7 -> "${it.substring(0, 4)}.${it.substring(4)}"
                            else -> it
                        }
                    }
                    updateField("frequency", formatted)
                } else {
                    updateField("frequency", text)
                }
            }
            _uiState.value = _uiState.value.copy(smartInput = "")
        } else if (s.mode.isBlank()) {
            updateField("mode", text.uppercase())
            _uiState.value = _uiState.value.copy(smartInput = "")
        } else { onSmartInputChanged(text); _uiState.value = _uiState.value.copy(smartInput = "") }
    }

    private val prefs by lazy { getApplication<android.app.Application>().getSharedPreferences("hamlog_form", android.content.Context.MODE_PRIVATE) }

    private fun persistFormState(s: LogEntryUiState) {
        try {
            prefs.edit()
                .putString("callsign", s.callsign)
                .putString("frequency", s.frequency)
                .putString("mode", s.mode)
                .putString("rstSent", s.rstSent)
                .putString("rstReceived", s.rstReceived)
                .putString("powerTx", s.powerTx)
                .putString("powerRx", s.powerRx)
                .putString("notes", s.notes)
                .putBoolean("isSmartMode", s.isSmartMode)
                .putLong("formDateEpochDay", s.dateEpochDay)
                .apply()
        } catch (_: Exception) {}
    }

    private fun restoreFormState(dateEpochDay: Long) {
        try {
            val p = prefs
            val savedDate = p.getLong("formDateEpochDay", -1L)
            if (savedDate == dateEpochDay) {
                _uiState.value = _uiState.value.copy(
                    callsign = p.getString("callsign", "") ?: "",
                    frequency = p.getString("frequency", "") ?: "",
                    mode = p.getString("mode", "") ?: "",
                    rstSent = p.getString("rstSent", "59") ?: "59",
                    rstReceived = p.getString("rstReceived", "59") ?: "59",
                    powerTx = p.getString("powerTx", "100W") ?: "100W",
                    powerRx = p.getString("powerRx", "100W") ?: "100W",
                    notes = p.getString("notes", "") ?: "",
                    isSmartMode = p.getBoolean("isSmartMode", true)
                )
            } else {
                // Different date, clear saved form and set defaults
                prefs.edit().clear().apply()
                _uiState.value = _uiState.value.copy(rstSent = "59", rstReceived = "59", powerTx = "100W", powerRx = "100W")
            }
        } catch (_: Exception) {}
    }

    /** Auto-update mode when frequency changes. Always applies unless no frequency. */
    private fun autoModeIfNeeded() {
        val s = _uiState.value
        if (s.frequency.isBlank()) return
        val mhz = s.frequency.toDoubleOrNull() ?: return
        val suggested = BandUtil.autoMode(mhz)
        if (suggested.isNotBlank() && s.mode != suggested) {
            _uiState.value = s.copy(mode = suggested)
            persistFormState(_uiState.value)
        }
    }

    fun saveContact() {
        viewModelScope.launch {
            try {
                historyJob?.cancel()
                val s = _uiState.value
                if (s.callsign.isBlank()) return@launch
                repository.insertContact(ContactRecord(
                    dateEpochDay = s.dateEpochDay, callsign = s.callsign.uppercase().trim(),
                    frequencyMHz = s.frequency.toDoubleOrNull() ?: 0.0, mode = s.mode.trim(),
                    rstSent = s.rstSent.trim(), rstReceived = s.rstReceived.trim(),
                    powerTx = s.powerTx.trim(), powerRx = s.powerRx.trim(), notes = s.notes.trim()
                ))
                // Reset RST/Power/Notes to defaults after save
                _uiState.value = _uiState.value.copy(
                    smartInput = "", callsign = "",
                    rstSent = "59", rstReceived = "59",
                    powerTx = "100W", powerRx = "100W",
                    notes = "", qsoTime = "",
                    showSavedToast = true, callsignSuggestions = emptyList(),
                    showSuggestions = false, historicalContacts = null, searchCallsign = "")
                persistFormState(_uiState.value)
            } catch (_: Exception) {}
        }
    }

    fun clearSavedToast() { _uiState.value = _uiState.value.copy(showSavedToast = false) }
    fun requestDelete(c: ContactRecord) { _uiState.value = _uiState.value.copy(showDeleteConfirm = c) }
    fun cancelDelete() { _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }
    fun updateContact(contact: ContactRecord) {
        viewModelScope.launch {
            try { repository.updateContact(contact) } catch (_: Exception) {}
        }
    }

    fun confirmDelete() {
        val c = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch { try { repository.deleteContact(c) } catch (_: Exception) {}; _uiState.value = _uiState.value.copy(showDeleteConfirm = null) }
    }
}
