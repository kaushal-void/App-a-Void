package com.example.app_a_void

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeWarpViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val timeWarpLimitDao = database.timeWarpLimitDao()
    private val appUsageDao = database.appUsageDao()

    // Backing StateFlow of all TimeWarpLimit records
    private val _limits = MutableStateFlow<List<TimeWarpLimit>>(emptyList())
    val limits: StateFlow<List<TimeWarpLimit>> = _limits

    init {
        loadLimits()
    }

    // Collect from the DAO and update our StateFlow
    private fun loadLimits() {
        viewModelScope.launch {
            timeWarpLimitDao.getAllLimits().collect { listOfLimits ->
                _limits.value = listOfLimits
            }
        }
    }

    // Insert or replace a limit record
    fun saveLimit(timeWarpLimit: TimeWarpLimit) {
        viewModelScope.launch {
            Log.d("TimeWarpViewModel", "Attempting to save limit: ${timeWarpLimit.limit} for ${timeWarpLimit.appName}")
            timeWarpLimitDao.insertLimit(timeWarpLimit)
            Log.d("TimeWarpViewModel", "Limit saved")
        }
    }

    // ✅ New: Return usage Flow filtered by package name and today's date
    fun getUsageFlow(packageName: String): Flow<AppUsage?> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return appUsageDao.getUsage(packageName, today)
            .onEach {
                Log.d("TimeWarpUsage", "[$today] $packageName = ${it?.totalUsage} ms")
            }
    }
}
