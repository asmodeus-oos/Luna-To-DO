package com.luna.app.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.domain.model.Priority
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.components.LunaPriorityBadge
import com.luna.app.ui.components.LunaTagChip
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

/**
 * Dedicated Apple Liquid Glass Global Search View
 * Real-time instant filtering across task titles, notes, tags, checklists, and projects.
 */
@Composable
fun LunaGlobalSearchView(
    tasks: List<TaskWithDetails>,
    onToggleTask: (TaskEntity) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTag by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    val isLight = palette.background.red > 0.5f

    // Specular border and glass fills
    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.90f) else Color(0xD91C1C24).copy(alpha = 0.85f)
    val glassBorderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.15f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            )
        }
    )

    // Handle system back gesture
    BackHandler(enabled = true) {
        onDismiss()
    }

    // Request keyboard focus immediately on open
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Real-time filtering logic
    val filteredTasks = remember(tasks, searchQuery, selectedFilterTag) {
        val query = searchQuery.trim().lowercase()
        tasks.filter { taskWithDetails ->
            val task = taskWithDetails.task
            val matchesTag = selectedFilterTag == null || task.tags.any { it.equals(selectedFilterTag, ignoreCase = true) }
            if (!matchesTag) return@filter false

            if (query.isEmpty()) return@filter true

            val matchesTitle = task.title.lowercase().contains(query)
            val matchesNotes = task.notes?.lowercase()?.contains(query) == true
            val matchesTags = task.tags.any { it.lowercase().contains(query) }
            val matchesSubtasks = taskWithDetails.subtasks.any { it.title.lowercase().contains(query) }
            val matchesSubtitles = task.subtitles.any { it.lowercase().contains(query) }

            matchesTitle || matchesNotes || matchesTags || matchesSubtasks || matchesSubtitles
        }
    }

    // Collect all distinct tags for quick chips
    val allTags = remember(tasks) {
        tasks.flatMap { it.task.tags }.distinct().take(8)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GLOBAL SEARCH",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Find Anything",
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Liquid Glass Circular Close Orb
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = if (isLight) Color(0x15000000) else Color(0x30000000),
                            spotColor = if (isLight) Color(0x20000000) else Color(0x40000000)
                        )
                        .clip(CircleShape)
                        .background(glassFill)
                        .border(1.dp, glassBorderBrush, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UntitledIcons.Close,
                        contentDescription = "Close search",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Apple Liquid Glass Search Capsule (Full width, 60dp height like bottom navbar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = if (isLight) Color(0x18000000) else Color(0x50000000),
                        spotColor = if (isLight) Color(0x20000000) else Color(0x60000000)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(glassFill)
                    .border(
                        width = 1.2.dp,
                        brush = glassBorderBrush,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = UntitledIcons.Search,
                        contentDescription = "Search icon",
                        tint = if (searchQuery.isNotEmpty()) palette.textPrimary else palette.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(palette.accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* hide kb */ }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search tasks, notes, checklists, #tags...",
                                    color = palette.textTertiary,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0x15000000) else Color(0x25FFFFFF))
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = UntitledIcons.Close,
                                contentDescription = "Clear search",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Quick Tag Pills (if available)
            if (allTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allTags.take(4).forEach { tag ->
                        val isSelected = selectedFilterTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) palette.accent else (if (isLight) Color(0xEEFFFFFF) else Color(0x25FFFFFF))
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else (if (isLight) Color(0x40FFFFFF) else Color(0x20FFFFFF)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedFilterTag = if (isSelected) null else tag
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = if (isSelected) palette.chipTextSelected else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Results count banner
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank() && selectedFilterTag == null) "RECENT & UPCOMING" else "RESULTS (${filteredTasks.size})",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search Results List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "Type to search your tasks" else "No matching tasks found",
                            color = palette.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Search queries filter titles, descriptions, and tags in real-time." else "Try different keywords or clear filters.",
                            color = palette.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.task.id }) { taskWithDetails ->
                        SearchResultItem(
                            taskWithDetails = taskWithDetails,
                            searchQuery = searchQuery,
                            onToggleTask = { onToggleTask(taskWithDetails.task) },
                            onClick = { onTaskClick(taskWithDetails) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    taskWithDetails: TaskWithDetails,
    searchQuery: String,
    onToggleTask: () -> Unit,
    onClick: () -> Unit
) {
    val palette = LunaTheme.colors
    val task = taskWithDetails.task
    val isLight = palette.background.red > 0.5f

    val cardBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.92f) else Color(0xD91E1E26).copy(alpha = 0.85f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.90f else 0.30f),
            Color.White.copy(alpha = if (isLight) 0.30f else 0.08f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Checkbox
            LunaCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleTask() },
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) palette.textTertiary else palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (!task.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                if (task.tags.isNotEmpty() || task.priority != Priority.NONE) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (task.priority != Priority.NONE) {
                            LunaPriorityBadge(priority = task.priority)
                        }
                        task.tags.take(3).forEach { tag ->
                            LunaTagChip(tag = tag)
                        }
                    }
                }
            }

            if (taskWithDetails.subtasks.isNotEmpty()) {
                val doneCount = taskWithDetails.subtasks.count { it.isCompleted }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0x15000000) else Color(0x20FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$doneCount/${taskWithDetails.subtasks.size}",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
