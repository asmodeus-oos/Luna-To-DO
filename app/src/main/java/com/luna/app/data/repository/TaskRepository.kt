package com.luna.app.data.repository

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
        initialSubtasks: List<String> = emptyList(),
        alarmOnStart: Boolean = false,
        alarmOnFinish: Boolean = false,
        weeklyDay: String? = null,
        weeklyTime: String? = null,
        place: String? = null
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

    // Backup & Import
    suspend fun getAllTasksWithDetailsList(): List<TaskWithDetails>
    suspend fun importTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>): Long
    suspend fun importHabit(habit: HabitEntity): Long
    suspend fun importRoutine(routine: RoutineEntity): Long
    suspend fun importProject(project: ProjectEntity): Long
    suspend fun importGoal(goal: GoalEntity): Long

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

    // Financial System: Transactions
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    fun getRecentTransactionsFlow(limit: Int = 20): Flow<List<TransactionEntity>>
    fun getTransactionsByAccountFlow(accountId: Long): Flow<List<TransactionEntity>>
    fun getTransactionsByCategoryFlow(category: String): Flow<List<TransactionEntity>>
    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: String = "EXPENSE",
        category: String = "General",
        tags: List<String> = emptyList(),
        accountId: Long = 1L,
        currency: String = "USD",
        receiptUri: String? = null,
        recurrenceRule: String = "NONE",
        linkedTaskId: Long? = null,
        linkedProjectId: Long? = null,
        linkedGoalId: Long? = null,
        notes: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Long
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(id: Long)

    // Financial System: Accounts & Wallets
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun addAccount(
        name: String,
        accountType: String = "CASH",
        balance: Double = 0.0,
        currencyCode: String = "USD",
        colorHex: String = "#3B82F6",
        icon: String = "Wallet"
    ): Long
    suspend fun updateAccount(account: AccountEntity)
    suspend fun deleteAccount(id: Long)
    suspend fun transferFunds(fromAccountId: Long, toAccountId: Long, amount: Double, notes: String? = null)

    // Financial System: Budgets
    fun getAllBudgetsFlow(): Flow<List<BudgetEntity>>
    suspend fun addBudget(
        categoryName: String,
        limitAmount: Double,
        period: String = "MONTHLY",
        alertThresholdPercent: Double = 80.0,
        currencyCode: String = "USD"
    ): Long
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(id: Long)

    // Financial System: Goals
    fun getAllFinancialGoalsFlow(): Flow<List<FinancialGoalEntity>>
    suspend fun addFinancialGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double = 0.0,
        targetDate: Long? = null,
        colorHex: String = "#10B981",
        icon: String = "Savings"
    ): Long
    suspend fun updateFinancialGoal(goal: FinancialGoalEntity)
    suspend fun deleteFinancialGoal(id: Long)

    // Batch Import Primitives for Backup/Restore
    suspend fun importTransactions(transactions: List<TransactionEntity>)
    suspend fun importAccounts(accounts: List<AccountEntity>)
    suspend fun importBudgets(budgets: List<BudgetEntity>)
    suspend fun importFinancialGoals(goals: List<FinancialGoalEntity>)
}
