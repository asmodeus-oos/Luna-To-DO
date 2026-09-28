package com.luna.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luna.app.audio.TactileSoundPlayer
import com.luna.app.data.local.DentalTimetableSeeder
import com.luna.app.data.local.entity.AccountEntity
import com.luna.app.data.local.entity.BudgetEntity
import com.luna.app.data.local.entity.FinancialGoalEntity
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.local.entity.TransactionEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.data.preferences.AppThemeMode
import com.luna.app.data.preferences.UserPreferencesRepository
import com.luna.app.data.repository.TaskRepository
import com.luna.app.domain.model.AppViewMode
import android.content.Context
import android.net.Uri
import com.luna.app.data.backup.BackupResult
import com.luna.app.data.backup.LunaBackupManager
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.SmartFilter
import com.luna.app.domain.model.TaskFilter
import com.luna.app.domain.model.TaskStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.luna.app.notification.LunaAlarmScheduler
import java.util.Calendar

data class CelebrationState(
    val task: TaskEntity,
    val xpAwarded: Int = 50,
    val timePerformanceText: String? = null
)

data class LunaUiState(
    val tasks: List<TaskWithDetails> = emptyList(),
    val allTasks: List<TaskWithDetails> = emptyList(),
    val rawTasks: List<TaskEntity> = emptyList(),
    val counts: Map<TaskFilter, Int> = emptyMap(),
    val smartCounts: Map<SmartFilter, Int> = emptyMap(),
    val selectedFilter: TaskFilter = TaskFilter.TODAY,
    val selectedSmartFilter: SmartFilter = SmartFilter.TODAY,
    val selectedEnergyFilter: String = "ALL",
    val searchQuery: String = "",
    val selectedViewMode: AppViewMode = AppViewMode.LIST,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isAmoledEnabled: Boolean = false,
    val showNavTabLabels: Boolean = false,
    val autoDeleteCompletedDaily: Boolean = false,
    val userCountry: String = "United States",
    val userTimezone: String = "UTC",
    val isSettingsOpen: Boolean = false,
    val isCreateSheetOpen: Boolean = false,
    val isTemplatesSheetOpen: Boolean = false,
    val editingTask: TaskWithDetails? = null,
    val selectedTaskIds: Set<Long> = emptySet(),
    val completingTaskIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val templates: List<TaskTemplateEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val routines: List<RoutineEntity> = emptyList(),
    val dependenciesMap: Map<Long, String> = emptyMap(),
    val userName: String = "",
    val userAvatarPath: String = "",
    val userCoverPath: String = "",
    val coverTitle: String = "Mountains",
    val showCoverBannerText: Boolean = true,
    val userGender: String = "BOY",
    val focusWorkSeconds: Int = 25 * 60,
    val shortBreakSeconds: Int = 5 * 60,
    val longBreakSeconds: Int = 15 * 60,
    val isOnboardingCompleted: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val financialGoals: List<FinancialGoalEntity> = emptyList(),
    val isCreateTransactionSheetOpen: Boolean = false,
    val isTransferSheetOpen: Boolean = false,
    val isCreateBudgetSheetOpen: Boolean = false,
    val isCreateFinancialGoalSheetOpen: Boolean = false,
    val editingTransaction: TransactionEntity? = null
)

class TaskViewModel(
    private val repository: TaskRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: LunaAlarmScheduler? = null,
    private val soundPlayer: TactileSoundPlayer = TactileSoundPlayer()
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskFilter.TODAY)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()

    private val _selectedSmartFilter = MutableStateFlow(SmartFilter.TODAY)
    val selectedSmartFilter: StateFlow<SmartFilter> = _selectedSmartFilter.asStateFlow()

    private val _selectedEnergyFilter = MutableStateFlow("ALL")
    val selectedEnergyFilter: StateFlow<String> = _selectedEnergyFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedViewMode = MutableStateFlow(AppViewMode.LIST)
    val selectedViewMode: StateFlow<AppViewMode> = _selectedViewMode.asStateFlow()

    private val _isCreateSheetOpen = MutableStateFlow(false)
    val isCreateSheetOpen: StateFlow<Boolean> = _isCreateSheetOpen.asStateFlow()

    private val _isTemplatesSheetOpen = MutableStateFlow(false)
    val isTemplatesSheetOpen: StateFlow<Boolean> = _isTemplatesSheetOpen.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskWithDetails?>(null)
    val editingTask: StateFlow<TaskWithDetails?> = _editingTask.asStateFlow()

    private val _selectedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTaskIds: StateFlow<Set<Long>> = _selectedTaskIds.asStateFlow()

    private val _celebration = MutableStateFlow<CelebrationState?>(null)
    val celebration: StateFlow<CelebrationState?> = _celebration.asStateFlow()

    private val _overdueTaskForReason = MutableStateFlow<TaskEntity?>(null)
    val overdueTaskForReason: StateFlow<TaskEntity?> = _overdueTaskForReason.asStateFlow()

    private val _taskToDeleteWithReason = MutableStateFlow<TaskEntity?>(null)
    val taskToDeleteWithReason: StateFlow<TaskEntity?> = _taskToDeleteWithReason.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _completingTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val completingTaskIds: StateFlow<Set<Long>> = _completingTaskIds.asStateFlow()

    private val _isCreateTransactionSheetOpen = MutableStateFlow(false)
    val isCreateTransactionSheetOpen: StateFlow<Boolean> = _isCreateTransactionSheetOpen.asStateFlow()

    private val _isTransferSheetOpen = MutableStateFlow(false)
    val isTransferSheetOpen: StateFlow<Boolean> = _isTransferSheetOpen.asStateFlow()

    private val _isCreateBudgetSheetOpen = MutableStateFlow(false)
    val isCreateBudgetSheetOpen: StateFlow<Boolean> = _isCreateBudgetSheetOpen.asStateFlow()

    private val _isCreateFinancialGoalSheetOpen = MutableStateFlow(false)
    val isCreateFinancialGoalSheetOpen: StateFlow<Boolean> = _isCreateFinancialGoalSheetOpen.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                checkOverdueTasks()
            }
        }
        viewModelScope.launch {
            while (true) {
                checkAndPerformDailyCleanup()
                delay(60_000L)
            }
        }
    }

    private suspend fun checkOverdueTasks() {
        try {
            val now = System.currentTimeMillis()
            val currentTasks = uiState.value.rawTasks
            for (t in currentTasks) {
                if (!t.isCompleted && t.status == TaskStatus.IN_PROGRESS && t.effectiveDeadlineEpoch != null) {
                    if (now >= t.effectiveDeadlineEpoch!!) {
                        repository.setTaskStatus(t.id, TaskStatus.OVERDUE_PENDING_REASON)
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore flow cancellation during app lifecycle transitions
        }
    }

    fun promptOverdueReason(task: TaskEntity) {
        _overdueTaskForReason.value = task
    }

    fun dismissOverdueReason() {
        _overdueTaskForReason.value = null
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    private val preferencesState = combine(
        combine(
            preferencesRepository.themeModeFlow,
            preferencesRepository.soundEnabledFlow,
            preferencesRepository.hapticsEnabledFlow,
            preferencesRepository.amoledEnabledFlow
        ) { theme, sound, haptics, amoled ->
            Quad(theme, sound, haptics, amoled)
        },
        combine(
            preferencesRepository.showNavTabLabelsFlow,
            preferencesRepository.autoDeleteCompletedDailyFlow,
            preferencesRepository.userCountryFlow,
            preferencesRepository.userTimezoneFlow
        ) { showLabels, autoDelete, country, timezone ->
            Quad(showLabels, autoDelete, country, timezone)
        },
        combine(
            preferencesRepository.userNameFlow,
            preferencesRepository.userAvatarPathFlow,
            preferencesRepository.focusWorkSecondsFlow,
            preferencesRepository.shortBreakSecondsFlow,
            preferencesRepository.longBreakSecondsFlow
        ) { name, avatar, workSec, shortSec, longSec ->
            Quint(name, avatar, workSec, shortSec, longSec)
        },
        combine(
            preferencesRepository.userCoverPathFlow,
            preferencesRepository.coverTitleFlow,
            preferencesRepository.showCoverBannerTextFlow,
            preferencesRepository.userGenderFlow
        ) { coverPath, coverTitle, showText, gender ->
            Quad(coverPath, coverTitle, showText, gender)
        },
        preferencesRepository.onboardingCompletedFlow
    ) { (theme, sound, haptics, amoled), (showLabels, autoDelete, country, timezone), (name, avatar, workSec, shortSec, longSec), (coverPath, coverTitle, showText, gender), onboardingDone ->
        UserPrefsBundle(
            theme, sound, haptics, amoled,
            showLabels, autoDelete, country, timezone,
            name, avatar, workSec, shortSec, longSec,
            coverPath, coverTitle, showText, gender,
            onboardingDone
        )
    }

    private val baseDataState = combine(
        repository.getTasksWithDetailsFlow(),
        repository.getTemplatesFlow(),
        repository.getProjectsFlow(),
        repository.getHabitsFlow()
    ) { tasks, templates, projects, habits ->
        Quad(tasks, templates, projects, habits)
    }

    private val extraDataState = combine(
        repository.getGoalsFlow(),
        repository.getRoutinesFlow()
    ) { goals, routines ->
        Pair(goals, routines)
    }

    private val financialDataState = combine(
        repository.getAllTransactionsFlow(),
        repository.getAllAccountsFlow(),
        repository.getAllBudgetsFlow(),
        repository.getAllFinancialGoalsFlow()
    ) { transactions, accounts, budgets, goals ->
        Quad(transactions, accounts, budgets, goals)
    }

    private val dataState = combine(baseDataState, extraDataState, financialDataState) { (tasks, templates, projects, habits), (goals, routines), (transactions, accounts, budgets, finGoals) ->
        DataBundle(tasks, templates, projects, habits, goals, routines, transactions, accounts, budgets, finGoals)
    }

    private val uiControlState = combine(
        _selectedSmartFilter,
        _searchQuery,
        _selectedViewMode,
        _isCreateSheetOpen,
        combine(
            _isTemplatesSheetOpen,
            _editingTask,
            _selectedTaskIds,
            _isSettingsOpen,
            _completingTaskIds
        ) { templatesOpen, editing, selectedIds, settingsOpen, completingIds ->
            Quint(templatesOpen, editing, selectedIds, settingsOpen, completingIds)
        }
    ) { smartFilter, query, viewMode, sheetOpen, (templatesOpen, editing, selectedIds, settingsOpen, completingIds) ->
        FilterQueryState(smartFilter, query, viewMode, sheetOpen, templatesOpen, editing, selectedIds, settingsOpen, completingIds)
    }

    private val financialUiControlState = combine(
        _isCreateTransactionSheetOpen,
        _isTransferSheetOpen,
        _isCreateBudgetSheetOpen,
        _isCreateFinancialGoalSheetOpen,
        _editingTransaction
    ) { txOpen, transferOpen, budgetOpen, goalOpen, editingTx ->
        FinancialUiControlState(txOpen, transferOpen, budgetOpen, goalOpen, editingTx)
    }

    val uiState: StateFlow<LunaUiState> = combine(
        dataState,
        preferencesState,
        uiControlState,
        financialUiControlState
    ) { dataBundle, prefs, uiControl, finControl ->
        val rawTasks = dataBundle.tasks.map { it.task }
        val smartCounts = calculateSmartFilterCounts(rawTasks)
        val legacyCounts = calculateLegacyFilterCounts(rawTasks)
        val filtered = filterTasks(dataBundle.tasks, uiControl.smartFilter, uiControl.query, _selectedEnergyFilter.value)

        val taskMap = rawTasks.associateBy { it.id }
        val dependencies = mutableMapOf<Long, String>()
        for (item in dataBundle.tasks) {
            val depId = item.task.dependsOnTaskId
            if (depId != null) {
                val parent = taskMap[depId]
                if (parent != null && !parent.isCompleted) {
                    dependencies[item.task.id] = parent.title
                }
            }
        }

        LunaUiState(
            tasks = filtered,
            allTasks = dataBundle.tasks,
            rawTasks = rawTasks,
            counts = legacyCounts,
            smartCounts = smartCounts,
            selectedSmartFilter = uiControl.smartFilter,
            selectedEnergyFilter = _selectedEnergyFilter.value,
            searchQuery = uiControl.query,
            selectedViewMode = uiControl.viewMode,
            themeMode = prefs.theme,
            isSoundEnabled = prefs.sound,
            isHapticsEnabled = prefs.haptics,
            isAmoledEnabled = prefs.amoled,
            showNavTabLabels = prefs.showTabLabels,
            autoDeleteCompletedDaily = prefs.autoDeleteDaily,
            userCountry = prefs.country,
            userTimezone = prefs.timezone,
            isSettingsOpen = uiControl.settingsOpen,
            isCreateSheetOpen = uiControl.sheetOpen,
            isTemplatesSheetOpen = uiControl.templatesOpen,
            editingTask = uiControl.editing,
            selectedTaskIds = uiControl.selectedIds,
            completingTaskIds = uiControl.completingIds,
            isSelectionMode = uiControl.selectedIds.isNotEmpty(),
            templates = dataBundle.templates,
            projects = dataBundle.projects,
            habits = dataBundle.habits,
            goals = dataBundle.goals,
            routines = dataBundle.routines,
            dependenciesMap = dependencies,
            userName = prefs.userName,
            userAvatarPath = prefs.userAvatarPath,
            userCoverPath = prefs.userCoverPath,
            coverTitle = prefs.coverTitle,
            showCoverBannerText = prefs.showCoverBannerText,
            userGender = prefs.userGender,
            focusWorkSeconds = prefs.focusWorkSeconds,
            shortBreakSeconds = prefs.shortBreakSeconds,
            longBreakSeconds = prefs.longBreakSeconds,
            isOnboardingCompleted = prefs.isOnboardingCompleted,
            transactions = dataBundle.transactions,
            accounts = dataBundle.accounts,
            budgets = dataBundle.budgets,
            financialGoals = dataBundle.financialGoals,
            isCreateTransactionSheetOpen = finControl.createTxOpen,
            isTransferSheetOpen = finControl.transferOpen,
            isCreateBudgetSheetOpen = finControl.createBudgetOpen,
            isCreateFinancialGoalSheetOpen = finControl.createFinancialGoalOpen,
            editingTransaction = finControl.editingTx
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LunaUiState(
            themeMode = preferencesRepository.getInitialThemeMode(),
            isOnboardingCompleted = preferencesRepository.getInitialOnboardingCompleted()
        )
    )

    fun selectFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun selectSmartFilter(filter: SmartFilter) {
        _selectedSmartFilter.value = filter
    }

    fun selectEnergyFilter(energy: String) {
        _selectedEnergyFilter.value = energy.uppercase()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectViewMode(mode: AppViewMode) {
        _selectedViewMode.value = mode
    }

    fun openCreateSheet() {
        _isCreateSheetOpen.value = true
    }

    fun closeCreateSheet() {
        _isCreateSheetOpen.value = false
    }

    fun openTemplatesSheet() {
        _isTemplatesSheetOpen.value = true
    }

    fun closeTemplatesSheet() {
        _isTemplatesSheetOpen.value = false
    }

    fun openTaskDetail(task: TaskWithDetails) {
        _editingTask.value = task
    }

    fun closeTaskDetail() {
        _editingTask.value = null
    }

    // --- Task CRUD ---
    fun addTask(
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
        subtasks: List<String> = emptyList(),
        alarmOnStart: Boolean = false,
        alarmOnFinish: Boolean = false,
        weeklyDay: String? = null,
        weeklyTime: String? = null,
        place: String? = null
    ) {
        viewModelScope.launch {
            val taskId = repository.addTask(
                title = title,
                notes = notes,
                subtitles = subtitles,
                startDate = startDate,
                dueDate = dueDate,
                dueTime = dueTime,
                deadlineMode = deadlineMode,
                durationValue = durationValue,
                durationUnit = durationUnit,
                priority = priority,
                tags = tags,
                recurrenceRule = recurrenceRule,
                initialSubtasks = subtasks,
                alarmOnStart = alarmOnStart,
                alarmOnFinish = alarmOnFinish,
                weeklyDay = weeklyDay,
                weeklyTime = weeklyTime,
                place = place
            )
            if (dueDate != null && dueDate > System.currentTimeMillis()) {
                val task = repository.getTaskById(taskId)
                if (task != null) alarmScheduler?.scheduleTaskAlarm(task)
            }
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun quickAddTask(title: String) {
        if (title.isNotBlank()) {
            val todayEnd = getEndOfDayMillis(0)
            addTask(title = title.trim(), dueDate = todayEnd)
        }
    }

    fun injectDentalTimetable(overwrite: Boolean = false, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val inserted = DentalTimetableSeeder.seed(repository, alarmScheduler = alarmScheduler, overwrite = overwrite)
            if (inserted > 0 && uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
            onComplete?.invoke(inserted)
        }
    }

    fun testTimetableAlarm(isStart: Boolean) {
        alarmScheduler?.scheduleTestAlarm(isStart = isStart, delaySeconds = 10)
    }

    fun toggleTimetableAlarms(task: TaskEntity, enableStart: Boolean, enableFinish: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(
                alarmOnStart = enableStart,
                alarmOnFinish = enableFinish
            )
            repository.updateTask(updated)
            alarmScheduler?.scheduleTaskAlarms(updated)
            if (uiState.value.isSoundEnabled) {
                if (enableStart || enableFinish) {
                    soundPlayer.playAdd()
                } else {
                    soundPlayer.playUncheck()
                }
            }
        }
    }

    fun completeOnboarding(name: String, coverTitle: String, gender: String, themeMode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
            preferencesRepository.setCoverTitle(coverTitle)
            preferencesRepository.setUserGender(gender)
            preferencesRepository.setThemeMode(themeMode)
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun startTask(taskId: Long) {
        viewModelScope.launch {
            repository.startTask(taskId)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun onLogMissed(taskId: Long, category: String, details: String) {
        viewModelScope.launch {
            repository.logMissedReason(
                taskId = taskId,
                category = category,
                details = details,
                actionTaken = "LOGGED_MISSED",
                extendedMinutes = 0
            )
            _overdueTaskForReason.value = null
        }
    }

    fun onRescheduleTask(taskId: Long, category: String, details: String, additionalMinutes: Int) {
        viewModelScope.launch {
            repository.logMissedReason(
                taskId = taskId,
                category = category,
                details = details,
                actionTaken = "RESCHEDULED",
                extendedMinutes = additionalMinutes
            )
            _overdueTaskForReason.value = null
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            // Re-schedule alarm if due date changed
            if (task.dueDate != null && task.dueDate > System.currentTimeMillis() && !task.isCompleted) {
                alarmScheduler?.scheduleTaskAlarm(task)
            } else {
                alarmScheduler?.cancelTaskAlarm(task.id)
            }
            if (_editingTask.value?.task?.id == task.id) {
                val updatedWithDetails = repository.getTaskWithDetailsById(task.id)
                _editingTask.value = updatedWithDetails
            }
        }
    }

    fun setTaskStatus(taskId: Long, status: TaskStatus) {
        viewModelScope.launch {
            if (status == TaskStatus.DONE) {
                // Use toggleTaskCompleted to properly handle recurrence
                repository.toggleTaskCompleted(taskId, true)
                val task = repository.getTaskById(taskId)
                if (task != null) {
                    triggerTaskCelebration(task)
                }
            } else {
                repository.setTaskStatus(taskId, status)
            }
        }
    }

    fun completeTask(taskId: Long) {
        setTaskStatus(taskId, TaskStatus.DONE)
    }

    fun logTaskMinutes(taskId: Long, minutes: Int) {
        viewModelScope.launch {
            repository.logTaskMinutes(taskId, minutes)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val nextCompleted = !task.isCompleted
            if (nextCompleted) {
                // Register local completing state so checkbox animates immediately in place
                _completingTaskIds.value = _completingTaskIds.value + task.id
                if (uiState.value.isSoundEnabled) {
                    soundPlayer.playComplete()
                }
                // Quick delay for checkbox checkmark stroke animation before showing celebration
                delay(180L)
                repository.toggleTaskCompleted(task.id, true)
                triggerTaskCelebration(task)
                delay(250L)
                _completingTaskIds.value = _completingTaskIds.value - task.id
            } else {
                if (uiState.value.isSoundEnabled) {
                    soundPlayer.playUncheck()
                }
                repository.toggleTaskCompleted(task.id, false)
            }
        }
    }

    private fun triggerTaskCelebration(task: TaskEntity) {
        val now = System.currentTimeMillis()
        var timePerfText: String? = null
        val deadline = task.effectiveDeadlineEpoch
        if (deadline != null && deadline > now) {
            val remainingMinutes = ((deadline - now) / (60 * 1000L)).toInt()
            timePerfText = if (remainingMinutes >= 60) {
                val hours = remainingMinutes / 60
                val mins = remainingMinutes % 60
                "Finished with ${hours}h ${mins}m to spare! ⚡"
            } else {
                "Finished with ${remainingMinutes}m to spare! ⚡"
            }
        }

        _celebration.value = CelebrationState(
            task = task,
            xpAwarded = task.xpValue.coerceAtLeast(50),
            timePerformanceText = timePerfText
        )

        if (uiState.value.isSoundEnabled) {
            soundPlayer.playCelebrationFanfare()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            alarmScheduler?.cancelTaskAlarm(taskId)
            repository.deleteTask(taskId)
            _selectedTaskIds.value = _selectedTaskIds.value - taskId
            if (_editingTask.value?.task?.id == taskId) {
                _editingTask.value = null
            }
        }
    }

    fun requestDeleteTask(task: TaskEntity) {
        if (task.isCompleted) {
            deleteTask(task.id)
        } else {
            _taskToDeleteWithReason.value = task
        }
    }

    fun dismissDeleteUnfinished() {
        _taskToDeleteWithReason.value = null
    }

    fun confirmDeleteUnfinished(taskId: Long, category: String, details: String) {
        viewModelScope.launch {
            repository.logMissedReason(
                taskId = taskId,
                category = category,
                details = details.ifBlank { "Task deleted unfinished: $category" },
                actionTaken = "DELETED_UNFINISHED"
            )
            deleteTask(taskId)
            _taskToDeleteWithReason.value = null
        }
    }

    fun duplicateTask(taskId: Long) {
        viewModelScope.launch {
            repository.duplicateTask(taskId)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
            _selectedTaskIds.value = emptySet()
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            repository.deleteAllTasks()
            _selectedTaskIds.value = emptySet()
            _editingTask.value = null
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun deleteAllRoutines() {
        viewModelScope.launch {
            repository.deleteAllRoutines()
        }
    }

    fun deleteAllHabits() {
        viewModelScope.launch {
            repository.deleteAllHabits()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedTaskIds.value = emptySet()
            _editingTask.value = null
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    // --- Backup & Restore (Full Data Export & Import) ---
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    fun dismissBackupStatus() {
        _backupStatus.value = null
    }

    fun exportBackupToUri(context: Context, uri: Uri, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val json = LunaBackupManager.exportBackupJson(repository, preferencesRepository)
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray(Charsets.UTF_8))
                }
                val msg = "✓ Luna backup exported successfully!"
                _backupStatus.value = msg
                if (uiState.value.isSoundEnabled) {
                    soundPlayer.playComplete()
                }
                onResult?.invoke(true, msg)
            } catch (e: Exception) {
                val err = "Export failed: ${e.localizedMessage}"
                _backupStatus.value = err
                onResult?.invoke(false, err)
            }
        }
    }

    fun importBackupFromUri(context: Context, uri: Uri, onResult: ((BackupResult) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().use { it.readText() }
                } ?: ""

                if (jsonString.isBlank()) {
                    val fail = BackupResult(false, errorMessage = "Selected file is empty")
                    _backupStatus.value = fail.summary
                    onResult?.invoke(fail)
                    return@launch
                }

                val result = LunaBackupManager.importBackupJson(
                    jsonString = jsonString,
                    repository = repository,
                    preferences = preferencesRepository,
                    alarmScheduler = alarmScheduler
                )
                _backupStatus.value = result.summary
                if (result.success && uiState.value.isSoundEnabled) {
                    soundPlayer.playComplete()
                }
                onResult?.invoke(result)
            } catch (e: Exception) {
                val fail = BackupResult(false, errorMessage = "Import failed: ${e.localizedMessage}")
                _backupStatus.value = fail.summary
                onResult?.invoke(fail)
            }
        }
    }

    // --- Subtasks ---
    fun addSubtask(taskId: Long, title: String, subtitleSection: String? = null) {
        viewModelScope.launch {
            repository.addSubtask(taskId, title, subtitleSection)
            val updated = repository.getTaskWithDetailsById(taskId)
            if (_editingTask.value?.task?.id == taskId) {
                _editingTask.value = updated
            }
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun toggleSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.toggleSubtask(subtask.id, !subtask.isCompleted)
            val updated = repository.getTaskWithDetailsById(subtask.taskId)
            if (_editingTask.value?.task?.id == subtask.taskId) {
                _editingTask.value = updated
            }
            if (uiState.value.isSoundEnabled) {
                if (!subtask.isCompleted) soundPlayer.playComplete() else soundPlayer.playUncheck()
            }
        }
    }

    fun deleteSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask.id)
            val updated = repository.getTaskWithDetailsById(subtask.taskId)
            if (_editingTask.value?.task?.id == subtask.taskId) {
                _editingTask.value = updated
            }
        }
    }

    // --- Multi-Select & Bulk Actions ---
    fun toggleSelection(taskId: Long) {
        val current = _selectedTaskIds.value
        _selectedTaskIds.value = if (current.contains(taskId)) {
            current - taskId
        } else {
            current + taskId
        }
    }

    fun clearSelection() {
        _selectedTaskIds.value = emptySet()
    }

    fun bulkCompleteSelected() {
        viewModelScope.launch {
            val ids = _selectedTaskIds.value.toList()
            repository.bulkComplete(ids, true)
            clearSelection()
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun bulkDeleteSelected() {
        viewModelScope.launch {
            val ids = _selectedTaskIds.value.toList()
            repository.bulkDelete(ids)
            clearSelection()
        }
    }

    fun bulkSetDueDate(dueDate: Long?) {
        viewModelScope.launch {
            val ids = _selectedTaskIds.value.toList()
            repository.bulkSetDueDate(ids, dueDate)
            clearSelection()
        }
    }

    fun bulkSetPriority(priority: Priority) {
        viewModelScope.launch {
            val ids = _selectedTaskIds.value.toList()
            repository.bulkSetPriority(ids, priority)
            clearSelection()
        }
    }

    // --- Habits ---
    fun toggleHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val currentCount = habit.todayCompletedCount()
            val target = habit.targetPerDay.coerceAtLeast(1)
            if (currentCount >= target) return@launch // Locked till next day

            repository.toggleHabitStep(habit)
            val newCount = currentCount + 1
            if (newCount >= target) {
                val newStreak = habit.currentStreak + 1
                val habitTask = TaskEntity(
                    id = -habit.id,
                    title = "Habit Completed: ${habit.name} ${habit.icon}",
                    notes = "Daily target achieved! Locked until 00:00 tomorrow. Active Streak: $newStreak Days 🔥",
                    xpValue = 100
                )
                _celebration.value = CelebrationState(
                    task = habitTask,
                    xpAwarded = 100,
                    timePerformanceText = "$newStreak Day Streak 🔥"
                )
                if (uiState.value.isSoundEnabled) {
                    soundPlayer.playCelebrationFanfare()
                }
            } else if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun addHabit(
        name: String,
        icon: String = "⚡",
        colorHex: String = "#008FFD",
        targetPerDay: Int = 1,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            repository.addHabit(name, icon, colorHex, targetPerDay, imageUri)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun duplicateHabit(id: Long) {
        viewModelScope.launch {
            repository.duplicateHabit(id)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    // --- Projects ---
    fun addProject(name: String, parentId: Long? = null) {
        viewModelScope.launch {
            repository.addProject(name = name, parentId = parentId)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    // --- Goals / OKRs ---
    fun addGoal(title: String, description: String?, targetValue: Int, unit: String) {
        viewModelScope.launch {
            repository.addGoal(
                title = title,
                description = description,
                targetValue = targetValue,
                unit = unit,
                colorHex = "#008FFD",
                icon = "🎯"
            )
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun updateGoalProgress(id: Long, value: Int) {
        viewModelScope.launch {
            repository.updateGoalProgress(id, value)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // --- Routines ---
    fun addRoutine(name: String, icon: String, timeOfDay: String, steps: List<String>) {
        viewModelScope.launch {
            repository.addRoutine(name, icon, timeOfDay, steps)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun updateRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }

    fun duplicateRoutine(id: Long) {
        viewModelScope.launch {
            repository.duplicateRoutine(id)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun toggleRoutineCompletedToday(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.toggleRoutineCompletedToday(routine)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playComplete()
            }
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
        }
    }

    // --- Templates ---
    fun createFromTemplate(template: TaskTemplateEntity) {
        viewModelScope.launch {
            repository.createFromTemplate(template)
            if (uiState.value.isSoundEnabled) {
                soundPlayer.playAdd()
            }
        }
    }

    fun saveTaskAsTemplate(taskId: Long, name: String) {
        viewModelScope.launch {
            repository.saveTaskAsTemplate(taskId, name)
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }

    fun updateTemplate(template: TaskTemplateEntity) {
        viewModelScope.launch {
            repository.updateTemplate(template)
        }
    }

    // --- Preferences ---
    fun toggleTheme() {
        viewModelScope.launch {
            val isAmoled = uiState.value.isAmoledEnabled
            preferencesRepository.toggleThemeMode(isAmoled)
        }
    }

    fun setAmoledEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAmoledEnabled(enabled)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(!uiState.value.isHapticsEnabled)
        }
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun cycleTheme() {
        viewModelScope.launch {
            val current = uiState.value.themeMode
            val next = when (current) {
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.BLACK
                AppThemeMode.BLACK -> AppThemeMode.LIGHT
                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            }
            preferencesRepository.setThemeMode(next)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(!uiState.value.isSoundEnabled)
        }
    }

    fun setShowNavTabLabels(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowNavTabLabels(enabled)
        }
    }

    fun setAutoDeleteCompletedDaily(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoDeleteCompletedDaily(enabled)
            if (enabled) {
                checkAndPerformDailyCleanup()
            }
        }
    }

    fun setUserCountry(country: String) {
        viewModelScope.launch {
            preferencesRepository.setUserCountry(country)
        }
    }

    fun setUserTimezone(timezone: String) {
        viewModelScope.launch {
            preferencesRepository.setUserTimezone(timezone)
            checkAndPerformDailyCleanup()
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
        }
    }

    fun setUserAvatarPath(path: String) {
        viewModelScope.launch {
            preferencesRepository.setUserAvatarPath(path)
        }
    }

    fun setUserCoverPath(path: String) {
        viewModelScope.launch {
            preferencesRepository.setUserCoverPath(path)
        }
    }

    fun setCoverTitle(title: String) {
        viewModelScope.launch {
            preferencesRepository.setCoverTitle(title)
        }
    }

    fun setShowCoverBannerText(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowCoverBannerText(show)
        }
    }

    fun setUserGender(gender: String) {
        viewModelScope.launch {
            preferencesRepository.setUserGender(gender)
        }
    }

    fun setFocusDurations(workSeconds: Int, shortBreakSeconds: Int, longBreakSeconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setFocusDurations(workSeconds, shortBreakSeconds, longBreakSeconds)
        }
    }

    private suspend fun checkAndPerformDailyCleanup() {
        try {
            val autoDeleteEnabled = preferencesRepository.autoDeleteCompletedDailyFlow.first()
            if (!autoDeleteEnabled) return

            val tzId = preferencesRepository.userTimezoneFlow.first()
            val zoneId = try {
                java.time.ZoneId.of(tzId)
            } catch (_: Exception) {
                java.time.ZoneId.systemDefault()
            }

            val nowZoned = java.time.ZonedDateTime.now(zoneId)
            val todayStr = nowZoned.toLocalDate().toString()
            val lastCleanup = preferencesRepository.lastCleanupDayFlow.first()

            if (todayStr != lastCleanup) {
                val startOfDay = nowZoned.toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()
                repository.deleteCompletedTasksBefore(startOfDay)
                preferencesRepository.setLastCleanupDay(todayStr)
            }
        } catch (_: Exception) {
            // Ignore background daily cleanup exceptions
        }
    }

    private fun filterTasks(
        tasks: List<TaskWithDetails>,
        smartFilter: SmartFilter,
        query: String,
        energyFilter: String = "ALL"
    ): List<TaskWithDetails> {
        val todayStart = getStartOfDayMillis(0)
        val todayEnd = getEndOfDayMillis(0)
        val tomorrowStart = getStartOfDayMillis(1)
        val tomorrowEnd = getEndOfDayMillis(1)
        val weekEnd = getEndOfDayMillis(7)

        val chronologicalComparator = compareBy<TaskWithDetails> { it.task.isCompleted }
            .thenBy { it.task.dueDate ?: it.task.startDate ?: Long.MAX_VALUE }
            .thenBy { it.task.dueTime ?: it.task.weeklyTime ?: "23:59" }
            .thenBy { it.task.orderIndex }

        val byFilter = when (smartFilter) {
            SmartFilter.TODAY -> tasks.filter { item ->
                !item.task.isCompleted &&
                (item.task.dueDate == null || item.task.dueDate in todayStart..todayEnd || item.task.dueDate < todayStart)
            }.sortedWith(chronologicalComparator)
            SmartFilter.TOMORROW -> tasks.filter { item ->
                !item.task.isCompleted &&
                item.task.dueDate != null && item.task.dueDate in tomorrowStart..tomorrowEnd
            }.sortedWith(chronologicalComparator)
            SmartFilter.THIS_WEEK -> tasks.filter { item ->
                !item.task.isCompleted &&
                item.task.dueDate != null && item.task.dueDate in todayStart..weekEnd
            }.sortedWith(chronologicalComparator)
            SmartFilter.OVERDUE -> tasks.filter { item ->
                !item.task.isCompleted && item.task.dueDate != null && item.task.dueDate < todayStart
            }.sortedWith(
                compareBy<TaskWithDetails> { it.task.dueDate ?: 0L }
                    .thenBy { it.task.dueTime ?: "00:00" }
            )
            SmartFilter.HIGH_PRIORITY -> tasks.filter { item ->
                !item.task.isCompleted && item.task.priority == Priority.P1
            }.sortedWith(chronologicalComparator)
            SmartFilter.DEEP_WORK -> tasks.filter { item ->
                !item.task.isCompleted && item.task.energyLevel == "HIGH"
            }.sortedWith(chronologicalComparator)
            SmartFilter.QUICK_WINS -> tasks.filter { item ->
                !item.task.isCompleted && (item.task.energyLevel == "LOW" || item.task.estimatedMinutes in 1..15)
            }.sortedWith(chronologicalComparator)
            SmartFilter.PINNED -> tasks.filter { item ->
                !item.task.isCompleted && item.task.isPinned
            }.sortedWith(chronologicalComparator)
            SmartFilter.ALL -> tasks.filter { !it.task.isCompleted }.sortedWith(chronologicalComparator)
            SmartFilter.COMPLETED -> tasks.filter { it.task.isCompleted }.sortedWith(
                compareByDescending<TaskWithDetails> { it.task.completedAt ?: it.task.dueDate ?: 0L }
            )
        }

        val byEnergy = if (energyFilter != "ALL") {
            byFilter.filter { it.task.energyLevel.equals(energyFilter, ignoreCase = true) }
        } else {
            byFilter
        }

        if (query.isBlank()) return byEnergy

        val q = query.trim().lowercase()
        return byEnergy.filter { item ->
            val t = item.task
            t.title.lowercase().contains(q) ||
                    (t.notes?.lowercase()?.contains(q) == true) ||
                    t.tags.any { it.lowercase().contains(q) } ||
                    t.category.lowercase().contains(q)
        }
    }

    private fun calculateSmartFilterCounts(tasks: List<TaskEntity>): Map<SmartFilter, Int> {
        val todayStart = getStartOfDayMillis(0)
        val todayEnd = getEndOfDayMillis(0)
        val tomorrowStart = getStartOfDayMillis(1)
        val tomorrowEnd = getEndOfDayMillis(1)
        val weekEnd = getEndOfDayMillis(7)

        val todayCount = tasks.count { !it.isCompleted && (it.dueDate == null || it.dueDate in todayStart..todayEnd || it.dueDate < todayStart) }
        val tomorrowCount = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate in tomorrowStart..tomorrowEnd }
        val weekCount = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate in todayStart..weekEnd }
        val overdueCount = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate < todayStart }
        val highPriorityCount = tasks.count { !it.isCompleted && it.priority == Priority.P1 }
        val deepWorkCount = tasks.count { !it.isCompleted && it.energyLevel == "HIGH" }
        val quickWinsCount = tasks.count { !it.isCompleted && (it.energyLevel == "LOW" || it.estimatedMinutes in 1..15) }
        val pinnedCount = tasks.count { !it.isCompleted && it.isPinned }
        val allCount = tasks.count { !it.isCompleted }
        val completedCount = tasks.count { it.isCompleted }

        return mapOf(
            SmartFilter.TODAY to todayCount,
            SmartFilter.TOMORROW to tomorrowCount,
            SmartFilter.THIS_WEEK to weekCount,
            SmartFilter.OVERDUE to overdueCount,
            SmartFilter.HIGH_PRIORITY to highPriorityCount,
            SmartFilter.DEEP_WORK to deepWorkCount,
            SmartFilter.QUICK_WINS to quickWinsCount,
            SmartFilter.PINNED to pinnedCount,
            SmartFilter.ALL to allCount,
            SmartFilter.COMPLETED to completedCount
        )
    }

    private fun calculateLegacyFilterCounts(tasks: List<TaskEntity>): Map<TaskFilter, Int> {
        val todayStart = getStartOfDayMillis(0)
        val todayEnd = getEndOfDayMillis(0)
        val tomorrowStart = getStartOfDayMillis(1)
        val tomorrowEnd = getEndOfDayMillis(1)

        val todayCount = tasks.count { !it.isCompleted && (it.dueDate == null || it.dueDate in todayStart..todayEnd || it.dueDate < todayStart) }
        val tomorrowCount = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate in tomorrowStart..tomorrowEnd }
        val upcomingCount = tasks.count { !it.isCompleted && it.dueDate != null && it.dueDate > tomorrowEnd }
        val allCount = tasks.count { !it.isCompleted }
        val completedCount = tasks.count { it.isCompleted }

        return mapOf(
            TaskFilter.TODAY to todayCount,
            TaskFilter.TOMORROW to tomorrowCount,
            TaskFilter.UPCOMING to upcomingCount,
            TaskFilter.ALL to allCount,
            TaskFilter.COMPLETED to completedCount
        )
    }

    private fun getStartOfDayMillis(dayOffset: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDayMillis(dayOffset: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    // --- Financial System Actions ---
    fun openCreateTransactionSheet(transaction: TransactionEntity? = null) {
        _editingTransaction.value = transaction
        _isCreateTransactionSheetOpen.value = true
    }

    fun closeCreateTransactionSheet() {
        _isCreateTransactionSheetOpen.value = false
        _editingTransaction.value = null
    }

    fun openTransferSheet() {
        _isTransferSheetOpen.value = true
    }

    fun closeTransferSheet() {
        _isTransferSheetOpen.value = false
    }

    fun openCreateBudgetSheet() {
        _isCreateBudgetSheetOpen.value = true
    }

    fun closeCreateBudgetSheet() {
        _isCreateBudgetSheetOpen.value = false
    }

    fun openCreateFinancialGoalSheet() {
        _isCreateFinancialGoalSheetOpen.value = true
    }

    fun closeCreateFinancialGoalSheet() {
        _isCreateFinancialGoalSheetOpen.value = false
    }

    fun addTransaction(
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
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                title = title,
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
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addAccount(
        name: String,
        accountType: String = "CASH",
        balance: Double = 0.0,
        currencyCode: String = "USD",
        colorHex: String = "#3B82F6",
        icon: String = "Wallet"
    ) {
        viewModelScope.launch {
            repository.addAccount(
                name = name,
                accountType = accountType,
                balance = balance,
                currencyCode = currencyCode,
                colorHex = colorHex,
                icon = icon
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun transferFunds(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.transferFunds(fromAccountId, toAccountId, amount, notes)
        }
    }

    fun addBudget(
        categoryName: String,
        limitAmount: Double,
        period: String = "MONTHLY",
        alertThresholdPercent: Double = 80.0,
        currencyCode: String = "USD"
    ) {
        viewModelScope.launch {
            repository.addBudget(
                categoryName = categoryName,
                limitAmount = limitAmount,
                period = period,
                alertThresholdPercent = alertThresholdPercent,
                currencyCode = currencyCode
            )
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    fun addFinancialGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double = 0.0,
        targetDate: Long? = null,
        colorHex: String = "#10B981",
        icon: String = "Savings"
    ) {
        viewModelScope.launch {
            repository.addFinancialGoal(
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate,
                colorHex = colorHex,
                icon = icon
            )
        }
    }

    fun updateFinancialGoal(goal: FinancialGoalEntity) {
        viewModelScope.launch {
            repository.updateFinancialGoal(goal)
        }
    }

    fun deleteFinancialGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteFinancialGoal(id)
        }
    }

    companion object {
        fun provideFactory(
            repository: TaskRepository,
            preferencesRepository: UserPreferencesRepository,
            alarmScheduler: LunaAlarmScheduler? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(repository, preferencesRepository, alarmScheduler) as T
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Quint<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
private data class UserPrefsBundle(
    val theme: AppThemeMode,
    val sound: Boolean,
    val haptics: Boolean,
    val amoled: Boolean,
    val showTabLabels: Boolean,
    val autoDeleteDaily: Boolean,
    val country: String,
    val timezone: String,
    val userName: String,
    val userAvatarPath: String,
    val focusWorkSeconds: Int,
    val shortBreakSeconds: Int,
    val longBreakSeconds: Int,
    val userCoverPath: String,
    val coverTitle: String,
    val showCoverBannerText: Boolean,
    val userGender: String,
    val isOnboardingCompleted: Boolean
)
private data class DataBundle(
    val tasks: List<TaskWithDetails>,
    val templates: List<TaskTemplateEntity>,
    val projects: List<ProjectEntity>,
    val habits: List<HabitEntity>,
    val goals: List<GoalEntity>,
    val routines: List<RoutineEntity>,
    val transactions: List<TransactionEntity>,
    val accounts: List<AccountEntity>,
    val budgets: List<BudgetEntity>,
    val financialGoals: List<FinancialGoalEntity>
)
private data class FinancialUiControlState(
    val createTxOpen: Boolean,
    val transferOpen: Boolean,
    val createBudgetOpen: Boolean,
    val createFinancialGoalOpen: Boolean,
    val editingTx: TransactionEntity?
)
private data class FilterQueryState(
    val smartFilter: SmartFilter,
    val query: String,
    val viewMode: AppViewMode,
    val sheetOpen: Boolean,
    val templatesOpen: Boolean,
    val editing: TaskWithDetails?,
    val selectedIds: Set<Long>,
    val settingsOpen: Boolean,
    val completingIds: Set<Long>
)
