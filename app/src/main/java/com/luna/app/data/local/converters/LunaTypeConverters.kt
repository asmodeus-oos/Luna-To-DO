package com.luna.app.data.local.converters

import androidx.room.TypeConverter
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus

class LunaTypeConverters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(";;;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromPriority(priority: Priority?): Int {
        return priority?.level ?: 0
    }

    @TypeConverter
    fun toPriority(level: Int): Priority {
        return Priority.fromLevel(level)
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus?): String {
        return status?.name ?: TaskStatus.TODO.name
    }

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus {
        return try {
            if (value != null) TaskStatus.valueOf(value) else TaskStatus.TODO
        } catch (_: Exception) {
            TaskStatus.TODO
        }
    }
}
