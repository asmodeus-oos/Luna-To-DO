package com.luna.app.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.components.LunaPriorityBadge
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LunaCalendarView(
    tasks: List<TaskWithDetails>,
    onToggleCompleted: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    var isCalendarExpanded by remember { mutableStateOf(false) }

    // Currently selected date (at start of day)
    var selectedCal by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
    }

    // Currently displayed Month & Year in full custom calendar
    var viewingCal by remember {
        mutableStateOf((selectedCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    val todayCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // Filter tasks for the selected date
    val isSelectedToday = isSameDay(selectedCal, todayCal)

    val dayTasks = tasks.filter { item ->
        val dueDate = item.task.dueDate
        if (dueDate != null) {
            isSameDay(dueDate, selectedCal)
        } else {
            // Flexible task without explicit due date belongs to Today's inbox
            isSelectedToday
        }
    }

    val timedTasks = dayTasks.filter { parseHourFromTime(it.task.dueTime) != null }
    val allDayTasks = dayTasks.filter { parseHourFromTime(it.task.dueTime) == null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Top Toolbar: View Header & Expand/Collapse Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CALENDAR",
                    color = palette.textTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Expand / Collapse Calendar Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.7f))
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                    .clickable { isCalendarExpanded = !isCalendarExpanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isCalendarExpanded) "Week View" else "Full Calendar",
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isCalendarExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isCalendarExpanded) "Collapse Calendar" else "Expand Calendar",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Full Custom Month / Year Calendar
        AnimatedVisibility(
            visible = isCalendarExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FullCustomCalendarGrid(
                viewingCal = viewingCal,
                selectedCal = selectedCal,
                todayCal = todayCal,
                tasks = tasks,
                onChangeMonth = { delta ->
                    viewingCal = (viewingCal.clone() as Calendar).apply {
                        add(Calendar.MONTH, delta)
                    }
                },
                onChangeYear = { delta ->
                    viewingCal = (viewingCal.clone() as Calendar).apply {
                        add(Calendar.YEAR, delta)
                    }
                },
                onSelectMonth = { monthIndex ->
                    viewingCal = (viewingCal.clone() as Calendar).apply {
                        set(Calendar.MONTH, monthIndex)
                    }
                },
                onSelectYear = { year ->
                    viewingCal = (viewingCal.clone() as Calendar).apply {
                        set(Calendar.YEAR, year)
                    }
                },
                onSelectDay = { targetCal ->
                    selectedCal = (targetCal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (targetCal.get(Calendar.MONTH) != viewingCal.get(Calendar.MONTH)) {
                        viewingCal = (targetCal.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                },
                onJumpToday = {
                    val now = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedCal = now
                    viewingCal = (now.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            )
        }

        // Compact Week Date Strip Header (shown when collapsed)
        AnimatedVisibility(
            visible = !isCalendarExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            WeekStripHeader(
                selectedCal = selectedCal,
                todayCal = todayCal,
                tasks = tasks,
                onSelectDate = { cal ->
                    selectedCal = cal
                    viewingCal = (cal.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Date Summary Bar
        val dateHeaderFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateHeaderFormat.format(selectedCal.time),
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${dayTasks.size} tasks • ${dayTasks.sumOf { it.task.estimatedMinutes }}m planned",
                color = palette.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Flexible / All-day Tasks section
            if (allDayTasks.isNotEmpty()) {
                item(key = "all_day_header") {
                    Text(
                        text = "ALL-DAY & FLEXIBLE",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 6.dp)
                    )
                }

                items(allDayTasks.size, key = { "allday_${allDayTasks[it].task.id}" }) { idx ->
                    val item = allDayTasks[idx]
                    CalendarTaskCard(
                        taskWithDetails = item,
                        onToggle = { onToggleCompleted(item) },
                        onClick = { onTaskClick(item) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Time Blocking Timeline (Hours from 07:00 to 22:00)
            item(key = "timeline_header") {
                Text(
                    text = "TIME BLOCKING TIMELINE",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp)
                )
            }

            val minHour = timedTasks.mapNotNull { parseHourFromTime(it.task.dueTime) }.minOrNull()?.coerceAtMost(7) ?: 7
            val maxHour = timedTasks.mapNotNull { parseHourFromTime(it.task.dueTime) }.maxOrNull()?.coerceAtLeast(22) ?: 22

            for (hour in minHour..maxHour) {
                val hourLabel = String.format(Locale.getDefault(), "%02d:00", hour)
                val matchingTasks = timedTasks.filter { item ->
                    parseHourFromTime(item.task.dueTime) == hour
                }

                item(key = "hour_$hour") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = hourLabel,
                            color = palette.textTertiary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(50.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (matchingTasks.isNotEmpty()) Color.Transparent
                                    else palette.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            if (matchingTasks.isEmpty()) {
                                Spacer(modifier = Modifier.height(26.dp))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    matchingTasks.forEach { taskWithDetails ->
                                        CalendarTaskCard(
                                            taskWithDetails = taskWithDetails,
                                            onToggle = { onToggleCompleted(taskWithDetails) },
                                            onClick = { onTaskClick(taskWithDetails) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "calendar_bottom_spacer") {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

/**
 * Full custom-designed month and year calendar grid with smooth navigation across
 * days, months, and years, task scheduled dots, and high-contrast styling.
 */
@Composable
private fun FullCustomCalendarGrid(
    viewingCal: Calendar,
    selectedCal: Calendar,
    todayCal: Calendar,
    tasks: List<TaskWithDetails>,
    onChangeMonth: (Int) -> Unit,
    onChangeYear: (Int) -> Unit,
    onSelectMonth: (Int) -> Unit,
    onSelectYear: (Int) -> Unit,
    onSelectDay: (Calendar) -> Unit,
    onJumpToday: () -> Unit
) {
    val palette = LunaTheme.colors
    val monthTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthsShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthScrollState = rememberScrollState()
    val yearScrollState = rememberScrollState()

    val currentViewingYear = viewingCal.get(Calendar.YEAR)
    val currentViewingMonth = viewingCal.get(Calendar.MONTH)

    val taskDueDates = remember(tasks) {
        tasks.mapNotNull { it.task.dueDate }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            // Month Header & Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onChangeMonth(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = monthTitleFormat.format(viewingCal.time),
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onChangeMonth(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Next Month",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Jump to Today Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.accent.copy(alpha = 0.12f))
                        .clickable(onClick = onJumpToday)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Today",
                        color = palette.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Year Quick Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(yearScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val baseYear = todayCal.get(Calendar.YEAR)
                for (year in (baseYear - 2)..(baseYear + 6)) {
                    val isYearSelected = year == currentViewingYear
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isYearSelected) palette.accent
                                else palette.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { onSelectYear(year) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = year.toString(),
                            color = if (isYearSelected) palette.onAccent else palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isYearSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Months Quick Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(monthScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                monthsShort.forEachIndexed { idx, mLabel ->
                    val isMonthSelected = idx == currentViewingMonth
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMonthSelected) palette.accent.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { onSelectMonth(idx) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mLabel,
                            color = if (isMonthSelected) palette.accent else palette.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = if (isMonthSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Column Names (Mon .. Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayHeaders = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                dayHeaders.forEach { name ->
                    Text(
                        text = name,
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day Cells Computation
            val firstDayCal = (viewingCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
            val leadingBlanks = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
            val daysInMonth = viewingCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val cells = mutableListOf<Calendar>()
            // Preceding month trailing days
            for (i in leadingBlanks downTo 1) {
                val c = (firstDayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -i) }
                cells.add(c)
            }
            // Current month days
            for (day in 1..daysInMonth) {
                val c = (viewingCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                cells.add(c)
            }
            // Trailing month leading days to complete full rows of 7
            val remaining = (7 - (cells.size % 7)) % 7
            for (i in 1..remaining) {
                val c = (viewingCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, daysInMonth)
                    add(Calendar.DAY_OF_MONTH, i)
                }
                cells.add(c)
            }

            // Render 7-column rows
            val rowCount = cells.size / 7
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (rowIdx in 0 until rowCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIdx in 0..6) {
                            val cellCal = cells[rowIdx * 7 + colIdx]
                            val isCurrentMonth = cellCal.get(Calendar.MONTH) == currentViewingMonth
                            val isSelected = isSameDay(cellCal, selectedCal)
                            val isToday = isSameDay(cellCal, todayCal)
                            val hasTasks = countTasksOnDay(cellCal, taskDueDates) > 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> palette.accent
                                            isToday -> palette.accent.copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        if (isToday && !isSelected) {
                                            Modifier.border(1.dp, palette.accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        } else Modifier
                                    )
                                    .clickable { onSelectDay(cellCal) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = cellCal.get(Calendar.DAY_OF_MONTH).toString(),
                                        color = when {
                                            isSelected -> palette.onAccent
                                            isCurrentMonth -> palette.textPrimary
                                            else -> palette.textTertiary.copy(alpha = 0.35f)
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                    )

                                    if (hasTasks) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) palette.onAccent else palette.accent)
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
}

/**
 * Compact horizontal week date strip with high-contrast text and task dots.
 */
@Composable
private fun WeekStripHeader(
    selectedCal: Calendar,
    todayCal: Calendar,
    tasks: List<TaskWithDetails>,
    onSelectDate: (Calendar) -> Unit
) {
    val palette = LunaTheme.colors
    val scrollState = rememberScrollState()

    val taskDueDates = remember(tasks) {
        tasks.mapNotNull { it.task.dueDate }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Render 21 days (-4 days to +16 days relative to selectedCal or today)
        for (offset in -4..16) {
            val cal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time).uppercase()
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toString()
            val isSelected = isSameDay(cal, selectedCal)
            val isToday = isSameDay(cal, todayCal)
            val hasTasks = countTasksOnDay(cal, taskDueDates) > 0

            Box(
                modifier = Modifier
                    .width(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            isSelected -> palette.accent
                            isToday -> palette.accent.copy(alpha = 0.12f)
                            else -> palette.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .then(
                        if (isToday && !isSelected) {
                            Modifier.border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        } else Modifier
                    )
                    .clickable { onSelectDate(cal) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayOfWeek,
                        color = if (isSelected) palette.onAccent else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayOfMonth,
                        color = if (isSelected) palette.onAccent else palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (hasTasks) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) palette.onAccent else palette.accent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarTaskCard(
    taskWithDetails: TaskWithDetails,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val task = taskWithDetails.task

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(LunaShapes.medium)
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle, LunaShapes.medium)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LunaCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                size = 18.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!task.dueTime.isNullOrBlank() || task.estimatedMinutes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!task.dueTime.isNullOrBlank()) {
                            Text(
                                text = "⏰ ${task.dueTime}",
                                color = palette.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (task.estimatedMinutes > 0) {
                            Text(
                                text = "• ${task.estimatedMinutes}m est",
                                color = palette.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (task.priority != Priority.NONE) {
                Spacer(modifier = Modifier.width(6.dp))
                LunaPriorityBadge(priority = task.priority)
            }
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameDay(epochMillis: Long, cal: Calendar): Boolean {
    val taskCal = Calendar.getInstance().apply { timeInMillis = epochMillis }
    return isSameDay(taskCal, cal)
}

private fun parseHourFromTime(timeStr: String?): Int? {
    if (timeStr.isNullOrBlank()) return null
    val clean = timeStr.trim().uppercase()
    val isPm = clean.contains("PM")
    val isAm = clean.contains("AM")
    val digitsOnly = clean.replace("AM", "").replace("PM", "").trim()
    val parts = digitsOnly.split(":")
    if (parts.isNotEmpty()) {
        val h = parts[0].toIntOrNull() ?: return null
        return when {
            isPm && h < 12 -> h + 12
            isAm && h == 12 -> 0
            else -> h
        }
    }
    return null
}

private fun countTasksOnDay(cal: Calendar, dueDates: List<Long>): Int {
    return dueDates.count { isSameDay(it, cal) }
}
