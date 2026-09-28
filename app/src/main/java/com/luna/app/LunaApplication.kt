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
            missedReasonDao = database.missedReasonDao(),
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            budgetDao = database.budgetDao(),
            financialGoalDao = database.financialGoalDao()
        )
        preferencesRepository = UserPreferencesRepository(this)

        LunaNotificationHelper.createNotificationChannels(this)

        seedDentalTimetableAndAlarms()
    }

    private fun seedDentalTimetableAndAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarmScheduler = com.luna.app.notification.LunaAlarmScheduler(this@LunaApplication)
                com.luna.app.data.local.DentalTimetableSeeder.seed(
                    repository = taskRepository,
                    alarmScheduler = alarmScheduler,
                    overwrite = false
                )
            } catch (e: Exception) {
                android.util.Log.e("LunaApplication", "Auto-seed timetable failed: ${e.message}")
            }
        }
    }
}
