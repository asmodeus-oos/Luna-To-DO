package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

/**
 * Apple Liquid Glass Dialog displayed when deleting an unfinished task.
 * Prompts the user for why the task wasn't finished before proceeding with deletion.
 */
@Composable
fun LunaDeleteUnfinishedDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirmDeleteWithReason: (category: String, details: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    val isLight = palette.background.red > 0.5f

    val chips = listOf(
        "No longer needed",
        "Ran out of time",
        "Deprioritized",
        "Underestimated effort",
        "Other"
    )

    var selectedCategory by remember { mutableStateOf(chips.first()) }
    var reasonDetails by remember { mutableStateOf("") }

    val glassFill = if (isLight) Color(0xF8FFFFFF) else Color(0xF21C1C24)
    val glassBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.35f else 0.10f),
            Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isLight) Color(0x25000000) else Color(0x60000000),
                    spotColor = if (isLight) Color(0x35000000) else Color(0x80000000)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(glassFill)
                .border(1.2.dp, glassBorder, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(22.dp)
        ) {
            Column {
                // Header with Close Orb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UNFINISHED TASK",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Why wasn't this finished?",
                            color = palette.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = UntitledIcons.Close,
                            contentDescription = "Dismiss",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Task Title preview capsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = task.title,
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select reason:",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { chip ->
                        val isSelected = chip == selectedCategory
                        val chipBg = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.7f)
                        val chipTextColor = if (isSelected) palette.chipTextSelected else palette.textSecondary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(chipBg)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCategory = chip
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                color = chipTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional notes input
                BasicTextField(
                    value = reasonDetails,
                    onValueChange = { reasonDetails = it },
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        if (reasonDetails.isEmpty()) {
                            Text(
                                text = "Add note or reflection (optional)...",
                                color = palette.textTertiary,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Confirm Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEF4444))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirmDeleteWithReason(selectedCategory, reasonDetails.trim())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = UntitledIcons.Trash,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete Task",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
