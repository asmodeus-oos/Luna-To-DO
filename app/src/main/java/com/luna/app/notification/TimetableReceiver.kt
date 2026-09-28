package com.luna.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimetableReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_INJECT_TIMETABLE) {
            Log.i("TimetableReceiver", "Timetable injection is deprecated. Timetable is now driven dynamically by user tasks.")
        }
    }

    companion object {
        const val ACTION_INJECT_TIMETABLE = "com.luna.app.ACTION_INJECT_TIMETABLE"
    }
}
