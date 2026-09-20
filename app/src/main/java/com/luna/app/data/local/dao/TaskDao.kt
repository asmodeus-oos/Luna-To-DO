package com.luna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // --- Task Queries ---
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, orderIndex ASC, createdAt DESC")
    fun getTasksWithDetailsFlow(): Flow<List<TaskWithDetails>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, orderIndex ASC, createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskWithDetailsById(id: Long): TaskWithDetails?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE title = :title AND isCompleted = 0 LIMIT 1")
    suspend fun getIncompleteTaskByTitle(title: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE title = :title")
    suspend fun deleteTasksByTitle(title: String)

    @Query("UPDATE tasks SET recurrenceRule = 'NONE' WHERE title = :title")
    suspend fun disableRecurrenceByTitle(title: String)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun setTaskStatus(id: Long, status: TaskStatus)

    @Query("UPDATE tasks SET isPinned = :isPinned WHERE id = :id")
    suspend fun setTaskPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE tasks SET energyLevel = :energy WHERE id = :id")
    suspend fun setTaskEnergyLevel(id: Long, energy: String)

    @Query("UPDATE tasks SET sectionName = :section WHERE id = :id")
    suspend fun setTaskSection(id: Long, section: String?)

    @Query("UPDATE tasks SET actualMinutes = actualMinutes + :additionalMinutes WHERE id = :id")
    suspend fun logTaskMinutes(id: Long, additionalMinutes: Int)

    @Query("DELETE FROM subtasks WHERE taskId IN (SELECT id FROM tasks WHERE isCompleted = 1)")
    suspend fun deleteSubtasksForCompletedTasks()

    @Query("DELETE FROM task_activities WHERE taskId IN (SELECT id FROM tasks WHERE isCompleted = 1)")
    suspend fun deleteActivitiesForCompletedTasks()

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()

    @Transaction
    suspend fun clearCompletedTasks() {
        deleteSubtasksForCompletedTasks()
        deleteActivitiesForCompletedTasks()
        deleteCompletedTasks()
    }

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM subtasks")
    suspend fun deleteAllSubtasks()

    @Query("DELETE FROM task_activities")
    suspend fun deleteAllActivities()

    @Transaction
    suspend fun clearAllTaskData() {
        deleteAllSubtasks()
        deleteAllActivities()
        deleteAllTasks()
    }

    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND ((completedAt IS NOT NULL AND completedAt < :cutoffEpochMillis) OR (completedAt IS NULL AND createdAt < :cutoffEpochMillis))")
    suspend fun deleteCompletedTasksBefore(cutoffEpochMillis: Long): Int

    // --- Subtasks / Checklist Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>): List<Long>

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtaskById(id: Long)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setSubtaskCompleted(id: Long, isCompleted: Boolean)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY orderIndex ASC, id ASC")
    suspend fun getSubtasksForTask(taskId: Long): List<SubtaskEntity>

    // --- Bulk Operations ---
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id IN (:ids)")
    suspend fun bulkSetCompleted(ids: List<Long>, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun bulkDelete(ids: List<Long>)

    @Query("UPDATE tasks SET dueDate = :dueDate WHERE id IN (:ids)")
    suspend fun bulkSetDueDate(ids: List<Long>, dueDate: Long?)

    @Query("UPDATE tasks SET priority = :priority WHERE id IN (:ids)")
    suspend fun bulkSetPriority(ids: List<Long>, priority: Priority)

    // --- Task Templates ---
    @Query("SELECT * FROM task_templates ORDER BY id ASC")
    fun getAllTemplatesFlow(): Flow<List<TaskTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TaskTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TaskTemplateEntity)

    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)
}
