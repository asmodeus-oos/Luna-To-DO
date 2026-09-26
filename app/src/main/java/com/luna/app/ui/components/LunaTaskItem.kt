package com.luna.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.RecurrenceType
import com.luna.app.domain.model.TaskStatus
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun LunaTaskItem(
    taskWithDetails: TaskWithDetails,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    blockedByTaskTitle: String?,
    onToggleCompleted: (Boolean) -> Unit,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onStartTask: (() -> Unit)? = null,
    onPromptOverdueReason: (() -> Unit)? = null,
    isCompleting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val task = taskWithDetails.task
    val palette = LunaTheme.colors
    val effectiveCompleted = task.isCompleted || isCompleting
    val isBlocked = blockedByTaskTitle != null && !effectiveCompleted
    var showMenu by remember { mutableStateOf(false) }

    val itemAlpha by animateFloatAsState(
        targetValue = if (effectiveCompleted) 0.50f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "itemAlpha"
    )

    val textColor by animateColorAsState(
        targetValue = if (effectiveCompleted) palette.textSecondary else palette.textPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    val borderStrokeColor = if (isSelected) {
        palette.accent
    } else {
        palette.borderSubtle
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) palette.accent.copy(alpha = 0.08f) else palette.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderStrokeColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onSelectToggle() else onClick()
                },
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (task.notes.isNullOrBlank() && !taskWithDetails.hasSubtasks && task.dueDate == null && task.deadlineMode != "DURATION") Alignment.CenterVertically else Alignment.Top
        ) {
            // Selection Checkbox or Task Checkbox
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) palette.accent else palette.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = UntitledIcons.Check,
                            contentDescription = "Selected",
                            tint = palette.chipTextSelected,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.padding(top = 1.dp)) {
                    LunaCheckbox(
                        checked = effectiveCompleted,
                        onCheckedChange = { checked ->
                            if (!isBlocked) {
                                onToggleCompleted(checked)
                            }
                        },
                        size = 22.dp,
                        enabled = !isBlocked
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content Body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Dependency warning if blocked
                if (isBlocked) {
                    LunaDependencyBadge(
                        parentTaskTitle = blockedByTaskTitle,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Title row with optional Start pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = if (effectiveCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (effectiveCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // If Duration Mode & Not Started: show Play/Start Pill
                    if (!effectiveCompleted && task.deadlineMode == "DURATION" && task.status == TaskStatus.NOT_STARTED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        DurationStartPill(
                            durationValue = task.durationValue,
                            durationUnit = task.durationUnit,
                            onStart = { onStartTask?.invoke() }
                        )
                    }
                }

                // In-Progress Live Countdown Progress Bar
                if (!task.isCompleted && task.status == TaskStatus.IN_PROGRESS && task.effectiveDeadlineEpoch != null) {
                    val remaining = task.remainingMillis() ?: 0L
                    val total = task.totalDurationMillis.coerceAtLeast(1L)
                    val ratio = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val barColor = when {
                        remaining <= 0 -> Color(0xFFEF4444)
                        ratio > 0.25f -> Color(0xFF10B981)
                        ratio > 0.10f -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                }

                // Notes preview
                if (!task.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.notes.lines().firstOrNull { it.isNotBlank() } ?: "",
                        color = palette.textTertiary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Subtask Progress Bar
                if (taskWithDetails.hasSubtasks && !task.isCompleted && task.status != TaskStatus.IN_PROGRESS) {
                    val subtaskRatio = (taskWithDetails.completedSubtasks.toFloat() / taskWithDetails.totalSubtasks.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(subtaskRatio)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(palette.accent)
                        )
                    }
                }

                // Metadata Chips row (Countdown, Due Date, Priority, Subtask progress, Recurrence, Attachments, Tags)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Live Countdown or Overdue Badge
                    if (!task.isCompleted) {
                        val isOverdueStatus = task.status == TaskStatus.OVERDUE_PENDING_REASON ||
                                (task.effectiveDeadlineEpoch != null && task.effectiveDeadlineEpoch!! <= System.currentTimeMillis())

                        if (isOverdueStatus) {
                            OverdueWarningBadge(
                                onLogReason = { onPromptOverdueReason?.invoke() }
                            )
                        } else if (task.status == TaskStatus.IN_PROGRESS && task.effectiveDeadlineEpoch != null) {
                            val rem = task.remainingMillis() ?: 0L
                            val total = task.totalDurationMillis.coerceAtLeast(1L)
                            val ratio = (rem.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            CountdownTimerBadge(
                                remainingMillis = rem,
                                ratio = ratio
                            )
                        } else if (task.status == TaskStatus.FAILED_LOGGED) {
                            MissedStatusBadge()
                        }
                    }

                    // 2. Fixed Due Date (when not in duration mode or if date set)
                    if (task.dueDate != null && (task.deadlineMode != "DURATION" || task.isCompleted)) {
                        DateBadge(
                            dueDate = task.dueDate,
                            dueTime = task.dueTime,
                            isCompleted = task.isCompleted
                        )
                    }

                    // Priority
                    if (task.priority != Priority.NONE) {
                        LunaPriorityBadge(priority = task.priority)
                    }

                    // Subtasks progress
                    if (taskWithDetails.hasSubtasks) {
                        SubtaskCountBadge(
                            completed = taskWithDetails.completedSubtasks,
                            total = taskWithDetails.totalSubtasks
                        )
                    }

                    // Recurring Indicator
                    if (task.recurrenceRule != "NONE") {
                        RecurrenceBadge(recurrenceRule = task.recurrenceRule)
                    }

                    // Place / Location Badge
                    if (!task.place.isNullOrBlank()) {
                        PlaceBadge(place = task.place)
                    }

                    // Attachment indicator
                    if (task.attachments.isNotEmpty()) {
                        AttachmentBadge(count = task.attachments.size)
                    }

                    // Tags
                    task.tags.take(3).forEach { tag ->
                        LunaTagChip(tag = tag)
                    }
                    if (task.tags.size > 3) {
                        Text(
                            text = "+${task.tags.size - 3}",
                            color = palette.textTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Dona.ai 3-dots more action button with DropdownMenu
            if (!isSelectionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                val isLight = palette.background.red > 0.5f
                val menuGlassFill = if (isLight) Color(0xF8FFFFFF).copy(alpha = 0.94f) else Color(0xF018181C).copy(alpha = 0.94f)
                val menuBorderBrush = Brush.verticalGradient(
                    colors = if (isLight) {
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLight) Color(0x14000000) else Color(0x22FFFFFF))
                        .border(
                            width = 0.8.dp,
                            brush = menuBorderBrush,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UntitledIcons.GridDots4,
                        contentDescription = "Task options",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .width(160.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = if (isLight) Color(0x20000000) else Color(0x60000000),
                                spotColor = if (isLight) Color(0x30000000) else Color(0x70000000)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(menuGlassFill)
                            .border(
                                width = 1.2.dp,
                                brush = menuBorderBrush,
                                shape = RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        containerColor = menuGlassFill,
                        shadowElevation = 0.dp,
                        border = null
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textPrimary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = UntitledIcons.Edit,
                                    contentDescription = "Edit",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit?.invoke()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            thickness = 0.5.dp,
                            color = if (isLight) Color(0x15000000) else Color(0x20FFFFFF)
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Duplicate",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textPrimary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = UntitledIcons.Duplicate,
                                    contentDescription = "Duplicate",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDuplicate?.invoke()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            thickness = 0.5.dp,
                            color = if (isLight) Color(0x15000000) else Color(0x20FFFFFF)
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textPrimary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = UntitledIcons.Trash,
                                    contentDescription = "Delete",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete?.invoke()
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownTimerBadge(
    remainingMillis: Long,
    ratio: Float,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f
    val color = if (remainingMillis <= 0) {
        if (isDark) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    } else palette.textPrimary
    val bg = if (remainingMillis <= 0) {
        if (isDark) Color(0x33FF3B30) else Color(0xFFFEE2E2)
    } else palette.surfaceVariant
    val text = formatRemainingTime(remainingMillis)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.Clock,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DurationStartPill(
    durationValue: Int,
    durationUnit: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val totalMins = when (durationUnit.uppercase()) {
        "DAYS" -> durationValue * 24 * 60
        "HOURS" -> durationValue * 60
        else -> durationValue
    }
    val d = totalMins / 1440
    val h = (totalMins % 1440) / 60
    val m = totalMins % 60
    val label = when {
        d > 0 && h > 0 -> "${d}d ${h}h"
        d > 0 -> "${d}d"
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.accent.copy(alpha = 0.14f))
            .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onStart)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.Play,
            contentDescription = "Start",
            tint = palette.accent,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Start ($label)",
            color = palette.accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OverdueWarningBadge(
    onLogReason: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f
    val bg = if (isDark) palette.danger.copy(alpha = 0.18f) else Color(0xFFFEE2E2)
    val border = if (isDark) palette.danger.copy(alpha = 0.50f) else palette.danger.copy(alpha = 0.4f)
    val color = if (isDark) palette.danger else Color(0xFFDC2626)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onLogReason)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.AlertCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Overdue • Log Reason ✍️",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MissedStatusBadge(
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Missed",
            color = palette.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatRemainingTime(millis: Long): String {
    if (millis <= 0) return "0m left"
    val totalSeconds = millis / 1000
    val totalMinutes = totalSeconds / 60
    val hours = totalMinutes / 60
    val days = hours / 24
    val remHours = hours % 24
    val remMinutes = totalMinutes % 60
    val remSeconds = totalSeconds % 60

    return when {
        days > 0 -> "${days}d ${remHours}h left"
        hours > 0 -> "${hours}h ${remMinutes}m left"
        totalMinutes > 0 -> "${totalMinutes}m left"
        else -> "${remSeconds}s left"
    }
}

@Composable
private fun DateBadge(
    dueDate: Long,
    dueTime: String?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val dateText = formatDateLabel(dueDate, dueTime)
    val isPastDue = isPastDue(dueDate) && !isCompleted

    val badgeBg = if (isPastDue) {
        if (palette.background.red > 0.5f) Color(0xFFD4D4D8) else Color(0xFF27272A)
    } else palette.surfaceVariant
    val badgeTextColor = if (isPastDue) palette.textPrimary else palette.textSecondary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.Calendar,
            contentDescription = null,
            tint = badgeTextColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = dateText,
            color = badgeTextColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubtaskCountBadge(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isAllDone = completed == total

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.Check,
            contentDescription = null,
            tint = if (isAllDone) palette.textPrimary else palette.textSecondary,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$completed/$total",
            color = if (isAllDone) palette.textPrimary else palette.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlaceBadge(
    place: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "📍", fontSize = 10.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = place,
            color = palette.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecurrenceBadge(
    recurrenceRule: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val recurrence = RecurrenceType.fromString(recurrenceRule)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.RotateCcw,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = when (recurrence) {
                RecurrenceType.DAILY -> "Daily"
                RecurrenceType.WEEKDAYS -> "Weekdays"
                RecurrenceType.WEEKLY -> "Weekly"
                RecurrenceType.MONTHLY -> "Monthly"
                RecurrenceType.NONE -> ""
            },
            color = palette.accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AttachmentBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = UntitledIcons.Paperclip,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(11.dp)
        )
        if (count > 1) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = count.toString(),
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatDateLabel(epochMillis: Long, dueTime: String?): String {
    val taskCal = Calendar.getInstance().apply { timeInMillis = epochMillis }
    val todayCal = Calendar.getInstance()
    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    val datePart = when {
        isSameDay(taskCal, todayCal) -> "Today"
        isSameDay(taskCal, tomorrowCal) -> "Tomorrow"
        taskCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
        }
        else -> {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
        }
    }

    return if (!dueTime.isNullOrBlank()) {
        "$datePart $dueTime"
    } else {
        datePart
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isPastDue(epochMillis: Long): Boolean {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return epochMillis < cal.timeInMillis
}
