package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val notes: String? = null,
    val subtitles: List<String> = emptyList(),
    val startDate: Long? = null,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val deadlineMode: String = "FIXED", // "FIXED" or "DURATION"
    val durationValue: Int = 0,
    val durationUnit: String = "HOURS", // "MINUTES", "HOURS", "DAYS"
    val startedAt: Long? = null,
    val computedDeadline: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val priority: Priority = Priority.NONE,
    val tags: List<String> = emptyList(),
    val recurrenceRule: String = "NONE",
    val dependsOnTaskId: Long? = null,
    val attachments: List<String> = emptyList(),
    val projectId: Long? = null,
    val sectionName: String? = null,
    val isPinned: Boolean = false,
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val energyLevel: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val assignee: String? = null,
    val goalId: Long? = null,
    val xpValue: Int = 50,
    val pauseCount: Int = 0,
    val pausedAt: Long? = null,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0,
    val alarmOnStart: Boolean = false,
    val alarmOnFinish: Boolean = false,
    val weeklyDay: String? = null,  // e.g. "MON", "TUE", "WED"
    val weeklyTime: String? = null  // e.g. "15:00"
) {
    /**
     * Total duration in milliseconds if duration mode is active
     */
    val totalDurationMillis: Long
        get() = when (durationUnit.uppercase()) {
            "MINUTES" -> durationValue * 60 * 1000L
            "DAYS" -> durationValue * 24 * 60 * 60 * 1000L
            else -> durationValue * 60 * 60 * 1000L // HOURS
        }

    /**
     * Effective target deadline epoch timestamp
     */
    val effectiveDeadlineEpoch: Long?
        get() = if (deadlineMode == "DURATION") {
            computedDeadline ?: (if (startedAt != null) startedAt + totalDurationMillis else null)
        } else {
            dueDate
        }

    /**
     * Calculate remaining time in millis from now
     */
    fun remainingMillis(now: Long = System.currentTimeMillis()): Long? {
        val deadline = effectiveDeadlineEpoch ?: return null
        return deadline - now
    }
}
