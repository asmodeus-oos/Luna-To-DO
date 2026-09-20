package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_activities")
data class TaskActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val taskId: Long,
    val author: String = "You",
    val message: String,
    val type: String = "LOG", // LOG, COMMENT, STATUS
    val timestamp: Long = System.currentTimeMillis()
)
