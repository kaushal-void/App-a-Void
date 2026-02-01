package com.example.app_a_void

import android.graphics.drawable.Drawable
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val formattedTime: String,
    val appIcon: Drawable
)

object AppUtils {
    fun formatUsageTime(timeInMillis: Long): String {
        val hours = timeInMillis / (1000 * 60 * 60)
        val minutes = (timeInMillis / (1000 * 60)) % 60
        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
            minutes > 0 -> String.format(Locale.US, "%02dm", minutes)
            else -> "Less than a minute"
        }
    }

    fun formatCooldownTime(milliseconds: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        return if (minutes > 0) {
            "${minutes}m ${seconds - (minutes * 60)}s"
        } else {
            "${seconds}s"
        }
    }
}