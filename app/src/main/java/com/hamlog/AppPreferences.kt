package com.hamlog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZoneId

object AppPreferences {
    private val _timezone = MutableStateFlow(ZoneId.of("UTC"))
    val timezone: StateFlow<ZoneId> = _timezone.asStateFlow()

    private val _callsign = MutableStateFlow("")
    val callsign: StateFlow<String> = _callsign.asStateFlow()

    private val _scaleFactor = MutableStateFlow(0.9f)
    val scaleFactor: StateFlow<Float> = _scaleFactor.asStateFlow()

    fun setTimezone(zoneId: ZoneId) { _timezone.value = zoneId }
    fun setCallsign(callsign: String) { _callsign.value = callsign.uppercase().trim() }
    fun setScaleFactor(scale: Float) { _scaleFactor.value = scale.coerceIn(0.4f, 1.3f) }
}
