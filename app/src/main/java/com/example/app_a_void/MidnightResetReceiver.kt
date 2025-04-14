package com.example.app_a_void

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log



class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {

            Log.d("MidnightResetReceiver", "Midnight, time, or timezone change detected. Resetting screen time statistics.")

            // Log current stats for debugging
            UsageStatsHelper.logCurrentScreenTimeStats(context)

            // Reset the screen time stats
            UsageStatsHelper.resetScreenTimeStats(context)
        }
    }
}