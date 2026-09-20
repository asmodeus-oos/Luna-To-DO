package com.luna.app

import android.app.Application
import com.luna.app.data.local.LunaDatabase
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.preferences.UserPreferencesRepository
import com.luna.app.data.repository.TaskRepository
import com.luna.app.data.repository.TaskRepositoryImpl
import com.luna.app.domain.model.Priority
import com.luna.app.notification.LunaNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class LunaApplication : Application() {

    lateinit var database: LunaDatabase
        private set

    lateinit var taskRepository: TaskRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = LunaDatabase.getInstance(this)
        taskRepository = TaskRepositoryImpl(
            taskDao = database.taskDao(),
            projectDao = database.projectDao(),
            habitDao = database.habitDao(),
            goalDao = database.goalDao(),
            routineDao = database.routineDao(),
            taskActivityDao = database.taskActivityDao(),
            missedReasonDao = database.missedReasonDao()
        )
        preferencesRepository = UserPreferencesRepository(this)

        LunaNotificationHelper.createNotificationChannels(this)

        seedWelcomeTasksIfFirstRun()
    }

    private fun seedWelcomeTasksIfFirstRun() {
        val prefs = getSharedPreferences("luna_fast_cache", MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data", false)
        if (hasSeeded) return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val existingTasks = taskRepository.getTasksWithDetailsFlow().first()
            if (existingTasks.isNotEmpty()) {
                prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
                return@launch
            }
            prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
            val todayEpoch = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                }.timeInMillis

                val tomorrowEpoch = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                }.timeInMillis

                // Task 1: Subtasks and checklist showcase
                taskRepository.addTask(
                    title = "Experience Luna's tactile checklist & subtasks",
                    notes = "### Welcome to Luna\nTap to open the task editor. You can check off individual steps or add new items.\n- [x] Native Kotlin + Jetpack Compose\n- [ ] Room SQLite persistence\n- [ ] Procedural audio synthesis",
                    dueDate = todayEpoch,
                    priority = Priority.P1,
                    tags = listOf("welcome", "design"),
                    initialSubtasks = listOf(
                        "Tap on this task to view full details",
                        "Check off a subtask step",
                        "Experience the dual-frequency chime"
                    )
                )

                // Task 2: Task with Dependency
                val parentId = taskRepository.addTask(
                    title = "Quality Audit & Verification",
                    notes = "Complete this task first to automatically unlock the dependent task below!",
                    dueDate = todayEpoch,
                    priority = Priority.P2,
                    tags = listOf("workflow")
                )

                taskRepository.addTask(
                    title = "Release Luna Update to Production",
                    notes = "This task is locked until 'Quality Audit & Verification' is marked complete.",
                    dueDate = tomorrowEpoch,
                    priority = Priority.P1,
                    dependsOnTaskId = parentId,
                    tags = listOf("release")
                )

                // Task 3: Recurring Habit
                taskRepository.addTask(
                    title = "Morning Mindful Planning",
                    notes = "Repeats every morning. When completed, the next day's task is automatically scheduled.",
                    dueDate = todayEpoch,
                    priority = Priority.P3,
                    recurrenceRule = "NONE",
                    tags = listOf("habits", "routine")
                )

                // Task 4: Duration Countdown Task with Subtitles & Subtasks
                taskRepository.addTask(
                    title = "Deep Focus: Complete System Architecture",
                    notes = "### Sprint Plan\nMust be finished within 2 hours starting from when you tap Start! Watch the live countdown badge and progress bar.",
                    subtitles = listOf("Phase 1: Research", "Phase 2: Execution"),
                    deadlineMode = "DURATION",
                    durationValue = 2,
                    durationUnit = "HOURS",
                    priority = Priority.P1,
                    tags = listOf("deep-work", "focus"),
                    initialSubtasks = listOf(
                        "Review current API specifications",
                        "Design state machine & database schema",
                        "Implement reactive Kotlin Flow pipelines",
                        "Verify with unit tests"
                    )
                )

                // Seed Default Templates
                taskRepository.insertTemplate(
                    TaskTemplateEntity(
                        name = "Weekly Review",
                        description = "End-of-week reflection and goal alignment",
                        defaultTitle = "Weekly Reflection & Planning",
                        defaultNotes = "Review past week's accomplishments and set top 3 priorities for next week.",
                        defaultPriority = Priority.P2,
                        defaultTags = listOf("planning", "review"),
                        checklistItems = listOf(
                            "Clear email & message inbox",
                            "Review completed tasks from this week",
                            "Plan top 3 MITs (Most Important Tasks) for next week",
                            "Organize workspace & desktop"
                        ),
                        defaultRecurrence = "WEEKLY"
                    )
                )

                taskRepository.insertTemplate(
                    TaskTemplateEntity(
                        name = "Bug Fix Workflow",
                        description = "Standard engineering checklist for reliable bug fixes",
                        defaultTitle = "Investigate & Resolve Bug",
                        defaultNotes = "Follow the test-driven procedure to isolate and fix.",
                        defaultPriority = Priority.P1,
                        defaultTags = listOf("dev", "bugs"),
                        checklistItems = listOf(
                            "Reproduce issue and document exact steps",
                            "Write automated regression test",
                            "Implement code fix",
                            "Verify on real Android device",
                            "Submit patch & review"
                        )
                    )
                )

                // Seed Default Habits
                taskRepository.addHabit("Drink 2L Water", "💧", "#3B82F6")
                taskRepository.addHabit("Deep Work Session", "🎯", "#8B5CF6")
                taskRepository.addHabit("Daily Reading", "📚", "#10B981")

                // Seed Default Projects
                val personalProjId = taskRepository.addProject("Personal", "#3B82F6", "👤")
                val engineeringProjId = taskRepository.addProject("Engineering", "#F59E0B", "💻")
                taskRepository.addProject("Design System", "#10B981", "🎨", parentId = engineeringProjId)

                // Seed Default Goals
                taskRepository.addGoal(
                    title = "Master Native Android Jetpack Compose",
                    description = "Build tactile, performant mobile apps with 60fps animations",
                    targetValue = 100,
                    unit = "%",
                    colorHex = "#008FFD",
                    icon = "🚀"
                )
                taskRepository.addGoal(
                    title = "Deep Work 40 Hours This Month",
                    description = "Focus blocks for high-leverage cognitive tasks",
                    targetValue = 40,
                    unit = "h",
                    colorHex = "#8B5CF6",
                    icon = "⏱️"
                )

                // Seed Default Routines
                taskRepository.addRoutine(
                    name = "Morning Clarity Flow",
                    icon = "🌅",
                    timeOfDay = "Morning",
                    steps = listOf(
                        "Hydrate with 500ml water",
                        "Review today's top 3 MITs",
                        "5-minute breathing / mindfulness",
                        "Start first 25m focus sprint"
                    ),
                    colorHex = "#F59E0B"
                )
                taskRepository.addRoutine(
                    name = "Evening Wind Down",
                    icon = "🌙",
                    timeOfDay = "Evening",
                    steps = listOf(
                        "Clear workspace & tidy desk",
                        "Review completed tasks in Luna",
                        "Plan tomorrow's priority task",
                        "Disconnect screens & read"
                    ),
                    colorHex = "#6366F1"
                )
        }
    }
}
