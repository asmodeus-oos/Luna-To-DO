package com.luna.app.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.DentalTimetableSeeder
import com.luna.app.data.local.TimetableSession
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.util.Calendar

enum class TimetableDayTab(val code: String, val label: String, val shortName: String) {
    ALL("ALL", "All Days", "All"),
    MON("MON", "Monday", "Mon"),
    TUE("TUE", "Tuesday", "Tue"),
    WED("WED", "Wednesday", "Wed")
}

/**
 * Modifier to draw a crisp dashed border with rounded corners.
 * Used for distinctive Clinic sessions.
 */
fun Modifier.dashedBorder(
    width: Dp = 1.5.dp,
    color: Color,
    cornerRadius: Dp = 16.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
) = this.drawBehind {
    val strokeWidth = width.toPx()
    val halfWidth = strokeWidth / 2f
    val r = cornerRadius.toPx()
    val stroke = Stroke(
        width = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(halfWidth, halfWidth),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(r, r),
        style = stroke
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LunaTimetableScheduleView(
    tasks: List<TaskWithDetails>,
    onTaskClick: (TaskWithDetails) -> Unit,
    onSeedTimetable: () -> Unit,
    onTestAlarm: (isStart: Boolean) -> Unit,
    onToggleAlarms: (TaskEntity, Boolean, Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    val isLight = palette.background.red > 0.5f

    var selectedDayTab by remember { mutableStateOf(TimetableDayTab.ALL) }
    var selectedSessionForModal by remember { mutableStateOf<TimetableSessionUiModel?>(null) }
    var showCoursesLegend by remember { mutableStateOf(false) }

    // Map tasks and fallback to DentalTimetableSeeder static definitions
    val dentalTasks = tasks.filter {
        it.task.weeklyDay != null ||
                it.task.category.equals("Dentistry", ignoreCase = true) ||
                it.task.tags.contains("Dentistry")
    }

    val sessionUiModels = remember(tasks) {
        DentalTimetableSeeder.SESSIONS.map { staticSession ->
            val matchingTask = dentalTasks.firstOrNull {
                it.task.title.equals(staticSession.title, ignoreCase = true) &&
                        it.task.weeklyDay == staticSession.dayOfWeekName
            }
            TimetableSessionUiModel(
                session = staticSession,
                taskWithDetails = matchingTask,
                courseMeta = DentalTimetableSeeder.getCourseMeta(staticSession.courseCode)
            )
        }
    }

    val filteredSessions = remember(selectedDayTab, sessionUiModels) {
        if (selectedDayTab == TimetableDayTab.ALL) {
            sessionUiModels
        } else {
            sessionUiModels.filter { it.session.dayOfWeekName == selectedDayTab.code }
        }
    }

    val totalHours = sessionUiModels.sumOf { it.session.durationHours }
    val lectureCount = sessionUiModels.count { it.session.type == "Lecture" }
    val clinicCount = sessionUiModels.count { it.session.type == "Clinic" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Header Banner & Term Details ---
        item(key = "timetable_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isLight) {
                                listOf(Color(0xFFEDE9FE), Color(0xFFE0E7FF), Color(0xFFFCE7F3))
                            } else {
                                listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF1F2937))
                            }
                        )
                    )
                    .border(
                        1.dp,
                        if (isLight) Color(0x406366F1) else Color(0x40818CF8),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) Color(0xFFDDD6FE) else Color(0x4D6366F1))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FACULTY OF DENTISTRY · LEVEL 4",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color(0xFF4338CA) else Color(0xFFA5B4FC),
                                letterSpacing = 0.5.sp
                            )
                        }

                        val dynamicAcademicTerm = remember {
                            val cal = Calendar.getInstance()
                            val year = cal.get(Calendar.YEAR)
                            val month = cal.get(Calendar.MONTH) + 1
                            val term = when (month) {
                                in 9..12 -> "Fall $year–${year + 1}"
                                1 -> "Fall ${year - 1}–$year"
                                in 2..5 -> "Spring ${year - 1}–$year"
                                else -> "Summer $year"
                            }
                            val week = cal.get(Calendar.WEEK_OF_YEAR)
                            "$term · Week $week"
                        }

                        Text(
                            text = dynamicAcademicTerm,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textSecondary
                        )
                    }

                    Text(
                        text = "Weekly Timetable",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.textPrimary
                    )

                    Text(
                        text = "Synchronized schedule with differentiated Lectures & Clinics, live room assignments, and automatic start/finish alarms.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 4 Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Sessions",
                            value = "${sessionUiModels.size}",
                            icon = "🗓️",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Hours",
                            value = "${totalHours}h",
                            icon = "⏱️",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Lectures",
                            value = "$lectureCount",
                            icon = "📚",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Clinics",
                            value = "$clinicCount",
                            icon = "🦷",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 2. Action Toolbar: Test Alarms & Sync ---
        item(key = "timetable_actions") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Start Alarm
                ActionPill(
                    icon = "🔔",
                    text = "Test Start Alarm (10s)",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTestAlarm(true)
                    }
                )

                // Test End Alarm
                ActionPill(
                    icon = "🏁",
                    text = "Test End Alarm (10s)",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTestAlarm(false)
                    }
                )

                // Sync / Re-seed Timetable
                ActionPill(
                    icon = "🔄",
                    text = "Sync Timetable",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSeedTimetable()
                    }
                )

                // Show Courses Catalog
                ActionPill(
                    icon = if (showCoursesLegend) "✕" else "📖",
                    text = if (showCoursesLegend) "Hide Courses" else "Course Legend",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showCoursesLegend = !showCoursesLegend
                    }
                )
            }
        }

        // --- 2.5 Optional Course Catalog Drawer ---
        if (showCoursesLegend) {
            item(key = "courses_catalog_section") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(0.8.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Course Directory & Departments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )

                        DentalTimetableSeeder.COURSES.forEach { course ->
                            val color = Color(android.graphics.Color.parseColor(course.colorHex))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${course.code} · ${course.name}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "${course.department} • ${course.creditHours}",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Day Tabs Selector (All, Mon, Tue, Wed) ---
        item(key = "day_tabs_selector") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color(0xFFE4E4E8) else Color(0x24FFFFFF))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimetableDayTab.entries.forEach { tab ->
                    val isSelected = tab == selectedDayTab
                    val sessionCount = if (tab == TimetableDayTab.ALL) sessionUiModels.size else sessionUiModels.count { it.session.dayOfWeekName == tab.code }

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) palette.accent else Color.Transparent,
                        animationSpec = tween(150),
                        label = "tabBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) palette.chipTextSelected else palette.textSecondary,
                        animationSpec = tween(150),
                        label = "tabText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDayTab = tab
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tab.shortName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            Text(
                                text = "$sessionCount sess",
                                fontSize = 10.sp,
                                color = if (isSelected) textColor.copy(alpha = 0.85f) else palette.textTertiary
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Timetable Sessions List ---
        // Group by Day
        val daysToDisplay = if (selectedDayTab == TimetableDayTab.ALL) {
            listOf(TimetableDayTab.MON, TimetableDayTab.TUE, TimetableDayTab.WED)
        } else {
            listOf(selectedDayTab)
        }

        daysToDisplay.forEach { dayTab ->
            val daySessions = sessionUiModels.filter { it.session.dayOfWeekName == dayTab.code }
                .sortedBy { it.session.startHour * 60 + it.session.startMinute }
            if (daySessions.isNotEmpty()) {
                item(key = "header_${dayTab.code}") {
                    DaySectionHeader(dayTab = dayTab, sessions = daySessions)
                }

                items(
                    items = daySessions,
                    key = { "${it.session.dayOfWeekName}_${it.session.startHour}_${it.session.courseCode}_${it.session.type}" }
                ) { itemModel ->
                    if (itemModel.session.type == "Clinic") {
                        ClinicSessionCard(
                            model = itemModel,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedSessionForModal = itemModel
                            }
                        )
                    } else {
                        LectureSessionCard(
                            model = itemModel,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedSessionForModal = itemModel
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Modal Bottom Sheet for Session Details ---
    selectedSessionForModal?.let { sessionModel ->
        TimetableSessionDetailSheet(
            model = sessionModel,
            onDismiss = { selectedSessionForModal = null },
            onOpenFullTask = {
                selectedSessionForModal = null
                sessionModel.taskWithDetails?.let { onTaskClick(it) }
            },
            onTestAlarm = { isStart ->
                onTestAlarm(isStart)
            },
            onToggleAlarms = onToggleAlarms
        )
    }
}

// -------------------------------------------------------------
// UI SUBCOMPONENTS
// -------------------------------------------------------------

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isLight) Color(0x66FFFFFF) else Color(0x22FFFFFF))
            .border(0.5.dp, if (isLight) Color(0x80FFFFFF) else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = palette.textSecondary
            )
        }
    }
}

@Composable
private fun ActionPill(
    icon: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isLight) Color(0xFFF1F1F5) else Color(0x26FFFFFF))
            .border(0.8.dp, if (isLight) Color(0xFFD4D4D8) else Color(0x33FFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun DaySectionHeader(
    dayTab: TimetableDayTab,
    sessions: List<TimetableSessionUiModel>
) {
    val palette = LunaTheme.colors
    val hours = sessions.sumOf { it.session.durationHours }
    val clinics = sessions.count { it.session.type == "Clinic" }
    val lectures = sessions.count { it.session.type == "Lecture" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.accent.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = dayTab.label.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.accent,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "• ${sessions.size} Sessions",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary
            )
        }

        Text(
            text = "${lectures}L · ${clinics}C · ${hours}h total",
            fontSize = 11.sp,
            color = palette.textTertiary
        )
    }
}

/**
 * Solid, polished card for Lecture sessions (📚).
 * Solid border, left accent strip, room, full cohort, instructor.
 */
@Composable
private fun LectureSessionCard(
    model: TimetableSessionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val courseColor = Color(android.graphics.Color.parseColor(model.courseMeta.colorHex))

    val endHour = model.session.startHour + model.session.durationHours
    val timeSpanText = String.format("%02d:00 – %02d:00", model.session.startHour, endHour)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isLight) Color(0xFFFFFFFF) else Color(0xFF1E212B))
            .border(1.dp, courseColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Left solid accent stripe
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(5.dp)
                .fillMaxSize()
                .background(courseColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Code, Badge, Room, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Course Code Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(courseColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.session.courseCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                    }

                    // Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33FFFFFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📚 LECTURE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                    }
                }

                // Room Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color(0x26FFFFFF))
                        .border(0.5.dp, palette.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📍 ${model.session.place}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
            }

            // Title
            Text(
                text = model.session.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Instructor & Cohort
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.session.instructor} • ${model.session.group}",
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$timeSpanText (${model.session.durationHours}h)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textSecondary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 2.dp),
                thickness = 0.5.dp,
                color = palette.borderSubtle.copy(alpha = 0.4f)
            )

            // Footer: Alarms & Status
            val matchingTask = model.taskWithDetails?.task
            val startArmed = matchingTask?.alarmOnStart == true
            val finishArmed = matchingTask?.alarmOnFinish == true
            val (alarmIcon, alarmStatusText, alarmColor) = when {
                matchingTask == null -> Triple("⚠️", "Not Synced (Tap Sync)", palette.textTertiary)
                startArmed && finishArmed -> Triple("🔔", "Start (${String.format("%02d:00", model.session.startHour)}) & End (${String.format("%02d:00", endHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                startArmed -> Triple("🔔", "Start (${String.format("%02d:00", model.session.startHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                finishArmed -> Triple("🏁", "End (${String.format("%02d:00", endHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                else -> Triple("🔕", "Alarms Off", palette.textTertiary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = alarmIcon, fontSize = 11.sp)
                    Text(
                        text = alarmStatusText,
                        fontSize = 10.sp,
                        color = alarmColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Weekly Recurrence 🔁",
                    fontSize = 10.sp,
                    color = palette.textTertiary
                )
            }
        }
    }
}

/**
 * Distinct, vibrant card for Clinic sessions (🦷).
 * Features DASHED BORDER, clinical pattern accent, tooth icon, Group badge (G3, G4, G5, G6),
 * and P1 Urgent Clinical Attendance marker.
 */
@Composable
private fun ClinicSessionCard(
    model: TimetableSessionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val courseColor = Color(android.graphics.Color.parseColor(model.courseMeta.colorHex))

    val endHour = model.session.startHour + model.session.durationHours
    val timeSpanText = String.format("%02d:00 – %02d:00", model.session.startHour, endHour)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isLight) {
                    Color(0xFFF8FAFC)
                } else {
                    Color(0xFF1B1E29)
                }
            )
            // DISTINCTIVE DASHED BORDER MATCHING USER HTML REQUIREMENT
            .dashedBorder(
                width = 1.8.dp,
                color = courseColor,
                cornerRadius = 16.dp,
                dashLength = 9.dp,
                gapLength = 6.dp
            )
            .clickable(onClick = onClick)
    ) {
        // Left clinical striped accent bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(6.dp)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(courseColor, courseColor.copy(alpha = 0.4f), courseColor)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Code, Clinic Badge with Group, Room
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Course Code Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(courseColor.copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.session.courseCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                    }

                    // CLINIC · GROUP Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(courseColor.copy(alpha = 0.15f))
                            .border(0.8.dp, courseColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🦷 CLINIC · ${model.session.group}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = courseColor
                        )
                    }
                }

                // Room Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color(0x26FFFFFF))
                        .border(0.5.dp, palette.borderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📍 ${model.session.place}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
            }

            // Title
            Text(
                text = model.session.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Instructor & Timing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.session.instructor} • Group ${model.session.group}",
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$timeSpanText (${model.session.durationHours}h)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 2.dp),
                thickness = 0.5.dp,
                color = palette.borderSubtle.copy(alpha = 0.4f)
            )

            // Footer: P1 Priority Badge & Dual Alarms Active
            val matchingTask = model.taskWithDetails?.task
            val startArmed = matchingTask?.alarmOnStart == true
            val finishArmed = matchingTask?.alarmOnFinish == true
            val (clinicAlarmIcon, clinicAlarmStatusText, clinicAlarmColor) = when {
                matchingTask == null -> Triple("⚠️", "Not Synced", palette.textTertiary)
                startArmed && finishArmed -> Triple("🔔", "Start (${String.format("%02d:00", model.session.startHour)}) & End (${String.format("%02d:00", endHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                startArmed -> Triple("🔔", "Start (${String.format("%02d:00", model.session.startHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                finishArmed -> Triple("🏁", "End (${String.format("%02d:00", endHour)}) Armed", if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80))
                else -> Triple("🔕", "Alarms Off", palette.textTertiary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = clinicAlarmIcon, fontSize = 11.sp)
                    Text(
                        text = clinicAlarmStatusText,
                        fontSize = 10.sp,
                        color = clinicAlarmColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Urgent Clinic Attendance Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(palette.danger.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚠️ P1 CLINIC",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.danger
                    )
                }
            }
        }
    }
}

/**
 * Interactive BottomSheet showing complete session details, alarms, and quick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableSessionDetailSheet(
    model: TimetableSessionUiModel,
    onDismiss: () -> Unit,
    onOpenFullTask: () -> Unit,
    onTestAlarm: (isStart: Boolean) -> Unit,
    onToggleAlarms: (TaskEntity, Boolean, Boolean) -> Unit = { _, _, _ -> }
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val courseColor = Color(android.graphics.Color.parseColor(model.courseMeta.colorHex))
    val endHour = model.session.startHour + model.session.durationHours
    val matchingTask = model.taskWithDetails?.task
    val startAlarmArmed = matchingTask?.alarmOnStart == true
    val finishAlarmArmed = matchingTask?.alarmOnFinish == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag Handle Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.borderSubtle)
            )

            // Header Row: Code & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(courseColor.copy(alpha = 0.2f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = model.session.courseCode,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (model.session.type == "Clinic") courseColor.copy(alpha = 0.15f) else palette.borderSubtle)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (model.session.type == "Clinic") "🦷 CLINIC" else "📚 LECTURE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (model.session.type == "Clinic") courseColor else palette.textPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "WEEKLY REPEATING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                }
            }

            // Course Name
            Text(
                text = model.session.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary
            )

            // Interactive Alarm Controls (Live switches for Start and Finish Alarms)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color(0xFFF1F5F9) else Color(0x22FFFFFF))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LIVE ALARM CONTROLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 Start Alarm (${String.format("%02d:00", model.session.startHour)})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (startAlarmArmed) "Alarm armed for session start" else "Alarm disabled",
                            fontSize = 11.sp,
                            color = if (startAlarmArmed) (if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80)) else palette.textTertiary
                        )
                    }
                    Switch(
                        checked = startAlarmArmed,
                        onCheckedChange = { checked ->
                            matchingTask?.let { task ->
                                onToggleAlarms(task, checked, task.alarmOnFinish)
                            }
                        },
                        enabled = matchingTask != null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.accent,
                            checkedTrackColor = palette.accent.copy(alpha = 0.5f)
                        )
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = palette.borderSubtle.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🏁 Finish Alarm (${String.format("%02d:00", endHour)})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (finishAlarmArmed) "Alarm armed for session conclusion" else "Alarm disabled",
                            fontSize = 11.sp,
                            color = if (finishAlarmArmed) (if (isLight) Color(0xFF16A34A) else Color(0xFF4ADE80)) else palette.textTertiary
                        )
                    }
                    Switch(
                        checked = finishAlarmArmed,
                        onCheckedChange = { checked ->
                            matchingTask?.let { task ->
                                onToggleAlarms(task, task.alarmOnStart, checked)
                            }
                        },
                        enabled = matchingTask != null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.accent,
                            checkedTrackColor = palette.accent.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Detailed Specifications Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color(0xFFF1F5F9) else Color(0x22FFFFFF))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "Day of Week", value = "${model.session.dayOfWeekName} (${when (model.session.dayOfWeekName) { "MON" -> "Monday"; "TUE" -> "Tuesday"; else -> "Wednesday" }})")
                DetailRow(label = "Time Slot", value = "${String.format("%02d:00", model.session.startHour)} – ${String.format("%02d:00", endHour)} (${model.session.durationHours} hr${if (model.session.durationHours > 1) "s" else ""})")
                DetailRow(label = "Room / Hall", value = model.session.place)
                DetailRow(label = "Cohort / Group", value = model.session.group)
                DetailRow(label = "Instructor", value = model.session.instructor)
                DetailRow(label = "Department", value = model.courseMeta.department)
                DetailRow(label = "Priority Level", value = if (model.session.priority == Priority.P1) "P1 (Urgent Clinical Attendance)" else "P2 (Standard Academic Session)")
                DetailRow(label = "Start Alarm", value = if (startAlarmArmed) "🔔 Armed for ${String.format("%02d:00", model.session.startHour)}" else "🔕 Disabled")
                DetailRow(label = "Finish Alarm", value = if (finishAlarmArmed) "🏁 Armed for ${String.format("%02d:00", endHour)}" else "🔕 Disabled")
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0xFFE2E8F0) else Color(0x33FFFFFF))
                        .clickable { onTestAlarm(true) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔔 Test Start Alarm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.accent)
                        .clickable { onOpenFullTask() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✏️ Open in Tasks",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.chipTextSelected
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val palette = LunaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = palette.textSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary
        )
    }
}

data class TimetableSessionUiModel(
    val session: TimetableSession,
    val taskWithDetails: TaskWithDetails?,
    val courseMeta: DentalTimetableSeeder.DentalCourseMeta
)
