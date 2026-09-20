package com.luna.app.data.backup

import com.luna.app.data.local.LunaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.flow.first

object LunaBackupManager {

    suspend fun exportBackupJson(database: LunaDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Tasks
        val tasksArray = JSONArray()
        val tasks = database.taskDao().getAllTasksFlow().first()
        for (t in tasks) {
            val taskObj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("notes", t.notes)
                put("isCompleted", t.isCompleted)
                put("priority", t.priority.name)
                put("dueDate", t.dueDate ?: JSONObject.NULL)
                put("createdAt", t.createdAt)
            }
            tasksArray.put(taskObj)
        }
        root.put("tasks", tasksArray)

        // Habits
        val habitsArray = JSONArray()
        val habits = database.habitDao().getAllHabitsFlow().first()
        for (h in habits) {
            val habitObj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("icon", h.icon)
                put("colorHex", h.colorHex)
                put("streakCount", h.currentStreak)
            }
            habitsArray.put(habitObj)
        }
        root.put("habits", habitsArray)

        // Projects
        val projectsArray = JSONArray()
        val projects = database.projectDao().getAllProjectsFlow().first()
        for (p in projects) {
            val projectObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("colorHex", p.colorHex)
                put("icon", p.icon)
            }
            projectsArray.put(projectObj)
        }
        root.put("projects", projectsArray)

        root.toString(2)
    }

    private const val MAX_FIELD_LENGTH = 500

    suspend fun importBackupJson(database: LunaDatabase, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (jsonString.length > 5_000_000) return@withContext false // 5MB max

            val root = JSONObject(jsonString)
            val version = root.optInt("version", 0)
            if (version < 1) return@withContext false

            // Import Tasks if present
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                val existingTasks = database.taskDao().getAllTasksFlow().first()
                for (i in 0 until tasksArray.length()) {
                    try {
                        val item = tasksArray.getJSONObject(i)
                        val title = item.getString("title").take(MAX_FIELD_LENGTH)
                        val notes = item.optString("notes", "").take(MAX_FIELD_LENGTH * 4)
                        val isCompleted = item.optBoolean("isCompleted", false)

                        if (title.isNotBlank() && existingTasks.none { it.title == title }) {
                            val newEntity = com.luna.app.data.local.entity.TaskEntity(
                                title = title,
                                notes = notes.ifBlank { null },
                                isCompleted = isCompleted
                            )
                            database.taskDao().insertTask(newEntity)
                        }
                    } catch (_: Exception) {
                        continue // Skip malformed entries
                    }
                }
            }
            // Import Habits if present
            if (root.has("habits")) {
                val habitsArray = root.getJSONArray("habits")
                val existingHabits = database.habitDao().getAllHabitsFlow().first()
                for (i in 0 until habitsArray.length()) {
                    try {
                        val item = habitsArray.getJSONObject(i)
                        val name = item.optString("name", "").take(MAX_FIELD_LENGTH)
                        val icon = item.optString("icon", "⚡").take(10)
                        val colorHex = item.optString("colorHex", "#008FFD").take(20)
                        val streakCount = item.optInt("streakCount", 0).coerceIn(0, 100_000)

                        if (name.isNotBlank() && existingHabits.none { it.name == name }) {
                            database.habitDao().insertHabit(
                                com.luna.app.data.local.entity.HabitEntity(
                                    name = name,
                                    icon = icon,
                                    colorHex = colorHex,
                                    currentStreak = streakCount
                                )
                            )
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }
            }

            // Import Projects if present
            if (root.has("projects")) {
                val projectsArray = root.getJSONArray("projects")
                val existingProjects = database.projectDao().getAllProjectsFlow().first()
                for (i in 0 until projectsArray.length()) {
                    try {
                        val item = projectsArray.getJSONObject(i)
                        val name = item.optString("name", "").take(MAX_FIELD_LENGTH)
                        val colorHex = item.optString("colorHex", "#008FFD").take(20)
                        val icon = item.optString("icon", "📁").take(10)

                        if (name.isNotBlank() && existingProjects.none { it.name == name }) {
                            database.projectDao().insertProject(
                                com.luna.app.data.local.entity.ProjectEntity(
                                    name = name,
                                    colorHex = colorHex,
                                    icon = icon
                                )
                            )
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
