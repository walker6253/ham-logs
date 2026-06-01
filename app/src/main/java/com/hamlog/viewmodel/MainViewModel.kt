package com.hamlog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.data.AppDatabase
import com.hamlog.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class DateItem(
    val dateEpochDay: Long,
    val dateString: String,
    val contactCount: Int,
    val isToday: Boolean
)

data class MainUiState(
    val dates: List<DateItem> = emptyList(),
    val isLoading: Boolean = true,
    val currentGrid: String = "",
    val currentAddress: String = "",
    val todayEpochDay: Long = LocalDate.now(ZoneId.of("Asia/Shanghai")).toEpochDay()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LogRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadDates()
    }

    private fun loadDates() {
        viewModelScope.launch {
            repository.getAllDates().collect { dateList ->
                val dateItems = dateList.map { dateEpochDay ->
                    val localDate = LocalDate.ofEpochDay(dateEpochDay)
                    val contactCount = repository.getContactCountByDate(dateEpochDay)
                    var count = 0
                    // Need to collect count separately
                    DateItem(
                        dateEpochDay = dateEpochDay,
                        dateString = formatDate(localDate),
                        contactCount = 0,
                        isToday = dateEpochDay == _uiState.value.todayEpochDay
                    )
                }
                _uiState.value = _uiState.value.copy(
                    dates = dateItems,
                    isLoading = false
                )
            }
        }
    }

    fun updateLocation(grid: String, address: String) {
        _uiState.value = _uiState.value.copy(
            currentGrid = grid,
            currentAddress = address
        )
    }

    fun refreshDates() {
        loadDates()
    }

    private fun formatDate(localDate: LocalDate): String {
        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        return when {
            localDate == today -> "今天"
            localDate == today.minusDays(1) -> "昨天"
            else -> "年月日"
        }
    }
}
