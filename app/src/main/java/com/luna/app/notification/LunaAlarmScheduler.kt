package com.luna.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.luna.app.data.local.entity.TaskEntity

class LunaAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskAlarm(task: TaskEntity) {
        scheduleTaskAlarms(task)
    }

    fun scheduleTaskAlarms(task: TaskEntity) {
        if (task.isCompleted) return

        val startTime = task.startDate ?: task.dueDate ?: return
        val hasStartAlarm = task.alarmOnStart || startTime > System.currentTimeMillis()
        val hasFinishAlarm = task.alarmOnFinish

        val sessionType = if (task.tags.any { it.equals("Clinic", ignoreCase = true) } || task.title.contains("Clinic", ignoreCase = true)) "Clinic" else "Lecture"
        val group = task.tags.firstOrNull { it.matches(Regex("G\\d+")) } ?: "Full cohort"

        // 1. Start Alarm
        if (hasStartAlarm && startTime > System.currentTimeMillis()) {
            scheduleSingleAlarm(
                requestCode = (task.id * 10).toInt(),
                triggerEpoch = startTime,
                taskId = task.id,
                taskTitle = task.title,
                priority = task.priority.name,
                alarmType = TaskAlarmReceiver.TYPE_START,
                sessionType = sessionType,
                place = task.place,
                group = group,
                weeklyDay = task.weeklyDay
            )
        }

        // 2. Finish Alarm
        if (hasFinishAlarm) {
            val durationHours = if (task.durationValue > 0) task.durationValue else (task.estimatedMinutes / 60).coerceAtLeast(1)
            val finishEpoch = startTime + (durationHours * 3600_000L)
            if (finishEpoch > System.currentTimeMillis()) {
                scheduleSingleAlarm(
                    requestCode = (task.id * 10 + 1).toInt(),
                    triggerEpoch = finishEpoch,
                    taskId = task.id,
                    taskTitle = task.title,
                    priority = task.priority.name,
                    alarmType = TaskAlarmReceiver.TYPE_FINISH,
                    sessionType = sessionType,
                    place = task.place,
                    group = group,
                    weeklyDay = task.weeklyDay
                )
            }
        }
    }

    fun scheduleNextWeekAlarm(task: TaskEntity, alarmType: String) {
        val baseTime = (task.startDate ?: task.dueDate) ?: return
        val nextWeekEpoch = baseTime + (7 * 24 * 3600_000L)
        val sessionType = if (task.tags.any { it.equals("Clinic", ignoreCase = true) } || task.title.contains("Clinic", ignoreCase = true)) "Clinic" else "Lecture"
        val group = task.tags.firstOrNull { it.matches(Regex("G\\d+")) } ?: "Full cohort"

        val durationHours = if (task.durationValue > 0) task.durationValue else 1
        val triggerEpoch = if (alarmType == TaskAlarmReceiver.TYPE_START) nextWeekEpoch else nextWeekEpoch + (durationHours * 3600_000L)

        val requestCode = if (alarmType == TaskAlarmReceiver.TYPE_START) (task.id * 10).toInt() else (task.id * 10 + 1).toInt()

        scheduleSingleAlarm(
            requestCode = requestCode,
            triggerEpoch = triggerEpoch,
            taskId = task.id,
            taskTitle = task.title,
            priority = task.priority.name,
            alarmType = alarmType,
            sessionType = sessionType,
            place = task.place,
            group = group,
            weeklyDay = task.weeklyDay
        )
    }

    private fun scheduleSingleAlarm(
        requestCode: Int,
        triggerEpoch: Long,
        taskId: Long,
        taskTitle: String,
        priority: String,
        alarmType: String,
        sessionType: String,
        place: String?,
        group: String?,
        weeklyDay: String?
    ) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_PRIORITY, priority)
            putExtra(TaskAlarmReceiver.EXTRA_ALARM_TYPE, alarmType)
            putExtra(TaskAlarmReceiver.EXTRA_SESSION_TYPE, sessionType)
            putExtra(TaskAlarmReceiver.EXTRA_PLACE, place)
            putExtra(TaskAlarmReceiver.EXTRA_GROUP, group)
            putExtra(TaskAlarmReceiver.EXTRA_WEEKLY_DAY, weeklyDay)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpoch,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpoch,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpoch,
                pendingIntent
            )
        }
    }

    fun scheduleTestAlarm(isStart: Boolean, delaySeconds: Int = 10) {
        val triggerEpoch = System.currentTimeMillis() + (delaySeconds * 1000L)
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, 99999L)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, if (isStart) "Fixed Prosthodontics V (Clinic)" else "Fixed Prosthodontics V (Clinic)")
            putExtra(TaskAlarmReceiver.EXTRA_TASK_PRIORITY, "P1")
            putExtra(TaskAlarmReceiver.EXTRA_ALARM_TYPE, if (isStart) TaskAlarmReceiver.TYPE_START else TaskAlarmReceiver.TYPE_FINISH)
            putExtra(TaskAlarmReceiver.EXTRA_SESSION_TYPE, "Clinic")
            putExtra(TaskAlarmReceiver.EXTRA_PLACE, "M116")
            putExtra(TaskAlarmReceiver.EXTRA_GROUP, "G4")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            if (isStart) 99991 else 99992,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpoch,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpoch,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpoch,
                pendingIntent
            )
        }
    }

    fun cancelTaskAlarm(taskId: Long) {
        // Cancel start alarm
        val startIntent = Intent(context, TaskAlarmReceiver::class.java)
        val startPending = PendingIntent.getBroadcast(
            context,
            (taskId * 10).toInt(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(startPending)

        // Cancel finish alarm
        val finishIntent = Intent(context, TaskAlarmReceiver::class.java)
        val finishPending = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 1).toInt(),
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(finishPending)

        // Legacy request ID
        val legacyPending = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(legacyPending)
    }
}
