package com.luna.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaOverdueReasonDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onLogMissed: (reasonCategory: String, reasonDetails: String) -> Unit,
    onReschedule: (reasonCategory: String, reasonDetails: String, additionalMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f

    // Theme contrast tokens
    val warningRedBg = if (isDark) Color(0x33FF3B30) else Color(0xFFFEE2E2)
    val warningRedBorder = if (isDark) Color(0x66FF3B30) else Color(0xFFEF4444).copy(alpha = 0.4f)
    val warningRedText = if (isDark) Color(0xFFFF6B6B) else Color(0xFFDC2626)

    val chips = listOf(
        "Ran out of time",
        "Got interrupted",
        "Underestimated effort",
        "Forgot",
        "Other"
    )

    var selectedCategory by remember { mutableStateOf(chips.first()) }
    var reasonDetails by remember { mutableStateOf("") }
    var extensionChoiceMinutes by remember { mutableIntStateOf(60) } // Default +1 hour

    val angryShake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        angryShake.animateTo(
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surface)
                .border(1.5.dp, warningRedBorder, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent click-through */ }
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Bar & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(warningRedBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OVERDUE TASK",
                                color = warningRedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = palette.textTertiary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onDismiss)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steaming Angry Emoji
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .graphicsLayer {
                            translationX = angryShake.value
                        }
                        .clip(CircleShape)
                        .background(warningRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "😠", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Why isn't this completed?",
                    color = palette.textPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"${task.title}\" passed its deadline. Log what happened for honest self-accountability.",
                    color = palette.textSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Reason Category Chips
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chips.forEach { chip ->
                        val isSelected = chip == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) warningRedBg
                                    else palette.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) warningRedBorder else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = chip }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chip,
                                color = if (isSelected) warningRedText else palette.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Text Details Input Field
                BasicTextField(
                    value = reasonDetails,
                    onValueChange = { reasonDetails = it },
                    textStyle = TextStyle(color = palette.textPrimary, fontSize = 12.5.sp),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    decorationBox = { inner ->
                        if (reasonDetails.isEmpty()) {
                            Text(
                                text = "Enter details or notes (optional)...",
                                color = palette.textTertiary,
                                fontSize = 12.sp
                            )
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Extension Steppers (+15m, +30m, +1h, +2h)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXTEND BY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (mins, label) ->
                            val isSel = extensionChoiceMinutes == mins
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) palette.accent.copy(alpha = 0.15f) else palette.surfaceVariant.copy(alpha = 0.4f))
                                    .border(1.dp, if (isSel) palette.accent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { extensionChoiceMinutes = mins }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) palette.accent else palette.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dual-Tone Button Row with Icons & Space Optimization
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Extend Deadline Button (Primary Accent Dual-Tone)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.accent)
                            .clickable {
                                onReschedule(selectedCategory, reasonDetails, extensionChoiceMinutes)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MoreTime,
                                contentDescription = null,
                                tint = palette.onAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Extend Deadline (+${if (extensionChoiceMinutes >= 60) "${extensionChoiceMinutes / 60}h" else "${extensionChoiceMinutes}m"})",
                                color = palette.onAccent,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Log as Missed Task Button (Translucent Red Dual-Tone)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(warningRedBg)
                            .border(1.dp, warningRedBorder, RoundedCornerShape(18.dp))
                            .clickable {
                                onLogMissed(selectedCategory, reasonDetails)
                                onDismiss()
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Cancel,
                                contentDescription = null,
                                tint = warningRedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Log as Missed Task",
                                color = warningRedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
