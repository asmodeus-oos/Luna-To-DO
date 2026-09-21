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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.RecurrenceType
import com.luna.app.domain.parser.NaturalLanguageTaskParser
import com.luna.app.ui.theme.LunaTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LunaCreateTaskSheet(
    onDismiss: () -> Unit,
    onOpenTemplates: () -> Unit,
    onTaskCreated: (
        title: String,
        notes: String?,
        subtitles: List<String>,
        startDate: Long?,
        dueDate: Long?,
        dueTime: String?,
        deadlineMode: String,
        durationValue: Int,
        durationUnit: String,
        priority: Priority,
        tags: List<String>,
        recurrenceRule: String,
        subtasks: List<String>,
        alarmOnStart: Boolean,
        alarmOnFinish: Boolean,
        weeklyDay: String?,
        weeklyTime: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    var rawInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.NONE) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var deadlineMode by remember { mutableStateOf("FIXED") } // "FIXED" or "DURATION"
    var durationDays by remember { mutableIntStateOf(0) }
    var durationHours by remember { mutableIntStateOf(1) }
    var durationMinutes by remember { mutableIntStateOf(0) }
    var subtitles by remember { mutableStateOf<List<String>>(emptyList()) }
    var newSubtitleInput by remember { mutableStateOf("") }
    var isAddingSubtitle by remember { mutableStateOf(false) }
    var subtasks by remember { mutableStateOf<List<String>>(emptyList()) }
    var newSubtaskInput by remember { mutableStateOf("") }

    // Alarm toggles
    var alarmOnStart by remember { mutableStateOf(false) }
    var alarmOnFinish by remember { mutableStateOf(false) }

    // Weekly day/time
    var weeklyDay by remember { mutableStateOf<String?>(null) }
    var weeklyTimeHour by remember { mutableIntStateOf(9) }
    var weeklyTimeMinute by remember { mutableIntStateOf(0) }
    var weeklyTimeIsAm by remember { mutableStateOf(true) }

    // Fixed Date/Time Options
    var isDateRangeMode by remember { mutableStateOf(false) }
    var singleDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }

    var isTimePeriodMode by remember { mutableStateOf(false) }
    var hasExactTime by remember { mutableStateOf(false) }
    var exactHour by remember { mutableIntStateOf(9) }
    var exactMinute by remember { mutableIntStateOf(0) }
    var exactIsAm by remember { mutableStateOf(true) }

    var hasPeriodTime by remember { mutableStateOf(false) }
    var periodFromHour by remember { mutableIntStateOf(9) }
    var periodFromMinute by remember { mutableIntStateOf(0) }
    var periodFromIsAm by remember { mutableStateOf(true) }
    var periodToHour by remember { mutableIntStateOf(5) }
    var periodToMinute by remember { mutableIntStateOf(0) }
    var periodToIsAm by remember { mutableStateOf(false) }

    // Live natural language parsing of input!
    val parsed = remember(rawInput) {
        NaturalLanguageTaskParser.parse(rawInput)
    }

    LaunchedEffect(parsed.dueDate) {
        parsed.dueDate?.let { singleDateMillis = it }
    }
    LaunchedEffect(parsed.dueTime) {
        if (parsed.dueTime != null) {
            hasExactTime = true
        }
    }

    // Effective priority & tags combined from manual + parsed
    val effectivePriority = if (parsed.priority != Priority.NONE) parsed.priority else selectedPriority

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isDark = palette.background.red < 0.5f
    val sheetBackground = if (isDark) Color(0xEE141418) else Color(0xF4F6F6F8)
    val sheetBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.35f else 0.85f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.20f),
            Color.Transparent
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.5.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x40FFFFFF) else Color(0x28000000))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = modifier.border(
            width = 1.dp,
            brush = sheetBorderBrush,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Task",
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Templates button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceVariant)
                            .clickable(onClick = onOpenTemplates),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = "Templates",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceVariant)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Task Input with natural language hint
            BasicTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (rawInput.isEmpty()) {
                        Text(
                            text = "e.g. Call John tomorrow 3pm #work p1",
                            color = palette.textTertiary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            )

            // Live Natural Language Parsed Chips
            AnimatedVisibility(
                visible = parsed.dueDate != null || parsed.tags.isNotEmpty() || parsed.priority != Priority.NONE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUTO-PARSED",
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (parsed.dueDate != null) {
                            val timePart = if (parsed.dueTime != null) " at ${parsed.dueTime}" else ""
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.accent.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "📅 Due: ${formatParsedDate(parsed.dueDate)}$timePart",
                                    color = palette.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (parsed.priority != Priority.NONE) {
                            LunaPriorityBadge(priority = parsed.priority)
                        }
                        parsed.tags.forEach { tag ->
                            LunaTagChip(tag = tag)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Notes Input
            BasicTextField(
                value = notes,
                onValueChange = { notes = it },
                textStyle = TextStyle(
                    color = palette.textSecondary,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (notes.isEmpty()) {
                        Text(
                            text = "Add notes or markdown description (optional)",
                            color = palette.textTertiary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Spacer(modifier = Modifier.height(18.dp))

            // Deadline System Mode (choose one mode per task)
            Text(
                text = "DEADLINE SYSTEM",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fixed Date/Time Mode
                val isFixed = deadlineMode == "FIXED"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFixed) palette.surface else Color.Transparent)
                        .clickable { deadlineMode = "FIXED" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅 Fixed Date/Time",
                        color = if (isFixed) palette.textPrimary else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Duration-based Countdown Mode
                val isDuration = deadlineMode == "DURATION"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDuration) palette.surface else Color.Transparent)
                        .clickable { deadlineMode = "DURATION" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Duration Countdown",
                        color = if (isDuration) palette.accent else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isDuration) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Fixed Date/Time Options
            if (deadlineMode == "FIXED") {
                Spacer(modifier = Modifier.height(10.dp))
                LunaFixedDateTimePicker(
                    isDateRangeMode = isDateRangeMode,
                    onDateRangeModeChange = { isDateRangeMode = it },
                    singleDateMillis = singleDateMillis,
                    onSingleDateChange = { singleDateMillis = it },
                    startDateMillis = startDateMillis,
                    onStartDateChange = { startDateMillis = it },
                    endDateMillis = endDateMillis,
                    onEndDateChange = { endDateMillis = it },
                    isTimePeriodMode = isTimePeriodMode,
                    onTimePeriodModeChange = { isTimePeriodMode = it },
                    hasExactTime = hasExactTime,
                    onHasExactTimeChange = { hasExactTime = it },
                    exactHour = exactHour,
                    onExactHourChange = { exactHour = it },
                    exactMinute = exactMinute,
                    onExactMinuteChange = { exactMinute = it },
                    exactIsAm = exactIsAm,
                    onExactIsAmChange = { exactIsAm = it },
                    hasPeriodTime = hasPeriodTime,
                    onHasPeriodTimeChange = { hasPeriodTime = it },
                    periodFromHour = periodFromHour,
                    onPeriodFromHourChange = { periodFromHour = it },
                    periodFromMinute = periodFromMinute,
                    onPeriodFromMinuteChange = { periodFromMinute = it },
                    periodFromIsAm = periodFromIsAm,
                    onPeriodFromIsAmChange = { periodFromIsAm = it },
                    periodToHour = periodToHour,
                    onPeriodToHourChange = { periodToHour = it },
                    periodToMinute = periodToMinute,
                    onPeriodToMinuteChange = { periodToMinute = it },
                    periodToIsAm = periodToIsAm,
                    onPeriodToIsAmChange = { periodToIsAm = it }
                )
            }

            // Duration Countdown Options (when DURATION is selected)
            if (deadlineMode == "DURATION") {
                Spacer(modifier = Modifier.height(8.dp))
                LunaDurationPicker(
                    days = durationDays,
                    hours = durationHours,
                    minutes = durationMinutes,
                    onDaysChange = { durationDays = it },
                    onHoursChange = { durationHours = it },
                    onMinutesChange = { durationMinutes = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitles & Section Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUBTITLES (SECTIONS)",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "+ Add Section",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { isAddingSubtitle = !isAddingSubtitle }
                        .padding(4.dp)
                )
            }

            if (subtitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subtitles.forEach { sub ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📁 $sub", color = palette.textPrimary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove",
                                    tint = palette.textTertiary,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { subtitles = subtitles.filter { it != sub } }
                                )
                            }
                        }
                    }
                }
            }

            if (isAddingSubtitle) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newSubtitleInput,
                        onValueChange = { newSubtitleInput = it },
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 12.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newSubtitleInput.isNotBlank()) {
                                    subtitles = subtitles + newSubtitleInput.trim()
                                    newSubtitleInput = ""
                                    isAddingSubtitle = false
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (newSubtitleInput.isEmpty()) {
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
                                if (newSubtitleInput.isNotBlank()) {
                                    subtitles = subtitles + newSubtitleInput.trim()
                                    newSubtitleInput = ""
                                    isAddingSubtitle = false
                                }
                            }
                            .padding(horizontal = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subtasks preview & quick add
            if (subtasks.isNotEmpty()) {
                Text(
                    text = "CHECKLIST ITEMS",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                subtasks.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "•", color = palette.accent, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove",
                            tint = palette.textTertiary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    subtasks = subtasks.filterIndexed { i, _ -> i != index }
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick add subtask field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = newSubtaskInput,
                    onValueChange = { newSubtaskInput = it },
                    textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newSubtaskInput.isNotBlank()) {
                                subtasks = subtasks + newSubtaskInput.trim()
                                newSubtaskInput = ""
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newSubtaskInput.isEmpty()) {
                            Text("+ Add checklist subtask...", color = palette.textTertiary, fontSize = 12.sp)
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Priority Chips
            Text(
                text = "PRIORITY",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Priority.entries.forEach { p ->
                    val isSelected = p == effectivePriority
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    if (p == Priority.NONE) palette.surfaceVariant else p.color.copy(alpha = 0.15f)
                                } else {
                                    palette.surfaceVariant.copy(alpha = 0.4f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    if (p == Priority.NONE) palette.border else p.color
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPriority = p }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (p == Priority.NONE) "None" else p.shortLabel,
                            color = if (isSelected) {
                                if (p == Priority.NONE) palette.textPrimary else p.color
                            } else {
                                palette.textSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recurrence Selector
            Text(
                text = "REPEATING TASK",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecurrenceType.entries.forEach { rec ->
                    val isSelected = rec == selectedRecurrence
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) palette.accent.copy(alpha = 0.15f)
                                else palette.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.accent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedRecurrence = rec }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (rec == RecurrenceType.NONE) "Does Not Repeat" else rec.label,
                            color = if (isSelected) palette.accent else palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Weekly Day & Time Picker (shown when WEEKLY is selected)
            if (selectedRecurrence == RecurrenceType.WEEKLY) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "REPEAT ON DAY & TIME",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Day of week chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { index, day ->
                        val isSelected = weeklyDay == day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) palette.accent.copy(alpha = 0.15f)
                                    else palette.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) palette.accent else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { weeklyDay = if (isSelected) null else day }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = dayLabels[index],
                                color = if (isSelected) palette.accent else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                // Time picker for weekly recurrence
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏰",
                        fontSize = 16.sp
                    )
                    // Hour stepper
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { weeklyTimeHour = if (weeklyTimeHour >= 12) 1 else weeklyTimeHour + 1 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "%02d".format(weeklyTimeHour),
                            color = palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = ":", color = palette.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    // Minute stepper
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { weeklyTimeMinute = (weeklyTimeMinute + 5) % 60 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "%02d".format(weeklyTimeMinute),
                            color = palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // AM/PM toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (weeklyTimeIsAm) palette.accent.copy(alpha = 0.15f) else palette.surfaceVariant)
                            .border(1.dp, if (weeklyTimeIsAm) palette.accent else palette.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { weeklyTimeIsAm = !weeklyTimeIsAm }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (weeklyTimeIsAm) "AM" else "PM",
                            color = if (weeklyTimeIsAm) palette.accent else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Toggles
            Text(
                text = "ALARMS",
                color = palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Alarm on Start toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (alarmOnStart) palette.accent.copy(alpha = 0.12f)
                            else palette.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (alarmOnStart) palette.accent.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { alarmOnStart = !alarmOnStart }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔔 Alarm when task starts",
                        color = if (alarmOnStart) palette.accent else palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (alarmOnStart) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (alarmOnStart) palette.accent else palette.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(20.dp)
                                .align(if (alarmOnStart) Alignment.CenterEnd else Alignment.CenterStart)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                // Alarm on Finish toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (alarmOnFinish) palette.accent.copy(alpha = 0.12f)
                            else palette.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (alarmOnFinish) palette.accent.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { alarmOnFinish = !alarmOnFinish }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔔 Alarm when task finishes",
                        color = if (alarmOnFinish) palette.accent else palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (alarmOnFinish) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (alarmOnFinish) palette.accent else palette.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(20.dp)
                                .align(if (alarmOnFinish) Alignment.CenterEnd else Alignment.CenterStart)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Create Task Pill Button
            val isEnabled = rawInput.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isEnabled) palette.accent else palette.surfaceVariant)
                    .clickable(
                        enabled = isEnabled,
                        onClick = {
                            val finalTitle = parsed.title.ifBlank { rawInput.trim() }
                            val totalMinutes = durationDays * 24 * 60 + durationHours * 60 + durationMinutes
                            val (effDurationValue, effDurationUnit) = when {
                                durationDays > 0 && durationHours == 0 && durationMinutes == 0 -> Pair(durationDays, "DAYS")
                                durationDays == 0 && durationHours > 0 && durationMinutes == 0 -> Pair(durationHours, "HOURS")
                                durationDays == 0 && durationHours == 0 && durationMinutes > 0 -> Pair(durationMinutes, "MINUTES")
                                else -> Pair(totalMinutes.coerceAtLeast(1), "MINUTES")
                            }
                            val effStartDate = if (deadlineMode == "FIXED" && isDateRangeMode) startDateMillis else null
                            val effDueDate = if (deadlineMode == "FIXED") {
                                if (isDateRangeMode) endDateMillis else singleDateMillis
                            } else null
                            val effDueTime = if (deadlineMode == "FIXED") {
                                when {
                                    isTimePeriodMode && hasPeriodTime -> "%02d:%02d %s - %02d:%02d %s".format(periodFromHour, periodFromMinute, if (periodFromIsAm) "AM" else "PM", periodToHour, periodToMinute, if (periodToIsAm) "AM" else "PM")
                                    !isTimePeriodMode && hasExactTime -> "%02d:%02d %s".format(exactHour, exactMinute, if (exactIsAm) "AM" else "PM")
                                    else -> parsed.dueTime
                                }
                            } else null

                            onTaskCreated(
                                finalTitle,
                                notes.ifBlank { null },
                                subtitles,
                                effStartDate,
                                effDueDate,
                                effDueTime,
                                deadlineMode,
                                effDurationValue,
                                effDurationUnit,
                                effectivePriority,
                                parsed.tags,
                                selectedRecurrence.name,
                                subtasks,
                                alarmOnStart,
                                alarmOnFinish,
                                if (selectedRecurrence == RecurrenceType.WEEKLY) weeklyDay else null,
                                if (selectedRecurrence == RecurrenceType.WEEKLY) String.format(java.util.Locale.US, "%02d:%02d %s", if (weeklyTimeHour == 0) 12 else if (weeklyTimeHour > 12) weeklyTimeHour - 12 else weeklyTimeHour, weeklyTimeMinute, if (weeklyTimeIsAm) "AM" else "PM") else null
                            )
                            onDismiss()
                        }
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = if (isEnabled) palette.onAccent else palette.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Task",
                        color = if (isEnabled) palette.onAccent else palette.textTertiary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatParsedDate(epochMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    return when {
        isSameDay(cal, today) -> "Today"
        isSameDay(cal, tomorrow) -> "Tomorrow"
        else -> java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(epochMillis))
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
