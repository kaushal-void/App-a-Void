package com.example.app_a_void

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeWarpLimitDao {

    // Insert or replace if conflict (same packageName => overwrites the old record)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: TimeWarpLimit)

    // Update existing record
    @Update
    suspend fun updateLimit(limit: TimeWarpLimit)

    // Delete a limit entry
    @Delete
    suspend fun deleteLimit(limit: TimeWarpLimit)

    // Retrieve all TimeWarpLimits
    @Query("SELECT * FROM time_warp_limits")
    fun getAllLimits(): Flow<List<TimeWarpLimit>>

    // Retrieve a single limit by package name
    @Query("SELECT * FROM time_warp_limits WHERE packageName = :pkgName LIMIT 1")
    fun getLimitByPackage(pkgName: String): Flow<TimeWarpLimit?>
}
