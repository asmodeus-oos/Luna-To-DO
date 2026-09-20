package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.domain.model.Priority
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaPriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    if (priority == Priority.NONE) return
    val palette = LunaTheme.colors

    val icon = when (priority) {
        Priority.P1 -> UntitledIcons.PriorityUrgent
        Priority.P2 -> UntitledIcons.PriorityHigh
        Priority.P3 -> UntitledIcons.PriorityMedium
        Priority.P4 -> UntitledIcons.PriorityLow
        Priority.NONE -> return
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (compact) priority.shortLabel else priority.label,
                color = palette.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
