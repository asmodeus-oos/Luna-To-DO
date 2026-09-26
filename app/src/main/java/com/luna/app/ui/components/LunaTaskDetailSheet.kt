package com.luna.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.luna.app.ui.icons.UntitledIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.EnergyLevel
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.RecurrenceType
import com.luna.app.domain.service.SmartTaskBreakdownService
import com.luna.app.ui.theme.LunaTheme
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LunaTaskDetailSheet(
    taskWithDetails: TaskWithDetails,
    allTasks: List<TaskEntity>,
    projects: List<ProjectEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onDuplicateTask: (Long) -> Unit,
    onSaveAsTemplate: (Long, String) -> Unit,
    onAddSubtask: (taskId: Long, title: String, subtitleSection: String?) -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onDeleteSubtask: (SubtaskEntity) -> Unit,
    onLogMinutes: (Long, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val originalTask = taskWithDetails.task
    var title by remember { mutableStateOf(originalTask.title) }
    var notes by remember { mutableStateOf(originalTask.notes ?: "") }
    var place by remember { mutableStateOf(originalTask.place ?: "") }
    var subtitles by remember { mutableStateOf(originalTask.subtitles) }
    var deadlineMode by remember { mutableStateOf(originalTask.deadlineMode) }
    val initialTotalMins = when (originalTask.durationUnit.uppercase()) {
        "DAYS" -> originalTask.durationValue * 24 * 60
        "HOURS" -> originalTask.durationValue * 60
        else -> if (originalTask.durationValue > 0) originalTask.durationValue else 60
    }
    var durationDays by remember { mutableIntStateOf(initialTotalMins / 1440) }
    var durationHours by remember { mutableIntStateOf((initialTotalMins % 1440) / 60) }
    var durationMinutes by remember { mutableIntStateOf(initialTotalMins % 60) }
    var priority by remember { mutableStateOf(originalTask.priority) }
    var recurrence by remember { mutableStateOf(RecurrenceType.fromString(originalTask.recurrenceRule)) }

    // Fixed Date/Time Options
    val initialTimeInfo = remember(originalTask.dueTime) { parseStoredDueTime(originalTask.dueTime) }
    val hasDateRange = originalTask.startDate != null
    var isDateRangeMode by remember { mutableStateOf(hasDateRange) }
    var singleDateMillis by remember { mutableStateOf(originalTask.dueDate ?: originalTask.startDate ?: System.currentTimeMillis()) }
    var startDateMillis by remember { mutableStateOf(originalTask.startDate ?: System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(originalTask.dueDate ?: (System.currentTimeMillis() + 86400000L)) }

    var isTimePeriodMode by remember { mutableStateOf(initialTimeInfo.isPeriod) }
    var hasExactTime by remember { mutableStateOf(!initialTimeInfo.isPeriod && initialTimeInfo.hasTime) }
    var exactHour by remember { mutableIntStateOf(initialTimeInfo.fromHour) }
    var exactMinute by remember { mutableIntStateOf(initialTimeInfo.fromMinute) }
    var exactIsAm by remember { mutableStateOf(initialTimeInfo.fromIsAm) }

    var hasPeriodTime by remember { mutableStateOf(initialTimeInfo.isPeriod && initialTimeInfo.hasTime) }
    var periodFromHour by remember { mutableIntStateOf(initialTimeInfo.fromHour) }
    var periodFromMinute by remember { mutableIntStateOf(initialTimeInfo.fromMinute) }
    var periodFromIsAm by remember { mutableStateOf(initialTimeInfo.fromIsAm) }
    var periodToHour by remember { mutableIntStateOf(initialTimeInfo.toHour) }
    var periodToMinute by remember { mutableIntStateOf(initialTimeInfo.toMinute) }
    var periodToIsAm by remember { mutableStateOf(initialTimeInfo.toIsAm) }
    var estimatedMinutes by remember { mutableIntStateOf(originalTask.estimatedMinutes) }
    var energyLevel by remember { mutableStateOf(EnergyLevel.fromString(originalTask.energyLevel)) }
    var selectedProjectId by remember { mutableStateOf(originalTask.projectId) }
    var sectionName by remember { mutableStateOf(originalTask.sectionName ?: "") }
    var dependsOnTaskId by remember { mutableStateOf(originalTask.dependsOnTaskId) }
    var tags by remember { mutableStateOf(originalTask.tags) }
    var attachments by remember { mutableStateOf(originalTask.attachments) }

    var newTagInput by remember { mutableStateOf("") }
    var smartBreakdownBanner by remember { mutableStateOf<String?>(null) }

    // Live Task Stopwatch
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            timerSeconds += 1
        }
    }

    val isDark = palette.background.red < 0.5f
    val sheetBackground = if (isDark) Color(0xEE141418) else Color(0xF4F6F6F8)
    val sheetBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.35f else 0.85f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.20f),
            Color.Transparent
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.5.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x40FFFFFF) else Color(0x28000000))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = modifier.border(
            width = 1.dp,
            brush = sheetBorderBrush,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header Bar with Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Task",
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Duplicate Action
                    DetailHeaderIcon(
                        icon = UntitledIcons.Duplicate,
                        contentDescription = "Duplicate",
                        onClick = {
                            onDuplicateTask(originalTask.id)
                            onDismiss()
                        }
                    )

                    // Close Action
                    DetailHeaderIcon(
                        icon = UntitledIcons.Close,
                        contentDescription = "Dismiss",
                        onClick = onDismiss
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Task Title
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Task title",
                            color = palette.textTertiary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes / Markdown Input
            Text(
                text = "NOTES & DESCRIPTION",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = notes,
                onValueChange = { notes = it },
                textStyle = TextStyle(
                    color = palette.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                decorationBox = { innerTextField ->
                    if (notes.isEmpty()) {
                        Text(
                            text = "Add details, checklist (- [ ]), or markdown notes...",
                            color = palette.textTertiary,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Task Place / Location Input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(text = "📍", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = place,
                    onValueChange = { place = it },
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (place.isEmpty()) {
                            Text(
                                text = "Add place / location (e.g. Office, Home, Gym)",
                                color = palette.textTertiary.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Focus Stopwatch & Time Tracking Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = UntitledIcons.Clock,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TIME TRACKING",
                                color = palette.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Live Stopwatch Time
                        val m = timerSeconds / 60
                        val s = timerSeconds % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", m, s),
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Comparison: actualMinutes vs estimatedMinutes
                    val actualMins = originalTask.actualMinutes + (timerSeconds / 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Actual: ${actualMins}m logged",
                            color = palette.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Estimated: ${estimatedMinutes}m",
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (estimatedMinutes > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = (actualMins.toFloat() / estimatedMinutes.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (actualMins > estimatedMinutes) Color(0xFFEF4444) else palette.accent,
                            trackColor = palette.border
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Start/Pause Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTimerRunning) Color(0xFFEF4444) else palette.accent)
                                .clickable { isTimerRunning = !isTimerRunning }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isTimerRunning) UntitledIcons.Pause else UntitledIcons.Play,
                                    contentDescription = null,
                                    tint = palette.chipTextSelected,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTimerRunning) "Pause" else "Start Stopwatch",
                                    color = palette.chipTextSelected,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Save Stopwatch Minutes
                        if (timerSeconds >= 60) {
                            val minsToAdd = timerSeconds / 60
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.accent)
                                    .clickable {
                                        onLogMinutes(originalTask.id, minsToAdd)
                                        timerSeconds = 0
                                        isTimerRunning = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$minsToAdd min",
                                    color = palette.chipTextSelected,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtasks Section Header with AI Generator Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUBTASKS",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                // AI Subtask Generator Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surfaceVariant)
                        .clickable {
                            val breakdown = SmartTaskBreakdownService.generateBreakdown(title)
                            breakdown.suggestedSubtasks.forEach { step ->
                                onAddSubtask(originalTask.id, step, null)
                            }
                            if (estimatedMinutes == 0) {
                                estimatedMinutes = breakdown.suggestedMinutes
                            }
                            energyLevel = breakdown.suggestedEnergy
                            priority = breakdown.suggestedPriority
                            smartBreakdownBanner = breakdown.rationale
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = UntitledIcons.Sparkles,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Breakdown",
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (smartBreakdownBanner != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💡 ${smartBreakdownBanner}",
                    color = palette.textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LunaSubtaskList(
                subtasks = taskWithDetails.subtasks,
                subtitles = subtitles,
                onToggleSubtask = onToggleSubtask,
                onDeleteSubtask = onDeleteSubtask,
                onAddSubtask = { subtaskTitle ->
                    onAddSubtask(originalTask.id, subtaskTitle, null)
                },
                onAddSubtaskToSection = { subtaskTitle, section ->
                    onAddSubtask(originalTask.id, subtaskTitle, section)
                },
                onAddSubtitle = { newSection ->
                    subtitles = (subtitles + newSection).distinct()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Deadline System Mode (choose one mode per task)
            Text(
                text = "DEADLINE SYSTEM",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isFixed = deadlineMode == "FIXED"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFixed) palette.surface else Color.Transparent)
                        .clickable { deadlineMode = "FIXED" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅 Fixed Date/Time",
                        color = if (isFixed) palette.textPrimary else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Medium
                    )
                }

                val isDuration = deadlineMode == "DURATION"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDuration) palette.surface else Color.Transparent)
                        .clickable { deadlineMode = "DURATION" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Duration Countdown",
                        color = if (isDuration) palette.accent else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isDuration) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Fixed Date/Time Options
            if (deadlineMode == "FIXED") {
                Spacer(modifier = Modifier.height(10.dp))
                LunaFixedDateTimePicker(
                    isDateRangeMode = isDateRangeMode,
                    onDateRangeModeChange = { isDateRangeMode = it },
                    singleDateMillis = singleDateMillis,
                    onSingleDateChange = { singleDateMillis = it },
                    startDateMillis = startDateMillis,
                    onStartDateChange = { startDateMillis = it },
                    endDateMillis = endDateMillis,
                    onEndDateChange = { endDateMillis = it },
                    isTimePeriodMode = isTimePeriodMode,
                    onTimePeriodModeChange = { isTimePeriodMode = it },
                    hasExactTime = hasExactTime,
                    onHasExactTimeChange = { hasExactTime = it },
                    exactHour = exactHour,
                    onExactHourChange = { exactHour = it },
                    exactMinute = exactMinute,
                    onExactMinuteChange = { exactMinute = it },
                    exactIsAm = exactIsAm,
                    onExactIsAmChange = { exactIsAm = it },
                    hasPeriodTime = hasPeriodTime,
                    onHasPeriodTimeChange = { hasPeriodTime = it },
                    periodFromHour = periodFromHour,
                    onPeriodFromHourChange = { periodFromHour = it },
                    periodFromMinute = periodFromMinute,
                    onPeriodFromMinuteChange = { periodFromMinute = it },
                    periodFromIsAm = periodFromIsAm,
                    onPeriodFromIsAmChange = { periodFromIsAm = it },
                    periodToHour = periodToHour,
                    onPeriodToHourChange = { periodToHour = it },
                    periodToMinute = periodToMinute,
                    onPeriodToMinuteChange = { periodToMinute = it },
                    periodToIsAm = periodToIsAm,
                    onPeriodToIsAmChange = { periodToIsAm = it }
                )
            }

            if (deadlineMode == "DURATION") {
                Spacer(modifier = Modifier.height(8.dp))
                LunaDurationPicker(
                    days = durationDays,
                    hours = durationHours,
                    minutes = durationMinutes,
                    onDaysChange = { durationDays = it },
                    onHoursChange = { durationHours = it },
                    onMinutesChange = { durationMinutes = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = palette.borderSubtle)
            Spacer(modifier = Modifier.height(16.dp))

            // Energy Level & Context Scheduling
            Text(
                text = "ENERGY LEVEL & EFFORT",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnergyLevel.entries.forEach { level ->
                    val isSelected = level == energyLevel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) palette.accent.copy(alpha = 0.15f)
                                else palette.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.accent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { energyLevel = level }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = level.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = level.label,
                                color = if (isSelected) palette.accent else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Priority Selector
            Text(
                text = "PRIORITY",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Priority.entries.forEach { p ->
                    val isSelected = p == priority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    if (p == Priority.NONE) palette.surfaceVariant else p.color.copy(alpha = 0.15f)
                                } else {
                                    palette.surfaceVariant.copy(alpha = 0.4f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    if (p == Priority.NONE) palette.border else p.color
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { priority = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (p == Priority.NONE) "None" else p.shortLabel,
                            color = if (isSelected) {
                                if (p == Priority.NONE) palette.textPrimary else p.color
                            } else {
                                palette.textSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Project Selector
            if (projects.isNotEmpty()) {
                Text(
                    text = "PROJECT / FOLDER",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isNoneSelected = selectedProjectId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isNoneSelected) palette.accent.copy(alpha = 0.12f) else palette.surfaceVariant)
                            .clickable { selectedProjectId = null }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "No Project",
                            color = if (isNoneSelected) palette.accent else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isNoneSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }

                    projects.forEach { proj ->
                        val isSelected = selectedProjectId == proj.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) palette.accent.copy(alpha = 0.15f) else palette.surfaceVariant)
                                .clickable { selectedProjectId = proj.id }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${proj.icon} ${proj.name}",
                                color = if (isSelected) palette.accent else palette.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Recurrence Selector
            Text(
                text = "REPEAT",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecurrenceType.entries.forEach { rec ->
                    val isSelected = rec == recurrence
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) palette.accent.copy(alpha = 0.12f) else palette.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.accent else palette.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { recurrence = rec }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (rec == RecurrenceType.NONE) "Does Not Repeat" else rec.label,
                            color = if (isSelected) palette.accent else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Task Dependencies: Blocked by another task
            Text(
                text = "DEPENDENCY (BLOCKED BY)",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val otherTasks = allTasks.filter { it.id != originalTask.id && !it.isCompleted }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isNoneSelected = dependsOnTaskId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isNoneSelected) palette.accent.copy(alpha = 0.12f) else palette.surfaceVariant)
                        .clickable { dependsOnTaskId = null }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "None",
                        color = if (isNoneSelected) palette.accent else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isNoneSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }

                otherTasks.take(6).forEach { other ->
                    val isSelected = dependsOnTaskId == other.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) palette.accent else palette.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.accent else palette.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                dependsOnTaskId = if (isSelected) null else other.id
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = other.title,
                            color = if (isSelected) palette.chipTextSelected else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tags & Labels
            Text(
                text = "TAGS",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    LunaTagChip(
                        tag = tag,
                        onRemove = { tags = tags.filter { it != tag } }
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    BasicTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it.replace(" ", "") },
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 12.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTagInput.isNotBlank()) {
                                    tags = (tags + newTagInput.trim().lowercase()).distinct()
                                    newTagInput = ""
                                }
                            }
                        ),
                        decorationBox = { inner ->
                            if (newTagInput.isEmpty()) {
                                Text("add tag", color = palette.textTertiary, fontSize = 12.sp)
                            }
                            inner()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = palette.borderSubtle)
            Spacer(modifier = Modifier.height(20.dp))

            // Save Changes Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(palette.accent)
                    .clickable {
                        val totalMinutes = durationDays * 24 * 60 + durationHours * 60 + durationMinutes
                        val (effDurationValue, effDurationUnit) = when {
                            durationDays > 0 && durationHours == 0 && durationMinutes == 0 -> Pair(durationDays, "DAYS")
                            durationDays == 0 && durationHours > 0 && durationMinutes == 0 -> Pair(durationHours, "HOURS")
                            durationDays == 0 && durationHours == 0 && durationMinutes > 0 -> Pair(durationMinutes, "MINUTES")
                            else -> Pair(totalMinutes.coerceAtLeast(1), "MINUTES")
                        }
                        val effStartDate = if (deadlineMode == "FIXED" && isDateRangeMode) startDateMillis else null
                        val effDueDate = if (deadlineMode == "FIXED") {
                            if (isDateRangeMode) endDateMillis else singleDateMillis
                        } else null
                        val effDueTime = if (deadlineMode == "FIXED") {
                            when {
                                isTimePeriodMode && hasPeriodTime -> "%02d:%02d %s - %02d:%02d %s".format(periodFromHour, periodFromMinute, if (periodFromIsAm) "AM" else "PM", periodToHour, periodToMinute, if (periodToIsAm) "AM" else "PM")
                                !isTimePeriodMode && hasExactTime -> "%02d:%02d %s".format(exactHour, exactMinute, if (exactIsAm) "AM" else "PM")
                                else -> null
                            }
                        } else null

                        val updated = originalTask.copy(
                            title = title.trim().ifEmpty { originalTask.title },
                            notes = notes.trim().ifEmpty { null },
                            subtitles = subtitles,
                            startDate = effStartDate,
                            dueDate = effDueDate,
                            dueTime = effDueTime,
                            deadlineMode = deadlineMode,
                            durationValue = effDurationValue,
                            durationUnit = effDurationUnit,
                            priority = priority,
                            recurrenceRule = recurrence.name,
                            estimatedMinutes = estimatedMinutes,
                            energyLevel = energyLevel.name,
                            projectId = selectedProjectId,
                            sectionName = sectionName.ifBlank { null },
                            dependsOnTaskId = dependsOnTaskId,
                            tags = tags,
                            attachments = attachments,
                            place = place.trim().ifEmpty { null }
                        )
                        onSaveTask(updated)
                        onDismiss()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save Changes",
                    color = palette.onAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions: Save as Template & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.surfaceVariant)
                        .clickable {
                            onSaveAsTemplate(originalTask.id, originalTask.title)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save as Template",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFFEE2E2))
                        .clickable {
                            onDeleteTask(originalTask.id)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Delete Task",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val palette = LunaTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(palette.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

private data class ParsedTimeInfo(
    val isPeriod: Boolean,
    val hasTime: Boolean,
    val fromHour: Int = 9,
    val fromMinute: Int = 0,
    val fromIsAm: Boolean = true,
    val toHour: Int = 5,
    val toMinute: Int = 0,
    val toIsAm: Boolean = false
)

private fun parseStoredDueTime(dueTime: String?): ParsedTimeInfo {
    if (dueTime.isNullOrBlank()) return ParsedTimeInfo(isPeriod = false, hasTime = false)
    val separator = when {
        dueTime.contains("–") -> "–"
        dueTime.contains("-") -> "-"
        else -> null
    }
    if (separator != null) {
        val parts = dueTime.split(separator)
        if (parts.size == 2) {
            val from = parseSingleTimeString(parts[0].trim())
            val to = parseSingleTimeString(parts[1].trim())
            return ParsedTimeInfo(
                isPeriod = true,
                hasTime = true,
                fromHour = from.first,
                fromMinute = from.second,
                fromIsAm = from.third,
                toHour = to.first,
                toMinute = to.second,
                toIsAm = to.third
            )
        }
    }
    val single = parseSingleTimeString(dueTime.trim())
    return ParsedTimeInfo(
        isPeriod = false,
        hasTime = true,
        fromHour = single.first,
        fromMinute = single.second,
        fromIsAm = single.third
    )
}

private fun parseSingleTimeString(timeStr: String): Triple<Int, Int, Boolean> {
    val isPm = timeStr.contains("PM", ignoreCase = true)
    val isAm = !isPm
    val clean = timeStr.replace("AM", "", ignoreCase = true).replace("PM", "", ignoreCase = true).trim()
    val parts = clean.split(":")
    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 9
    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    return Triple(if (h in 1..12) h else 9, if (m in 0..59) m else 0, isAm)
}

