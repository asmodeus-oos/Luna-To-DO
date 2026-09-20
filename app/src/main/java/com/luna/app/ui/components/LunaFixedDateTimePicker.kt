package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun LunaFixedDateTimePicker(
    isDateRangeMode: Boolean,
    onDateRangeModeChange: (Boolean) -> Unit,
    singleDateMillis: Long,
    onSingleDateChange: (Long) -> Unit,
    startDateMillis: Long,
    onStartDateChange: (Long) -> Unit,
    endDateMillis: Long,
    onEndDateChange: (Long) -> Unit,
    isTimePeriodMode: Boolean,
    onTimePeriodModeChange: (Boolean) -> Unit,
    hasExactTime: Boolean,
    onHasExactTimeChange: (Boolean) -> Unit,
    exactHour: Int,
    onExactHourChange: (Int) -> Unit,
    exactMinute: Int,
    onExactMinuteChange: (Int) -> Unit,
    exactIsAm: Boolean,
    onExactIsAmChange: (Boolean) -> Unit,
    hasPeriodTime: Boolean,
    onHasPeriodTimeChange: (Boolean) -> Unit,
    periodFromHour: Int,
    onPeriodFromHourChange: (Int) -> Unit,
    periodFromMinute: Int,
    onPeriodFromMinuteChange: (Int) -> Unit,
    periodFromIsAm: Boolean,
    onPeriodFromIsAmChange: (Boolean) -> Unit,
    periodToHour: Int,
    onPeriodToHourChange: (Int) -> Unit,
    periodToMinute: Int,
    onPeriodToMinuteChange: (Int) -> Unit,
    periodToIsAm: Boolean,
    onPeriodToIsAmChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    val cardBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.90f) else Color(0xD91E1E26).copy(alpha = 0.85f)
    val cardBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.40f else 0.10f),
            Color.Transparent
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.2.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // --- 1. DATE CONFIGURATION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = UntitledIcons.Calendar,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DATE CONFIGURATION",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Toggle Single vs Range
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isDateRangeMode) palette.accent else Color.Transparent)
                        .clickable { onDateRangeModeChange(false) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Single",
                        color = if (!isDateRangeMode) palette.chipTextSelected else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDateRangeMode) palette.accent else Color.Transparent)
                        .clickable { onDateRangeModeChange(true) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "From:To",
                        color = if (isDateRangeMode) palette.chipTextSelected else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isDateRangeMode) {
            // Single Date Display & Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLight) Color(0x10000000) else Color(0x18FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Day
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color.White else Color(0x30FFFFFF))
                        .clickable { onSingleDateChange(singleDateMillis - 86400000L) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "‹", fontSize = 16.sp, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = formatFullDate(singleDateMillis),
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Next Day
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color.White else Color(0x30FFFFFF))
                        .clickable { onSingleDateChange(singleDateMillis + 86400000L) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "›", fontSize = 16.sp, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Single Date Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickDateChip(text = "Today", onClick = { onSingleDateChange(System.currentTimeMillis()) }, isLight = isLight)
                QuickDateChip(text = "Tomorrow", onClick = { onSingleDateChange(System.currentTimeMillis() + 86400000L) }, isLight = isLight)
                QuickDateChip(text = "+3 Days", onClick = { onSingleDateChange(System.currentTimeMillis() + 3 * 86400000L) }, isLight = isLight)
                QuickDateChip(text = "Next Week", onClick = { onSingleDateChange(System.currentTimeMillis() + 7 * 86400000L) }, isLight = isLight)
            }
        } else {
            // Date Range (From:To) Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FROM Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0x10000000) else Color(0x18FFFFFF))
                        .padding(10.dp)
                ) {
                    Text(text = "FROM", color = palette.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = formatShortDate(startDateMillis), color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepButton(label = "-1d", onClick = { onStartDateChange(startDateMillis - 86400000L) }, isLight = isLight)
                        StepButton(label = "+1d", onClick = { onStartDateChange(startDateMillis + 86400000L) }, isLight = isLight)
                    }
                }

                Text(text = "➔", color = palette.textTertiary, fontSize = 14.sp)

                // TO Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0x10000000) else Color(0x18FFFFFF))
                        .padding(10.dp)
                ) {
                    Text(text = "TO", color = palette.textTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = formatShortDate(endDateMillis), color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StepButton(label = "-1d", onClick = { onEndDateChange(endDateMillis - 86400000L) }, isLight = isLight)
                        StepButton(label = "+1d", onClick = { onEndDateChange(endDateMillis + 86400000L) }, isLight = isLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 2. TIME CONFIGURATION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = UntitledIcons.Clock,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TIME & PERIOD",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Toggle Exact vs Period
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isTimePeriodMode) palette.accent else Color.Transparent)
                        .clickable { onTimePeriodModeChange(false) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Exact Time",
                        color = if (!isTimePeriodMode) palette.chipTextSelected else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTimePeriodMode) palette.accent else Color.Transparent)
                        .clickable { onTimePeriodModeChange(true) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Period (From:To)",
                        color = if (isTimePeriodMode) palette.chipTextSelected else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isTimePeriodMode) {
            // Exact Time Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLight) Color(0x10000000) else Color(0x18FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasExactTime) formatTime(exactHour, exactMinute, exactIsAm) else "No Time Set",
                    color = if (hasExactTime) palette.textPrimary else palette.textTertiary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Enable", color = palette.textSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = hasExactTime,
                        onCheckedChange = onHasExactTimeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isLight) Color(0xFF34C759) else Color(0xFF30D158),
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = if (isLight) Color.White else Color(0xFFD4D4D8),
                            uncheckedTrackColor = if (isLight) Color(0xFFE4E4E8) else Color(0xFF323238),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (hasExactTime) {
                Spacer(modifier = Modifier.height(8.dp))
                TimePickerControls(
                    hour = exactHour,
                    onHourChange = onExactHourChange,
                    minute = exactMinute,
                    onMinuteChange = onExactMinuteChange,
                    isAm = exactIsAm,
                    onIsAmChange = onExactIsAmChange,
                    isLight = isLight
                )
            }
        } else {
            // Period (From:To) Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLight) Color(0x10000000) else Color(0x18FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasPeriodTime) {
                        "${formatTime(periodFromHour, periodFromMinute, periodFromIsAm)} – ${formatTime(periodToHour, periodToMinute, periodToIsAm)}"
                    } else "No Period Set",
                    color = if (hasPeriodTime) palette.textPrimary else palette.textTertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Enable", color = palette.textSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = hasPeriodTime,
                        onCheckedChange = onHasPeriodTimeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isLight) Color(0xFF34C759) else Color(0xFF30D158),
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = if (isLight) Color.White else Color(0xFFD4D4D8),
                            uncheckedTrackColor = if (isLight) Color(0xFFE4E4E8) else Color(0xFF323238),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (hasPeriodTime) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "START TIME", color = palette.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                TimePickerControls(
                    hour = periodFromHour,
                    onHourChange = onPeriodFromHourChange,
                    minute = periodFromMinute,
                    onMinuteChange = onPeriodFromMinuteChange,
                    isAm = periodFromIsAm,
                    onIsAmChange = onPeriodFromIsAmChange,
                    isLight = isLight
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "END TIME", color = palette.textTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                TimePickerControls(
                    hour = periodToHour,
                    onHourChange = onPeriodToHourChange,
                    minute = periodToMinute,
                    onMinuteChange = onPeriodToMinuteChange,
                    isAm = periodToIsAm,
                    onIsAmChange = onPeriodToIsAmChange,
                    isLight = isLight
                )
            }
        }
    }
}

@Composable
private fun TimePickerControls(
    hour: Int,
    onHourChange: (Int) -> Unit,
    minute: Int,
    onMinuteChange: (Int) -> Unit,
    isAm: Boolean,
    onIsAmChange: (Boolean) -> Unit,
    isLight: Boolean
) {
    val palette = LunaTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour Stepper
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x30FFFFFF))
                    .clickable { onHourChange(if (hour <= 1) 12 else hour - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "-", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(text = "${hour}h", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x30FFFFFF))
                    .clickable { onHourChange(if (hour >= 12) 1 else hour + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Minute Stepper
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x30FFFFFF))
                    .clickable { onMinuteChange(if (minute <= 0) 59 else minute - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "-", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(
                text = String.format(Locale.getDefault(), "%02dm", minute),
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color.White else Color(0x30FFFFFF))
                    .clickable { onMinuteChange((minute + 1) % 60) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = palette.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // AM / PM Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isAm) palette.accent else Color.Transparent)
                    .clickable { onIsAmChange(true) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "AM",
                    color = if (isAm) palette.chipTextSelected else palette.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isAm) palette.accent else Color.Transparent)
                    .clickable { onIsAmChange(false) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PM",
                    color = if (!isAm) palette.chipTextSelected else palette.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickDateChip(text: String, onClick: () -> Unit, isLight: Boolean) {
    val palette = LunaTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLight) Color(0x10000000) else Color(0x20FFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = palette.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit, isLight: Boolean) {
    val palette = LunaTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLight) Color.White else Color(0x28FFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = palette.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatFullDate(epochMillis: Long): String {
    return SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(epochMillis))
}

private fun formatShortDate(epochMillis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
}

private fun formatTime(hour: Int, minute: Int, isAm: Boolean): String {
    return "%02d:%02d %s".format(hour, minute, if (isAm) "AM" else "PM")
}
