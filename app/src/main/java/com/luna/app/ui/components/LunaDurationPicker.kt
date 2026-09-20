package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.luna.app.ui.icons.UntitledIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.theme.LunaTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LunaDurationPicker(
    days: Int,
    hours: Int,
    minutes: Int,
    onDaysChange: (Int) -> Unit,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    val durationPresets = listOf(
        Triple("15m", 15, "MINUTES"),
        Triple("30m", 30, "MINUTES"),
        Triple("45m", 45, "MINUTES"),
        Triple("1h", 1, "HOURS"),
        Triple("2h", 2, "HOURS"),
        Triple("3h", 3, "HOURS"),
        Triple("4h", 4, "HOURS"),
        Triple("1d", 1, "DAYS")
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Quick Presets
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            durationPresets.forEach { (label, value, unit) ->
                val isSelected = when (unit) {
                    "MINUTES" -> days == 0 && hours == 0 && minutes == value
                    "HOURS" -> days == 0 && hours == value && minutes == 0
                    "DAYS" -> days == value && hours == 0 && minutes == 0
                    else -> false
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) palette.accent else palette.surfaceVariant)
                        .clickable {
                            when (unit) {
                                "MINUTES" -> {
                                    onDaysChange(0)
                                    onHoursChange(0)
                                    onMinutesChange(value)
                                }
                                "HOURS" -> {
                                    onDaysChange(0)
                                    onHoursChange(value)
                                    onMinutesChange(0)
                                }
                                "DAYS" -> {
                                    onDaysChange(value)
                                    onHoursChange(0)
                                    onMinutesChange(0)
                                }
                            }
                        }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) palette.chipTextSelected else palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Duration Controls (Days | Hours | Minutes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surfaceVariant.copy(alpha = 0.6f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationUnitStepper(
                label = "DAYS",
                value = days,
                maxValue = 365,
                onValueChange = onDaysChange,
                modifier = Modifier.weight(1f)
            )

            DurationUnitStepper(
                label = "HOURS",
                value = hours,
                maxValue = 23,
                onValueChange = onHoursChange,
                modifier = Modifier.weight(1f)
            )

            DurationUnitStepper(
                label = "MINUTES",
                value = minutes,
                maxValue = 59,
                onValueChange = onMinutesChange,
                wrapAround = true,
                formatTwoDigits = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Total Summary Display
        val totalMins = days * 1440 + hours * 60 + minutes
        val summaryText = formatDurationSummary(days, hours, minutes)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = UntitledIcons.Clock,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Total Duration:",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = if (totalMins > 0) summaryText else "Set duration above",
                color = if (totalMins > 0) palette.accent else palette.textTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Timer begins automatically when you tap 'Start' on the task.",
            color = palette.textTertiary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DurationUnitStepper(
    label: String,
    value: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    wrapAround: Boolean = false,
    formatTwoDigits: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = palette.textTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Decrement button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(palette.surfaceVariant)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (wrapAround) {
                                onValueChange(if (value <= 0) maxValue else value - 1)
                            } else {
                                onValueChange((value - 1).coerceAtLeast(0))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = UntitledIcons.Minus,
                    contentDescription = "Decrease $label",
                    tint = if (!wrapAround && value == 0) palette.textTertiary else palette.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Editable Numeric Input
            val displayText = if (formatTwoDigits) {
                java.lang.String.format(java.util.Locale.getDefault(), "%02d", value)
            } else {
                value.toString()
            }

            BasicTextField(
                value = displayText,
                onValueChange = { input ->
                    val num = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                    onValueChange(num.coerceIn(0, maxValue))
                },
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /* focus stays clean */ }
                ),
                singleLine = true,
                modifier = Modifier
                    .width(36.dp)
                    .padding(horizontal = 2.dp)
            )

            // Increment button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(palette.surfaceVariant)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (wrapAround) {
                                onValueChange(if (value >= maxValue) 0 else value + 1)
                            } else {
                                onValueChange((value + 1).coerceAtMost(maxValue))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = UntitledIcons.Plus,
                    contentDescription = "Increase $label",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

fun formatDurationSummary(days: Int, hours: Int, minutes: Int): String {
    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    return if (parts.isEmpty()) "0m" else parts.joinToString(" ")
}
