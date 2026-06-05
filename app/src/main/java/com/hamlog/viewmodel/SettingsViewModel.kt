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
import com.hamlog.util.CloudlogSync
import com.hamlog.util.AdifImporter
import com.hamlog.util.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId

data class SettingsUiState(
    val totalContacts: Int = 0,
    val isExporting: Boolean = false, val exportComplete: Boolean = false,
    val exportUri: Uri? = null, val selectedTimezone: ZoneId = ZoneId.of("Asia/Shanghai"),
    val isImporting: Boolean = false, val importResult: AdifImporter.ImportResult? = null,
    val isSyncing: Boolean = false, val syncResult: SyncResult? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val repository: LogRepository

    init {
        _uiState.value = _uiState.value.copy(selectedTimezone = AppPreferences.timezone.value)
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

    fun importAdif(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, importResult = null)
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.readText() ?: ""
                inputStream?.close()

                val records = AdifImporter.parse(content)
                var imported = 0
                var skipped = 0
                for (record in records) {
                    try {
                        repository.insertContact(record)
                        imported++
                    } catch (e: Exception) {
                        skipped++
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = AdifImporter.ImportResult(imported, skipped, emptyList())
                )
                loadStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = AdifImporter.ImportResult(0, 0, listOf(e.message ?: "Unknown error"))
                )
            }
        }
    }

    suspend fun getAllContactsForSync(): List<ContactRecord> {
        return repository.getAllContacts().first()
    }

    fun syncToCloudlog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncResult = null)
            try {
                val contacts = repository.getAllContacts().first()
                val result = CloudlogSync.syncContacts(
                    baseUrl = AppPreferences.cloudlogUrl.value,
                    apiKey = AppPreferences.cloudlogApiKey.value,
                    contacts = contacts,
                    callsign = AppPreferences.callsign.value,
                    gridSquare = AppPreferences.gridSquare.value,
                    stationProfileId = AppPreferences.stationProfileId.value.ifBlank { "1" },
                    onProgress = { cur, total -> }
                )
                _uiState.value = _uiState.value.copy(isSyncing = false, syncResult = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncResult = SyncResult(errors = listOf(e.message ?: "Unknown error"))
                )
            }
        }
    }

    fun dismissSyncResult() { _uiState.value = _uiState.value.copy(syncResult = null) }

    fun dismissImportResult() { _uiState.value = _uiState.value.copy(importResult = null) }
}