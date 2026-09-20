package com.luna.app.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.components.LunaPriorityBadge
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaBoardView(
    tasks: List<TaskWithDetails>,
    onStatusChange: (Long, TaskStatus) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TaskStatus.entries.forEach { status ->
            val columnTasks = tasks.filter { it.task.status == status }
            KanbanColumn(
                status = status,
                tasks = columnTasks,
                onStatusChange = onStatusChange,
                onTaskClick = onTaskClick,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    status: TaskStatus,
    tasks: List<TaskWithDetails>,
    onStatusChange: (Long, TaskStatus) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp)
    ) {
        // Column Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.label.uppercase(),
                color = palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.border)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tasks.size.toString(),
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cards list
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks in ${status.label}",
                    color = palette.textTertiary,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tasks, key = { it.task.id }) { item ->
                    KanbanCard(
                        taskWithDetails = item,
                        currentStatus = status,
                        onStatusChange = { newStatus -> onStatusChange(item.task.id, newStatus) },
                        onClick = { onTaskClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanCard(
    taskWithDetails: TaskWithDetails,
    currentStatus: TaskStatus,
    onStatusChange: (TaskStatus) -> Unit,
    onClick: () -> Unit
) {
    val task = taskWithDetails.task
    val palette = LunaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LunaShapes.medium)
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle, LunaShapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = task.title,
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.priority != Priority.NONE) {
                    LunaPriorityBadge(priority = task.priority)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Move status button
                val nextStatus = when (currentStatus) {
                    TaskStatus.NOT_STARTED -> TaskStatus.IN_PROGRESS
                    TaskStatus.IN_PROGRESS -> TaskStatus.DONE
                    TaskStatus.OVERDUE_PENDING_REASON -> TaskStatus.IN_PROGRESS
                    TaskStatus.FAILED_LOGGED -> TaskStatus.NOT_STARTED
                    TaskStatus.DONE -> TaskStatus.NOT_STARTED
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceVariant)
                        .clickable { onStatusChange(nextStatus) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nextStatus.label,
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
