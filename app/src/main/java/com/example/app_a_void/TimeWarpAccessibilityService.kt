package com.example.app_a_void

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeWarpAccessibilityService : AccessibilityService() {

    private lateinit var timeWarpRepo: TimeWarpLimitRepository
    private var currentPackage: String? = null
    private var lastTimestamp: Long = 0L
    private val usageMap = mutableMapOf<String, Long>()
    private lateinit var usageDao: AppUsageDao
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TimeWarpAccessibility", "Service connected.")

        val channelId = "TimeWarpChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Time Warp Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for Time Warp accessibility service"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("App-a-Void Running")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        try {
            startForeground(1, notification)
        } catch (e: Exception) {
            Log.e("TimeWarpAccessibility", "Failed to start foreground service: ${e.message}")
        }

        val db = AppDatabase.getDatabase(applicationContext)
        usageDao = db.appUsageDao()
        val dao = db.timeWarpLimitDao()
        timeWarpRepo = TimeWarpLimitRepository.getInstance(dao)

        lastTimestamp = System.currentTimeMillis()

        // Limit check loop
        serviceScope.launch {
            while (true) {
                delay(1000L)
                resetUsageIfNeeded()
                val current = currentPackage ?: continue
                val limitEntry = timeWarpRepo.getLimitForPackageSync(current)
                if (limitEntry != null) {
                    val limitMinutes = limitEntry.limit.removeSuffix(" min").toLongOrNull() ?: 0L
                    val limitMs = limitMinutes * 60_000L
                    val usage = usageMap.getOrDefault(current, 0L)
                    if (usage >= limitMs) {
                        Log.d("TimeWarpAccessibility", "Limit exceeded for $current ($usage ms >= $limitMs ms). Initiating lock.")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val locked = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                            if (!locked) {
                                launchLockScreenFallback()
                            }
                        } else {
                            launchLockScreenFallback()
                        }
                    }
                } else {
                    Log.d("TimeWarpAccessibility", "No limit set for $current")
                }
            }
        }
    }

    private fun launchLockScreenFallback() {
        val lockIntent = Intent(this, LockScreenActivity::class.java)
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(lockIntent)
    }

    private fun resetUsageIfNeeded() {
        Log.d("TimeWarpAccessibility", "resetUsageIfNeeded() called")
        val now = System.currentTimeMillis()
        val midnight = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (midnight in lastTimestamp..now) {
            Log.d("TimeWarpAccessibility", "New day detected. Clearing usageMap.")
            usageMap.clear()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        Log.d("TimeWarpEvent", "Event received: ${event.eventType}, package: ${event.packageName}")
        resetUsageIfNeeded()

        val newPackage = event.packageName?.toString() ?: return
        Log.d("TimeWarpDebug", "App switched to: $newPackage")

        val now = System.currentTimeMillis()

        if (currentPackage != null && currentPackage != newPackage) {
            val timeSpent = now - lastTimestamp
            val oldTotal = usageMap.getOrDefault(currentPackage, 0L)
            val newTotal = oldTotal + timeSpent
            usageMap[currentPackage!!] = newTotal
            Log.d("TimeWarpAccessibility", "Switch Event: User spent $timeSpent ms in $currentPackage (total: $newTotal ms)")

            // Save to database
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            serviceScope.launch {
                usageDao.insertOrUpdateUsage(
                    AppUsage(packageName = currentPackage!!, date = today, totalUsage = newTotal)
                )
                Log.d("TimeWarpAccessibility", "DB Saved: $currentPackage, Usage: $newTotal ms")
            }
        }

        currentPackage = newPackage
        lastTimestamp = now
        Log.d("TimeWarpAccessibility", "Switch Event: Now in foreground: $newPackage")
    }

    override fun onInterrupt() {
        Log.d("TimeWarpAccessibility", "Service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("TimeWarpAccessibility", "Service destroyed.")
    }
}