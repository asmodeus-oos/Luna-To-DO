package com.luna.app.data.backup

import com.luna.app.data.LunaAttribution
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.preferences.AppThemeMode
import com.luna.app.data.preferences.UserPreferencesRepository
import com.luna.app.data.repository.TaskRepository
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import com.luna.app.notification.LunaAlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupResult(
    val success: Boolean,
    val taskCount: Int = 0,
    val habitCount: Int = 0,
    val routineCount: Int = 0,
    val projectCount: Int = 0,
    val goalCount: Int = 0,
    val templateCount: Int = 0,
    val errorMessage: String? = null
) {
    val summary: String
        get() = if (success) {
            "✓ Restored: $taskCount tasks, $habitCount habits, $routineCount routines, $projectCount projects, $goalCount goals, $templateCount templates"
        } else {
            errorMessage ?: "Import failed. Please check the backup file."
        }
}

object LunaBackupManager {

    private const val MAX_BACKUP_SIZE = 25_000_000 // 25 MB

    suspend fun exportBackupJson(
        repository: TaskRepository,
        preferences: UserPreferencesRepository
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // 1. Metadata
        root.put("formatVersion", 2)
        root.put("app", LunaAttribution.APP_NAME)
        root.put("appVersion", LunaAttribution.APP_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        // 2. User Preferences & Profile
        val prefsObj = JSONObject().apply {
            put("userName", preferences.userNameFlow.first())
            put("userGender", preferences.userGenderFlow.first())
            put("themeMode", preferences.themeModeFlow.first().name)
            put("userCountry", preferences.userCountryFlow.first())
            put("userTimezone", preferences.userTimezoneFlow.first())
            put("isSoundEnabled", preferences.soundEnabledFlow.first())
            put("isHapticsEnabled", preferences.hapticsEnabledFlow.first())
            put("isAmoledEnabled", preferences.amoledEnabledFlow.first())
            put("coverTitle", preferences.coverTitleFlow.first())
            put("showCoverBannerText", preferences.showCoverBannerTextFlow.first())
            put("focusWorkSeconds", preferences.focusWorkSecondsFlow.first())
            put("shortBreakSeconds", preferences.shortBreakSecondsFlow.first())
            put("longBreakSeconds", preferences.longBreakSecondsFlow.first())
        }
        root.put("preferences", prefsObj)

        // 3. Tasks (with subtasks & timetable data)
        val tasksArray = JSONArray()
        val allTasks = repository.getAllTasksWithDetailsList()
        for (twd in allTasks) {
            val t = twd.task
            val taskObj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("notes", t.notes ?: JSONObject.NULL)
                put("subtitles", JSONArray(t.subtitles))
                put("startDate", t.startDate ?: JSONObject.NULL)
                put("dueDate", t.dueDate ?: JSONObject.NULL)
                put("dueTime", t.dueTime ?: JSONObject.NULL)
                put("deadlineMode", t.deadlineMode)
                put("durationValue", t.durationValue)
                put("durationUnit", t.durationUnit)
                put("startedAt", t.startedAt ?: JSONObject.NULL)
                put("computedDeadline", t.computedDeadline ?: JSONObject.NULL)
                put("isCompleted", t.isCompleted)
                put("completedAt", t.completedAt ?: JSONObject.NULL)
                put("status", t.status.name)
                put("priority", t.priority.name)
                put("tags", JSONArray(t.tags))
                put("recurrenceRule", t.recurrenceRule)
                put("dependsOnTaskId", t.dependsOnTaskId ?: JSONObject.NULL)
                put("attachments", JSONArray(t.attachments))
                put("projectId", t.projectId ?: JSONObject.NULL)
                put("sectionName", t.sectionName ?: JSONObject.NULL)
                put("isPinned", t.isPinned)
                put("estimatedMinutes", t.estimatedMinutes)
                put("actualMinutes", t.actualMinutes)
                put("energyLevel", t.energyLevel)
                put("assignee", t.assignee ?: JSONObject.NULL)
                put("goalId", t.goalId ?: JSONObject.NULL)
                put("xpValue", t.xpValue)
                put("pauseCount", t.pauseCount)
                put("pausedAt", t.pausedAt ?: JSONObject.NULL)
                put("category", t.category)
                put("createdAt", t.createdAt)
                put("orderIndex", t.orderIndex)
                put("alarmOnStart", t.alarmOnStart)
                put("alarmOnFinish", t.alarmOnFinish)
                put("weeklyDay", t.weeklyDay ?: JSONObject.NULL)
                put("weeklyTime", t.weeklyTime ?: JSONObject.NULL)
                put("place", t.place ?: JSONObject.NULL)

                // Subtasks
                val subArray = JSONArray()
                for (sub in twd.subtasks) {
                    val subObj = JSONObject().apply {
                        put("title", sub.title)
                        put("isCompleted", sub.isCompleted)
                        put("orderIndex", sub.orderIndex)
                        put("subtitleSection", sub.subtitleSection ?: JSONObject.NULL)
                    }
                    subArray.put(subObj)
                }
                put("subtasks", subArray)
            }
            tasksArray.put(taskObj)
        }
        root.put("tasks", tasksArray)

        // 4. Habits
        val habitsArray = JSONArray()
        val habits = repository.getHabitsFlow().first()
        for (h in habits) {
            val habitObj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("icon", h.icon)
                put("colorHex", h.colorHex)
                put("currentStreak", h.currentStreak)
                put("bestStreak", h.bestStreak)
                put("completedDates", JSONArray(h.completedDates))
                put("targetPerDay", h.targetPerDay)
                put("imageUri", h.imageUri ?: JSONObject.NULL)
                put("createdAt", h.createdAt)
            }
            habitsArray.put(habitObj)
        }
        root.put("habits", habitsArray)

        // 5. Routines
        val routinesArray = JSONArray()
        val routines = repository.getRoutinesFlow().first()
        for (r in routines) {
            val routineObj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("icon", r.icon)
                put("timeOfDay", r.timeOfDay)
                put("steps", JSONArray(r.steps))
                put("colorHex", r.colorHex)
                put("lastCompletedDate", r.lastCompletedDate ?: JSONObject.NULL)
                put("createdAt", r.createdAt)
            }
            routinesArray.put(routineObj)
        }
        root.put("routines", routinesArray)

        // 6. Projects
        val projectsArray = JSONArray()
        val projects = repository.getProjectsFlow().first()
        for (p in projects) {
            val projectObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("colorHex", p.colorHex)
                put("icon", p.icon)
                put("sections", JSONArray(p.sections))
                put("isFavorite", p.isFavorite)
                put("createdAt", p.createdAt)
            }
            projectsArray.put(projectObj)
        }
        root.put("projects", projectsArray)

        // 7. Goals
        val goalsArray = JSONArray()
        val goals = repository.getGoalsFlow().first()
        for (g in goals) {
            val goalObj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("description", g.description ?: JSONObject.NULL)
                put("targetValue", g.targetValue)
                put("currentValue", g.currentValue)
                put("unit", g.unit)
                put("colorHex", g.colorHex)
                put("icon", g.icon)
                put("isCompleted", g.isCompleted)
                put("createdAt", g.createdAt)
            }
            goalsArray.put(goalObj)
        }
        root.put("goals", goalsArray)

        // 8. Task Templates
        val templatesArray = JSONArray()
        val templates = repository.getTemplatesFlow().first()
        for (tpl in templates) {
            val tplObj = JSONObject().apply {
                put("id", tpl.id)
                put("name", tpl.name)
                put("description", tpl.description ?: JSONObject.NULL)
                put("defaultTitle", tpl.defaultTitle)
                put("defaultNotes", tpl.defaultNotes ?: JSONObject.NULL)
                put("defaultPriority", tpl.defaultPriority.name)
                put("defaultTags", JSONArray(tpl.defaultTags))
                put("checklistItems", JSONArray(tpl.checklistItems))
                put("defaultRecurrence", tpl.defaultRecurrence)
            }
            templatesArray.put(tplObj)
        }
        root.put("templates", templatesArray)

        root.toString(2)
    }

    suspend fun importBackupJson(
        jsonString: String,
        repository: TaskRepository,
        preferences: UserPreferencesRepository,
        alarmScheduler: LunaAlarmScheduler? = null
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            if (jsonString.length > MAX_BACKUP_SIZE) {
                return@withContext BackupResult(false, errorMessage = "Backup file exceeds maximum allowed size (25MB)")
            }

            val root = JSONObject(jsonString)
            val formatVersion = root.optInt("formatVersion", root.optInt("version", 1))
            if (formatVersion < 1) {
                return@withContext BackupResult(false, errorMessage = "Invalid backup format: Version missing or unsupported")
            }

            var importedTasks = 0
            var importedHabits = 0
            var importedRoutines = 0
            var importedProjects = 0
            var importedGoals = 0
            var importedTemplates = 0

            // 1. Restore Preferences if present
            if (root.has("preferences")) {
                try {
                    val p = root.getJSONObject("preferences")
                    p.optNullableString("userName")?.let { preferences.setUserName(it) }
                    p.optNullableString("userGender")?.let { preferences.setUserGender(it) }
                    p.optNullableString("themeMode")?.let {
                        try { preferences.setThemeMode(AppThemeMode.valueOf(it)) } catch (_: Exception) {}
                    }
                    p.optNullableString("userCountry")?.let { preferences.setUserCountry(it) }
                    p.optNullableString("userTimezone")?.let { preferences.setUserTimezone(it) }
                    if (p.has("isSoundEnabled")) preferences.setSoundEnabled(p.getBoolean("isSoundEnabled"))
                    if (p.has("isHapticsEnabled")) preferences.setHapticsEnabled(p.getBoolean("isHapticsEnabled"))
                    if (p.has("isAmoledEnabled")) preferences.setAmoledEnabled(p.getBoolean("isAmoledEnabled"))
                    p.optNullableString("coverTitle")?.let { preferences.setCoverTitle(it) }
                    if (p.has("showCoverBannerText")) preferences.setShowCoverBannerText(p.getBoolean("showCoverBannerText"))
                } catch (_: Exception) {}
            }

            // 2. Restore Projects
            if (root.has("projects")) {
                val array = root.getJSONArray("projects")
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val name = item.getString("name").trim()
                        if (name.isBlank()) continue
                        val colorHex = item.optString("colorHex", "#008FFD")
                        val icon = item.optString("icon", "📁")
                        val sections = item.optJSONArray("sections")?.toStringList() ?: listOf("To Do", "In Progress", "Done")
                        val isFavorite = item.optBoolean("isFavorite", false)

                        repository.importProject(
                            ProjectEntity(
                                name = name,
                                colorHex = colorHex,
                                icon = icon,
                                sections = sections,
                                isFavorite = isFavorite
                            )
                        )
                        importedProjects++
                    } catch (_: Exception) {}
                }
            }

            // 3. Restore Goals
            if (root.has("goals")) {
                val array = root.getJSONArray("goals")
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val title = item.getString("title").trim()
                        if (title.isBlank()) continue
                        val desc = item.optNullableString("description")
                        val targetVal = item.optInt("targetValue", 100)
                        val curVal = item.optInt("currentValue", 0)
                        val unit = item.optString("unit", "%")
                        val colorHex = item.optString("colorHex", "#008FFD")
                        val icon = item.optString("icon", "🎯")
                        val isCompleted = item.optBoolean("isCompleted", false)

                        repository.importGoal(
                            GoalEntity(
                                title = title,
                                description = desc,
                                targetValue = targetVal,
                                currentValue = curVal,
                                unit = unit,
                                colorHex = colorHex,
                                icon = icon,
                                isCompleted = isCompleted
                            )
                        )
                        importedGoals++
                    } catch (_: Exception) {}
                }
            }

            // 4. Restore Habits
            if (root.has("habits")) {
                val array = root.getJSONArray("habits")
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val name = item.getString("name").trim()
                        if (name.isBlank()) continue
                        val icon = item.optString("icon", "⚡")
                        val colorHex = item.optString("colorHex", "#008FFD")
                        val currentStreak = item.optInt("currentStreak", item.optInt("streakCount", 0))
                        val bestStreak = item.optInt("bestStreak", currentStreak)
                        val completedDates = item.optJSONArray("completedDates")?.toStringList() ?: emptyList()
                        val targetPerDay = item.optInt("targetPerDay", 1)
                        val imageUri = item.optNullableString("imageUri")

                        repository.importHabit(
                            HabitEntity(
                                name = name,
                                icon = icon,
                                colorHex = colorHex,
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                completedDates = completedDates,
                                targetPerDay = targetPerDay,
                                imageUri = imageUri
                            )
                        )
                        importedHabits++
                    } catch (_: Exception) {}
                }
            }

            // 5. Restore Routines
            if (root.has("routines")) {
                val array = root.getJSONArray("routines")
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val name = item.getString("name").trim()
                        if (name.isBlank()) continue
                        val icon = item.optString("icon", "🌅")
                        val timeOfDay = item.optString("timeOfDay", "Morning")
                        val steps = item.optJSONArray("steps")?.toStringList() ?: emptyList()
                        val colorHex = item.optString("colorHex", "#F59E0B")
                        val lastCompletedDate = item.optNullableString("lastCompletedDate")

                        repository.importRoutine(
                            RoutineEntity(
                                name = name,
                                icon = icon,
                                timeOfDay = timeOfDay,
                                steps = steps,
                                colorHex = colorHex,
                                lastCompletedDate = lastCompletedDate
                            )
                        )
                        importedRoutines++
                    } catch (_: Exception) {}
                }
            }

            // 6. Restore Templates
            if (root.has("templates")) {
                val array = root.getJSONArray("templates")
                val existingTemplates = repository.getTemplatesFlow().first()
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val name = item.getString("name").trim()
                        if (name.isBlank()) continue
                        val description = item.optNullableString("description")
                        val defaultTitle = item.optString("defaultTitle", name)
                        val defaultNotes = item.optNullableString("defaultNotes")
                        val priority = try {
                            Priority.valueOf(item.optString("defaultPriority", "NONE"))
                        } catch (_: Exception) { Priority.NONE }
                        val defaultTags = item.optJSONArray("defaultTags")?.toStringList() ?: emptyList()
                        val checklistItems = item.optJSONArray("checklistItems")?.toStringList() ?: emptyList()
                        val defaultRecurrence = item.optString("defaultRecurrence", "NONE")

                        val entity = TaskTemplateEntity(
                            name = name,
                            description = description,
                            defaultTitle = defaultTitle,
                            defaultNotes = defaultNotes,
                            defaultPriority = priority,
                            defaultTags = defaultTags,
                            checklistItems = checklistItems,
                            defaultRecurrence = defaultRecurrence
                        )

                        val existing = existingTemplates.firstOrNull { it.name.equals(name, ignoreCase = true) }
                        if (existing != null) {
                            repository.updateTemplate(entity.copy(id = existing.id))
                        } else {
                            repository.insertTemplate(entity)
                        }
                        importedTemplates++
                    } catch (_: Exception) {}
                }
            }

            // 7. Restore Tasks (with Subtasks & Timetable Attributes)
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    try {
                        val item = array.getJSONObject(i)
                        val title = item.getString("title").trim()
                        if (title.isBlank()) continue

                        val notes = item.optNullableString("notes")
                        val subtitles = item.optJSONArray("subtitles")?.toStringList() ?: emptyList()
                        val startDate = item.optNullableLong("startDate")
                        val dueDate = item.optNullableLong("dueDate")
                        val dueTime = item.optNullableString("dueTime")
                        val deadlineMode = item.optString("deadlineMode", "FIXED")
                        val durationValue = item.optInt("durationValue", 0)
                        val durationUnit = item.optString("durationUnit", "HOURS")
                        val startedAt = item.optNullableLong("startedAt")
                        val computedDeadline = item.optNullableLong("computedDeadline")
                        val isCompleted = item.optBoolean("isCompleted", false)
                        val completedAt = item.optNullableLong("completedAt")
                        val status = if (item.has("status")) {
                            TaskStatus.fromString(item.getString("status"))
                        } else {
                            if (isCompleted) TaskStatus.DONE else TaskStatus.NOT_STARTED
                        }
                        val priority = try {
                            Priority.valueOf(item.optString("priority", "NONE"))
                        } catch (_: Exception) { Priority.NONE }
                        val tags = item.optJSONArray("tags")?.toStringList() ?: emptyList()
                        val recurrenceRule = item.optString("recurrenceRule", "NONE")
                        val attachments = item.optJSONArray("attachments")?.toStringList() ?: emptyList()
                        val sectionName = item.optNullableString("sectionName")
                        val isPinned = item.optBoolean("isPinned", false)
                        val estimatedMinutes = item.optInt("estimatedMinutes", 0)
                        val actualMinutes = item.optInt("actualMinutes", 0)
                        val energyLevel = item.optString("energyLevel", "MEDIUM")
                        val assignee = item.optNullableString("assignee")
                        val xpValue = item.optInt("xpValue", 50)
                        val category = item.optString("category", "General")
                        val createdAt = item.optLong("createdAt", System.currentTimeMillis())
                        val orderIndex = item.optInt("orderIndex", 0)
                        val alarmOnStart = item.optBoolean("alarmOnStart", false)
                        val alarmOnFinish = item.optBoolean("alarmOnFinish", false)
                        val weeklyDay = item.optNullableString("weeklyDay")
                        val weeklyTime = item.optNullableString("weeklyTime")
                        val place = item.optNullableString("place")

                        // Parse subtasks
                        val subtasksList = mutableListOf<SubtaskEntity>()
                        if (item.has("subtasks")) {
                            val subArr = item.getJSONArray("subtasks")
                            for (si in 0 until subArr.length()) {
                                try {
                                    val subItem = subArr.getJSONObject(si)
                                    val subTitle = subItem.getString("title").trim()
                                    if (subTitle.isNotBlank()) {
                                        subtasksList.add(
                                            SubtaskEntity(
                                                taskId = 0L,
                                                title = subTitle,
                                                isCompleted = subItem.optBoolean("isCompleted", false),
                                                orderIndex = subItem.optInt("orderIndex", si),
                                                subtitleSection = subItem.optNullableString("subtitleSection")
                                            )
                                        )
                                    }
                                } catch (_: Exception) {}
                            }
                        }

                        val taskEntity = TaskEntity(
                            title = title,
                            notes = notes,
                            subtitles = subtitles,
                            startDate = startDate,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            deadlineMode = deadlineMode,
                            durationValue = durationValue,
                            durationUnit = durationUnit,
                            startedAt = startedAt,
                            computedDeadline = computedDeadline,
                            isCompleted = isCompleted,
                            completedAt = completedAt,
                            status = status,
                            priority = priority,
                            tags = tags,
                            recurrenceRule = recurrenceRule,
                            attachments = attachments,
                            sectionName = sectionName,
                            isPinned = isPinned,
                            estimatedMinutes = estimatedMinutes,
                            actualMinutes = actualMinutes,
                            energyLevel = energyLevel,
                            assignee = assignee,
                            xpValue = xpValue,
                            category = category,
                            createdAt = createdAt,
                            orderIndex = orderIndex,
                            alarmOnStart = alarmOnStart,
                            alarmOnFinish = alarmOnFinish,
                            weeklyDay = weeklyDay,
                            weeklyTime = weeklyTime,
                            place = place
                        )

                        val insertedTaskId = repository.importTaskWithSubtasks(taskEntity, subtasksList)
                        importedTasks++

                        // Re-arm alarms for timetable sessions or timed tasks
                        if (!isCompleted && (alarmOnStart || alarmOnFinish || (weeklyDay != null && weeklyTime != null))) {
                            alarmScheduler?.scheduleTaskAlarms(taskEntity.copy(id = insertedTaskId))
                        }
                    } catch (_: Exception) {}
                }
            }

            BackupResult(
                success = true,
                taskCount = importedTasks,
                habitCount = importedHabits,
                routineCount = importedRoutines,
                projectCount = importedProjects,
                goalCount = importedGoals,
                templateCount = importedTemplates
            )
        } catch (e: Exception) {
            BackupResult(success = false, errorMessage = "Failed to parse backup JSON: ${e.localizedMessage}")
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            val str = optString(i, "").trim()
            if (str.isNotBlank()) list.add(str)
        }
        return list
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        val s = optString(key, "").trim()
        return if (s.isBlank()) null else s
    }

    private fun JSONObject.optNullableLong(key: String): Long? {
        if (!has(key) || isNull(key)) return null
        val v = optLong(key, -1L)
        return if (v != -1L) v else null
    }
}
