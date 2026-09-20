package com.luna.app.ui.views

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LunaHabitsView(
    habits: List<HabitEntity>,
    onToggleHabit: (HabitEntity) -> Unit,
    onAddHabit: (name: String, icon: String, colorHex: String, targetPerDay: Int, imageUri: String?) -> Unit,
    onUpdateHabit: (HabitEntity) -> Unit,
    onDuplicateHabit: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    var quickHabitName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Header Toolbar with "+ New Habit" Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HABITS & STREAKS",
                color = palette.textTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // "+ New Habit" Button
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
                        contentDescription = "New Habit",
                        tint = palette.onAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New Habit",
                        color = palette.onAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Add Habit Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚡", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = quickHabitName,
                onValueChange = { quickHabitName = it },
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (quickHabitName.isNotBlank()) {
                            onAddHabit(quickHabitName.trim(), "⚡", "#008FFD", 1, null)
                            quickHabitName = ""
                        }
                    }
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (quickHabitName.isEmpty()) {
                        Text(
                            text = "Quick add habit (e.g. Drink 2L water, Walk 30m)...",
                            color = palette.textTertiary,
                            fontSize = 13.sp
                        )
                    }
                    inner()
                }
            )
            if (quickHabitName.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            onAddHabit(quickHabitName.trim(), "⚡", "#008FFD", 1, null)
                            quickHabitName = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = palette.onAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⚡", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build positive streaks!",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ New Habit' above to create your first habit with image & daily dots.",
                        color = palette.textTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onToggleStep = { onToggleHabit(habit) },
                        onEdit = { editingHabit = habit },
                        onDuplicate = { onDuplicateHabit(habit.id) },
                        onDelete = { onDeleteHabit(habit.id) }
                    )
                }

                item(key = "habits_bottom_spacer") {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    // Create Habit Dialog
    if (showCreateDialog) {
        HabitEditorDialog(
            title = "New Habit",
            initialName = "",
            initialIcon = "⚡",
            initialColorHex = "#008FFD",
            initialTargetPerDay = 1,
            initialImageUri = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, icon, colorHex, targetPerDay, imageUri ->
                onAddHabit(name, icon, colorHex, targetPerDay, imageUri)
                showCreateDialog = false
            }
        )
    }

    // Edit Habit Dialog
    editingHabit?.let { habit ->
        HabitEditorDialog(
            title = "Edit Habit",
            initialName = habit.name,
            initialIcon = habit.icon,
            initialColorHex = habit.colorHex,
            initialTargetPerDay = habit.targetPerDay,
            initialImageUri = habit.imageUri,
            onDismiss = { editingHabit = null },
            onConfirm = { name, icon, colorHex, targetPerDay, imageUri ->
                onUpdateHabit(
                    habit.copy(
                        name = name,
                        icon = icon,
                        colorHex = colorHex,
                        targetPerDay = targetPerDay,
                        imageUri = imageUri
                    )
                )
                editingHabit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    habit: HabitEntity,
    onToggleStep: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LunaTheme.colors
    val todayCount = habit.todayCompletedCount()
    val target = habit.targetPerDay.coerceAtLeast(1)
    val isAchievedToday = habit.isCompletedToday()
    var showDropdownMenu by remember { mutableStateOf(false) }

    val imagePath = habit.imageUri
    val imageBitmap = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) {
            val file = File(imagePath)
            if (file.exists()) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (_: Throwable) { null }
            } else null
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(
                width = if (isAchievedToday) 1.5.dp else 1.dp,
                color = if (isAchievedToday) Color(0xFF22C55E).copy(alpha = 0.7f) else palette.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = {
                    if (!isAchievedToday) {
                        onToggleStep()
                    }
                },
                onLongClick = { showDropdownMenu = true }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Compact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Habit Icon / Custom Image (Compact 36dp)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isAchievedToday) Color(0xFF22C55E).copy(alpha = 0.15f)
                            else palette.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = habit.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = habit.icon, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = habit.name,
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Streak Pill
                        if (habit.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF97316).copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${habit.currentStreak}d",
                                    color = Color(0xFFF97316),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Unlimited Scrollable Target Dots Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Scrollable Dots Container (Supports unlimited dots cleanly!)
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (dotIdx in 0 until target) {
                                val isDotAchieved = dotIdx < todayCount
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDotAchieved) Color(0xFF22C55E)
                                            else palette.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isDotAchieved) Color(0xFF22C55E) else palette.borderSubtle,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDotAchieved) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "$todayCount/$target today",
                            color = if (isAchievedToday) Color(0xFF22C55E) else palette.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Increment Action Button / Locked Completed Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAchievedToday) {
                        // Locked Completed Badge until next day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Done Today",
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Done • Locked 🔒",
                                    color = Color(0xFF22C55E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Increment Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accent)
                                .clickable(onClick = onToggleStep),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add progress",
                                tint = palette.onAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 3-Dot Overflow Menu Button
                    Box {
                        IconButton(
                            onClick = { showDropdownMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options",
                                tint = palette.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            modifier = Modifier.background(palette.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Habit", color = palette.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate Habit", color = palette.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Habit", color = Color(0xFFEF4444)) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Congratulations banner when achieved today
            if (isAchievedToday) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🎉 Goal finished! Great job — locked until tomorrow!",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Habit Editor Modal: Allows setting Habit Name, Habit Photo Image, Fallback Icon, and Times Per Day.
 */
@Composable
private fun HabitEditorDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialColorHex: String,
    initialTargetPerDay: Int,
    initialImageUri: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, colorHex: String, targetPerDay: Int, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    val palette = LunaTheme.colors

    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var targetPerDay by remember { mutableIntStateOf(initialTargetPerDay.coerceAtLeast(1)) }
    var imageUri by remember { mutableStateOf(initialImageUri) }

    val iconPresets = listOf("⚡", "💧", "🏃", "🧘", "🥗", "📚", "💤", "💊", "✍️", "🎯", "🏋️", "🍎")
    val frequencyPresets = listOf(1, 2, 3, 4, 5, 8)

    // Gallery Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val time = System.currentTimeMillis()
                val targetFile = File(context.filesDir, "habit_icon_$time.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (targetFile.exists() && targetFile.length() > 0) {
                    imageUri = targetFile.absolutePath
                }
            } catch (_: Throwable) {}
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Habit Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Drink 8 Glasses of Water") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Habit Image & Icon
                Text(
                    text = "HABIT IMAGE & ICON",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Image Preview / Picker Box
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surfaceVariant)
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val currentPath = imageUri
                        val bitmap = remember(currentPath) {
                            if (!currentPath.isNullOrBlank()) {
                                val f = File(currentPath)
                                if (f.exists()) {
                                    try { BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() }
                                    catch (_: Throwable) { null }
                                } else null
                            } else null
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Habit Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = "Pick Image",
                                    tint = palette.textTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Photo",
                                    color = palette.textTertiary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.accent.copy(alpha = 0.12f))
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (imageUri != null) "Change Photo" else "Set Photo 📷",
                                    color = palette.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (imageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surfaceVariant)
                                        .clickable { imageUri = null }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Remove",
                                        color = palette.textTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pick a custom photo or use an emoji below",
                            color = palette.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Emoji Presets
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
                                    if (selectedIcon == ic && imageUri == null) palette.accent.copy(alpha = 0.2f)
                                    else palette.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    if (selectedIcon == ic && imageUri == null) 1.5.dp else 0.dp,
                                    if (selectedIcon == ic && imageUri == null) palette.accent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedIcon = ic
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ic, fontSize = 18.sp)
                        }
                    }
                }

                // Daily Target (Times per Day represented as Dots)
                Text(
                    text = "DAILY TARGET (TIMES PER DAY)",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceVariant)
                                .clickable {
                                    if (targetPerDay > 1) targetPerDay -= 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Decrease",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$targetPerDay time${if (targetPerDay > 1) "s" else ""} / day",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceVariant)
                                .clickable {
                                    if (targetPerDay < 15) targetPerDay += 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Increase",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Dots preview
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0 until targetPerDay.coerceAtMost(6)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                        if (targetPerDay > 6) {
                            Text(
                                text = "+${targetPerDay - 6}",
                                color = palette.textTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Quick Target Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    frequencyPresets.forEach { count ->
                        val isSel = count == targetPerDay
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) palette.accent
                                    else palette.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { targetPerDay = count }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${count}x",
                                color = if (isSel) palette.onAccent else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
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
                            onConfirm(name.trim(), selectedIcon, initialColorHex, targetPerDay, imageUri)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Save Habit",
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
