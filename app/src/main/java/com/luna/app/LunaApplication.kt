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
        prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
    }
}
