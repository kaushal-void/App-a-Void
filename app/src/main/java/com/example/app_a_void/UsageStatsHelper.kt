package com.example.app_a_void

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale

object UsageStatsHelper {

    private const val TAG = "UsageStatsHelper"

    /**
     * Retrieves app usage stats for a specified time range.
     */
    private fun getUsageStats(context: Context, startTime: Long, endTime: Long): List<AppUsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        Log.d(TAG, "Fetching usage stats from $startTime to $endTime.")

        val rawStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .filter { it.totalTimeInForeground > 0 }

        if (rawStats.isEmpty()) {
            Log.w(TAG, "No usage stats found for the specified period.")
        }

        return rawStats.map { stats ->

            // Retrieve app name, with a fallback in case of error
            val appName = getAppName(packageManager, stats.packageName)

            // Retrieve app icon, with a fallback in case of error
            val appIcon = getAppIcon(packageManager, context, stats.packageName)

            Log.d(TAG, "App: $appName, Package: ${stats.packageName}, Usage Time: ${stats.totalTimeInForeground}")

            AppUsageStats(
                packageName = stats.packageName,
                appName = appName,
                totalTimeInForeground = stats.totalTimeInForeground,
                formattedTime = formatUsageTime(stats.totalTimeInForeground),
                appIcon = appIcon
            )
        }.sortedByDescending { it.totalTimeInForeground }
    }

    /**
     * Retrieves the app name for a given package.
     * Returns a custom name for common social media apps.
     */
    private fun getAppName(packageManager: PackageManager, packageName: String): String {
        // Checking for social media apps and returning custom names
        return when (packageName) {
            "com.instagram.android" -> "Instagram"
            "com.snapchat.android" -> "Snapchat"
            "com.facebook.katana" -> "Facebook"
            "com.twitter.android" -> "Twitter"
            "com.whatsapp" -> "WhatsApp"
            "com.tiktok.android" -> "TikTok"
            "com.linkedin.android" -> "LinkedIn"
            "com.pinterest" -> "Pinterest"
            "com.reddit.frontpage" -> "Reddit"
            "com.skype.raider" -> "Skype"
            "com.google.android.youtube" -> "YouTube"
            else -> {
                // Fall back to default app name if it's not a recognized social media app
                getDefaultAppName(packageManager, packageName)
            }
        }
    }

    /**
     * Helper method to get the default app name when not a recognized social media app.
     */
    private fun getDefaultAppName(packageManager: PackageManager, packageName: String): String {
        return try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "App name not found for package: $packageName", e)
            packageName.substringAfterLast('.') // Default to package name's last segment
        }
    }

    /**
     * Retrieves the app icon for a given package.
     * Returns a default icon if the app icon is not found.
     */
    private fun getAppIcon(packageManager: PackageManager, context: Context, packageName: String): Drawable {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "App icon not found for package: $packageName. Using default icon.", e)
            ContextCompat.getDrawable(context, R.drawable.ic_default_app_icon)
                ?: throw IllegalStateException("Default app icon is missing in resources.")
        }
    }

    /**
     * Gets the screen time usage stats for the current day.
     */
    fun getTodayUsageStats(context: Context): List<AppUsageStats> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        Log.d(TAG, "Fetching today's stats: Start Time = $startTime, End Time = $endTime")
        return getUsageStats(context, startTime, endTime)
    }

    /**
     * Formats usage time into a human-readable string.
     */
    fun formatUsageTime(timeInMillis: Long): String {
        val hours = timeInMillis / (1000 * 60 * 60)
        val minutes = (timeInMillis / (1000 * 60)) % 60
        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
            minutes > 0 -> String.format(Locale.US, "%02dm", minutes)
            else -> "Less than a minute"
        }
    }

    /**
     * Clears all stored screen time statistics at midnight.
     */
    fun resetScreenTimeStats(context: Context) {
        val sharedPreferences = context.getSharedPreferences("ScreenTimeStats", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Screen time stats reset at midnight.")
    }

    /**
     * Logs the current state of screen time stats for debugging.
     */
    fun logCurrentScreenTimeStats(context: Context) {
        val sharedPreferences = context.getSharedPreferences("ScreenTimeStats", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all
        Log.d(TAG, "Current Screen Time Stats:")
        allEntries.forEach { (key, value) -> Log.d(TAG, "$key: $value") }
    }
}
