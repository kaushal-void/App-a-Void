package com.example.app_a_void

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * The status of a Task.
 */
enum class TaskStatus { COMPLETED, PENDING, SUSPENDED }

/**
 * Converter to store TaskStatus enum in the DB as a String.
 */
class TaskStatusConverter {
    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(statusString: String): TaskStatus = enumValueOf(statusString)
}

/**
 * Our Task entity, stored in the "tasks" table.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // auto-generate an ID
    val name: String,
    val duration: String,
    val status: TaskStatus = TaskStatus.PENDING
)
