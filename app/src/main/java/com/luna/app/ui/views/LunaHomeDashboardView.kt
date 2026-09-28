package com.luna.app.ui.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.components.LunaCoverBannerCard
import com.luna.app.ui.components.LunaTaskItem
import com.luna.app.ui.components.UserAvatarView
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 🏠 Luna Home: The Daily Command Center
 * Answering "What do I need to do right now" at a glance:
 * - Personalized Greeting + Motivational Line + Live Date
 * - Quick Stats & XP Strip (Streak, Completed Count, Level)
 * - Overdue Alert Banner (if tasks need reasons)
 * - Today's Live Countdown Stack (active tasks with countdowns)
 * - Suggested Focus Task ("Do this next" recommendation)
 * - "Start Your Day" Section (unstarted tasks with Start pills)
 * - Fast Quick-Add Task Input Bar
 * - Up Next (Tomorrow's preview)
 */
@Composable
fun LunaHomeDashboardView(
    tasks: List<TaskWithDetails>,
    onToggleTask: (TaskWithDetails) -> Unit,
    onStartTask: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    onTaskLongClick: (TaskWithDetails) -> Unit,
    onQuickAddTask: (String) -> Unit,
    onPromptOverdueReason: (TaskWithDetails) -> Unit,
    onEditTask: (TaskWithDetails) -> Unit = {},
    onDuplicateTask: (TaskWithDetails) -> Unit = {},
    onDeleteTask: (TaskWithDetails) -> Unit = {},
    completingTaskIds: Set<Long> = emptySet(),
    userName: String = "",
    userAvatarPath: String = "",
    onSetUserAvatarPath: (String) -> Unit = {},
    userCoverPath: String = "",
    coverTitle: String = "Mountains",
    showCoverBannerText: Boolean = true,
    onSetUserCoverPath: (String) -> Unit = {},
    onSetCoverTitle: (String) -> Unit = {},
    habits: List<HabitEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val avatarFile = File(context.filesDir, "user_avatar.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(avatarFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onSetUserAvatarPath(avatarFile.absolutePath)
            } catch (_: Exception) {}
        }
    }

    var quickAddText by remember { mutableStateOf("") }

    // Date & Time Greeting
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Good morning, $userName"
        in 12..17 -> "Good afternoon, $userName"
        in 18..22 -> "Good evening, $userName"
        else -> "Night owl focus, $userName"
    }
    val dateString = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    // Filter tasks
    val allActive = tasks.filter { !it.task.isCompleted }
    val completedToday = tasks.count { it.task.isCompleted }
    val totalToday = tasks.size

    val inProgressTasks = allActive.filter { it.task.status == TaskStatus.IN_PROGRESS }
        .sortedBy { it.task.effectiveDeadlineEpoch ?: Long.MAX_VALUE }

    val overdueTasks = allActive.filter {
        it.task.status == TaskStatus.OVERDUE_PENDING_REASON ||
                (it.task.effectiveDeadlineEpoch != null && it.task.effectiveDeadlineEpoch!! < System.currentTimeMillis())
    }

    val unstartedToday = allActive.filter {
        it.task.status == TaskStatus.NOT_STARTED && !overdueTasks.contains(it)
    }

    // Suggested focus task (highest priority among active)
    val suggestedTask = inProgressTasks.firstOrNull()
        ?: unstartedToday.maxByOrNull { it.task.priority.ordinal }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settable Cover Banner Card directly under top bar (styled like /home/vlad/Pictures/c83bfe10adeb5c8dfe6daeb9f7383e1d.jpg)
        item(key = "home_cover_banner") {
            Spacer(modifier = Modifier.height(2.dp))
            LunaCoverBannerCard(
                coverPath = userCoverPath,
                title = coverTitle,
                showTitle = showCoverBannerText,
                onSetCoverPath = onSetUserCoverPath,
                onSetTitle = onSetCoverTitle
            )
        }

        // Daily Greeting Header + User Avatar
        item(key = "home_greeting_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (inProgressTasks.isNotEmpty()) "You have ${inProgressTasks.size} active session(s) ticking." else "What do you want to accomplish right now?",
                        color = palette.textSecondary,
                        fontSize = 13.sp
                    )
                }

                UserAvatarView(
                    avatarPath = userAvatarPath,
                    displayName = userName,
                    size = 46.dp,
                    onClick = { photoPickerLauncher.launch("image/*") }
                )
            }
        }

        // Quick Stats Strip (Streak, Completed, XP Level)
        item(key = "home_quick_stats") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Today Completed
                Column {
                    Text(
                        text = "$completedToday / $totalToday",
                        color = palette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed today",
                        color = palette.textTertiary,
                        fontSize = 11.sp
                    )
                }

                // Current Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = UntitledIcons.Flame,
                        contentDescription = "Streak",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    // Dynamic streak: highest active habit streak, or task completion activity
                    val habitStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
                    val dynamicStreak = if (habitStreak > 0) habitStreak else {
                        val completedDates = tasks.filter {
                            it.task.isCompleted &&
                            it.task.status != TaskStatus.FAILED_LOGGED &&
                            it.task.completedAt != null
                        }.map {
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.task.completedAt!! }
                            "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                        }.toSet()
                        if (completedDates.isNotEmpty()) 1 else 0
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "$dynamicStreak ${if (dynamicStreak == 1) "Day" else "Days"}",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Streak",
                            color = palette.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                // XP Progress (Dynamic 50 XP per completed task, excluding abandoned/failed tasks)
                val completedTaskXp = tasks.filter {
                    it.task.isCompleted &&
                    it.task.status != TaskStatus.FAILED_LOGGED
                }.sumOf { it.task.xpValue.coerceAtLeast(50) }
                val dynamicLevel = (completedTaskXp / 100) + 1
                val dynamicLevelProgress = (completedTaskXp % 100) / 100f
                val levelDescriptor = when {
                    dynamicLevel <= 1 -> "Starter"
                    dynamicLevel <= 3 -> "Flow"
                    dynamicLevel <= 5 -> "Focus"
                    dynamicLevel <= 8 -> "Ninja"
                    else -> "Master"
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Level $dynamicLevel: $levelDescriptor",
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { dynamicLevelProgress.coerceAtLeast(0.06f) },
                        modifier = Modifier
                            .width(80.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = palette.accent,
                        trackColor = palette.surfaceVariant
                    )
                }
            }
        }

        // Overdue Alert Banner (if any)
        if (overdueTasks.isNotEmpty()) {
            item(key = "home_overdue_banner") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = UntitledIcons.AlertCircle,
                            contentDescription = "Overdue",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${overdueTasks.size} task(s) need attention",
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Awaiting honesty check or reason log",
                                color = Color(0xFFB91C1C),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444))
                            .clickable {
                                overdueTasks.firstOrNull()?.let { onPromptOverdueReason(it) }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Log Reason",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Suggested Focus Task ("Do This Next")
        if (suggestedTask != null) {
            item(key = "home_suggested_focus") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUGGESTED FOCUS TASK",
                            color = palette.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "⚡ Recommended",
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { onTaskClick(suggestedTask) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestedTask.task.title,
                                color = palette.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!suggestedTask.task.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = suggestedTask.task.notes!!,
                                    color = palette.textSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        if (suggestedTask.task.status == TaskStatus.NOT_STARTED) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.accent)
                                    .clickable { onStartTask(suggestedTask) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = UntitledIcons.Play,
                                        contentDescription = "Start",
                                        tint = palette.chipTextSelected,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Start Now",
                                        color = palette.chipTextSelected,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "In Progress",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active In-Progress Live Countdown Stack
        if (inProgressTasks.isNotEmpty()) {
            item(key = "home_in_progress_header") {
                Text(
                    text = "ACTIVE COUNTDOWN STACK (${inProgressTasks.size})",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(
                items = inProgressTasks,
                key = { "progress_${it.task.id}" }
            ) { taskWithDetails ->
                LunaTaskItem(
                    taskWithDetails = taskWithDetails,
                    isSelectionMode = false,
                    isSelected = false,
                    blockedByTaskTitle = null,
                    isCompleting = completingTaskIds.contains(taskWithDetails.task.id),
                    onToggleCompleted = { onToggleTask(taskWithDetails) },
                    onSelectToggle = {},
                    onClick = { onTaskClick(taskWithDetails) },
                    onLongClick = { onTaskLongClick(taskWithDetails) },
                    onEdit = { onEditTask(taskWithDetails) },
                    onDuplicate = { onDuplicateTask(taskWithDetails) },
                    onDelete = { onDeleteTask(taskWithDetails) },
                    onStartTask = { onStartTask(taskWithDetails) },
                    onPromptOverdueReason = { onPromptOverdueReason(taskWithDetails) }
                )
            }
        }

        // Fast Quick-Add Task Input Bar
        item(key = "home_quick_add_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = UntitledIcons.Plus,
                    contentDescription = "Add",
                    tint = palette.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = quickAddText,
                    onValueChange = { quickAddText = it },
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (quickAddText.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onQuickAddTask(quickAddText.trim())
                                quickAddText = ""
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (quickAddText.isEmpty()) {
                            Text(
                                text = "Quick add a task...",
                                color = palette.textTertiary,
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                )
                if (quickAddText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.accent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onQuickAddTask(quickAddText.trim())
                                quickAddText = ""
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Add",
                            color = palette.chipTextSelected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // "Start Your Day" Unstarted Tasks
        if (unstartedToday.isNotEmpty()) {
            item(key = "home_start_day_header") {
                Text(
                    text = "START YOUR DAY (${unstartedToday.size})",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(
                items = unstartedToday.take(5),
                key = { it.task.id }
            ) { taskWithDetails ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 200),
                        fadeOutSpec = tween(durationMillis = 150),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    LunaTaskItem(
                        taskWithDetails = taskWithDetails,
                        isSelectionMode = false,
                        isSelected = false,
                        blockedByTaskTitle = null,
                        isCompleting = completingTaskIds.contains(taskWithDetails.task.id),
                        onToggleCompleted = { onToggleTask(taskWithDetails) },
                        onSelectToggle = {},
                        onClick = { onTaskClick(taskWithDetails) },
                        onLongClick = { onTaskLongClick(taskWithDetails) },
                        onEdit = { onEditTask(taskWithDetails) },
                        onDuplicate = { onDuplicateTask(taskWithDetails) },
                        onDelete = { onDeleteTask(taskWithDetails) },
                        onStartTask = { onStartTask(taskWithDetails) },
                        onPromptOverdueReason = { onPromptOverdueReason(taskWithDetails) }
                    )
                }
            }
        }

        // Completed Tasks Section (animates smoothly when tasks are completed)
        val completedTasks = tasks.filter { it.task.isCompleted }
        if (completedTasks.isNotEmpty()) {
            item(key = "home_completed_header") {
                Text(
                    text = "COMPLETED TODAY (${completedTasks.size})",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(
                items = completedTasks,
                key = { it.task.id }
            ) { taskWithDetails ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 200),
                        fadeOutSpec = tween(durationMillis = 150),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    LunaTaskItem(
                        taskWithDetails = taskWithDetails,
                        isSelectionMode = false,
                        isSelected = false,
                        blockedByTaskTitle = null,
                        isCompleting = false,
                        onToggleCompleted = { onToggleTask(taskWithDetails) },
                        onSelectToggle = {},
                        onClick = { onTaskClick(taskWithDetails) },
                        onLongClick = { onTaskLongClick(taskWithDetails) },
                        onEdit = { onEditTask(taskWithDetails) },
                        onDuplicate = { onDuplicateTask(taskWithDetails) },
                        onDelete = { onDeleteTask(taskWithDetails) },
                        onStartTask = { onStartTask(taskWithDetails) },
                        onPromptOverdueReason = { onPromptOverdueReason(taskWithDetails) }
                    )
                }
            }
        }

        // Celebration Recap if tasks were completed
        if (completedToday > 0) {
            item(key = "home_celebration_recap") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.45f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎉", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "You crushed $completedToday task(s) today!",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keep up the momentum. Consistency beats intensity.",
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item(key = "home_bottom_spacing") {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}
