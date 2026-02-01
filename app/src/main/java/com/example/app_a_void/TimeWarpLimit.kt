package com.example.app_a_void

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the Time Warp limit for a specific app/package.
 * The limit is stored as a String (e.g., "15 min" or "01:30" etc.).
 */
@Entity(tableName = "time_warp_limits")
data class TimeWarpLimit(
    @PrimaryKey
    val packageName: String,  // e.g., "com.instagram.android"
    val appName: String,      // e.g., "Instagram"
    val limit: String         // e.g., "15 min" or "01:30"
)
