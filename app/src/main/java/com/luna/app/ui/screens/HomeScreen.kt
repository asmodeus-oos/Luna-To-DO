package com.luna.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.domain.model.AppViewMode
import com.luna.app.domain.model.Priority
import com.luna.app.domain.model.SmartFilter
import com.luna.app.ui.components.LunaBulkActionBar
import com.luna.app.ui.components.LunaCelebrationOverlay
import com.luna.app.ui.components.LunaCreateTaskSheet
import com.luna.app.ui.components.LunaDeleteUnfinishedDialog
import com.luna.app.ui.components.LunaHeader
import com.luna.app.ui.components.LunaLiquidGlassNavBar
import com.luna.app.ui.components.LunaNavTab
import com.luna.app.ui.components.LunaOverdueReasonDialog
import com.luna.app.ui.components.LunaSettingsSheet
import com.luna.app.ui.components.LunaSmartFilterBar
import com.luna.app.ui.components.LunaTaskDetailSheet
import com.luna.app.ui.components.LunaTaskItem
import com.luna.app.ui.components.LunaTemplatesSheet
import com.luna.app.ui.components.LunaViewSelector
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import com.luna.app.ui.viewmodel.TaskViewModel
import com.luna.app.ui.views.LunaAnalyticsView
import com.luna.app.ui.views.LunaBoardView
import com.luna.app.ui.views.LunaCalendarView
import com.luna.app.ui.views.LunaFocusView
import com.luna.app.ui.views.LunaGlobalSearchView
import com.luna.app.ui.views.LunaHabitsView
import com.luna.app.ui.views.LunaHomeDashboardView
import com.luna.app.ui.views.LunaMatrixView
import com.luna.app.ui.views.LunaProjectsView
import com.luna.app.ui.views.LunaRoutinesView
import com.luna.app.ui.views.LunaSettingsView
import com.luna.app.ui.views.LunaTimetableScheduleView
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val celebration by viewModel.celebration.collectAsState()
    val overdueTaskForReason by viewModel.overdueTaskForReason.collectAsState()
    val taskToDeleteWithReason by viewModel.taskToDeleteWithReason.collectAsState()
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current

    val listState = rememberLazyListState()
    var pendingScrollAnchor by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val pagerState = rememberPagerState(initialPage = 0) { LunaNavTab.entries.size }
    val coroutineScope = rememberCoroutineScope()
    var isSearchPageOpen by remember { mutableStateOf(false) }

    // Intercept back gesture when search overlay is active
    BackHandler(enabled = isSearchPageOpen) {
        isSearchPageOpen = false
    }

    LaunchedEffect(uiState.tasks) {
        pendingScrollAnchor?.let { (idx, off) ->
            if (listState.firstVisibleItemIndex != idx) {
                listState.scrollToItem(idx, off)
            }
            pendingScrollAnchor = null
        }
    }

    if (!uiState.isOnboardingCompleted) {
        LunaOnboardingScreen(
            initialName = uiState.userName,
            initialGender = uiState.userGender,
            initialCoverTitle = uiState.coverTitle,
            initialThemeMode = uiState.themeMode,
            onCompleteOnboarding = { name, coverTitle, gender, themeMode ->
                viewModel.completeOnboarding(name, coverTitle, gender, themeMode)
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.background)
        ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = palette.background,
            topBar = {
            if (!isSearchPageOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    LunaHeader(
                        currentTheme = uiState.themeMode,
                        isSoundEnabled = uiState.isSoundEnabled,
                        onToggleTheme = {
                            if (uiState.isHapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            viewModel.toggleTheme()
                        },
                        onToggleSound = {
                            if (uiState.isHapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            viewModel.toggleSound()
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
        ) {
            // Swipeable Horizontal Pager across tabs: HOME, TASKS, TIMERS, STATS
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) { page ->
                when (LunaNavTab.entries[page]) {
                    LunaNavTab.HOME -> {
                        LunaHomeDashboardView(
                            tasks = uiState.allTasks,
                            onToggleTask = { viewModel.toggleTask(it.task) },
                            onStartTask = { viewModel.startTask(it.task.id) },
                            onTaskClick = { viewModel.openTaskDetail(it) },
                            onTaskLongClick = { viewModel.toggleSelection(it.task.id) },
                            onQuickAddTask = { title -> viewModel.quickAddTask(title) },
                            onPromptOverdueReason = { viewModel.promptOverdueReason(it.task) },
                            onEditTask = { viewModel.openTaskDetail(it) },
                            onDuplicateTask = { viewModel.duplicateTask(it.task.id) },
                            onDeleteTask = { viewModel.requestDeleteTask(it.task) },
                            completingTaskIds = uiState.completingTaskIds,
                            userName = uiState.userName,
                            userAvatarPath = uiState.userAvatarPath,
                            onSetUserAvatarPath = { viewModel.setUserAvatarPath(it) },
                            userCoverPath = uiState.userCoverPath,
                            coverTitle = uiState.coverTitle,
                            showCoverBannerText = uiState.showCoverBannerText,
                            onSetUserCoverPath = { viewModel.setUserCoverPath(it) },
                            onSetCoverTitle = { viewModel.setCoverTitle(it) }
                        )
                    }
                    LunaNavTab.TASKS -> {
                        val tasksIsLight = palette.background.red > 0.5f
                        val tasksGlassFill = if (tasksIsLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD9242429).copy(alpha = 0.82f)
                        val tasksGlassBorder = Brush.verticalGradient(
                            colors = if (tasksIsLight) {
                                listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.15f))
                            } else {
                                listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.10f), Color.Transparent)
                            }
                        )

                        Column(modifier = Modifier.fillMaxSize()) {
                            // 1. View Mode Switcher Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = if (tasksIsLight) Color(0x10000000) else Color(0x30000000),
                                        spotColor = if (tasksIsLight) Color(0x15000000) else Color(0x40000000)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(tasksGlassFill)
                                    .border(
                                        width = 1.dp,
                                        brush = tasksGlassBorder,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                LunaViewSelector(
                                    selectedView = uiState.selectedViewMode,
                                    onViewSelected = { mode ->
                                        if (uiState.isHapticsEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        viewModel.selectViewMode(mode)
                                    }
                                )
                            }

                            if (uiState.selectedViewMode == AppViewMode.LIST) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    LunaSmartFilterBar(
                                        searchQuery = uiState.searchQuery,
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        selectedFilter = uiState.selectedSmartFilter,
                                        onFilterSelected = { filter ->
                                            if (uiState.isHapticsEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            viewModel.selectSmartFilter(filter)
                                        },
                                        counts = uiState.smartCounts,
                                        templates = uiState.templates,
                                        onOpenTemplates = {
                                            if (uiState.isHapticsEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            viewModel.openTemplatesSheet()
                                        },
                                        onSelectTemplate = { template ->
                                            viewModel.createFromTemplate(template)
                                        },
                                        onEditTemplate = { template ->
                                            viewModel.updateTemplate(template)
                                        },
                                        onDeleteTemplate = { id ->
                                            viewModel.deleteTemplate(id)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            when (uiState.selectedViewMode) {
                                AppViewMode.LIST -> {

                                    if (uiState.tasks.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            EmptyStateView(
                                                filter = uiState.selectedSmartFilter,
                                                actionLabel = if (uiState.selectedSmartFilter == SmartFilter.ALL || uiState.selectedSmartFilter == SmartFilter.TODAY) "+ Create Task" else "Show All Tasks",
                                                onAction = {
                                                    if (uiState.selectedSmartFilter == SmartFilter.ALL || uiState.selectedSmartFilter == SmartFilter.TODAY) {
                                                        viewModel.openCreateSheet()
                                                    } else {
                                                        viewModel.selectSmartFilter(SmartFilter.ALL)
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            state = listState,
                                            contentPadding = PaddingValues(
                                                start = 20.dp,
                                                end = 20.dp,
                                                top = 8.dp,
                                                bottom = 150.dp
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            item(key = "top_scroll_anchor") {
                                                Spacer(modifier = Modifier.height(0.dp))
                                            }

                                            if (uiState.selectedSmartFilter == SmartFilter.COMPLETED) {
                                                item(key = "clear_completed_header") {
                                                    ClearCompletedButton(
                                                        onClear = { viewModel.clearCompleted() }
                                                    )
                                                }
                                            }

                                            items(
                                                items = uiState.tasks,
                                                key = { it.task.id }
                                            ) { taskWithDetails ->
                                                val task = taskWithDetails.task
                                                val isSelected = uiState.selectedTaskIds.contains(task.id)
                                                val blockedByTitle = uiState.dependenciesMap[task.id]

                                                LunaTaskItem(
                                                    modifier = Modifier.animateItem(
                                                        fadeInSpec = tween(durationMillis = 200),
                                                        fadeOutSpec = tween(durationMillis = 150),
                                                        placementSpec = spring(
                                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                                            stiffness = Spring.StiffnessMediumLow
                                                        )
                                                    ),
                                                    taskWithDetails = taskWithDetails,
                                                    isSelectionMode = uiState.isSelectionMode,
                                                    isSelected = isSelected,
                                                    blockedByTaskTitle = blockedByTitle,
                                                    isCompleting = uiState.completingTaskIds.contains(task.id),
                                                    onToggleCompleted = {
                                                        if (uiState.isHapticsEnabled) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                        if (!task.isCompleted) {
                                                            val currentIndex = listState.firstVisibleItemIndex
                                                            val currentOffset = listState.firstVisibleItemScrollOffset
                                                            val taskIndex = uiState.tasks.indexOfFirst { it.task.id == task.id }
                                                            val targetIndex = if (taskIndex in 0 until currentIndex) {
                                                                (currentIndex - 1).coerceAtLeast(0)
                                                            } else {
                                                                currentIndex
                                                            }
                                                            pendingScrollAnchor = Pair(targetIndex, currentOffset)
                                                        }
                                                        viewModel.toggleTask(task)
                                                    },
                                                    onSelectToggle = {
                                                        if (uiState.isHapticsEnabled) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                        viewModel.toggleSelection(task.id)
                                                    },
                                                    onClick = {
                                                        if (uiState.isSelectionMode) {
                                                            viewModel.toggleSelection(task.id)
                                                        } else {
                                                            if (uiState.isHapticsEnabled) {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                            viewModel.openTaskDetail(taskWithDetails)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (uiState.isHapticsEnabled) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                        viewModel.toggleSelection(task.id)
                                                    },
                                                    onEdit = {
                                                        viewModel.openTaskDetail(taskWithDetails)
                                                    },
                                                    onDuplicate = {
                                                        viewModel.duplicateTask(task.id)
                                                    },
                                                    onDelete = {
                                                        viewModel.requestDeleteTask(task)
                                                    },
                                                    onStartTask = {
                                                        viewModel.startTask(task.id)
                                                    },
                                                    onPromptOverdueReason = {
                                                        viewModel.promptOverdueReason(task)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                AppViewMode.TIMETABLE -> {
                                    LunaTimetableScheduleView(
                                        tasks = uiState.allTasks,
                                        onTaskClick = { viewModel.openTaskDetail(it) },
                                        onSeedTimetable = { viewModel.injectDentalTimetable(overwrite = true) },
                                        onTestAlarm = { isStart -> viewModel.testTimetableAlarm(isStart) }
                                    )
                                }
                                AppViewMode.CALENDAR -> {
                                    LunaCalendarView(
                                        tasks = uiState.allTasks,
                                        onToggleCompleted = { viewModel.toggleTask(it.task) },
                                        onTaskClick = { viewModel.openTaskDetail(it) }
                                    )
                                }
                                AppViewMode.HABITS -> {
                                    LunaHabitsView(
                                        habits = uiState.habits,
                                        onToggleHabit = { viewModel.toggleHabit(it) },
                                        onAddHabit = { name, icon, colorHex, target, imgUri ->
                                            viewModel.addHabit(name, icon, colorHex, target, imgUri)
                                        },
                                        onUpdateHabit = { viewModel.updateHabit(it) },
                                        onDuplicateHabit = { viewModel.duplicateHabit(it) },
                                        onDeleteHabit = { id -> viewModel.deleteHabit(id) }
                                    )
                                }
                                AppViewMode.ROUTINES -> {
                                    LunaRoutinesView(
                                        routines = uiState.routines,
                                        onToggleRoutineCompleted = { viewModel.toggleRoutineCompletedToday(it) },
                                        onAddRoutine = { name, icon, time, steps -> viewModel.addRoutine(name, icon, time, steps) },
                                        onUpdateRoutine = { viewModel.updateRoutine(it) },
                                        onDuplicateRoutine = { viewModel.duplicateRoutine(it) },
                                        onDeleteRoutine = { viewModel.deleteRoutine(it) }
                                    )
                                }
                                AppViewMode.MATRIX -> {
                                    LunaMatrixView(
                                        tasks = uiState.allTasks,
                                        onToggleCompleted = { viewModel.toggleTask(it.task) },
                                        onTaskClick = { viewModel.openTaskDetail(it) }
                                    )
                                }
                            }
                        }
                    }
                    LunaNavTab.TIMERS -> {
                        LunaFocusView(
                            tasks = uiState.allTasks,
                            onLogTaskMinutes = { id, mins -> viewModel.logTaskMinutes(id, mins) },
                            onCompleteTask = { id -> viewModel.completeTask(id) },
                            workDurationSeconds = uiState.focusWorkSeconds,
                            shortBreakDurationSeconds = uiState.shortBreakSeconds,
                            longBreakDurationSeconds = uiState.longBreakSeconds,
                            onUpdateDurations = { w, s, l -> viewModel.setFocusDurations(w, s, l) }
                        )
                    }
                    LunaNavTab.STATS -> {
                        LunaAnalyticsView(
                            tasks = uiState.allTasks,
                            habits = uiState.habits,
                            goals = uiState.goals
                        )
                    }
                }
            }

            val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val floatingBottomMargin = maxOf(innerPadding.calculateBottomPadding(), navBarBottomInset).coerceAtLeast(14.dp) + 16.dp

            // Floating Action Buttons: Liquid Glass Settings FAB on top, Add Task FAB below
            if (!uiState.isSelectionMode && !isSearchPageOpen) {
                val isLight = palette.background.red > 0.5f
                val fabGlassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.90f) else Color(0xD9242429).copy(alpha = 0.85f)
                val fabBorderBrush = Brush.verticalGradient(
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

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = floatingBottomMargin + 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Settings Liquid Glass FAB (Apple Glass Orb, 56dp matching New Task FAB)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = if (isLight) Color(0x20000000) else Color(0x50000000),
                                spotColor = if (isLight) Color(0x30000000) else Color(0x60000000)
                            )
                            .clip(CircleShape)
                            .background(fabGlassFill)
                            .border(
                                width = 1.2.dp,
                                brush = fabBorderBrush,
                                shape = CircleShape
                            )
                            .clickable {
                                if (uiState.isHapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.openSettings()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = UntitledIcons.SlidersSettings,
                            contentDescription = "Settings",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Add Task Liquid Glass FAB (Apple Glass Orb, 56dp)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = if (isLight) Color(0x20000000) else Color(0x50000000),
                                spotColor = if (isLight) Color(0x30000000) else Color(0x60000000)
                            )
                            .clip(CircleShape)
                            .background(fabGlassFill)
                            .border(
                                width = 1.2.dp,
                                brush = fabBorderBrush,
                                shape = CircleShape
                            )
                            .clickable {
                                if (uiState.isHapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.openCreateSheet()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = UntitledIcons.Plus,
                            contentDescription = "New Task",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Floating Liquid Glass Navigation Bar (Smooth scrollPosition sync)
            if (!uiState.isSelectionMode && !isSearchPageOpen) {
                val currentTab = LunaNavTab.entries[pagerState.currentPage]
                val scrollPos = pagerState.currentPage + pagerState.currentPageOffsetFraction

                LunaLiquidGlassNavBar(
                    selectedTab = currentTab,
                    scrollPosition = scrollPos,
                    showTabLabels = uiState.showNavTabLabels,
                    onTabSelected = { tab ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    },
                    onSearchClick = {
                        isSearchPageOpen = true
                    },
                    onSearchLongClick = {
                        viewModel.openCreateSheet()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = floatingBottomMargin)
                )
            }

            // Bulk Action Bar when multi-selecting
            LunaBulkActionBar(
                selectedCount = uiState.selectedTaskIds.size,
                onCompleteSelected = { viewModel.bulkCompleteSelected() },
                onDeleteSelected = { viewModel.bulkDeleteSelected() },
                onSetDueDate = {
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                    }
                    viewModel.bulkSetDueDate(cal.timeInMillis)
                },
                onSetPriority = {
                    viewModel.bulkSetPriority(Priority.P1)
                },
                onClearSelection = { viewModel.clearSelection() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Create Task Sheet
            AnimatedVisibility(
                visible = uiState.isCreateSheetOpen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LunaCreateTaskSheet(
                    onDismiss = { viewModel.closeCreateSheet() },
                    onOpenTemplates = {
                        viewModel.closeCreateSheet()
                        viewModel.openTemplatesSheet()
                    },
                    onTaskCreated = { title, notes, subtitles, startDate, dueDate, dueTime, deadlineMode, durationValue, durationUnit, priority, tags, recurrence, subtasks, alarmOnStart, alarmOnFinish, weeklyDay, weeklyTime, place ->
                        viewModel.addTask(
                            title = title,
                            notes = notes,
                            subtitles = subtitles,
                            startDate = startDate,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            deadlineMode = deadlineMode,
                            durationValue = durationValue,
                            durationUnit = durationUnit,
                            priority = priority,
                            tags = tags,
                            recurrenceRule = recurrence,
                            subtasks = subtasks,
                            alarmOnStart = alarmOnStart,
                            alarmOnFinish = alarmOnFinish,
                            weeklyDay = weeklyDay,
                            weeklyTime = weeklyTime,
                            place = place
                        )
                    }
                )
            }

            // Task Detail & Edit Sheet
            val currentEditing = uiState.editingTask
            if (currentEditing != null) {
                LunaTaskDetailSheet(
                    taskWithDetails = currentEditing,
                    allTasks = uiState.rawTasks,
                    projects = uiState.projects,
                    onDismiss = { viewModel.closeTaskDetail() },
                    onSaveTask = { updated -> viewModel.updateTask(updated) },
                    onDeleteTask = { id ->
                        val t = uiState.rawTasks.find { it.id == id }
                        if (t != null) viewModel.requestDeleteTask(t) else viewModel.deleteTask(id)
                    },
                    onDuplicateTask = { id -> viewModel.duplicateTask(id) },
                    onSaveAsTemplate = { id, name -> viewModel.saveTaskAsTemplate(id, name) },
                    onAddSubtask = { taskId, title, section -> viewModel.addSubtask(taskId, title, section) },
                    onToggleSubtask = { subtask -> viewModel.toggleSubtask(subtask) },
                    onDeleteSubtask = { subtask -> viewModel.deleteSubtask(subtask) },
                    onLogMinutes = { id, mins -> viewModel.logTaskMinutes(id, mins) }
                )
            }

            // Templates Sheet
            if (uiState.isTemplatesSheetOpen) {
                LunaTemplatesSheet(
                    templates = uiState.templates,
                    onSelectTemplate = { template -> viewModel.createFromTemplate(template) },
                    onDeleteTemplate = { id -> viewModel.deleteTemplate(id) },
                    onEditTemplate = { template -> viewModel.updateTemplate(template) },
                    onDismiss = { viewModel.closeTemplatesSheet() }
                )
            }

            // Settings Sheet (invoked from Liquid Glass Settings FAB)
            if (uiState.isSettingsOpen) {
                LunaSettingsSheet(
                    themeMode = uiState.themeMode,
                    isAmoledEnabled = uiState.isAmoledEnabled,
                    isSoundEnabled = uiState.isSoundEnabled,
                    isHapticsEnabled = uiState.isHapticsEnabled,
                    showTabLabels = uiState.showNavTabLabels,
                    autoDeleteCompletedDaily = uiState.autoDeleteCompletedDaily,
                    userCountry = uiState.userCountry,
                    userTimezone = uiState.userTimezone,
                    userName = uiState.userName,
                    userAvatarPath = uiState.userAvatarPath,
                    userGender = uiState.userGender,
                    showCoverBannerText = uiState.showCoverBannerText,
                    coverTitle = uiState.coverTitle,
                    userCoverPath = uiState.userCoverPath,
                    onSetUserName = { viewModel.setUserName(it) },
                    onSetUserAvatarPath = { viewModel.setUserAvatarPath(it) },
                    onSetUserGender = { viewModel.setUserGender(it) },
                    onToggleShowCoverBannerText = { viewModel.setShowCoverBannerText(it) },
                    onSetCoverTitle = { viewModel.setCoverTitle(it) },
                    onSetUserCoverPath = { viewModel.setUserCoverPath(it) },
                    onToggleTheme = {
                        if (uiState.isHapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        viewModel.toggleTheme()
                    },
                    onToggleAmoled = { enabled ->
                        if (uiState.isHapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        viewModel.setAmoledEnabled(enabled)
                    },
                    onToggleSound = {
                        if (uiState.isHapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        viewModel.toggleSound()
                    },
                    onToggleHaptics = {
                        viewModel.toggleHaptics()
                    },
                    onToggleShowTabLabels = {
                        viewModel.setShowNavTabLabels(it)
                    },
                    onToggleAutoDeleteDaily = {
                        viewModel.setAutoDeleteCompletedDaily(it)
                    },
                    onSelectCountry = {
                        viewModel.setUserCountry(it)
                    },
                    onSelectTimezone = {
                        viewModel.setUserTimezone(it)
                    },
                    onClearCompleted = {
                        viewModel.clearCompleted()
                    },
                    onClearAllData = {
                        viewModel.clearAllData()
                    },
                    onImportTimetable = {
                        viewModel.injectDentalTimetable()
                    },
                    completedCount = uiState.smartCounts[SmartFilter.COMPLETED] ?: 0,
                    totalCount = uiState.rawTasks.size,
                    onDismiss = { viewModel.closeSettings() }
                )
            }

            // Celebration Modal Overlay (60fps Confetti + Fanfare + XP pill)
            celebration?.let { celeb ->
                LunaCelebrationOverlay(
                    visible = true,
                    taskTitle = celeb.task.title,
                    xpAwarded = celeb.xpAwarded,
                    timePerformanceText = celeb.timePerformanceText,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }

            // Overdue Accountability Reason Dialog
            overdueTaskForReason?.let { task ->
                LunaOverdueReasonDialog(
                    task = task,
                    onDismiss = { viewModel.dismissOverdueReason() },
                    onLogMissed = { cat, details -> viewModel.onLogMissed(task.id, cat, details) },
                    onReschedule = { cat, details, mins -> viewModel.onRescheduleTask(task.id, cat, details, mins) }
                )
            }

            // Unfinished Task Deletion Accountability Reason Dialog
            taskToDeleteWithReason?.let { task ->
                LunaDeleteUnfinishedDialog(
                    task = task,
                    onDismiss = { viewModel.dismissDeleteUnfinished() },
                    onConfirmDeleteWithReason = { category, details ->
                        viewModel.confirmDeleteUnfinished(task.id, category, details)
                    }
                )
            }
        }
    }

    // Dedicated Real-time Global Search Overlay View (Full screen, sits cleanly over top bar and pager)
    AnimatedVisibility(
        visible = isSearchPageOpen,
        enter = fadeIn(tween(180)) + slideInVertically(spring(dampingRatio = 0.8f)) { it / 6 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(180)) { it / 6 }
    ) {
        LunaGlobalSearchView(
            tasks = uiState.allTasks,
            onToggleTask = { viewModel.toggleTask(it) },
            onTaskClick = {
                viewModel.openTaskDetail(it)
            },
            onDismiss = { isSearchPageOpen = false }
        )
    }
}
}
}

@Composable
private fun EmptyStateView(
    filter: SmartFilter,
    onAction: () -> Unit,
    actionLabel: String,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = filter.icon, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                SmartFilter.ALL -> "No tasks yet"
                SmartFilter.TODAY -> "Nothing due today"
                SmartFilter.HIGH_PRIORITY -> "No high priority tasks"
                SmartFilter.COMPLETED -> "No completed tasks yet"
                else -> "No tasks found"
            },
            color = palette.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (filter) {
                SmartFilter.ALL -> "Tap below to create your first task"
                SmartFilter.TODAY -> "Enjoy your free time or add a new task"
                SmartFilter.HIGH_PRIORITY -> "Mark tasks with P1 to see them here"
                SmartFilter.COMPLETED -> "Tasks you complete will be archived here"
                else -> "Tasks matching this filter will appear here"
            },
            color = palette.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(palette.accent)
                .clickable(onClick = onAction)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                color = palette.onAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ClearCompletedButton(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.85f) else Color(0x22FFFFFF))
            .border(
                width = 0.8.dp,
                color = if (isLight) Color.White.copy(alpha = 0.90f) else Color(0x25FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClear)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = UntitledIcons.Trash,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Clear All Completed Tasks",
                color = palette.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
