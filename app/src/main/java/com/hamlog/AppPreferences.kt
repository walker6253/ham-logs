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
        _cloudlogUrl.value = p.getString("cloudlogUrl", "") ?: ""
        _cloudlogApiKey.value = p.getString("cloudlogApiKey", "") ?: ""
        _stationProfileId.value = p.getString("stationProfileId", "1") ?: "1"
        _stationListJson.value = p.getString("stationListJson", "[]") ?: "[]"
        _autoUploadEnabled.value = p.getBoolean("autoUploadEnabled", false)
        _lastUpdateCheckDate.value = p.getString("lastUpdateCheckDate", "") ?: ""
        _updateIgnoredDate.value = p.getString("updateIgnoredDate", "") ?: ""
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
    private val _cloudlogUrl = MutableStateFlow("")
    val cloudlogUrl: StateFlow<String> = _cloudlogUrl.asStateFlow()

    private val _cloudlogApiKey = MutableStateFlow("")
    val cloudlogApiKey: StateFlow<String> = _cloudlogApiKey.asStateFlow()

    private val _stationProfileId = MutableStateFlow("1")
    val stationProfileId: StateFlow<String> = _stationProfileId.asStateFlow()

    private val _stationListJson = MutableStateFlow("[]")
    val stationListJson: StateFlow<String> = _stationListJson.asStateFlow()

    private val _autoUploadEnabled = MutableStateFlow(false)
    val autoUploadEnabled: StateFlow<Boolean> = _autoUploadEnabled.asStateFlow()

    private val _lastUpdateCheckDate = MutableStateFlow("")
    val lastUpdateCheckDate: StateFlow<String> = _lastUpdateCheckDate.asStateFlow()

    private val _updateIgnoredDate = MutableStateFlow("")
    val updateIgnoredDate: StateFlow<String> = _updateIgnoredDate.asStateFlow()


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

    fun setCloudlogUrl(url: String) {
        _cloudlogUrl.value = url.trim()
        prefs?.edit()?.putString("cloudlogUrl", _cloudlogUrl.value)?.apply()
    }

    fun setCloudlogApiKey(key: String) {
        _cloudlogApiKey.value = key.trim()
        prefs?.edit()?.putString("cloudlogApiKey", _cloudlogApiKey.value)?.apply()
    }

    fun setStationProfileId(id: String) {
        _stationProfileId.value = id.trim()
        prefs?.edit()?.putString("stationProfileId", _stationProfileId.value)?.apply()
    }

    fun setStationListJson(json: String) {
        _stationListJson.value = json
        prefs?.edit()?.putString("stationListJson", json)?.apply()
    }

    fun setAutoUploadEnabled(enabled: Boolean) {
        _autoUploadEnabled.value = enabled
        prefs?.edit()?.putBoolean("autoUploadEnabled", enabled)?.apply()
    }

    fun setGridSquare(grid: String) {
        _gridSquare.value = grid.trim()
        prefs?.edit()?.putString("gridSquare", _gridSquare.value)?.apply()
    }

    fun setLastUpdateCheckDate(date: String) {
        _lastUpdateCheckDate.value = date
        prefs?.edit()?.putString("lastUpdateCheckDate", date)?.apply()
    }

    fun setUpdateIgnoredDate(date: String) {
        _updateIgnoredDate.value = date
        prefs?.edit()?.putString("updateIgnoredDate", date)?.apply()
    }
}
