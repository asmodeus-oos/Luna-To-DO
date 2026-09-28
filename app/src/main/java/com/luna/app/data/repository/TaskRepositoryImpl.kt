package com.luna.app.data.repository

import com.luna.app.data.local.dao.AccountDao
import com.luna.app.data.local.dao.BudgetDao
import com.luna.app.data.local.dao.FinancialGoalDao
import com.luna.app.data.local.dao.GoalDao
import com.luna.app.data.local.dao.HabitDao
import com.luna.app.data.local.dao.MissedReasonDao
import com.luna.app.data.local.dao.ProjectDao
import com.luna.app.data.local.dao.RoutineDao
import com.luna.app.data.local.dao.TaskActivityDao
import com.luna.app.data.local.dao.TaskDao
import com.luna.app.data.local.dao.TransactionDao
import com.luna.app.data.local.entity.AccountEntity
import com.luna.app.data.local.entity.BudgetEntity
import com.luna.app.data.local.entity.FinancialGoalEntity
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.MissedReasonEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskActivityEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.local.entity.TransactionEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.RecurrenceType
import com.luna.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val habitDao: HabitDao,
    private val goalDao: GoalDao,
    private val routineDao: RoutineDao,
    private val taskActivityDao: TaskActivityDao,
    private val missedReasonDao: MissedReasonDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val financialGoalDao: FinancialGoalDao
) : TaskRepository {

    override fun getTasksWithDetailsFlow(): Flow<List<TaskWithDetails>> =
        taskDao.getTasksWithDetailsFlow()

    override suspend fun getTaskWithDetailsById(id: Long): TaskWithDetails? =
        taskDao.getTaskWithDetailsById(id)

    override suspend fun getTaskById(id: Long): TaskEntity? =
        taskDao.getTaskById(id)

    override suspend fun addTask(
        title: String,
        notes: String?,
        subtitles: List<String>,
        startDate: Long?,
        dueDate: Long?,
        dueTime: String?,
        deadlineMode: String,
        durationValue: Int,
        durationUnit: String,
        priority: Priority,
        tags: List<String>,
        recurrenceRule: String,
        dependsOnTaskId: Long?,
        attachments: List<String>,
        projectId: Long?,
        sectionName: String?,
        estimatedMinutes: Int,
        energyLevel: String,
        assignee: String?,
        goalId: Long?,
        category: String,
        initialSubtasks: List<String>,
        alarmOnStart: Boolean,
        alarmOnFinish: Boolean,
        weeklyDay: String?,
        weeklyTime: String?,
        place: String?
    ): Long {
        val entity = TaskEntity(
            title = title.trim(),
            notes = notes?.trim()?.ifEmpty { null },
            subtitles = subtitles,
            startDate = startDate,
            dueDate = dueDate,
            dueTime = dueTime,
            deadlineMode = deadlineMode,
            durationValue = durationValue,
            durationUnit = durationUnit,
            priority = priority,
            tags = tags.map { it.trim().lowercase() }.filter { it.isNotBlank() }.distinct(),
            recurrenceRule = recurrenceRule,
            dependsOnTaskId = dependsOnTaskId,
            attachments = attachments,
            projectId = projectId,
            sectionName = sectionName,
            estimatedMinutes = estimatedMinutes,
            energyLevel = energyLevel,
            assignee = assignee,
            goalId = goalId,
            category = category,
            createdAt = System.currentTimeMillis(),
            alarmOnStart = alarmOnStart,
            alarmOnFinish = alarmOnFinish,
            weeklyDay = weeklyDay,
            weeklyTime = weeklyTime,
            place = place?.trim()?.ifEmpty { null }
        )
        val taskId = taskDao.insertTask(entity)

        if (initialSubtasks.isNotEmpty()) {
            val subtaskEntities = initialSubtasks.mapIndexed { index, subtaskTitle ->
                SubtaskEntity(
                    taskId = taskId,
                    title = subtaskTitle.trim(),
                    orderIndex = index
                )
            }
            taskDao.insertSubtasks(subtaskEntities)
        }

        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = taskId,
                author = "You",
                message = "Task created",
                type = "LOG"
            )
        )

        return taskId
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    private suspend fun handleRecurrence(currentTask: TaskEntity) {
        if (currentTask.recurrenceRule != "NONE") {
            val existingIncomplete = taskDao.getIncompleteTaskByTitle(currentTask.title)
            if (existingIncomplete == null) {
                val recurrence = RecurrenceType.fromString(currentTask.recurrenceRule)
                val nextDueDate = recurrence.nextOccurrenceEpoch(currentTask.dueDate)
                val existingSubtasks = taskDao.getSubtasksForTask(currentTask.id)

                val nextOccurrence = currentTask.copy(
                    id = 0L,
                    dueDate = nextDueDate,
                    isCompleted = false,
                    completedAt = null,
                    status = TaskStatus.TODO,
                    createdAt = System.currentTimeMillis()
                )
                val nextTaskId = taskDao.insertTask(nextOccurrence)

                if (existingSubtasks.isNotEmpty()) {
                    val freshSubtasks = existingSubtasks.mapIndexed { index, subtask ->
                        SubtaskEntity(
                            taskId = nextTaskId,
                            title = subtask.title,
                            isCompleted = false,
                            orderIndex = index
                        )
                    }
                    taskDao.insertSubtasks(freshSubtasks)
                }
            }
        }
    }

    override suspend fun toggleTaskCompleted(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        val status = if (isCompleted) TaskStatus.DONE else TaskStatus.TODO
        taskDao.setTaskCompleted(id, isCompleted, completedAt)
        taskDao.setTaskStatus(id, status)

        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = id,
                author = "You",
                message = if (isCompleted) "Completed task" else "Marked task incomplete",
                type = "STATUS"
            )
        )

        if (isCompleted) {
            val currentTask = taskDao.getTaskById(id)
            if (currentTask != null) {
                handleRecurrence(currentTask)
            }
        }
    }

    override suspend fun setTaskStatus(id: Long, status: TaskStatus) {
        val isCompleted = status == TaskStatus.DONE
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.setTaskStatus(id, status)
        taskDao.setTaskCompleted(id, isCompleted, completedAt)

        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = id,
                author = "You",
                message = "Status changed to ${status.label}",
                type = "STATUS"
            )
        )

        if (isCompleted) {
            val currentTask = taskDao.getTaskById(id)
            if (currentTask != null) {
                handleRecurrence(currentTask)
            }
        }
    }

    override suspend fun setTaskPinned(id: Long, isPinned: Boolean) {
        taskDao.setTaskPinned(id, isPinned)
    }

    override suspend fun setTaskEnergyLevel(id: Long, energy: String) {
        taskDao.setTaskEnergyLevel(id, energy)
    }

    override suspend fun setTaskSection(id: Long, section: String?) {
        taskDao.setTaskSection(id, section)
    }

    override suspend fun logTaskMinutes(id: Long, additionalMinutes: Int) {
        taskDao.logTaskMinutes(id, additionalMinutes)
        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = id,
                author = "You",
                message = "Logged ${additionalMinutes}m focus time",
                type = "LOG"
            )
        )
    }

    override suspend fun deleteTask(id: Long) {
        taskDao.deleteTaskById(id)
        taskActivityDao.deleteActivitiesForTask(id)
    }

    override suspend fun disableRecurrenceByTitle(title: String) {
        taskDao.disableRecurrenceByTitle(title)
    }

    override suspend fun duplicateTask(id: Long): Long {
        val originalWithDetails = taskDao.getTaskWithDetailsById(id) ?: return -1L
        val original = originalWithDetails.task

        val duplicated = original.copy(
            id = 0L,
            title = "${original.title} (Copy)",
            isCompleted = false,
            completedAt = null,
            status = TaskStatus.TODO,
            createdAt = System.currentTimeMillis()
        )
        val newTaskId = taskDao.insertTask(duplicated)

        if (originalWithDetails.subtasks.isNotEmpty()) {
            val duplicatedSubtasks = originalWithDetails.subtasks.mapIndexed { index, sub ->
                SubtaskEntity(
                    taskId = newTaskId,
                    title = sub.title,
                    isCompleted = false,
                    orderIndex = index
                )
            }
            taskDao.insertSubtasks(duplicatedSubtasks)
        }

        return newTaskId
    }

    override suspend fun clearCompletedTasks() {
        taskDao.clearCompletedTasks()
    }

    override suspend fun deleteAllTasks() {
        taskDao.clearAllTaskData()
    }

    override suspend fun deleteAllRoutines() {
        routineDao.deleteAllRoutines()
    }

    override suspend fun deleteAllHabits() {
        habitDao.deleteAllHabits()
    }

    override suspend fun clearAllData() {
        taskDao.clearAllTaskData()
        routineDao.deleteAllRoutines()
        habitDao.deleteAllHabits()
    }

    override suspend fun deleteCompletedTasksBefore(cutoffEpochMillis: Long): Int {
        return taskDao.deleteCompletedTasksBefore(cutoffEpochMillis)
    }

    override suspend fun getAllTasksWithDetailsList(): List<TaskWithDetails> {
        return taskDao.getTasksWithDetailsFlow().first()
    }

    override suspend fun importTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>): Long {
        val allTasks = taskDao.getAllTasksFlow().first()
        val existing = allTasks.firstOrNull { 
            it.title.equals(task.title, ignoreCase = true) && 
            it.weeklyDay == task.weeklyDay && 
            it.weeklyTime == task.weeklyTime 
        } ?: allTasks.firstOrNull {
            task.weeklyDay == null && it.title.equals(task.title, ignoreCase = true) && !it.isCompleted
        }

        val taskId = if (existing != null) {
            val updated = task.copy(id = existing.id)
            taskDao.updateTask(updated)
            existing.id
        } else {
            taskDao.insertTask(task.copy(id = 0L))
        }

        if (subtasks.isNotEmpty()) {
            val existingSubtasks = taskDao.getSubtasksForTask(taskId)
            val subtasksToInsert = subtasks.filter { newSub ->
                existingSubtasks.none { it.title.equals(newSub.title, ignoreCase = true) }
            }.map {
                it.copy(id = 0L, taskId = taskId)
            }
            if (subtasksToInsert.isNotEmpty()) {
                taskDao.insertSubtasks(subtasksToInsert)
            }
        }
        return taskId
    }

    override suspend fun importHabit(habit: HabitEntity): Long {
        val existing = habitDao.getAllHabitsFlow().first().firstOrNull { it.name.equals(habit.name, ignoreCase = true) }
        return if (existing != null) {
            habitDao.updateHabit(habit.copy(id = existing.id))
            existing.id
        } else {
            habitDao.insertHabit(habit.copy(id = 0L))
        }
    }

    override suspend fun importRoutine(routine: RoutineEntity): Long {
        val existing = routineDao.getAllRoutinesFlow().first().firstOrNull { it.name.equals(routine.name, ignoreCase = true) }
        return if (existing != null) {
            routineDao.updateRoutine(routine.copy(id = existing.id))
            existing.id
        } else {
            routineDao.insertRoutine(routine.copy(id = 0L))
        }
    }

    override suspend fun importProject(project: ProjectEntity): Long {
        val existing = projectDao.getAllProjectsFlow().first().firstOrNull { it.name.equals(project.name, ignoreCase = true) }
        return if (existing != null) {
            projectDao.updateProject(project.copy(id = existing.id))
            existing.id
        } else {
            projectDao.insertProject(project.copy(id = 0L))
        }
    }

    override suspend fun importGoal(goal: GoalEntity): Long {
        val existing = goalDao.getAllGoalsFlow().first().firstOrNull { it.title.equals(goal.title, ignoreCase = true) }
        return if (existing != null) {
            goalDao.updateGoal(goal.copy(id = existing.id))
            existing.id
        } else {
            goalDao.insertGoal(goal.copy(id = 0L))
        }
    }

    // --- Countdown, Deadline & Accountability ---
    override suspend fun startTask(id: Long) {
        val task = taskDao.getTaskById(id) ?: return
        val now = System.currentTimeMillis()
        val computed = if (task.deadlineMode == "DURATION") {
            now + task.totalDurationMillis
        } else {
            task.dueDate
        }
        val updated = task.copy(
            status = TaskStatus.IN_PROGRESS,
            startedAt = task.startedAt ?: now,
            computedDeadline = computed
        )
        taskDao.updateTask(updated)
        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = id,
                author = "You",
                message = "Started countdown timer",
                type = "STATUS"
            )
        )
    }

    override suspend fun extendTaskDeadline(id: Long, additionalMinutes: Int) {
        val task = taskDao.getTaskById(id) ?: return
        val now = System.currentTimeMillis()
        val currentDeadline = task.effectiveDeadlineEpoch ?: now
        val newDeadline = maxOf(now, currentDeadline) + (additionalMinutes * 60 * 1000L)
        val updated = task.copy(
            status = TaskStatus.IN_PROGRESS,
            computedDeadline = newDeadline
        )
        taskDao.updateTask(updated)
        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = id,
                author = "You",
                message = "Extended countdown deadline by ${additionalMinutes}m",
                type = "STATUS"
            )
        )
    }

    override suspend fun logMissedReason(
        taskId: Long,
        category: String,
        details: String,
        actionTaken: String,
        extendedMinutes: Int
    ): Long {
        val task = taskDao.getTaskById(taskId)
        val taskTitle = task?.title ?: "Task #$taskId"
        val entry = MissedReasonEntity(
            taskId = taskId,
            taskTitle = taskTitle,
            category = category,
            reasonText = details,
            actionTaken = actionTaken,
            extendedMinutes = extendedMinutes,
            timestamp = System.currentTimeMillis()
        )
        val reasonId = missedReasonDao.insertMissedReason(entry)

        if (actionTaken == "LOGGED_MISSED" || actionTaken == "ABANDONED") {
            task?.let {
                taskDao.updateTask(
                    it.copy(
                        status = TaskStatus.FAILED_LOGGED,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
        } else if (actionTaken == "RESCHEDULED" && extendedMinutes > 0) {
            extendTaskDeadline(taskId, extendedMinutes)
        }

        taskActivityDao.insertActivity(
            TaskActivityEntity(
                taskId = taskId,
                author = "You",
                message = "Accountability check logged: $category - $details (Action: $actionTaken)",
                type = "LOG"
            )
        )
        return reasonId
    }

    override fun getMissedReasonsFlow(): Flow<List<MissedReasonEntity>> =
        missedReasonDao.getAllMissedReasonsFlow()

    // --- Subtasks ---
    override suspend fun addSubtask(taskId: Long, title: String, subtitleSection: String?): Long {
        val subtask = SubtaskEntity(
            taskId = taskId,
            title = title.trim(),
            subtitleSection = subtitleSection
        )
        return taskDao.insertSubtask(subtask)
    }

    override suspend fun toggleSubtask(id: Long, isCompleted: Boolean) {
        taskDao.setSubtaskCompleted(id, isCompleted)
    }

    override suspend fun deleteSubtask(id: Long) {
        taskDao.deleteSubtaskById(id)
    }

    // --- Bulk Operations ---
    override suspend fun bulkComplete(ids: List<Long>, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        val status = if (isCompleted) TaskStatus.DONE else TaskStatus.TODO
        taskDao.bulkSetCompleted(ids, isCompleted, completedAt)
        for (id in ids) {
            taskDao.setTaskStatus(id, status)
            if (isCompleted) {
                val currentTask = taskDao.getTaskById(id)
                if (currentTask != null && currentTask.recurrenceRule != "NONE") {
                    val existingIncomplete = taskDao.getIncompleteTaskByTitle(currentTask.title)
                    if (existingIncomplete == null) {
                        val recurrence = RecurrenceType.fromString(currentTask.recurrenceRule)
                        val nextDueDate = recurrence.nextOccurrenceEpoch(currentTask.dueDate)
                        val existingSubtasks = taskDao.getSubtasksForTask(id)
                        val nextOccurrence = currentTask.copy(
                            id = 0L,
                            dueDate = nextDueDate,
                            isCompleted = false,
                            completedAt = null,
                            status = TaskStatus.TODO,
                            createdAt = System.currentTimeMillis()
                        )
                        val nextTaskId = taskDao.insertTask(nextOccurrence)
                        if (existingSubtasks.isNotEmpty()) {
                            val freshSubtasks = existingSubtasks.mapIndexed { index, subtask ->
                                SubtaskEntity(
                                    taskId = nextTaskId,
                                    title = subtask.title,
                                    isCompleted = false,
                                    orderIndex = index
                                )
                            }
                            taskDao.insertSubtasks(freshSubtasks)
                        }
                    }
                }
            }
        }
    }

    override suspend fun bulkDelete(ids: List<Long>) {
        taskDao.bulkDelete(ids)
    }

    override suspend fun bulkSetDueDate(ids: List<Long>, dueDate: Long?) {
        taskDao.bulkSetDueDate(ids, dueDate)
    }

    override suspend fun bulkSetPriority(ids: List<Long>, priority: Priority) {
        taskDao.bulkSetPriority(ids, priority)
    }

    // --- Templates ---
    override fun getTemplatesFlow(): Flow<List<TaskTemplateEntity>> =
        taskDao.getAllTemplatesFlow()

    override suspend fun insertTemplate(template: TaskTemplateEntity): Long =
        taskDao.insertTemplate(template)

    override suspend fun updateTemplate(template: TaskTemplateEntity) {
        taskDao.updateTemplate(template)
    }

    override suspend fun createFromTemplate(template: TaskTemplateEntity): Long {
        return addTask(
            title = template.defaultTitle,
            notes = template.defaultNotes,
            priority = template.defaultPriority,
            tags = template.defaultTags,
            recurrenceRule = template.defaultRecurrence,
            initialSubtasks = template.checklistItems
        )
    }

    override suspend fun saveTaskAsTemplate(taskId: Long, templateName: String): Long {
        val taskWithDetails = taskDao.getTaskWithDetailsById(taskId) ?: return -1L
        val template = TaskTemplateEntity(
            name = templateName.trim(),
            description = "Template from ${taskWithDetails.task.title}",
            defaultTitle = taskWithDetails.task.title,
            defaultNotes = taskWithDetails.task.notes,
            defaultPriority = taskWithDetails.task.priority,
            defaultTags = taskWithDetails.task.tags,
            checklistItems = taskWithDetails.subtasks.map { it.title },
            defaultRecurrence = taskWithDetails.task.recurrenceRule
        )
        return taskDao.insertTemplate(template)
    }

    override suspend fun deleteTemplate(id: Long) {
        taskDao.deleteTemplateById(id)
    }

    // --- Projects ---
    override fun getProjectsFlow(): Flow<List<ProjectEntity>> =
        projectDao.getAllProjectsFlow()

    override suspend fun addProject(
        name: String,
        colorHex: String,
        icon: String,
        parentId: Long?,
        sections: List<String>
    ): Long {
        val entity = ProjectEntity(
            name = name.trim(),
            colorHex = colorHex,
            icon = icon,
            parentId = parentId,
            sections = sections
        )
        return projectDao.insertProject(entity)
    }

    override suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project)
    }

    override suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }

    // --- Habits ---
    override fun getHabitsFlow(): Flow<List<HabitEntity>> =
        habitDao.getAllHabitsFlow()

    override suspend fun addHabit(
        name: String,
        icon: String,
        colorHex: String,
        targetPerDay: Int,
        imageUri: String?
    ): Long {
        val entity = HabitEntity(
            name = name.trim(),
            icon = icon,
            colorHex = colorHex,
            targetPerDay = targetPerDay.coerceAtLeast(1),
            imageUri = imageUri
        )
        return habitDao.insertHabit(entity)
    }

    override suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    override suspend fun duplicateHabit(id: Long) {
        val habit = habitDao.getHabitById(id) ?: return
        val duplicate = habit.copy(
            id = 0L,
            name = "${habit.name} (Copy)",
            currentStreak = 0,
            bestStreak = 0,
            completedDates = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        habitDao.insertHabit(duplicate)
    }

    override suspend fun toggleHabitStep(habit: HabitEntity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val currentTodayCount = habit.todayCompletedCount()
        val target = habit.targetPerDay.coerceAtLeast(1)

        // Locked till next day if already completed today
        if (currentTodayCount >= target) {
            return
        }

        // Limit list size to last 365 days to prevent unbounded DB growth (#16)
        val prunedDates = (habit.completedDates + todayStr).takeLast(365 * 3)

        val newCount = currentTodayCount + 1

        // Check if streak broke (#17): evaluate last completed date before today
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(yesterdayCal.time)
        val lastDateBeforeToday = habit.completedDates.lastOrNull { it != todayStr }
        
        val baseStreak = if (lastDateBeforeToday != null && lastDateBeforeToday != yesterdayStr) {
            0 // Streak reset because yesterday was missed
        } else {
            habit.currentStreak
        }

        val newStreak = if (newCount >= target) baseStreak + 1 else baseStreak
        val newBest = maxOf(newStreak, habit.bestStreak)

        habitDao.updateHabit(
            habit.copy(
                completedDates = prunedDates,
                currentStreak = newStreak,
                bestStreak = newBest
            )
        )
    }

    override suspend fun toggleHabitToday(habit: HabitEntity) {
        toggleHabitStep(habit)
    }

    override suspend fun deleteHabit(id: Long) {
        habitDao.deleteHabitById(id)
    }

    // --- Goals / OKRs ---
    override fun getGoalsFlow(): Flow<List<GoalEntity>> =
        goalDao.getAllGoalsFlow()

    override suspend fun addGoal(
        title: String,
        description: String?,
        targetValue: Int,
        unit: String,
        colorHex: String,
        icon: String
    ): Long {
        val entity = GoalEntity(
            title = title.trim(),
            description = description?.trim()?.ifEmpty { null },
            targetValue = targetValue,
            currentValue = 0,
            unit = unit,
            colorHex = colorHex,
            icon = icon
        )
        return goalDao.insertGoal(entity)
    }

    override suspend fun updateGoalProgress(id: Long, value: Int) {
        goalDao.updateGoalProgress(id, value)
    }

    override suspend fun deleteGoal(id: Long) {
        goalDao.deleteGoalById(id)
    }

    // --- Routines ---
    override fun getRoutinesFlow(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutinesFlow()

    override suspend fun addRoutine(
        name: String,
        icon: String,
        timeOfDay: String,
        steps: List<String>,
        colorHex: String
    ): Long {
        val entity = RoutineEntity(
            name = name.trim(),
            icon = icon,
            timeOfDay = timeOfDay,
            steps = steps,
            colorHex = colorHex
        )
        return routineDao.insertRoutine(entity)
    }

    override suspend fun updateRoutine(routine: RoutineEntity) {
        routineDao.updateRoutine(routine)
    }

    override suspend fun duplicateRoutine(id: Long) {
        val routine = routineDao.getRoutineById(id) ?: return
        val duplicate = routine.copy(
            id = 0L,
            name = "${routine.name} (Copy)",
            lastCompletedDate = null,
            createdAt = System.currentTimeMillis()
        )
        routineDao.insertRoutine(duplicate)
    }

    override suspend fun toggleRoutineCompletedToday(routine: RoutineEntity) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isDone = routine.lastCompletedDate == todayStr
        val nextDate = if (isDone) null else todayStr
        routineDao.setRoutineCompletedDate(routine.id, nextDate)
    }

    override suspend fun deleteRoutine(id: Long) {
        routineDao.deleteRoutineById(id)
    }

    // --- Task Activities & Comments ---
    override fun getActivitiesForTaskFlow(taskId: Long): Flow<List<TaskActivityEntity>> =
        taskActivityDao.getActivitiesForTaskFlow(taskId)

    override fun getRecentActivitiesFlow(): Flow<List<TaskActivityEntity>> =
        taskActivityDao.getRecentActivitiesFlow()

    override suspend fun addActivity(taskId: Long, message: String, type: String, author: String): Long {
        val entity = TaskActivityEntity(
            taskId = taskId,
            author = author,
            message = message.trim(),
            type = type
        )
        return taskActivityDao.insertActivity(entity)
    }

    // --- Financial System: Transactions ---
    override fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactionsFlow()

    override fun getRecentTransactionsFlow(limit: Int): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactionsFlow(limit)

    override fun getTransactionsByAccountFlow(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccountFlow(accountId)

    override fun getTransactionsByCategoryFlow(category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategoryFlow(category)

    override suspend fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        tags: List<String>,
        accountId: Long,
        currency: String,
        receiptUri: String?,
        recurrenceRule: String,
        linkedTaskId: Long?,
        linkedProjectId: Long?,
        linkedGoalId: Long?,
        notes: String?,
        timestamp: Long
    ): Long {
        val entity = TransactionEntity(
            title = title.trim(),
            amount = amount,
            type = type,
            category = category,
            tags = tags,
            accountId = accountId,
            currency = currency,
            receiptUri = receiptUri,
            recurrenceRule = recurrenceRule,
            linkedTaskId = linkedTaskId,
            linkedProjectId = linkedProjectId,
            linkedGoalId = linkedGoalId,
            notes = notes,
            timestamp = timestamp
        )
        val id = transactionDao.insertTransaction(entity)
        val delta = if (type.equals("INCOME", ignoreCase = true)) amount else -amount
        accountDao.updateBalance(accountId, delta)
        return id
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        val old = transactionDao.getTransactionById(transaction.id)
        if (old != null) {
            val oldDelta = if (old.type.equals("INCOME", ignoreCase = true)) -old.amount else old.amount
            accountDao.updateBalance(old.accountId, oldDelta)
        }
        transactionDao.updateTransaction(transaction)
        val newDelta = if (transaction.type.equals("INCOME", ignoreCase = true)) transaction.amount else -transaction.amount
        accountDao.updateBalance(transaction.accountId, newDelta)
    }

    override suspend fun deleteTransaction(id: Long) {
        val old = transactionDao.getTransactionById(id)
        if (old != null) {
            val oldDelta = if (old.type.equals("INCOME", ignoreCase = true)) -old.amount else old.amount
            accountDao.updateBalance(old.accountId, oldDelta)
            transactionDao.deleteTransactionById(id)
        }
    }

    // --- Financial System: Accounts & Wallets ---
    override fun getAllAccountsFlow(): Flow<List<AccountEntity>> =
        accountDao.getAllAccountsFlow()

    override suspend fun addAccount(
        name: String,
        accountType: String,
        balance: Double,
        currencyCode: String,
        colorHex: String,
        icon: String
    ): Long {
        val entity = AccountEntity(
            name = name.trim(),
            accountType = accountType,
            balance = balance,
            currencyCode = currencyCode,
            colorHex = colorHex,
            icon = icon
        )
        return accountDao.insertAccount(entity)
    }

    override suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccountById(id)
    }

    override suspend fun transferFunds(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        notes: String?
    ) {
        val fromAccount = accountDao.getAccountById(fromAccountId)
        val toAccount = accountDao.getAccountById(toAccountId)
        val fromName = fromAccount?.name ?: "Account #$fromAccountId"
        val toName = toAccount?.name ?: "Account #$toAccountId"
        val now = System.currentTimeMillis()

        val outTx = TransactionEntity(
            title = "Transfer to $toName",
            amount = amount,
            type = "EXPENSE",
            category = "Transfer",
            tags = listOf("transfer", "transfer-out"),
            accountId = fromAccountId,
            currency = fromAccount?.currencyCode ?: "USD",
            notes = notes,
            timestamp = now
        )
        transactionDao.insertTransaction(outTx)
        accountDao.updateBalance(fromAccountId, -amount)

        val inTx = TransactionEntity(
            title = "Transfer from $fromName",
            amount = amount,
            type = "INCOME",
            category = "Transfer",
            tags = listOf("transfer", "transfer-in"),
            accountId = toAccountId,
            currency = toAccount?.currencyCode ?: "USD",
            notes = notes,
            timestamp = now
        )
        transactionDao.insertTransaction(inTx)
        accountDao.updateBalance(toAccountId, amount)
    }

    // --- Financial System: Budgets ---
    override fun getAllBudgetsFlow(): Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgetsFlow()

    override suspend fun addBudget(
        categoryName: String,
        limitAmount: Double,
        period: String,
        alertThresholdPercent: Double,
        currencyCode: String
    ): Long {
        val entity = BudgetEntity(
            categoryName = categoryName.trim(),
            limitAmount = limitAmount,
            period = period,
            alertThresholdPercent = alertThresholdPercent,
            currencyCode = currencyCode
        )
        return budgetDao.insertBudget(entity)
    }

    override suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    override suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    // --- Financial System: Goals ---
    override fun getAllFinancialGoalsFlow(): Flow<List<FinancialGoalEntity>> =
        financialGoalDao.getAllFinancialGoalsFlow()

    override suspend fun addFinancialGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        targetDate: Long?,
        colorHex: String,
        icon: String
    ): Long {
        val entity = FinancialGoalEntity(
            title = title.trim(),
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate,
            colorHex = colorHex,
            icon = icon
        )
        return financialGoalDao.insertFinancialGoal(entity)
    }

    override suspend fun updateFinancialGoal(goal: FinancialGoalEntity) {
        financialGoalDao.updateFinancialGoal(goal)
    }

    override suspend fun deleteFinancialGoal(id: Long) {
        financialGoalDao.deleteFinancialGoalById(id)
    }

    // --- Batch Import Primitives for Backup/Restore ---
    override suspend fun importTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }

    override suspend fun importAccounts(accounts: List<AccountEntity>) {
        accountDao.insertAccounts(accounts)
    }

    override suspend fun importBudgets(budgets: List<BudgetEntity>) {
        budgetDao.insertBudgets(budgets)
    }

    override suspend fun importFinancialGoals(goals: List<FinancialGoalEntity>) {
        financialGoalDao.insertFinancialGoals(goals)
    }
}
