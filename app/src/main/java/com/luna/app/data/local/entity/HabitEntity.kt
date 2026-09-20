package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String = "⚡",
    val colorHex: String = "#008FFD",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedDates: List<String> = emptyList(), // "yyyy-MM-dd"
    val targetPerDay: Int = 1,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun todayCompletedCount(): Int {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return completedDates.count { it == todayStr }
    }

    fun isCompletedToday(): Boolean {
        return todayCompletedCount() >= targetPerDay
    }

    fun isCompletedOnDate(epochMillis: Long): Boolean {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(epochMillis))
        return completedDates.count { it == dateStr } >= targetPerDay
    }
}
