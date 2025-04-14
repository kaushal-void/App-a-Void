package com.example.app_a_void

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    // 🔄 Get usage for a specific app on a specific date
    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date LIMIT 1")
    fun getUsage(packageName: String, date: String): Flow<AppUsage?>

    // 🔄 Get usage for all apps on a specific date
    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY totalUsage DESC")
    fun getAllUsageForDate(date: String): Flow<List<AppUsage>>

    // ✅ Insert or update usage for that app-date entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(appUsage: AppUsage)
}
