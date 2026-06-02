package com.hamlog

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZoneId

object AppPreferences {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("hamlog_prefs", Context.MODE_PRIVATE)
        val p = prefs!!
        _timezone.value = ZoneId.of(p.getString("timezone", "Asia/Shanghai") ?: "Asia/Shanghai")
        _callsign.value = p.getString("callsign", "") ?: ""
        _opName.value = p.getString("opName", "") ?: ""
        _equipment.value = p.getString("equipment", "") ?: ""
        _location.value = p.getString("location", "") ?: ""
        _gridSquare.value = p.getString("gridSquare", "") ?: ""
    }

    private val _timezone = MutableStateFlow(ZoneId.of("Asia/Shanghai"))
    val timezone: StateFlow<ZoneId> = _timezone.asStateFlow()

    private val _callsign = MutableStateFlow("")
    val callsign: StateFlow<String> = _callsign.asStateFlow()

    private val _opName = MutableStateFlow("")
    val opName: StateFlow<String> = _opName.asStateFlow()

    private val _equipment = MutableStateFlow("")
    val equipment: StateFlow<String> = _equipment.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _gridSquare = MutableStateFlow("")
    val gridSquare: StateFlow<String> = _gridSquare.asStateFlow()

    fun setTimezone(zoneId: ZoneId) {
        _timezone.value = zoneId
        prefs?.edit()?.putString("timezone", zoneId.id)?.apply()
    }

    fun setCallsign(callsign: String) {
        _callsign.value = callsign.uppercase().trim()
        prefs?.edit()?.putString("callsign", _callsign.value)?.apply()
    }

    fun setOpName(name: String) {
        _opName.value = name.trim()
        prefs?.edit()?.putString("opName", _opName.value)?.apply()
    }

    fun setEquipment(equip: String) {
        _equipment.value = equip.trim()
        prefs?.edit()?.putString("equipment", _equipment.value)?.apply()
    }

    fun setLocation(loc: String) {
        _location.value = loc.trim()
        prefs?.edit()?.putString("location", _location.value)?.apply()
    }

    fun setGridSquare(grid: String) {
        _gridSquare.value = grid.uppercase().trim()
        prefs?.edit()?.putString("gridSquare", _gridSquare.value)?.apply()
    }
}
