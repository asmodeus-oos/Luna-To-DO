package com.luna.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.luna.app.data.local.LunaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Re-initialize channels and prepare task rescheduling
            LunaNotificationHelper.createNotificationChannels(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = LunaDatabase.getInstance(context)
                    val scheduler = LunaAlarmScheduler(context)
                    val activeTasks = db.taskDao().getAllTasksFlow().first()
                        .filter { !it.isCompleted && it.dueDate != null }
                    
                    for (task in activeTasks) {
                        val dueDate = task.dueDate ?: continue
                        if (dueDate > System.currentTimeMillis()) {
                            scheduler.scheduleTaskAlarm(task)
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
