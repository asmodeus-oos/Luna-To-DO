package com.luna.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Brand accents
val LunaBlue = Color(0xFF008FFD)
val LunaBlueLight = Color(0xFF33A5FD)
val LunaBlueDark = Color(0xFF0073CC)

val LunaSuccess = Color(0xFF22C55E)
val LunaPurple = Color(0xFF8B5CF6)
val LunaCoral = Color(0xFFF97316)

// 3-Tier Palette Definitions
@Immutable
data class LunaColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color,
    val borderSubtle: Color,
    val accent: Color,
    val onAccent: Color,
    val checkboxUnchecked: Color,
    val checkboxChecked: Color,
    val chipBackground: Color,
    val chipSelected: Color,
    val chipTextSelected: Color,
    val danger: Color = Color(0xFFEF4444),
    val warning: Color = Color(0xFFF59E0B),
    val success: Color = Color(0xFF22C55E),
    val info: Color = Color(0xFF3B82F6)
)

val LightPalette = LunaColorPalette(
    background = Color(0xFFE8E8E8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDBDBDB),
    textPrimary = Color(0xFF18181B),
    textSecondary = Color(0xFF71717A),
    textTertiary = Color(0xFFA1A1AA),
    border = Color(0xFFD4D4D8),
    borderSubtle = Color.Transparent,
    accent = Color(0xFF18181B),
    onAccent = Color(0xFFFFFFFF),
    checkboxUnchecked = Color(0xFFE2E4E8),
    checkboxChecked = Color(0xFF18181B),
    chipBackground = Color(0xFFDBDBDB),
    chipSelected = Color(0xFF18181B),
    chipTextSelected = Color(0xFFFFFFFF),
    danger = Color(0xFFEF4444),
    warning = Color(0xFFF59E0B),
    success = Color(0xFF22C55E),
    info = Color(0xFF3B82F6)
)

val DarkPalette = LunaColorPalette(
    background = Color(0xFF121214),
    surface = Color(0xFF1C1C20),
    surfaceElevated = Color(0xFF242429),
    surfaceVariant = Color(0xFF2C2C33),
    textPrimary = Color(0xFFF4F4F5),
    textSecondary = Color(0xFFA1A1AA),
    textTertiary = Color(0xFF71717A),
    border = Color(0xFF27272A),
    borderSubtle = Color(0xFF202024),
    accent = Color(0xFFF4F4F5),
    onAccent = Color(0xFF121214),
    checkboxUnchecked = Color(0xFF2C2C33),
    checkboxChecked = Color(0xFFF4F4F5),
    chipBackground = Color(0xFF2C2C33),
    chipSelected = Color(0xFFF4F4F5),
    chipTextSelected = Color(0xFF121214),
    danger = Color(0xFFFF5252),
    warning = Color(0xFFFFB74D),
    success = Color(0xFF4ADE80),
    info = Color(0xFF60A5FA)
)

val BlackPalette = LunaColorPalette(
    background = Color(0xFF000000),
    surface = Color(0xFF0E0E10),
    surfaceElevated = Color(0xFF16161A),
    surfaceVariant = Color(0xFF202024),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    textTertiary = Color(0xFF71717A),
    border = Color(0xFF202024),
    borderSubtle = Color(0xFF141416),
    accent = Color(0xFFFFFFFF),
    onAccent = Color(0xFF000000),
    checkboxUnchecked = Color(0xFF202024),
    checkboxChecked = Color(0xFFFFFFFF),
    chipBackground = Color(0xFF202024),
    chipSelected = Color(0xFFFFFFFF),
    chipTextSelected = Color(0xFF000000),
    danger = Color(0xFFFF453A),
    warning = Color(0xFFFF9F0A),
    success = Color(0xFF30D158),
    info = Color(0xFF0A84FF)
)
