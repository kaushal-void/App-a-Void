package com.example.app_a_void

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Our Room database.
 * - Entities = [Task, TimeWarpLimit, AppUsage]
 * - Version is bumped from 2 to 3 for schema changes.
 */
@Database(
    entities = [
        Task::class,
        TimeWarpLimit::class,
        AppUsage::class  // New entity for realtime screen time tracking.
    ],
    version = 3, // ✅ Updated version number
    exportSchema = false
)
@TypeConverters(TaskStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // DAO for Tasks
    abstract fun taskDao(): TaskDao

    // DAO for Time Warp Limits
    abstract fun timeWarpLimitDao(): TimeWarpLimitDao

    // DAO for App Usage tracking
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * A thread-safe way to get the singleton instance of [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // ✅ Allows schema reset during dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
