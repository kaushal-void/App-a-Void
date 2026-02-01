package com.example.app_a_void

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TimeWarpLimitRepository private constructor(
    private val dao: TimeWarpLimitDao
) {
    /**
     * Retrieves the TimeWarpLimit entry for a specific package name *synchronously*.
     * We use runBlocking + Flow.first() here just for simplicity/demonstration.
     * In a production app, you might prefer a suspend function or Flow directly.
     */
    fun getLimitForPackageSync(packageName: String): TimeWarpLimit? = runBlocking {
        dao.getLimitByPackage(packageName).first()
    }

    companion object {
        @Volatile
        private var INSTANCE: TimeWarpLimitRepository? = null

        /**
         * Returns the single instance of [TimeWarpLimitRepository].
         * Ensures that only one instance is created (thread-safe).
         */
        fun getInstance(dao: TimeWarpLimitDao): TimeWarpLimitRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TimeWarpLimitRepository(dao)
                INSTANCE = instance
                instance
            }
        }
    }
}
