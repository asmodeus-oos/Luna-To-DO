package com.luna.app.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.luna.app.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class FocusTimerService : Service() {

    companion object {
        const val NOTIFICATION_ID = 8881

        const val ACTION_START = "com.luna.app.action.FOCUS_START"
        const val ACTION_PAUSE = "com.luna.app.action.FOCUS_PAUSE"
        const val ACTION_RESUME = "com.luna.app.action.FOCUS_RESUME"
        const val ACTION_RESET = "com.luna.app.action.FOCUS_RESET"
        const val ACTION_STOP = "com.luna.app.action.FOCUS_STOP"

        const val EXTRA_DURATION_SEC = "extra_duration_sec"
        const val EXTRA_REMAINING_SEC = "extra_remaining_sec"
        const val EXTRA_MODE_NAME = "extra_mode_name"
        const val EXTRA_TASK_TITLE = "extra_task_title"

        private val _remainingSecondsFlow = MutableStateFlow(0)
        val remainingSecondsFlow: StateFlow<Int> = _remainingSecondsFlow.asStateFlow()

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

        private val _modeNameFlow = MutableStateFlow("Focus Work")
        val modeNameFlow: StateFlow<String> = _modeNameFlow.asStateFlow()

        private val _taskTitleFlow = MutableStateFlow("Deep Work")
        val taskTitleFlow: StateFlow<String> = _taskTitleFlow.asStateFlow()

        fun startService(
            context: Context,
            remainingSec: Int,
            totalSec: Int,
            modeName: String,
            taskTitle: String,
            isRunning: Boolean
        ) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = if (isRunning) ACTION_START else ACTION_PAUSE
                putExtra(EXTRA_REMAINING_SEC, remainingSec)
                putExtra(EXTRA_DURATION_SEC, totalSec)
                putExtra(EXTRA_MODE_NAME, modeName)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    private var totalDurationSec = 25 * 60
    private var currentRemainingSec = 25 * 60
    private var currentModeName = "Focus Work"
    private var currentTaskTitle = "Focus Session"
    private var isCurrentlyRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.action ?: ACTION_START

            if (it.hasExtra(EXTRA_REMAINING_SEC)) {
                currentRemainingSec = it.getIntExtra(EXTRA_REMAINING_SEC, currentRemainingSec)
                _remainingSecondsFlow.value = currentRemainingSec
            }
            if (it.hasExtra(EXTRA_DURATION_SEC)) {
                totalDurationSec = it.getIntExtra(EXTRA_DURATION_SEC, totalDurationSec)
            }
            if (it.hasExtra(EXTRA_MODE_NAME)) {
                currentModeName = it.getStringExtra(EXTRA_MODE_NAME) ?: currentModeName
                _modeNameFlow.value = currentModeName
            }
            if (it.hasExtra(EXTRA_TASK_TITLE)) {
                currentTaskTitle = it.getStringExtra(EXTRA_TASK_TITLE) ?: currentTaskTitle
                _taskTitleFlow.value = currentTaskTitle
            }

            when (action) {
                ACTION_START, ACTION_RESUME -> {
                    isCurrentlyRunning = true
                    _isRunningFlow.value = true
                    startTimerTicker()
                }
                ACTION_PAUSE -> {
                    isCurrentlyRunning = false
                    _isRunningFlow.value = false
                    stopTimerTicker()
                    updateNotification()
                }
                ACTION_RESET -> {
                    isCurrentlyRunning = false
                    _isRunningFlow.value = false
                    stopTimerTicker()
                    currentRemainingSec = totalDurationSec
                    _remainingSecondsFlow.value = currentRemainingSec
                    updateNotification()
                }
                ACTION_STOP -> {
                    stopTimerTicker()
                    stopForeground(true)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }

        startForegroundNotification()
        return START_STICKY
    }

    private fun startTimerTicker() {
        stopTimerTicker()
        timerJob = serviceScope.launch {
            while (isCurrentlyRunning && currentRemainingSec > 0) {
                updateNotification()
                delay(1000L)
                if (isCurrentlyRunning) {
                    currentRemainingSec -= 1
                    _remainingSecondsFlow.value = currentRemainingSec
                }
            }
            if (currentRemainingSec <= 0) {
                isCurrentlyRunning = false
                _isRunningFlow.value = false
                updateNotification()
            }
        }
    }

    private fun stopTimerTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Actions: Pause/Resume, Reset, Stop
        val toggleActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = if (isCurrentlyRunning) ACTION_PAUSE else ACTION_RESUME
        }
        val pendingToggle = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_RESET
        }
        val pendingReset = PendingIntent.getService(
            this,
            2,
            resetActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = currentRemainingSec / 60
        val seconds = currentRemainingSec % 60
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        val stateLabel = if (isCurrentlyRunning) "Focusing" else "Paused"

        val builder = NotificationCompat.Builder(this, LunaNotificationHelper.CHANNEL_FOCUS_TIMER)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("$currentModeName • $timeString")
            .setContentText("$currentTaskTitle ($stateLabel)")
            .setOngoing(isCurrentlyRunning)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpenApp)
            .setProgress(totalDurationSec.coerceAtLeast(1), (totalDurationSec - currentRemainingSec).coerceAtLeast(0), false)
            .addAction(
                if (isCurrentlyRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isCurrentlyRunning) "Pause" else "Resume",
                pendingToggle
            )
            .addAction(
                android.R.drawable.ic_menu_rotate,
                "Reset",
                pendingReset
            )

        return builder.build()
    }

    override fun onDestroy() {
        stopTimerTicker()
        serviceJob.cancel()
        super.onDestroy()
    }
}
