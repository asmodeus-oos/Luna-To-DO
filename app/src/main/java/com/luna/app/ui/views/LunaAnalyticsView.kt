package com.luna.app.ui.views

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import com.luna.app.ui.icons.UntitledIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.AchievementBadge
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.TaskStatus
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun LunaAnalyticsView(
    tasks: List<TaskWithDetails>,
    habits: List<HabitEntity>,
    goals: List<GoalEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val totalTasks = tasks.size
    val completedTasks = tasks.count {
        it.task.isCompleted &&
        it.task.status != TaskStatus.FAILED_LOGGED
    }
    val completionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

    val totalMinutesLogged = tasks.sumOf { it.task.actualMinutes }
    val activeHabitStreaks = habits.sumOf { it.currentStreak }

    // Gamification XP: +50 per completed task, +1 per min focus, +10 per habit streak
    val taskXp = tasks.filter {
        it.task.isCompleted &&
        it.task.status != TaskStatus.FAILED_LOGGED
    }.sumOf { it.task.xpValue.coerceAtLeast(50) }
    val totalXp = taskXp + totalMinutesLogged + (activeHabitStreaks * 10)
    val currentLevel = (totalXp / 100) + 1
    val currentLevelXp = totalXp % 100
    val levelProgress = currentLevelXp.toFloat() / 100f

    val levelTitle = when {
        currentLevel <= 1 -> "Novice Organizer"
        currentLevel <= 3 -> "Productive Starter"
        currentLevel <= 5 -> "Flow Master"
        currentLevel <= 8 -> "Deep Work Ninja"
        else -> "Grandmaster of Time"
    }

    // Achievements calculation
    val badges = listOf(
        AchievementBadge(
            id = "first_step",
            title = "First Step",
            description = "Completed your first task in Luna",
            icon = "🌱",
            isUnlocked = completedTasks >= 1,
            progressText = "$completedTasks/1"
        ),
        AchievementBadge(
            id = "focus_titan",
            title = "Focus Titan",
            description = "Logged 60+ minutes of deep focus",
            icon = "⏱️",
            isUnlocked = totalMinutesLogged >= 60,
            progressText = "${totalMinutesLogged}m/60m"
        ),
        AchievementBadge(
            id = "streak_champion",
            title = "Streak Champion",
            description = "Maintained a 3+ day habit streak",
            icon = "🔥",
            isUnlocked = habits.any { it.currentStreak >= 3 },
            progressText = "${habits.maxOfOrNull { it.currentStreak } ?: 0}/3"
        ),
        AchievementBadge(
            id = "matrix_master",
            title = "Matrix Master",
            description = "Completed 3+ urgent P1 tasks",
            icon = "🎯",
            isUnlocked = tasks.count { it.task.isCompleted && it.task.priority == Priority.P1 } >= 3,
            progressText = "${tasks.count { it.task.isCompleted && it.task.priority == Priority.P1 }}/3"
        ),
        AchievementBadge(
            id = "centurion",
            title = "Centurion",
            description = "Completed 15+ tasks overall",
            icon = "🏆",
            isUnlocked = completedTasks >= 15,
            progressText = "$completedTasks/15"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gamification Level Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MilitaryTech,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LEVEL $currentLevel",
                                color = palette.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = levelTitle,
                                color = palette.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "$totalXp XP",
                        color = palette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.accent,
                    trackColor = palette.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$currentLevelXp / 100 XP to Level ${currentLevel + 1}",
                    color = palette.textTertiary,
                    fontSize = 11.sp
                )
            }
        }

        // 7-Day Activity Bar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "7-DAY ACTIVITY",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tasks Completed Daily",
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calculate daily counts for past 7 days
                val past7DaysCounts = remember(tasks) {
                    (0..6).reversed().map { offset ->
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -offset)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dayStart = cal.timeInMillis
                        val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1L
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                        val count = tasks.count {
                            it.task.isCompleted &&
                            it.task.status != TaskStatus.FAILED_LOGGED &&
                            ((it.task.completedAt != null && it.task.completedAt in dayStart..dayEnd) ||
                             (it.task.completedAt == null && offset == 0))
                        }
                        Pair(dayLabel, count)
                    }
                }

                val maxCount = maxOf(past7DaysCounts.maxOfOrNull { it.second } ?: 1, 5)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    past7DaysCounts.forEach { pair ->
                        val label = pair.first
                        val count = pair.second
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            val barRatio = count.toFloat() / maxCount.toFloat()
                            val barHeight = (barRatio * 75).coerceAtLeast(6f)

                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(barHeight.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (count > 0) palette.accent else palette.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                color = palette.textTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Metric Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Rounded.CheckCircle,
                iconColor = Color(0xFF22C55E),
                title = "Completed",
                value = "$completedTasks tasks",
                subtitle = "$completionRate% completion rate",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = UntitledIcons.Clock,
                iconColor = palette.accent,
                title = "Focus Time",
                value = "${totalMinutesLogged}m",
                subtitle = "Total logged focus",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = Icons.Rounded.LocalFireDepartment,
                iconColor = Color(0xFFF97316),
                title = "Habit Streaks",
                value = "$activeHabitStreaks days",
                subtitle = "${habits.size} active habits",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Rounded.MilitaryTech,
                iconColor = Color(0xFFF59E0B),
                title = "Total Tasks",
                value = "$totalTasks",
                subtitle = "In your library",
                modifier = Modifier.weight(1f)
            )
        }

        // OKRs & Goals Section
        if (goals.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "GOALS & OKRs",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        goals.forEach { goal ->
                            val progress = if (goal.targetValue > 0) {
                                goal.currentValue.toFloat() / goal.targetValue.toFloat()
                            } else 0f

                            val goalColor = try {
                                Color(android.graphics.Color.parseColor(goal.colorHex))
                            } catch (_: Exception) {
                                palette.accent
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = goal.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = goal.title,
                                            color = palette.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "${goal.currentValue} / ${goal.targetValue} ${goal.unit}",
                                        color = palette.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = goalColor,
                                    trackColor = palette.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Badges & Achievements Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACHIEVEMENTS",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    badges.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (badge.isUnlocked) palette.accent.copy(alpha = 0.08f)
                                    else palette.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = badge.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    color = if (badge.isUnlocked) palette.textPrimary else palette.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = badge.description,
                                    color = palette.textTertiary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (badge.isUnlocked) Color(0xFF22C55E).copy(alpha = 0.15f)
                                        else palette.border
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (badge.isUnlocked) "UNLOCKED" else badge.progressText,
                                    color = if (badge.isUnlocked) Color(0xFF22C55E) else palette.textTertiary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Options (CSV & JSON)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Export JSON
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceVariant)
                    .clickable {
                        val jsonSummary = buildJsonExport(tasks)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Luna Tasks (JSON)")
                            putExtra(Intent.EXTRA_TEXT, jsonSummary)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export JSON"))
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export JSON",
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Export CSV
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceVariant)
                    .clickable {
                        val csvSummary = buildCsvExport(tasks)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Luna Tasks (CSV)")
                            putExtra(Intent.EXTRA_TEXT, csvSummary)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export CSV",
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Box(
        modifier = modifier
            .clip(LunaShapes.medium)
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle, LunaShapes.medium)
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = palette.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = palette.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = palette.textTertiary,
                fontSize = 10.sp
            )
        }
    }
}

private fun buildJsonExport(tasks: List<TaskWithDetails>): String {
    val sb = StringBuilder("[\n")
    tasks.forEachIndexed { index, item ->
        val t = item.task
        sb.append("  {\n")
        sb.append("    \"id\": ${t.id},\n")
        sb.append("    \"title\": \"${t.title.replace("\"", "\\\"")}\",\n")
        sb.append("    \"isCompleted\": ${t.isCompleted},\n")
        sb.append("    \"priority\": \"${t.priority.name}\",\n")
        sb.append("    \"energyLevel\": \"${t.energyLevel}\",\n")
        sb.append("    \"tags\": [${t.tags.joinToString(", ") { "\"$it\"" }}],\n")
        sb.append("    \"actualMinutes\": ${t.actualMinutes}\n")
        sb.append("  }${if (index < tasks.size - 1) "," else ""}\n")
    }
    sb.append("]")
    return sb.toString()
}

private fun buildCsvExport(tasks: List<TaskWithDetails>): String {
    val sb = StringBuilder("ID,Title,Completed,Priority,EnergyLevel,Tags,ActualMinutes\n")
    tasks.forEach { item ->
        val t = item.task
        val cleanTitle = t.title.replace(",", ";").replace("\"", "'")
        sb.append("${t.id},\"$cleanTitle\",${t.isCompleted},${t.priority.name},${t.energyLevel},\"${t.tags.joinToString(";")}\",${t.actualMinutes}\n")
    }
    return sb.toString()
}
