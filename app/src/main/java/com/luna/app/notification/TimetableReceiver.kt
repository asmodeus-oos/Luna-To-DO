package com.luna.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.luna.app.LunaApplication
import com.luna.app.data.local.DentalTimetableSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetableReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_INJECT_TIMETABLE) {
            val app = context.applicationContext as? LunaApplication ?: return
            val overwrite = intent.getBooleanExtra("overwrite", false)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarmScheduler = LunaAlarmScheduler(context)
                    val count = DentalTimetableSeeder.seed(app.taskRepository, alarmScheduler = alarmScheduler, overwrite = overwrite)
                    Log.i("TimetableReceiver", "Successfully injected $count timetable sessions into Luna.")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "Luna 🌙: Injected $count Dentistry timetable sessions!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("TimetableReceiver", "Failed to inject timetable", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_INJECT_TIMETABLE = "com.luna.app.ACTION_INJECT_TIMETABLE"
    }
}
