package com.luna.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Upcoming Task"
        val priorityLabel = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "Normal"

        if (taskId != -1L) {
            LunaNotificationHelper.showTaskDueNotification(
                context = context,
                taskId = taskId,
                taskTitle = taskTitle,
                priorityLabel = priorityLabel
            )
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_PRIORITY = "EXTRA_TASK_PRIORITY"
    }
}
