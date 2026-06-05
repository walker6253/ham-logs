package com.hamlog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.repository.LogRepository
import com.hamlog.util.BandUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

// ── Granularity & Range ──────────────────────────────────────────────────────
enum class TrendGranularity(val label: String) {
    DAILY("按天"),
    WEEKLY("按周"),
    MONTHLY("按月")
}

enum class TrendRange(val label: String, val days: Int) {
    DAYS_7("7 天", 7),
    DAYS_30("30 天", 30),
    DAYS_90("90 天", 90),
    DAYS_180("半年", 180),
    DAYS_365("一年", 365)
}

// ── Data classes ────────────────────────────────────────────────────────────
data class OverviewStats(
    val totalContacts: Int = 0,
    val distinctCallsigns: Int = 0,
    val activeDays: Int = 0,
    val bandsUsed: Int = 0,
    val modesUsed: Int = 0,
    val firstEpochDay: Long? = null,
    val lastEpochDay: Long? = null
)

data class TrendPoint(
    val key: String,        // 用于比较 (e.g. "2025-01-15", "2025-W03", "2025-01")
    val label: String,      // 用于展示
    val value: Int
)

data class TrendData(
    val points: List<TrendPoint> = emptyList(),
    val maxValue: Int = 0
)

data class BandStat(
    val band: String,
    val count: Int,
    val percentage: Float
)

data class ModeStat(
    val mode: String,
    val count: Int,
    val percentage: Float
)

data class HourStat(
    val hour: Int,
    val count: Int
)

data class CallsignStat(
    val callsign: String,
    val count: Int,
    val lastBand: String,
    val lastMode: String
)

data class StatsUiState(
    val isLoading: Boolean = true,
    val overview: OverviewStats = OverviewStats(),
    val trend: TrendData = TrendData(),
    val trendGranularity: TrendGranularity = TrendGranularity.DAILY,
    val trendRange: TrendRange = TrendRange.DAYS_30,
    val bandDistribution: List<BandStat> = emptyList(),
    val modeDistribution: List<ModeStat> = emptyList(),
    val hourDistribution: List<HourStat> = emptyList(),
    val topCallsigns: List<CallsignStat> = emptyList(),
    val hasAnyData: Boolean = false
)

// ── ViewModel ───────────────────────────────────────────────────────────────
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val repository: LogRepository
    private val tz: ZoneId = ZoneId.of("Asia/Shanghai")

    init {
        val db = try { AppDatabase.getInstance(application) }
        catch (e: Exception) { Log.e("StatsVM", "DB", e); AppDatabase.getInstance(application) }
        repository = LogRepository(db)

        // 概览数据：嵌套 combine（6 路 Flow 拆为 3+3 避免类型推断问题）
        combine(
            combine(
                repository.getAllContactsForStats(),
                repository.getDistinctCallsignCount(),
                repository.getActiveDaysCount()
            ) { contacts, cs, days -> Triple(contacts, cs, days) },
            combine(
                repository.getFirstContactDate(),
                repository.getLastContactDate(),
                repository.getModeDistribution()
            ) { first, last, mode -> Triple(first, last, mode) }
        ) { left, right ->
            buildState(left.first, left.second, left.third, right.first, right.second, right.third)
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    private fun buildState(
        contacts: List<ContactRecord>,
        distinctCs: Int,
        activeDays: Int,
        firstDate: Long?,
        lastDate: Long?,
        modeDist: List<com.hamlog.data.dao.ModeCount>
    ): StatsUiState {
        val cur = _uiState.value
        val total = contacts.size
        if (total == 0) {
            return cur.copy(
                isLoading = false,
                hasAnyData = false,
                overview = OverviewStats(0, 0, 0, 0, 0, null, null),
                trend = TrendData(),
                bandDistribution = emptyList(),
                modeDistribution = emptyList(),
                hourDistribution = emptyList(),
                topCallsigns = emptyList()
            )
        }

        // ── Band distribution (from frequencyMHz) ─────────────────────────
        val bandCount = HashMap<String, Int>()
        contacts.forEach { c ->
            val b = BandUtil.getBand(c.frequencyMHz)
            val key = b.ifBlank { "其他" }
            bandCount[key] = (bandCount[key] ?: 0) + 1
        }
        val bandList = bandCount.entries
            .sortedByDescending { it.value }
            .map { BandStat(it.key, it.value, it.value.toFloat() / total) }

        // ── Mode distribution ─────────────────────────────────────────────
        val modeList: List<ModeStat> = if (modeDist.isNotEmpty()) {
            modeDist
                .filter { it.mode.isNotBlank() }
                .map { ModeStat(it.mode, it.count, it.count.toFloat() / total) }
        } else {
            val mc = HashMap<String, Int>()
            contacts.forEach { c ->
                val m = c.mode.ifBlank { "其他" }
                mc[m] = (mc[m] ?: 0) + 1
            }
            mc.entries.sortedByDescending { it.value }
                .map { ModeStat(it.key, it.value, it.value.toFloat() / total) }
        }

        // ── Hour distribution (from createdAt) ────────────────────────────
        val hourArr = IntArray(24)
        contacts.forEach { c ->
            val zdt = Instant.ofEpochMilli(c.createdAt).atZone(tz)
            val h = zdt.hour
            hourArr[h] = hourArr[h] + 1
        }
        val hourList = hourArr.mapIndexed { idx, v -> HourStat(idx, v) }

        // ── Top callsigns (Top 10) ────────────────────────────────────────
        val csCount = LinkedHashMap<String, Int>()
        val csLastBand = HashMap<String, String>()
        val csLastMode = HashMap<String, String>()
        contacts.sortedByDescending { it.createdAt }.forEach { c ->
            val key = c.callsign.trim().uppercase()
            if (key.isBlank()) return@forEach
            csCount[key] = (csCount[key] ?: 0) + 1
            if (!csLastBand.containsKey(key)) {
                csLastBand[key] = BandUtil.getBand(c.frequencyMHz)
                csLastMode[key] = c.mode
            }
        }
        val topList = csCount.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { (cs, cnt) ->
                CallsignStat(cs, cnt, csLastBand[cs] ?: "", csLastMode[cs] ?: "")
            }

        // ── Trend data ────────────────────────────────────────────────────
        val trend = buildTrend(contacts, cur.trendGranularity, cur.trendRange)

        return cur.copy(
            isLoading = false,
            hasAnyData = true,
            overview = OverviewStats(
                totalContacts = total,
                distinctCallsigns = distinctCs,
                activeDays = activeDays,
                bandsUsed = bandList.size,
                modesUsed = modeList.size,
                firstEpochDay = firstDate,
                lastEpochDay = lastDate
            ),
            trend = trend,
            bandDistribution = bandList,
            modeDistribution = modeList,
            hourDistribution = hourList,
            topCallsigns = topList
        )
    }

    private fun buildTrend(
        contacts: List<ContactRecord>,
        granularity: TrendGranularity,
        range: TrendRange
    ): TrendData {
        if (contacts.isEmpty()) return TrendData()

        val now = Instant.now().atZone(tz)
        val cutoffMs = now.minusDays(range.days.toLong()).toInstant().toEpochMilli()
        val filtered = contacts.filter { it.createdAt >= cutoffMs }
        if (filtered.isEmpty()) return TrendData()

        val points = when (granularity) {
            TrendGranularity.DAILY -> {
                val byDay = HashMap<LocalDate, Int>()
                filtered.forEach { c ->
                    val d = Instant.ofEpochMilli(c.createdAt).atZone(tz).toLocalDate()
                    byDay[d] = (byDay[d] ?: 0) + 1
                }
                val firstDay = filtered.minOf { Instant.ofEpochMilli(it.createdAt).atZone(tz).toLocalDate() }
                val lastDay = filtered.maxOf { Instant.ofEpochMilli(it.createdAt).atZone(tz).toLocalDate() }
                val days = generateSequence(firstDay) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(lastDay) }
                days.map { d ->
                    val cnt = byDay[d] ?: 0
                    TrendPoint(
                        key = d.toString(),
                        label = "${d.monthValue}/${d.dayOfMonth}",
                        value = cnt
                    )
                }.toList()
            }
            TrendGranularity.WEEKLY -> {
                val wf = WeekFields.of(Locale.getDefault())
                val byWeek = HashMap<String, Int>()
                val labels = HashMap<String, String>()
                filtered.forEach { c ->
                    val z = Instant.ofEpochMilli(c.createdAt).atZone(tz)
                    val week = z.get(wf.weekOfWeekBasedYear())
                    val year = z.get(wf.weekBasedYear())
                    val key = "$year-W${week.toString().padStart(2, '0')}"
                    byWeek[key] = (byWeek[key] ?: 0) + 1
                    if (!labels.containsKey(key)) {
                        val ld = z.toLocalDate()
                        labels[key] = "${ld.monthValue}/${ld.dayOfMonth}"
                    }
                }
                byWeek.entries.sortedBy { it.key }
                    .map { (k, v) -> TrendPoint(k, labels[k] ?: k, v) }
            }
            TrendGranularity.MONTHLY -> {
                val byMonth = HashMap<String, Int>()
                val labels = HashMap<String, String>()
                filtered.forEach { c ->
                    val z = Instant.ofEpochMilli(c.createdAt).atZone(tz)
                    val ym = "%04d-%02d".format(z.year, z.monthValue)
                    byMonth[ym] = (byMonth[ym] ?: 0) + 1
                    if (!labels.containsKey(ym)) {
                        labels[ym] = "${z.year}年${z.monthValue}月"
                    }
                }
                byMonth.entries.sortedBy { it.key }
                    .map { (k, v) -> TrendPoint(k, labels[k] ?: k, v) }
            }
        }

        val maxV = points.maxOfOrNull { it.value } ?: 0
        return TrendData(points, maxV)
    }

    fun setGranularity(g: TrendGranularity) {
        val cur = _uiState.value
        _uiState.value = cur.copy(trendGranularity = g)
        reloadTrend()
    }

    fun setRange(r: TrendRange) {
        val cur = _uiState.value
        _uiState.value = cur.copy(trendRange = r)
        reloadTrend()
    }

    private fun reloadTrend() {
        viewModelScope.launch {
            try {
                val contacts = repository.getAllContactsForStats().first()
                val cur = _uiState.value
                val trend = buildTrend(contacts, cur.trendGranularity, cur.trendRange)
                _uiState.value = cur.copy(trend = trend)
            } catch (_: Exception) {}
        }
    }
}
