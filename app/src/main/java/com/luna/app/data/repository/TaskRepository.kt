package com.luna.app.data.repository

import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.MissedReasonEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskActivityEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksWithDetailsFlow(): Flow<List<TaskWithDetails>>
    suspend fun getTaskWithDetailsById(id: Long): TaskWithDetails?
    suspend fun getTaskById(id: Long): TaskEntity?

    suspend fun addTask(
        title: String,
        notes: String? = null,
        subtitles: List<String> = emptyList(),
        startDate: Long? = null,
        dueDate: Long? = null,
        dueTime: String? = null,
        deadlineMode: String = "FIXED",
        durationValue: Int = 0,
        durationUnit: String = "HOURS",
        priority: Priority = Priority.NONE,
        tags: List<String> = emptyList(),
        recurrenceRule: String = "NONE",
        dependsOnTaskId: Long? = null,
        attachments: List<String> = emptyList(),
        projectId: Long? = null,
        sectionName: String? = null,
        estimatedMinutes: Int = 0,
        energyLevel: String = "MEDIUM",
        assignee: String? = null,
        goalId: Long? = null,
        category: String = "General",
        initialSubtasks: List<String> = emptyList()
    ): Long

    suspend fun updateTask(task: TaskEntity)
    suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean)
    suspend fun setTaskStatus(id: Long, status: TaskStatus)
    suspend fun setTaskPinned(id: Long, isPinned: Boolean)
    suspend fun setTaskEnergyLevel(id: Long, energy: String)
    suspend fun setTaskSection(id: Long, section: String?)
    suspend fun logTaskMinutes(id: Long, additionalMinutes: Int)
    suspend fun deleteTask(id: Long)
    suspend fun disableRecurrenceByTitle(title: String)
    suspend fun duplicateTask(id: Long): Long
    suspend fun clearCompletedTasks()
    suspend fun deleteAllTasks()
    suspend fun deleteAllRoutines()
    suspend fun deleteAllHabits()
    suspend fun clearAllData()
    suspend fun deleteCompletedTasksBefore(cutoffEpochMillis: Long): Int

    // Countdown & Deadline
    suspend fun startTask(id: Long)
    suspend fun extendTaskDeadline(id: Long, additionalMinutes: Int)
    suspend fun logMissedReason(
        taskId: Long,
        category: String,
        details: String,
        actionTaken: String = "LOGGED_MISSED",
        extendedMinutes: Int = 0
    ): Long
    fun getMissedReasonsFlow(): Flow<List<MissedReasonEntity>>

    // Subtasks / Checklists
    suspend fun addSubtask(taskId: Long, title: String, subtitleSection: String? = null): Long
    suspend fun toggleSubtask(id: Long, isCompleted: Boolean)
    suspend fun deleteSubtask(id: Long)

    // Bulk Operations
    suspend fun bulkComplete(ids: List<Long>, isCompleted: Boolean)
    suspend fun bulkDelete(ids: List<Long>)
    suspend fun bulkSetDueDate(ids: List<Long>, dueDate: Long?)
    suspend fun bulkSetPriority(ids: List<Long>, priority: Priority)

    // Templates
    fun getTemplatesFlow(): Flow<List<TaskTemplateEntity>>
    suspend fun insertTemplate(template: TaskTemplateEntity): Long
    suspend fun updateTemplate(template: TaskTemplateEntity)
    suspend fun createFromTemplate(template: TaskTemplateEntity): Long
    suspend fun saveTaskAsTemplate(taskId: Long, templateName: String): Long
    suspend fun deleteTemplate(id: Long)

    // Projects
    fun getProjectsFlow(): Flow<List<ProjectEntity>>
    suspend fun addProject(
        name: String,
        colorHex: String = "#008FFD",
        icon: String = "📁",
        parentId: Long? = null,
        sections: List<String> = listOf("To Do", "In Progress", "Done")
    ): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(id: Long)

    // Habits
    fun getHabitsFlow(): Flow<List<HabitEntity>>
    suspend fun addHabit(
        name: String,
        icon: String = "⚡",
        colorHex: String = "#008FFD",
        targetPerDay: Int = 1,
        imageUri: String? = null
    ): Long
    suspend fun updateHabit(habit: HabitEntity)
    suspend fun duplicateHabit(id: Long)
    suspend fun toggleHabitStep(habit: HabitEntity)
    suspend fun toggleHabitToday(habit: HabitEntity)
    suspend fun deleteHabit(id: Long)

    // Goals / OKRs
    fun getGoalsFlow(): Flow<List<GoalEntity>>
    suspend fun addGoal(
        title: String,
        description: String? = null,
        targetValue: Int = 100,
        unit: String = "%",
        colorHex: String = "#008FFD",
        icon: String = "🎯"
    ): Long
    suspend fun updateGoalProgress(id: Long, value: Int)
    suspend fun deleteGoal(id: Long)

    // Routines
    fun getRoutinesFlow(): Flow<List<RoutineEntity>>
    suspend fun addRoutine(
        name: String,
        icon: String = "🌅",
        timeOfDay: String = "Morning",
        steps: List<String> = emptyList(),
        colorHex: String = "#F59E0B"
    ): Long
    suspend fun updateRoutine(routine: RoutineEntity)
    suspend fun duplicateRoutine(id: Long)
    suspend fun toggleRoutineCompletedToday(routine: RoutineEntity)
    suspend fun deleteRoutine(id: Long)

    // Task Activities & Comments
    fun getActivitiesForTaskFlow(taskId: Long): Flow<List<TaskActivityEntity>>
    fun getRecentActivitiesFlow(): Flow<List<TaskActivityEntity>>
    suspend fun addActivity(taskId: Long, message: String, type: String = "LOG", author: String = "You"): Long
}
