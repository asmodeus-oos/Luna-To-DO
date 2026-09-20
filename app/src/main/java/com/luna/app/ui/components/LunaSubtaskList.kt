package com.luna.app.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaSubtaskList(
    subtasks: List<SubtaskEntity>,
    subtitles: List<String> = emptyList(),
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onDeleteSubtask: (SubtaskEntity) -> Unit,
    onAddSubtask: (String) -> Unit,
    onAddSubtaskToSection: ((title: String, section: String) -> Unit)? = null,
    onAddSubtitle: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    var newSubtaskText by remember { mutableStateOf("") }
    var newSubtitleText by remember { mutableStateOf("") }
    var isAddingSubtitle by remember { mutableStateOf(false) }

    val total = subtasks.size
    val completed = subtasks.count { it.isCompleted }
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    // Combine explicit subtitles with sections discovered on subtasks
    val allSections = remember(subtitles, subtasks) {
        val fromSubtasks = subtasks.mapNotNull { it.subtitleSection }
        (subtitles + fromSubtasks).distinct()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header with progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUBTASKS & SECTIONS",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (total > 0) {
                    Text(
                        text = "$completed/$total",
                        color = if (completed == total) palette.accent else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Add Section Button
                Text(
                    text = "+ Section",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isAddingSubtitle = !isAddingSubtitle }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (total > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = palette.accent,
                trackColor = palette.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // New Subtitle Input Prompt
        AnimatedVisibility(
            visible = isAddingSubtitle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newSubtitleText,
                    onValueChange = { newSubtitleText = it },
                    textStyle = TextStyle(color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newSubtitleText.isNotBlank()) {
                                onAddSubtitle?.invoke(newSubtitleText.trim())
                                newSubtitleText = ""
                                isAddingSubtitle = false
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newSubtitleText.isEmpty()) {
                            Text("Section name (e.g. Materials Needed, Steps)...", color = palette.textTertiary, fontSize = 12.sp)
                        }
                        inner()
                    }
                )
                Text(
                    text = "Add",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            if (newSubtitleText.isNotBlank()) {
                                onAddSubtitle?.invoke(newSubtitleText.trim())
                                newSubtitleText = ""
                                isAddingSubtitle = false
                            }
                        }
                        .padding(horizontal = 6.dp)
                )
            }
        }

        // Sectioned Subtasks or Standalone Subtasks
        if (allSections.isNotEmpty()) {
            allSections.forEach { section ->
                val sectionSubtasks = subtasks.filter { it.subtitleSection == section }
                SubtitleSectionBlock(
                    sectionName = section,
                    subtasks = sectionSubtasks,
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onAddSubtaskToSection = { step ->
                        if (onAddSubtaskToSection != null) {
                            onAddSubtaskToSection(step, section)
                        } else {
                            onAddSubtask(step)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Also render non-sectioned subtasks if any
            val generalSubtasks = subtasks.filter { it.subtitleSection == null }
            if (generalSubtasks.isNotEmpty()) {
                Text(
                    text = "GENERAL ITEMS",
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                generalSubtasks.forEach { subtask ->
                    SubtaskItemRow(
                        subtask = subtask,
                        onToggle = { onToggleSubtask(subtask) },
                        onDelete = { onDeleteSubtask(subtask) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        } else {
            // Standalone subtasks list
            subtasks.forEach { subtask ->
                SubtaskItemRow(
                    subtask = subtask,
                    onToggle = { onToggleSubtask(subtask) },
                    onDelete = { onDeleteSubtask(subtask) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick add subtask input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = palette.textTertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newSubtaskText.isNotBlank()) {
                            onAddSubtask(newSubtaskText.trim())
                            newSubtaskText = ""
                        }
                    }
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (newSubtaskText.isEmpty()) {
                        Text(
                            text = "Add step or checklist item...",
                            color = palette.textTertiary,
                            fontSize = 13.sp
                        )
                    }
                    innerTextField()
                }
            )
            if (newSubtaskText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            onAddSubtask(newSubtaskText.trim())
                            newSubtaskText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = palette.onAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtitleSectionBlock(
    sectionName: String,
    subtasks: List<SubtaskEntity>,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onDeleteSubtask: (SubtaskEntity) -> Unit,
    onAddSubtaskToSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    var sectionInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.25f))
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📁 $sectionName",
                    color = palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${subtasks.count { it.isCompleted }}/${subtasks.size})",
                    color = palette.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        subtasks.forEach { subtask ->
            SubtaskItemRow(
                subtask = subtask,
                onToggle = { onToggleSubtask(subtask) },
                onDelete = { onDeleteSubtask(subtask) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Mini input for this section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = sectionInput,
                onValueChange = { sectionInput = it },
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (sectionInput.isNotBlank()) {
                            onAddSubtaskToSection(sectionInput.trim())
                            sectionInput = ""
                        }
                    }
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (sectionInput.isEmpty()) {
                        Text("+ Add to $sectionName...", color = palette.textTertiary, fontSize = 11.sp)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun SubtaskItemRow(
    subtask: SubtaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.35f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LunaCheckbox(
            checked = subtask.isCompleted,
            onCheckedChange = { onToggle() },
            size = 18.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = subtask.title,
            color = if (subtask.isCompleted) palette.textTertiary else palette.textPrimary,
            fontSize = 13.sp,
            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Delete subtask",
                tint = palette.textTertiary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
