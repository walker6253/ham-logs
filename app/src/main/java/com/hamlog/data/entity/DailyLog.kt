package com.hamlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey
    val dateEpochDay: Long,
    val latitude: Double,
    val longitude: Double,
    val gridSquare: String,
    val address: String
)
