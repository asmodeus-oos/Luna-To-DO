package com.luna.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.luna.app.LunaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Upcoming Session"
        val priorityLabel = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "Normal"
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE)
        val sessionType = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: if (taskTitle.contains("Clinic", ignoreCase = true)) "Clinic" else "Lecture"
        val place = intent.getStringExtra(EXTRA_PLACE)
        val group = intent.getStringExtra(EXTRA_GROUP)
        val weeklyDay = intent.getStringExtra(EXTRA_WEEKLY_DAY)

        if (taskId != -1L) {
            if (alarmType != null) {
                LunaNotificationHelper.showTimetableSessionNotification(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    alarmType = alarmType,
                    sessionType = sessionType,
                    place = place,
                    group = group
                )
            } else {
                LunaNotificationHelper.showTaskDueNotification(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    priorityLabel = priorityLabel
                )
            }

            // Re-arm weekly recurrence:
            if (weeklyDay != null) {
                val app = context.applicationContext as? LunaApplication
                if (app != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val task = app.taskRepository.getTaskById(taskId)
                        if (task != null && !task.isCompleted) {
                            val scheduler = LunaAlarmScheduler(context)
                            scheduler.scheduleNextWeekAlarm(task, alarmType ?: TYPE_START)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_PRIORITY = "EXTRA_TASK_PRIORITY"
        const val EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE"
        const val EXTRA_SESSION_TYPE = "EXTRA_SESSION_TYPE"
        const val EXTRA_PLACE = "EXTRA_PLACE"
        const val EXTRA_GROUP = "EXTRA_GROUP"
        const val EXTRA_WEEKLY_DAY = "EXTRA_WEEKLY_DAY"

        const val TYPE_START = "START"
        const val TYPE_FINISH = "FINISH"
    }
}
