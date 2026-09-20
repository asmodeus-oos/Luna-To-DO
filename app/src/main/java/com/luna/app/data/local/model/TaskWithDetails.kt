package com.luna.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskEntity

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity> = emptyList()
) {
    val totalSubtasks: Int get() = subtasks.size
    val completedSubtasks: Int get() = subtasks.count { it.isCompleted }
    val hasSubtasks: Boolean get() = subtasks.isNotEmpty()
}
