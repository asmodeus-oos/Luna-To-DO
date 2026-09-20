package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String = "🌅",
    val timeOfDay: String = "Morning", // Morning, Afternoon, Evening
    val steps: List<String> = emptyList(),
    val colorHex: String = "#F59E0B",
    val lastCompletedDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isCompletedToday(): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return lastCompletedDate == todayStr
    }
}
