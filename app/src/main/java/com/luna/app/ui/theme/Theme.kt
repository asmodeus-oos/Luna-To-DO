package com.luna.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.luna.app.data.preferences.AppThemeMode

val LocalLunaColors = staticCompositionLocalOf { LightPalette }

object LunaTheme {
    val colors: LunaColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalLunaColors.current
}

@Composable
fun LunaTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val palette = when (themeMode) {
        AppThemeMode.LIGHT -> LightPalette
        AppThemeMode.DARK -> DarkPalette
        AppThemeMode.BLACK -> BlackPalette
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkPalette else LightPalette
    }

    val isDarkAppearance = themeMode == AppThemeMode.DARK ||
            themeMode == AppThemeMode.BLACK ||
            (themeMode == AppThemeMode.SYSTEM && isSystemDark)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.decorView.setBackgroundColor(palette.background.toArgb())
                window.statusBarColor = palette.background.toArgb()
                window.navigationBarColor = palette.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkAppearance
                insetsController.isAppearanceLightNavigationBars = !isDarkAppearance
            }
        }
    }

    val materialColors = if (isDarkAppearance) {
        darkColorScheme(
            primary = palette.accent,
            background = palette.background,
            surface = palette.surface,
            onPrimary = palette.onAccent,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            background = palette.background,
            surface = palette.surface,
            onPrimary = palette.onAccent,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalLunaColors provides palette
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = LunaTypography,
            shapes = LunaShapes
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = palette.background
            ) {
                content()
            }
        }
    }
}
