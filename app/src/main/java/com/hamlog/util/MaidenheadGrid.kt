package com.hamlog.util

import kotlin.math.floor

object MaidenheadGrid {

    fun latLngToGridSquare(lat: Double, lng: Double): String {
        val adjustedLat = lat + 90.0
        val adjustedLng = lng + 180.0

        // Field (first two chars: AA-RR)
        val fieldLng = floor(adjustedLng / 20.0).toInt()
        val fieldLat = floor(adjustedLat / 10.0).toInt()
        val field = ""

        // Square (next two chars: 00-99)
        val squareLng = floor((adjustedLng % 20.0) / 2.0).toInt()
        val squareLat = floor((adjustedLat % 10.0) / 1.0).toInt()
        val square = ""

        // Sub-square (next two chars: aa-xx)
        val subLng = floor(((adjustedLng % 20.0) % 2.0) / (2.0 / 24.0)).toInt()
        val subLat = floor(((adjustedLat % 10.0) % 1.0) / (1.0 / 24.0)).toInt()
        val sub = ""

        return field + square + sub
    }
}
