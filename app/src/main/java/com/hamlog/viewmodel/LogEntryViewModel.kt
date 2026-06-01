package com.hamlog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.repository.LogRepository
import com.hamlog.util.ParsedFields
import com.hamlog.util.SmartInputParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class LogEntryUiState(
    val dateEpochDay: Long = LocalDate.now(ZoneId.of("Asia/Shanghai")).toEpochDay(),
    val dateString: String = "",
    val contacts: List<ContactRecord> = emptyList(),
    val isSmartMode: Boolean = true,
    val smartInput: String = "",
    val parsedFields: ParsedFields = ParsedFields(),
    // Independent fields
    val callsign: String = "",
    val frequency: String = "",
    val mode: String = "",
    val rstSent: String = "",
    val rstReceived: String = "",
    val powerTx: String = "",
    val powerRx: String = "",
    val notes: String = "",
    val showSavedToast: Boolean = false,
    val showDeleteConfirm: ContactRecord? = null
)

class LogEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LogRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    fun init(dateEpochDay: Long) {
        val localDate = LocalDate.ofEpochDay(dateEpochDay)
        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        val dateString = when {
            localDate == today -> "今天"
            localDate == today.minusDays(1) -> "昨天"
            else -> "年月日"
        }
        _uiState.value = _uiState.value.copy(
            dateEpochDay = dateEpochDay,
            dateString = dateString
        )
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            repository.getContactsByDate(_uiState.value.dateEpochDay).collect { contacts ->
                _uiState.value = _uiState.value.copy(contacts = contacts)
            }
        }
    }

    fun toggleInputMode() {
        _uiState.value = _uiState.value.copy(
            isSmartMode = !_uiState.value.isSmartMode
        )
    }

    fun onSmartInputChanged(input: String) {
        val parsed = SmartInputParser.parse(input)
        _uiState.value = _uiState.value.copy(
            smartInput = input,
            parsedFields = parsed
        )
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "callsign" -> _uiState.value.copy(callsign = value.uppercase())
            "frequency" -> _uiState.value.copy(frequency = value)
            "mode" -> _uiState.value.copy(mode = value)
            "rstSent" -> _uiState.value.copy(rstSent = value)
            "rstReceived" -> _uiState.value.copy(rstReceived = value)
            "powerTx" -> _uiState.value.copy(powerTx = value)
            "powerRx" -> _uiState.value.copy(powerRx = value)
            "notes" -> _uiState.value.copy(notes = value)
            else -> _uiState.value
        }
    }

    fun saveContact() {
        viewModelScope.launch {
            val state = _uiState.value
            val contact = if (state.isSmartMode) {
                val p = state.parsedFields
                ContactRecord(
                    dateEpochDay = state.dateEpochDay,
                    callsign = p.callsign,
                    frequencyMHz = p.frequencyMHz.toDoubleOrNull() ?: 0.0,
                    mode = p.mode,
                    rstSent = p.rstSent,
                    rstReceived = p.rstReceived,
                    powerTx = p.powerTx,
                    powerRx = p.powerRx,
                    notes = p.notes
                )
            } else {
                ContactRecord(
                    dateEpochDay = state.dateEpochDay,
                    callsign = state.callsign.uppercase(),
                    frequencyMHz = state.frequency.toDoubleOrNull() ?: 0.0,
                    mode = state.mode,
                    rstSent = state.rstSent,
                    rstReceived = state.rstReceived,
                    powerTx = state.powerTx,
                    powerRx = state.powerRx,
                    notes = state.notes
                )
            }

            repository.insertContact(contact)

            // Reset input
            _uiState.value = _uiState.value.copy(
                smartInput = "",
                parsedFields = ParsedFields(),
                callsign = "",
                frequency = "",
                mode = "",
                rstSent = "",
                rstReceived = "",
                powerTx = "",
                powerRx = "",
                notes = "",
                showSavedToast = true
            )
        }
    }

    fun clearSavedToast() {
        _uiState.value = _uiState.value.copy(showSavedToast = false)
    }

    fun requestDelete(contact: ContactRecord) {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = contact)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
    }

    fun confirmDelete() {
        val contact = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            repository.deleteContact(contact)
            _uiState.value = _uiState.value.copy(showDeleteConfirm = null)
        }
    }
}
