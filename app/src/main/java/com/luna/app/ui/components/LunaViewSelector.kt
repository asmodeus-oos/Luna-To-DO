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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import com.luna.app.domain.model.AppViewMode
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaViewSelector(
    selectedView: AppViewMode,
    onViewSelected: (AppViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val scrollState = rememberScrollState()

    val isLight = palette.background.red > 0.5f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppViewMode.entries.forEach { mode ->
            val isSelected = mode == selectedView

            val unselectedBg = if (isLight) Color(0xFFE4E4E8) else Color(0x24FFFFFF)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) palette.accent else unselectedBg,
                animationSpec = tween(150),
                label = "viewBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) palette.chipTextSelected else (if (isLight) Color(0xFF18181B) else palette.textSecondary),
                animationSpec = tween(150),
                label = "viewText"
            )

            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(
                        width = 0.8.dp,
                        color = if (isSelected) Color.Transparent else (if (isLight) Color(0xFFC4C4C8) else Color(0x20FFFFFF)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onViewSelected(mode) }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${mode.icon} ${mode.label}",
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}
