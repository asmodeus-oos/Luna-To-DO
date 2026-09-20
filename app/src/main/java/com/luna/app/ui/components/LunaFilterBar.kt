package com.luna.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.domain.model.TaskFilter
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaFilterBar(
    selectedFilter: TaskFilter,
    taskCounts: Map<TaskFilter, Int>,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val count = taskCounts[filter] ?: 0

            LunaFilterChip(
                label = filter.displayName,
                count = count,
                isSelected = isSelected,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
fun LunaFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) palette.chipSelected else palette.chipBackground,
        animationSpec = tween(durationMillis = 180),
        label = "chipBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) palette.chipTextSelected else palette.textSecondary,
        animationSpec = tween(durationMillis = 180),
        label = "chipText"
    )

    val countBadgeBg by animateColorAsState(
        targetValue = if (isSelected) palette.chipTextSelected.copy(alpha = 0.2f) else palette.border,
        animationSpec = tween(durationMillis = 180),
        label = "badgeBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )

            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(countBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
