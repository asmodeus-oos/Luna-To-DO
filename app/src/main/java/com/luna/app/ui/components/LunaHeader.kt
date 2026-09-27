package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.luna.app.ui.icons.UntitledIcons
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.preferences.AppThemeMode
import com.luna.app.ui.theme.LunaTheme

import androidx.compose.foundation.layout.width

@Composable
fun LunaHeader(
    currentTheme: AppThemeMode,
    isSoundEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD9242429).copy(alpha = 0.82f)
    val glassBorderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.15f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.30f),
                Color.White.copy(alpha = 0.10f),
                Color.Transparent
            )
        }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .shadow(
                elevation = 8.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                ambientColor = if (isLight) Color(0x12000000) else Color(0x40000000),
                spotColor = if (isLight) Color(0x18000000) else Color(0x50000000)
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(glassFill)
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.luna.app.R.drawable.ic_main_icon_logo_removebg_black),
                    contentDescription = "Luna Logo",
                    colorFilter = if (!isLight) androidx.compose.ui.graphics.ColorFilter.tint(palette.textPrimary) else null,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Luna",
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound feedback toggle (Linear Volume icon)
                HeaderIconButton(
                    onClick = onToggleSound,
                    icon = {
                        Icon(
                            imageVector = if (isSoundEnabled) UntitledIcons.Volume2 else UntitledIcons.VolumeX,
                            contentDescription = "Toggle sound",
                            tint = if (isSoundEnabled) palette.accent else palette.textTertiary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                )

                // Theme toggle: Moon for Dark/Amoled, Sun for Light (Linear Sun & Moon icons)
                val isDark = currentTheme == AppThemeMode.DARK || currentTheme == AppThemeMode.BLACK
                HeaderIconButton(
                    onClick = onToggleTheme,
                    icon = {
                        Icon(
                            imageVector = if (isDark) UntitledIcons.Moon else UntitledIcons.Sun,
                            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val palette = LunaTheme.colors

    val isLight = palette.background.red > 0.5f
    val glassFill = if (isLight) Color(0xF2FFFFFF).copy(alpha = 0.85f) else Color(0x28FFFFFF)
    val borderBrush = Brush.verticalGradient(
        colors = if (isLight) {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.10f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.10f),
                Color.Transparent
            )
        }
    )

    Box(
        modifier = Modifier
            .size(34.dp)
            .minimumInteractiveComponentSize()
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = if (isLight) Color(0x15000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x20000000) else Color(0x40000000)
            )
            .clip(CircleShape)
            .background(glassFill)
            .border(
                width = 0.8.dp,
                brush = borderBrush,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
