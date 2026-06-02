package com.hamlog.util

import kotlin.math.*

object GridCalculator {

    /**
     * Convert latitude and longitude to Maidenhead grid square (6-char).
     * e.g. 39.9N, 116.4E -> "OM89ew"
     */
    fun latLngToGrid(lat: Double, lng: Double): String {
        val adjLat = lat + 90.0
        val adjLng = lng + 180.0

        // Field (first two letters)
        val fieldLon = (adjLng / 20.0).toInt().coerceIn(0, 17)
        val fieldLat = (adjLat / 10.0).toInt().coerceIn(0, 17)
        val field = "${('A' + fieldLon)}${('A' + fieldLat)}"

        // Square (two digits)
        val squareLon = ((adjLng / 2.0) % 10.0).toInt().coerceIn(0, 9)
        val squareLat = ((adjLat / 1.0) % 10.0).toInt().coerceIn(0, 9)
        val square = "$squareLon$squareLat"

        // Subsquare (two lowercase letters)
        val subLon = ((adjLng * 12.0) % 2.0 * 12.0).toInt().coerceIn(0, 23)
        val subLat = ((adjLat * 24.0) % 1.0 * 24.0).toInt().coerceIn(0, 23)
        val sub = "${'a' + subLon}${'a' + subLat}"

        return "$field$square$sub"
    }
}
