package com.luna.app.ui.views

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import com.luna.app.domain.model.Priority
import com.luna.app.notification.FocusTimerService
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaColorPalette
import com.luna.app.ui.theme.LunaTheme
import kotlinx.coroutines.delay
import java.util.Locale

enum class PomodoroMode(val label: String) {
    WORK("Focus Work"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

@Composable
fun LunaFocusView(
    tasks: List<TaskWithDetails>,
    onLogTaskMinutes: (Long, Int) -> Unit,
    onCompleteTask: (Long) -> Unit,
    workDurationSeconds: Int = 25 * 60,
    shortBreakDurationSeconds: Int = 5 * 60,
    longBreakDurationSeconds: Int = 15 * 60,
    onUpdateDurations: (workSec: Int, shortSec: Int, longSec: Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activeTasks = tasks.filter { !it.task.isCompleted }

    var selectedTaskIndex by remember { mutableIntStateOf(0) }
    val currentTask = activeTasks.getOrNull(selectedTaskIndex)

    var currentMode by remember { mutableStateOf(PomodoroMode.WORK) }

    fun getModeSeconds(mode: PomodoroMode): Int = when (mode) {
        PomodoroMode.WORK -> workDurationSeconds
        PomodoroMode.SHORT_BREAK -> shortBreakDurationSeconds
        PomodoroMode.LONG_BREAK -> longBreakDurationSeconds
    }

    var remainingSeconds by remember { mutableIntStateOf(getModeSeconds(currentMode)) }
    var isRunning by remember { mutableStateOf(false) }
    var isAlarmActive by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showTaskPickerModal by remember { mutableStateOf(false) }

    fun updateModeDuration(mode: PomodoroMode, newSeconds: Int) {
        val clamped = newSeconds.coerceIn(30, 180 * 60)
        when (mode) {
            PomodoroMode.WORK -> onUpdateDurations(clamped, shortBreakDurationSeconds, longBreakDurationSeconds)
            PomodoroMode.SHORT_BREAK -> onUpdateDurations(workDurationSeconds, clamped, longBreakDurationSeconds)
            PomodoroMode.LONG_BREAK -> onUpdateDurations(workDurationSeconds, shortBreakDurationSeconds, clamped)
        }
        if (!isRunning) {
            remainingSeconds = clamped
        }
    }

    // Synchronize remaining seconds when durations change and timer is not running
    LaunchedEffect(currentMode, workDurationSeconds, shortBreakDurationSeconds, longBreakDurationSeconds) {
        if (!isRunning) {
            remainingSeconds = getModeSeconds(currentMode)
        }
    }

    // Timer Ticker & Foreground Notification Sync
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning) {
            FocusTimerService.startService(
                context = context,
                remainingSec = remainingSeconds,
                totalSec = getModeSeconds(currentMode),
                modeName = currentMode.label,
                taskTitle = currentTask?.task?.title ?: "Focus Session",
                isRunning = true
            )
        }

        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            // Countdown reached 0: trigger alarm, log focus minutes, and prep next cycle
            isRunning = false
            isAlarmActive = true

            FocusTimerService.stopService(context)

            if (currentMode == PomodoroMode.WORK && currentTask != null) {
                val loggedMinutes = (workDurationSeconds / 60).coerceAtLeast(1)
                onLogTaskMinutes(currentTask.task.id, loggedMinutes)
            }

            // Advance mode for subsequent session
            currentMode = if (currentMode == PomodoroMode.WORK) PomodoroMode.SHORT_BREAK else PomodoroMode.WORK
            remainingSeconds = getModeSeconds(currentMode)
        } else if (!isRunning) {
            FocusTimerService.stopService(context)
        }
    }

    // Continuous Alarm: Clean MediaPlayer looping with instant cleanup on Turn Off
    DisposableEffect(isAlarmActive) {
        var mediaPlayer: android.media.MediaPlayer? = null
        var vibrator: Vibrator? = null

        if (isAlarmActive) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                mediaPlayer = android.media.MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {}

            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                val pattern = longArrayOf(0, 750, 350, 750, 350)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (_: Exception) {}
        }

        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
            try {
                vibrator?.cancel()
                vibrator = null
            } catch (_: Exception) {}
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector (Work / Break) - High contrast & Liquid Glass Capsule
        val selectorContainerBg = if (isLight) Color(0xFFDFDFE3) else Color(0xFF1E1E24)
        val selectorContainerBorder = Brush.verticalGradient(
            colors = if (isLight) {
                listOf(Color.White.copy(alpha = 0.80f), Color.White.copy(alpha = 0.20f))
            } else {
                listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.05f))
            }
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(selectorContainerBg)
                .border(1.dp, selectorContainerBorder, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PomodoroMode.entries.forEach { mode ->
                val isSelected = mode == currentMode

                val activeBgBrush = if (isLight) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF242429), Color(0xFF141416))
                    )
                } else {
                    // Crisp solid white chip in Dark Mode for complete legibility
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFEDEDF0))
                    )
                }

                val activeBorderBrush = if (isLight) {
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.40f))
                    )
                }

                val textColor = if (isSelected) {
                    if (isLight) Color.White else Color(0xFF141416)
                } else {
                    palette.textSecondary
                }

                Box(
                    modifier = Modifier
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = Color(0x20000000),
                                        spotColor = Color(0x30000000)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(activeBgBrush)
                                    .border(1.dp, activeBorderBrush, RoundedCornerShape(20.dp))
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Transparent)
                            }
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentMode = mode
                            remainingSeconds = getModeSeconds(mode)
                            isRunning = false
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Redesigned Set Time Duration Controls Card
        val modeTitle = when (currentMode) {
            PomodoroMode.WORK -> "FOCUS WORK DURATION"
            PomodoroMode.SHORT_BREAK -> "SHORT BREAK DURATION"
            PomodoroMode.LONG_BREAK -> "LONG BREAK DURATION"
        }

        val activeDurationSec = getModeSeconds(currentMode)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isLight) Color(0xFFF2F2F7) else Color(0xFF1C1C24))
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modeTitle,
                        color = palette.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDurationDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Customize",
                            tint = palette.textTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CUSTOMIZE",
                            color = palette.textTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Time Stepper Bar (-5m, -1m, Time Display, +1m, +5m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TimeStepperChip(label = "-5m", isLight = isLight, palette = palette) {
                            val newSec = (activeDurationSec - 5 * 60).coerceAtLeast(60)
                            updateModeDuration(currentMode, newSec)
                        }
                        TimeStepperChip(label = "-1m", isLight = isLight, palette = palette) {
                            val newSec = (activeDurationSec - 60).coerceAtLeast(60)
                            updateModeDuration(currentMode, newSec)
                        }
                    }

                    val mins = activeDurationSec / 60
                    val secs = activeDurationSec % 60
                    Text(
                        text = if (secs > 0) "${mins}m ${secs}s" else "${mins}m",
                        color = palette.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TimeStepperChip(label = "+1m", isLight = isLight, palette = palette) {
                            val newSec = (activeDurationSec + 60).coerceAtMost(180 * 60)
                            updateModeDuration(currentMode, newSec)
                        }
                        TimeStepperChip(label = "+5m", isLight = isLight, palette = palette) {
                            val newSec = (activeDurationSec + 5 * 60).coerceAtMost(180 * 60)
                            updateModeDuration(currentMode, newSec)
                        }
                    }
                }

                // Quick Preset Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = if (currentMode == PomodoroMode.WORK) {
                        listOf(15, 25, 45, 60)
                    } else if (currentMode == PomodoroMode.SHORT_BREAK) {
                        listOf(3, 5, 10, 15)
                    } else {
                        listOf(10, 15, 20, 30)
                    }

                    presets.forEach { targetMin ->
                        val targetSec = targetMin * 60
                        val isPresetActive = activeDurationSec == targetSec

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPresetActive) {
                                        palette.accent.copy(alpha = 0.15f)
                                    } else if (isLight) {
                                        Color.White
                                    } else {
                                        Color(0xFF262630)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isPresetActive) palette.accent.copy(alpha = 0.5f) else palette.borderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    updateModeDuration(currentMode, targetSec)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${targetMin}m",
                                color = if (isPresetActive) palette.accent else palette.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Animated Timer Ring & Time display
        val totalModeSec = getModeSeconds(currentMode).coerceAtLeast(1)
        val progress = (totalModeSec - remainingSeconds).toFloat() / totalModeSec.toFloat()
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")

        Box(
            modifier = Modifier.size(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                // Track
                drawCircle(
                    color = if (isLight) Color(0xFFD6D6DB) else Color(0xFF24242A),
                    radius = (size.minDimension - strokeWidth) / 2f,
                    style = Stroke(strokeWidth)
                )
                // Progress
                drawArc(
                    color = if (isLight) Color(0xFF18181B) else Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                Text(
                    text = timeString,
                    color = palette.textPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = if (isRunning) "FOCUSING" else "PAUSED",
                    color = if (isLight) palette.textPrimary else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Active Focus Task Card with Custom Liquid Glass Modal Trigger
        if (activeTasks.isNotEmpty()) {
            val validTaskIndex = selectedTaskIndex.coerceIn(0, activeTasks.size - 1)
            val activeTask = activeTasks.getOrNull(validTaskIndex) ?: activeTasks.first()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                showTaskPickerModal = true
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CURRENT TASK ▾",
                                color = palette.accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${activeTasks.size} available)",
                                color = palette.textTertiary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = activeTask.task.title,
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (activeTask.task.actualMinutes > 0) {
                            Text(
                                text = "${activeTask.task.actualMinutes}m logged",
                                color = palette.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onCompleteTask(activeTask.task.id)
                                selectedTaskIndex = 0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Complete task",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No active tasks remaining. Add a task to start focusing!",
                color = palette.textTertiary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Controls: Reset, Play/Pause, Skip
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                        spotColor = if (isLight) Color(0x20000000) else Color(0x40000000)
                    )
                    .clip(CircleShape)
                    .background(if (isLight) Color(0xFFE2E2E6) else Color(0xFF26262E))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isRunning = false
                        remainingSeconds = getModeSeconds(currentMode)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reset",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Play / Pause Big Primary Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = if (isLight) Color(0x25000000) else Color(0x50000000),
                        spotColor = if (isLight) Color(0x35000000) else Color(0x60000000)
                    )
                    .clip(CircleShape)
                    .background(if (isLight) Color(0xFF18181B) else Color(0xFFF4F4F5))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = !isRunning
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = if (isLight) Color.White else Color(0xFF18181B),
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                        spotColor = if (isLight) Color(0x20000000) else Color(0x40000000)
                    )
                    .clip(CircleShape)
                    .background(if (isLight) Color(0xFFE2E2E6) else Color(0xFF26262E))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isRunning = false
                        currentMode = if (currentMode == PomodoroMode.WORK) PomodoroMode.SHORT_BREAK else PomodoroMode.WORK
                        remainingSeconds = getModeSeconds(currentMode)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Skip",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Overscroll padding so big buttons remain clear of navbar
        Spacer(modifier = Modifier.height(260.dp))
    }

    // Modal: Liquid Glass Task Selector
    if (showTaskPickerModal && activeTasks.isNotEmpty()) {
        LunaTaskPickerModal(
            activeTasks = activeTasks,
            selectedIndex = selectedTaskIndex.coerceIn(0, activeTasks.size - 1),
            onSelectTask = { idx ->
                selectedTaskIndex = idx
            },
            onDismiss = { showTaskPickerModal = false }
        )
    }

    // Modal: Customize Durations Dialog (Work, Short Break, Long Break in min & sec)
    if (showDurationDialog) {
        LunaFocusDurationDialog(
            currentWorkSec = workDurationSeconds,
            currentShortSec = shortBreakDurationSeconds,
            currentLongSec = longBreakDurationSeconds,
            onSave = { workSec, shortSec, longSec ->
                onUpdateDurations(workSec, shortSec, longSec)
                if (!isRunning) {
                    remainingSeconds = when (currentMode) {
                        PomodoroMode.WORK -> workSec
                        PomodoroMode.SHORT_BREAK -> shortSec
                        PomodoroMode.LONG_BREAK -> longSec
                    }
                }
                showDurationDialog = false
            },
            onDismiss = { showDurationDialog = false }
        )
    }

    // Modal: Continuous Alarm Alert (Ringing & Vibrating until user confirms)
    if (isAlarmActive) {
        LunaAlarmAlertDialog(
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isAlarmActive = false
            }
        )
    }
}

/**
 * ⏰ High-Priority Continuous Alarm Modal Dialog
 * Displays with an animated pulsing icon until the user actively taps "Turn Off Alarm".
 */
@Composable
private fun LunaAlarmAlertDialog(
    onConfirm: () -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val cardBg = if (isLight) Color(0xF8FFFFFF).copy(alpha = 0.92f) else Color(0xD91C1C24).copy(alpha = 0.88f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.40f else 0.10f),
            Color.Transparent
        )
    )

    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isLight) Color(0x15000000) else Color(0x40000000),
                    spotColor = if (isLight) Color(0x20000000) else Color(0x50000000)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(cardBg)
                .border(1.5.dp, cardBorder, RoundedCornerShape(26.dp))
                .padding(26.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Pulsing Alarm Icon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(pulseScale.value)
                        .clip(CircleShape)
                        .background(Color(0xFFB91C3C).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UntitledIcons.Clock,
                        contentDescription = "Alarm Ringing",
                        tint = Color(0xFFB91C3C),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Session Complete!",
                        color = palette.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your countdown timer has ended. Take a breather or move to your next session!",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                // Dual-Tone Translucent Turn Off Alarm Button (Solid Ruby Text + Translucent Ruby Background)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFB91C3C).copy(alpha = 0.14f))
                        .clickable { onConfirm() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Turn Off Alarm",
                        color = Color(0xFFB91C3C),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

/**
 * ⚙️ Duration Configuration Dialog
 * Lets user customize minutes and seconds for Work, Short Break, and Long Break.
 */
@Composable
private fun LunaFocusDurationDialog(
    currentWorkSec: Int,
    currentShortSec: Int,
    currentLongSec: Int,
    onSave: (workSec: Int, shortSec: Int, longSec: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val haptic = LocalHapticFeedback.current

    var workMin by remember { mutableIntStateOf(currentWorkSec / 60) }
    var workSec by remember { mutableIntStateOf(currentWorkSec % 60) }

    var shortMin by remember { mutableIntStateOf(currentShortSec / 60) }
    var shortSec by remember { mutableIntStateOf(currentShortSec % 60) }

    var longMin by remember { mutableIntStateOf(currentLongSec / 60) }
    var longSec by remember { mutableIntStateOf(currentLongSec % 60) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isLight) Color(0xFFFFFFFF) else Color(0xFF1E1E24))
                .border(
                    1.dp,
                    if (isLight) Color(0x20000000) else Color(0x25FFFFFF),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize Durations",
                        color = palette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Min & Sec",
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Mode 1: Focus Work
                DurationRowEditor(
                    label = "Focus Work",
                    minutes = workMin,
                    seconds = workSec,
                    onMinutesChange = { workMin = it },
                    onSecondsChange = { workSec = it },
                    palette = palette,
                    isLight = isLight
                )

                // Mode 2: Short Break
                DurationRowEditor(
                    label = "Short Break",
                    minutes = shortMin,
                    seconds = shortSec,
                    onMinutesChange = { shortMin = it },
                    onSecondsChange = { shortSec = it },
                    palette = palette,
                    isLight = isLight
                )

                // Mode 3: Long Break
                DurationRowEditor(
                    label = "Long Break",
                    minutes = longMin,
                    seconds = longSec,
                    onMinutesChange = { longMin = it },
                    onSecondsChange = { longSec = it },
                    palette = palette,
                    isLight = isLight
                )

                // Quick Presets
                Text(
                    text = "QUICK PRESETS",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Triple("25 / 5", 25, 5),
                        Triple("50 / 10", 50, 10),
                        Triple("15 / 3", 15, 3),
                        Triple("90 / 20", 90, 20)
                    )

                    presets.forEach { (label, w, s) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLight) Color(0xFFF0F0F4) else Color(0xFF282830))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    workMin = w; workSec = 0
                                    shortMin = s; shortSec = 0
                                    longMin = s * 3; longSec = 0
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = palette.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color(0xFFE5E5EA) else Color(0xFF2C2C34))
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = palette.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color(0xFF18181B) else Color(0xFFFFFFFF))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val calculatedWork = (workMin * 60 + workSec).coerceAtLeast(5)
                                val calculatedShort = (shortMin * 60 + shortSec).coerceAtLeast(5)
                                val calculatedLong = (longMin * 60 + longSec).coerceAtLeast(5)
                                onSave(calculatedWork, calculatedShort, calculatedLong)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save",
                            color = if (isLight) Color.White else Color(0xFF18181B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper row to edit minutes and seconds with stepper buttons
 */
@Composable
private fun DurationRowEditor(
    label: String,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    palette: LunaColorPalette,
    isLight: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Minutes Stepper
            StepperBox(
                valueText = "${minutes}m",
                onDecrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMinutesChange((minutes - 1).coerceAtLeast(0))
                },
                onIncrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMinutesChange((minutes + 1).coerceAtMost(180))
                },
                modifier = Modifier.weight(1f),
                isLight = isLight,
                palette = palette
            )

            // Seconds Stepper
            StepperBox(
                valueText = "${seconds}s",
                onDecrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSecondsChange((seconds - 5).coerceAtLeast(0))
                },
                onIncrement = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSecondsChange((seconds + 5).coerceAtMost(59))
                },
                modifier = Modifier.weight(1f),
                isLight = isLight,
                palette = palette
            )
        }
    }
}

@Composable
private fun StepperBox(
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    isLight: Boolean,
    palette: LunaColorPalette
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isLight) Color(0xFFF2F2F7) else Color(0xFF26262E))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isLight) Color(0xFFE5E5EA) else Color(0xFF32323A))
                .clickable { onDecrement() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−",
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = valueText,
            color = palette.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (isLight) Color(0xFFE5E5EA) else Color(0xFF32323A))
                .clickable { onIncrement() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RowScope.QuickSectionStepper(
    title: String,
    durationSeconds: Int,
    isSelected: Boolean,
    onAdjust: (Int) -> Unit,
    palette: LunaColorPalette,
    isLight: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val mins = durationSeconds / 60

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) {
                    palette.accent.copy(alpha = 0.12f)
                } else if (isLight) {
                    Color(0xFFF2F2F6)
                } else {
                    Color(0xFF222228)
                }
            )
            .border(
                1.dp,
                if (isSelected) palette.accent.copy(alpha = 0.4f) else palette.borderSubtle,
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isSelected) palette.accent else palette.textTertiary,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x25FFFFFF))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAdjust(durationSeconds - 5 * 60)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "-", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Text(
                text = "${mins}m",
                color = palette.textPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x25FFFFFF))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAdjust(durationSeconds + 5 * 60)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TimeStepperChip(
    label: String,
    isLight: Boolean,
    palette: LunaColorPalette,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLight) Color.White else Color(0xFF2B2B36))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(8.dp))
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = palette.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 🪟 Custom Liquid Glass Task Selector Modal
 * Translucent frosted glass bottom sheet / dialog replacing standard Material3 menus.
 */
@Composable
private fun LunaTaskPickerModal(
    activeTasks: List<TaskWithDetails>,
    selectedIndex: Int,
    onSelectTask: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredTasks = remember(activeTasks, searchQuery) {
        if (searchQuery.isBlank()) {
            activeTasks
        } else {
            activeTasks.filter {
                it.task.title.contains(searchQuery, ignoreCase = true) ||
                it.task.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val cardBg = if (isLight) Color(0xF8FFFFFF).copy(alpha = 0.94f) else Color(0xD91C1C24).copy(alpha = 0.90f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.40f else 0.10f),
            Color.Transparent
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isLight) Color(0x15000000) else Color(0x40000000),
                    spotColor = if (isLight) Color(0x20000000) else Color(0x50000000)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(cardBg)
                .border(1.5.dp, cardBorder, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Active Task",
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "${activeTasks.size} tasks available",
                            color = palette.textTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isLight) Color(0xFFEAEAEE) else Color(0xFF2D2D38))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Search Bar (if 3 or more tasks)
                if (activeTasks.size >= 3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color(0xFFF2F2F7) else Color(0xFF26262E))
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = palette.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Task List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredTasks) { _, tWithDetails ->
                        val originalIndex = activeTasks.indexOf(tWithDetails)
                        val isSelected = originalIndex == selectedIndex

                        val itemBg = if (isSelected) {
                            palette.accent.copy(alpha = 0.14f)
                        } else if (isLight) {
                            Color(0xFFF7F7FA)
                        } else {
                            Color(0xFF22222A)
                        }

                        val itemBorder = if (isSelected) {
                            palette.accent.copy(alpha = 0.5f)
                        } else {
                            palette.borderSubtle
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(itemBg)
                                .border(1.dp, itemBorder, RoundedCornerShape(16.dp))
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    onSelectTask(if (originalIndex >= 0) originalIndex else 0)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (pLabel, pBg, pColor) = when (tWithDetails.task.priority) {
                                        Priority.P1 -> Triple("P1", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
                                        Priority.P2 -> Triple("P2", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
                                        Priority.P3 -> Triple("P3", Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF3B82F6))
                                        Priority.P4 -> Triple("P4", Color(0xFF94A3B8).copy(alpha = 0.15f), Color(0xFF94A3B8))
                                        else -> Triple("P4", Color(0xFF94A3B8).copy(alpha = 0.15f), Color(0xFF94A3B8))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(pBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = pLabel,
                                            color = pColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = tWithDetails.task.title,
                                        color = palette.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }

                                if (tWithDetails.task.actualMinutes > 0 || tWithDetails.task.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (tWithDetails.task.actualMinutes > 0) {
                                            Text(
                                                text = "${tWithDetails.task.actualMinutes}m logged",
                                                color = palette.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (tWithDetails.task.tags.isNotEmpty()) {
                                            if (tWithDetails.task.actualMinutes > 0) {
                                                Text(
                                                    text = " • ",
                                                    color = palette.textTertiary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = tWithDetails.task.tags.first(),
                                                color = palette.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(palette.accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
