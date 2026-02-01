package com.example.app_a_void

import androidx.room.Entity

@Entity(tableName = "app_usage", primaryKeys = ["packageName", "date"])
data class AppUsage(
    val packageName: String,
    val date: String, // Format: yyyy-MM-dd (e.g., "2025-04-12")
    val totalUsage: Long // total usage time in milliseconds
)
