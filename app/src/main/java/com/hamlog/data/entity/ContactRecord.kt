package com.hamlog.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["dateEpochDay"])]
)
data class ContactRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val callsign: String,
    val frequencyMHz: Double,
    val mode: String,
    val rstSent: String,
    val rstReceived: String,
    val powerTx: String,
    val powerRx: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)
