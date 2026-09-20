package com.luna.app.ui.views

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaMatrixView(
    tasks: List<TaskWithDetails>,
    onToggleCompleted: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val q1Tasks = tasks.filter { !it.task.isCompleted && it.task.priority == Priority.P1 }
    val q2Tasks = tasks.filter { !it.task.isCompleted && it.task.priority == Priority.P2 }
    val q3Tasks = tasks.filter { !it.task.isCompleted && it.task.priority == Priority.P3 }
    val q4Tasks = tasks.filter { !it.task.isCompleted && (it.task.priority == Priority.P4 || it.task.priority == Priority.NONE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Row: Q1 (Do First) & Q2 (Schedule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MatrixQuadrant(
                title = "DO FIRST",
                subtitle = "Urgent & Important",
                color = Color(0xFFEF4444),
                tasks = q1Tasks,
                onToggleCompleted = onToggleCompleted,
                onTaskClick = onTaskClick,
                modifier = Modifier.weight(1f)
            )
            MatrixQuadrant(
                title = "SCHEDULE",
                subtitle = "Important",
                color = Color(0xFFF59E0B),
                tasks = q2Tasks,
                onToggleCompleted = onToggleCompleted,
                onTaskClick = onTaskClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Row: Q3 (Delegate) & Q4 (Backlog)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MatrixQuadrant(
                title = "DELEGATE",
                subtitle = "Urgent",
                color = Color(0xFF3B82F6),
                tasks = q3Tasks,
                onToggleCompleted = onToggleCompleted,
                onTaskClick = onTaskClick,
                modifier = Modifier.weight(1f)
            )
            MatrixQuadrant(
                title = "BACKLOG",
                subtitle = "Low Priority",
                color = Color(0xFF94A3B8),
                tasks = q4Tasks,
                onToggleCompleted = onToggleCompleted,
                onTaskClick = onTaskClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MatrixQuadrant(
    title: String,
    subtitle: String,
    color: Color,
    tasks: List<TaskWithDetails>,
    onToggleCompleted: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = subtitle,
                    color = palette.textTertiary,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tasks.size.toString(),
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks",
                    color = palette.textTertiary,
                    fontSize = 11.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tasks, key = { it.task.id }) { item ->
                    MatrixItemRow(
                        taskWithDetails = item,
                        onToggle = { onToggleCompleted(item) },
                        onClick = { onTaskClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixItemRow(
    taskWithDetails: TaskWithDetails,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val palette = LunaTheme.colors
    val task = taskWithDetails.task

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunaCheckbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            size = 16.dp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = task.title,
            color = palette.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
