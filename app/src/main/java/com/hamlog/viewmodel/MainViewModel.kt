package com.hamlog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.data.AppDatabase
import com.hamlog.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class DateItem(val dateEpochDay: Long, val dateString: String, val contactCount: Int, val isToday: Boolean)

data class MainUiState(
    val dates: List<DateItem> = emptyList(), val isLoading: Boolean = true,
    val todayEpochDay: Long = LocalDate.now(ZoneId.of("Asia/Shanghai")).toEpochDay()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val repository: LogRepository

    init {
        val db = try { AppDatabase.getInstance(application) }
        catch (e: Exception) { Log.e("MainVM", "DB", e); AppDatabase.getInstance(application) }
        repository = LogRepository(db)
        loadDates()
    }

    private fun loadDates() {
        viewModelScope.launch {
            try {
                repository.getAllDatesWithCount().catch {}.collect { list ->
                    val items = list.mapNotNull { dc ->
                        try { DateItem(dc.dateEpochDay, fmt(LocalDate.ofEpochDay(dc.dateEpochDay)), dc.count, dc.dateEpochDay == _uiState.value.todayEpochDay) }
                        catch (_: Exception) { null }
                    }
                    _uiState.value = _uiState.value.copy(dates = items, isLoading = false)
                }
            } catch (_: Exception) { _uiState.value = _uiState.value.copy(isLoading = false) }
        }
    }

    private fun fmt(d: LocalDate): String {
        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        return when { d == today -> "今天"; d == today.minusDays(1) -> "昨天"; else -> "${d.year}年${d.monthValue}月${d.dayOfMonth}日" }
    }
}
