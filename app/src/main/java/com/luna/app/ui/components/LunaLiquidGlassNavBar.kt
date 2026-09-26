package com.luna.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

enum class LunaNavTab {
    HOME,
    TASKS,
    TIMERS,
    STATS
}

/**
 * Luna Liquid Glass Floating Navigation Bar
 * Features:
 * - Liquid glass capsule holding 4 primary tabs: Home, Tasks, Timers, Stats
 * - Smooth sliding liquid bubble indicator that tracks scroll position with spring physics
 * - Configurable tab names toggle below icons
 * - Companion floating liquid glass Search orb on the right
 */
@Composable
fun LunaLiquidGlassNavBar(
    selectedTab: LunaNavTab,
    scrollPosition: Float = selectedTab.ordinal.toFloat(),
    onTabSelected: (LunaNavTab) -> Unit,
    onSearchClick: () -> Unit,
    onSearchLongClick: (() -> Unit)? = null,
    showTabLabels: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current

    // Glass material tints based on light / dark mode
    val isLight = palette.background.red > 0.5f // Light mode background is #E8E8E8
    val glassFill = if (isLight) {
        Color(0xF0FFFFFF).copy(alpha = 0.76f)
    } else {
        Color(0xD91C1C24).copy(alpha = 0.80f)
    }

    val glassBorderBrush = Brush.linearGradient(
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

    val shadowColor = if (isLight) Color(0x18000000) else Color(0x50000000)
    val capsuleHeight = if (showTabLabels) 64.dp else 60.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Main Glass Capsule (Holds 4 tabs: Home, Tasks, Timers, Stats)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(capsuleHeight)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(RoundedCornerShape(32.dp))
                .background(glassFill)
                .border(
                    width = 1.2.dp,
                    brush = glassBorderBrush,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val totalWidth = maxWidth
                val tabCount = 4
                val isHomeSelected = selectedTab == LunaNavTab.HOME
                val isTasksSelected = selectedTab == LunaNavTab.TASKS
                val isTimersSelected = selectedTab == LunaNavTab.TIMERS
                val isStatsSelected = selectedTab == LunaNavTab.STATS

                val animatedIndex by animateFloatAsState(
                    targetValue = scrollPosition,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "liquid_bubble_pos"
                )

                val tabWidth = totalWidth / tabCount
                val bubbleWidth = tabWidth - 2.dp
                val bubbleHeight = maxHeight - 2.dp
                val bubbleOffsetX = (tabWidth * animatedIndex) + 1.dp

                val bubbleBrush = if (isLight) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF242429),
                            Color(0xFF141416)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x38FFFFFF),
                            Color(0x18FFFFFF)
                        )
                    )
                }

                val bubbleBorderBrush = if (isLight) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                }

                // Sliding Liquid Bubble
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = bubbleOffsetX.roundToPx(), y = 1.dp.roundToPx()) }
                        .width(bubbleWidth)
                        .height(bubbleHeight)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = if (isLight) Color(0x30000000) else Color(0x20000000),
                            spotColor = if (isLight) Color(0x40000000) else Color(0x30000000)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(bubbleBrush)
                        .border(
                            width = 1.dp,
                            brush = bubbleBorderBrush,
                            shape = RoundedCornerShape(26.dp)
                        )
                )

                // 4 Interactive Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarTabItem(
                        icon = UntitledIcons.EstateHome,
                        label = "Home",
                        showLabel = showTabLabels,
                        isSelected = isHomeSelected,
                        isLight = isLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(LunaNavTab.HOME)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NavBarTabItem(
                        icon = UntitledIcons.ClipboardNotes,
                        label = "Tasks",
                        showLabel = showTabLabels,
                        isSelected = isTasksSelected,
                        isLight = isLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(LunaNavTab.TASKS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NavBarTabItem(
                        icon = UntitledIcons.StopwatchTab,
                        label = "Time",
                        showLabel = showTabLabels,
                        isSelected = isTimersSelected,
                        isLight = isLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(LunaNavTab.TIMERS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    NavBarTabItem(
                        icon = UntitledIcons.Target,
                        label = "Stats",
                        showLabel = showTabLabels,
                        isSelected = isStatsSelected,
                        isLight = isLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(LunaNavTab.STATS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Single Companion Liquid Glass Search Orb (56.dp)
        // Seamlessly aligned horizontally with the capsule
        GlassOrbButton(
            icon = UntitledIcons.Search,
            contentDescription = "Search",
            glassFill = glassFill,
            glassBorderBrush = glassBorderBrush,
            shadowColor = shadowColor,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSearchClick()
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSearchLongClick?.invoke()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GlassOrbButton(
    icon: ImageVector,
    contentDescription: String,
    glassFill: Color,
    glassBorderBrush: Brush,
    shadowColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(CircleShape)
            .background(glassFill)
            .border(
                width = 1.2.dp,
                brush = glassBorderBrush,
                shape = CircleShape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.textPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun NavBarTabItem(
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
    isSelected: Boolean,
    isLight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_icon_scale"
    )

    val activeColor = Color.White
    val inactiveColor = palette.textSecondary

    val itemTint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "tab_item_tint"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = if (isSelected) 10.dp else 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = itemTint,
                modifier = Modifier
                    .size(18.dp)
                    .scale(iconScale)
            )
            if (isSelected || showLabel) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = itemTint,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
