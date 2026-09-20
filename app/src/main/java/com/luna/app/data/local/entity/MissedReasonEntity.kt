package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_reasons")
data class MissedReasonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val taskId: Long,
    val taskTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // "Ran out of time", "Got interrupted", "Underestimated effort", "Forgot", "Other"
    val reasonText: String,
    val actionTaken: String = "LOGGED_MISSED", // "RESCHEDULED", "LOGGED_MISSED", "ABANDONED"
    val extendedMinutes: Int = 0
)
