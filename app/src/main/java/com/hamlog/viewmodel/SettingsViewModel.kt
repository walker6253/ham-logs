package com.hamlog.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.AppPreferences
import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.repository.LogRepository
import com.hamlog.util.AdifExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId

data class SettingsUiState(
    val isSmartMode: Boolean = true, val totalContacts: Int = 0,
    val isExporting: Boolean = false, val exportComplete: Boolean = false,
    val exportUri: Uri? = null, val selectedTimezone: ZoneId = ZoneId.of("UTC")
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val repository: LogRepository

    init {
        val db = try { AppDatabase.getInstance(application) }
        catch (e: Exception) { Log.e("SettingsVM", "DB", e); AppDatabase.getInstance(application) }
        repository = LogRepository(db)
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try { repository.getTotalCount().catch {}.collect { _uiState.value = _uiState.value.copy(totalContacts = it) } }
            catch (_: Exception) {}
        }
    }

    fun setTimezone(zoneId: ZoneId) { _uiState.value = _uiState.value.copy(selectedTimezone = zoneId); AppPreferences.setTimezone(zoneId) }
    fun toggleSmartMode() { _uiState.value = _uiState.value.copy(isSmartMode = !_uiState.value.isSmartMode) }

    fun exportAdif() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val contacts: List<ContactRecord> = repository.getAllContacts().first()
                val content = AdifExporter.export(contacts)
                val file = File(getApplication<Application>().cacheDir, "hamlog_export.adi")
                file.writeText(content)
                val uri = FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", file)
                _uiState.value = _uiState.value.copy(isExporting = false, exportComplete = true, exportUri = uri)
            } catch (_: Exception) { _uiState.value = _uiState.value.copy(isExporting = false) }
        }
    }

    fun resetExportState() { _uiState.value = _uiState.value.copy(exportComplete = false, exportUri = null) }
}
