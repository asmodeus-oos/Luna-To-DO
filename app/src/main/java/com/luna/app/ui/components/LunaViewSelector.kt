package com.luna.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppViewMode.entries.forEach { mode ->
            val isSelected = mode == selectedView

            val unselectedBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.85f) else Color(0x24FFFFFF)
            val selectedBg = palette.accent

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) selectedBg else unselectedBg,
                animationSpec = tween(150),
                label = "viewBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) palette.chipTextSelected else (if (isLight) Color(0xFF18181B) else palette.textSecondary),
                animationSpec = tween(150),
                label = "viewText"
            )

            val borderBrush = if (isSelected) {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color.Transparent
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        if (isLight) Color.White.copy(alpha = 0.90f) else Color(0x25FFFFFF),
                        if (isLight) Color.White.copy(alpha = 0.40f) else Color(0x10FFFFFF)
                    )
                )
            }

            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .border(
                        width = 0.8.dp,
                        brush = borderBrush,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onViewSelected(mode) }
                    )
                    .padding(horizontal = 11.dp, vertical = 6.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mode.icon,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = mode.label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
