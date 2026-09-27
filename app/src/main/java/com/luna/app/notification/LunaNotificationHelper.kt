package com.luna.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.luna.app.MainActivity

object LunaNotificationHelper {
    const val CHANNEL_TASK_ALARMS = "luna_task_alarms"
    const val CHANNEL_FOCUS_TIMER = "luna_focus_timer"
    const val CHANNEL_HABIT_REMINDERS = "luna_habit_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val taskAlarmsChannel = NotificationChannel(
                CHANNEL_TASK_ALARMS,
                "Task Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications for task due dates and exact alarms"
                enableVibration(true)
                enableLights(true)
            }

            val focusTimerChannel = NotificationChannel(
                CHANNEL_FOCUS_TIMER,
                "Focus Session Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground notifications and timer completion alerts for Focus sessions"
                enableVibration(true)
            }

            val habitRemindersChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDERS,
                "Habits & Routines",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit check-ins and routine reminders"
            }

            notificationManager.createNotificationChannels(
                listOf(taskAlarmsChannel, focusTimerChannel, habitRemindersChannel)
            )
        }
    }

    fun showTaskDueNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        priorityLabel: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TASK_ID", taskId)
        }

        // Safe notification ID avoiding collisions with FocusTimerService (8881) or small integer collisions (#7)
        val notificationId = 10000 + (taskId % 1000000L).toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_ALARMS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Due: $taskTitle")
            .setContentText("Priority: $priorityLabel • Tap to open Luna")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun showTimetableSessionNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        alarmType: String,
        sessionType: String = "Lecture",
        place: String? = null,
        group: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TASK_ID", taskId)
            putExtra("EXTRA_NAV_VIEW", "TIMETABLE")
        }

        val isStart = alarmType.equals("START", ignoreCase = true)
        val isClinic = sessionType.contains("Clinic", ignoreCase = true) || taskTitle.contains("Clinic", ignoreCase = true)
        val iconEmoji = if (isClinic) "🦷" else "📚"

        val notificationId = if (isStart) {
            20000 + (taskId % 10000L).toInt()
        } else {
            30000 + (taskId % 10000L).toInt()
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isStart) {
            "$iconEmoji $sessionType Starting Now: $taskTitle"
        } else {
            "🏁 Session Completed: $taskTitle"
        }

        val locationText = place?.let { "📍 Room: $it" } ?: "📍 Campus"
        val groupText = group?.let { " • Group: $it" } ?: ""

        val content = if (isStart) {
            "$locationText$groupText • Class is starting now. Tap to view notes & attendance."
        } else {
            "$locationText • Session finished! Great job. Tap to review tasks in Luna."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_ALARMS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
