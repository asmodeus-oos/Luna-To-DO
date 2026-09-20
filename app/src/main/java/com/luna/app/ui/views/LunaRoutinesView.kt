package com.luna.app.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LunaRoutinesView(
    routines: List<RoutineEntity>,
    onToggleRoutineCompleted: (RoutineEntity) -> Unit,
    onAddRoutine: (String, String, String, List<String>) -> Unit,
    onUpdateRoutine: (RoutineEntity) -> Unit,
    onDuplicateRoutine: (Long) -> Unit,
    onDeleteRoutine: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    var activeRunningRoutine by remember { mutableStateOf<RoutineEntity?>(null) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<RoutineEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Header Toolbar with "+ New Routine" Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROUTINES & RITUALS",
                color = palette.textTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // "+ New Routine" Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.accent)
                    .clickable { showCreateDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Routine",
                        tint = palette.onAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Routine",
                        color = palette.onAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Routine Step Runner Banner
        val running = activeRunningRoutine
        if (running != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.accent.copy(alpha = 0.12f))
                    .border(1.5.dp, palette.accent, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = running.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = running.name.uppercase(),
                                color = palette.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Step ${currentStepIndex + 1} of ${running.steps.size}",
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val currentStep = running.steps.getOrNull(currentStepIndex) ?: "Completed!"
                    Text(
                        text = currentStep,
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                activeRunningRoutine = null
                                currentStepIndex = 0
                            }
                        ) {
                            Text("Cancel Flow", color = palette.textTertiary, fontSize = 12.sp)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.accent)
                                .clickable {
                                    if (currentStepIndex + 1 < running.steps.size) {
                                        currentStepIndex += 1
                                    } else {
                                        onToggleRoutineCompleted(running)
                                        activeRunningRoutine = null
                                        currentStepIndex = 0
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (currentStepIndex + 1 >= running.steps.size) Icons.Rounded.Check else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = palette.onAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentStepIndex + 1 >= running.steps.size) "Finish Routine" else "Next Step",
                                    color = palette.onAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Routines List
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔄", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build seamless daily routines.",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ New Routine' above to create your first ritual!",
                        color = palette.textTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(routines, key = { it.id }) { routine ->
                    val isDoneToday = routine.isCompletedToday()
                    var showDropdownMenu by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LunaShapes.medium)
                            .background(palette.surface)
                            .border(1.dp, palette.borderSubtle, LunaShapes.medium)
                            .combinedClickable(
                                onClick = {
                                    activeRunningRoutine = routine
                                    currentStepIndex = 0
                                },
                                onLongClick = {
                                    showDropdownMenu = true
                                }
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isDoneToday) Color(0xFF22C55E).copy(alpha = 0.15f)
                                                else palette.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = routine.icon, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = routine.name,
                                            color = palette.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${routine.timeOfDay} • ${routine.steps.size} steps",
                                            color = palette.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Start Flow Runner Button
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(palette.accent.copy(alpha = 0.12f))
                                            .clickable {
                                                activeRunningRoutine = routine
                                                currentStepIndex = 0
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Start",
                                                tint = palette.accent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Run",
                                                color = palette.accent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Mark Done Button
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isDoneToday) Color(0xFF22C55E) else palette.surfaceVariant
                                            )
                                            .clickable { onToggleRoutineCompleted(routine) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDoneToday) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Done today",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // 3-Dot Overflow Menu Button
                                    Box {
                                        IconButton(
                                            onClick = { showDropdownMenu = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreVert,
                                                contentDescription = "Routine Options",
                                                tint = palette.textTertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Long Press / 3-Dot Dropdown Menu
                                        DropdownMenu(
                                            expanded = showDropdownMenu,
                                            onDismissRequest = { showDropdownMenu = false },
                                            modifier = Modifier.background(palette.surface)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit Routine", color = palette.textPrimary) },
                                                leadingIcon = {
                                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showDropdownMenu = false
                                                    editingRoutine = routine
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Duplicate Routine", color = palette.textPrimary) },
                                                leadingIcon = {
                                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showDropdownMenu = false
                                                    onDuplicateRoutine(routine.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Routine", color = Color(0xFFEF4444)) },
                                                leadingIcon = {
                                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showDropdownMenu = false
                                                    onDeleteRoutine(routine.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Steps list preview
                            if (routine.steps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    routine.steps.forEach { step ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.textTertiary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = step,
                                                color = palette.textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "routines_bottom_spacer") {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    // Create Routine Dialog
    if (showCreateDialog) {
        RoutineEditorDialog(
            title = "New Routine",
            initialName = "",
            initialIcon = "🌅",
            initialTimeOfDay = "Morning",
            initialSteps = listOf("Wake up & Hydrate", "5-Min Stretch", "Review Today's Priorities"),
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, icon, timeOfDay, steps ->
                onAddRoutine(name, icon, timeOfDay, steps)
                showCreateDialog = false
            }
        )
    }

    // Edit Routine Dialog
    editingRoutine?.let { routine ->
        RoutineEditorDialog(
            title = "Edit Routine",
            initialName = routine.name,
            initialIcon = routine.icon,
            initialTimeOfDay = routine.timeOfDay,
            initialSteps = routine.steps,
            onDismiss = { editingRoutine = null },
            onConfirm = { name, icon, timeOfDay, steps ->
                onUpdateRoutine(
                    routine.copy(
                        name = name,
                        icon = icon,
                        timeOfDay = timeOfDay,
                        steps = steps
                    )
                )
                editingRoutine = null
            }
        )
    }
}

/**
 * Clean Liquid Glass Dialog for Creating and Editing Routines with steps management.
 */
@Composable
private fun RoutineEditorDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialTimeOfDay: String,
    initialSteps: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, timeOfDay: String, steps: List<String>) -> Unit
) {
    val palette = LunaTheme.colors
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedTimeOfDay by remember { mutableStateOf(initialTimeOfDay) }
    val steps = remember { mutableStateListOf<String>().apply { addAll(initialSteps) } }
    var newStepInput by remember { mutableStateOf("") }

    val iconPresets = listOf("🌅", "☀️", "🌙", "🏃", "☕", "🧘", "📚", "⚡", "💧", "🍳", "🚶", "🎯")
    val timePresets = listOf("Morning", "Afternoon", "Evening", "Night", "Anytime")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    placeholder = { Text("e.g. Morning Focus Ritual") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon presets
                Text(
                    text = "CHOOSE ICON",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconPresets.take(6).forEach { ic ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedIcon == ic) palette.accent.copy(alpha = 0.2f)
                                    else palette.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    if (selectedIcon == ic) 1.5.dp else 0.dp,
                                    if (selectedIcon == ic) palette.accent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedIcon = ic },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ic, fontSize = 18.sp)
                        }
                    }
                }

                // Time of Day
                Text(
                    text = "TIME OF DAY",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePresets.forEach { time ->
                        val isSel = time == selectedTimeOfDay
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) palette.accent
                                    else palette.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { selectedTimeOfDay = time }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = time,
                                color = if (isSel) palette.onAccent else palette.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Steps Builder
                Text(
                    text = "ROUTINE STEPS (${steps.size})",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    steps.forEachIndexed { index, stepText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. $stepText",
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { steps.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove Step",
                                    tint = palette.textTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Add Step input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newStepInput,
                        onValueChange = { newStepInput = it },
                        placeholder = { Text("Add next step...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.borderSubtle
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.accent)
                            .clickable {
                                if (newStepInput.isNotBlank()) {
                                    steps.add(newStepInput.trim())
                                    newStepInput = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Step",
                            tint = palette.onAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent)
                    .clickable {
                        if (name.isNotBlank()) {
                            onConfirm(name.trim(), selectedIcon, selectedTimeOfDay, steps.toList())
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Save Routine",
                    color = palette.onAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textTertiary, fontSize = 12.sp)
            }
        }
    )
}
