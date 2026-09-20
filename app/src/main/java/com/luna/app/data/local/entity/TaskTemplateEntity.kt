package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luna.app.domain.model.Priority

@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val defaultTitle: String,
    val defaultNotes: String? = null,
    val defaultPriority: Priority = Priority.NONE,
    val defaultTags: List<String> = emptyList(),
    val checklistItems: List<String> = emptyList(),
    val defaultRecurrence: String = "NONE"
)
