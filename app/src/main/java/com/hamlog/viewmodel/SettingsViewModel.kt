package com.hamlog.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.repository.LogRepository
import com.hamlog.util.AdifExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val isSmartMode: Boolean = true,
    val totalContacts: Int = 0,
    val isExporting: Boolean = false,
    val exportComplete: Boolean = false,
    val exportUri: Uri? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LogRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getTotalCount().collect { count ->
                _uiState.value = _uiState.value.copy(totalContacts = count)
            }
        }
    }

    fun toggleSmartMode() {
        _uiState.value = _uiState.value.copy(
            isSmartMode = !_uiState.value.isSmartMode
        )
    }

    fun exportAdif() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val contacts: List<ContactRecord> = repository.getAllContacts().first()
                val adifContent = AdifExporter.export(contacts)

                val file = File(getApplication<Application>().cacheDir, "hamlog_export.adi")
                file.writeText(adifContent)

                val uri = FileProvider.getUriForFile(
                    getApplication(),
                    ".fileprovider",
                    file
                )

                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportComplete = true,
                    exportUri = uri
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    fun resetExportState() {
        _uiState.value = _uiState.value.copy(exportComplete = false, exportUri = null)
    }
}
