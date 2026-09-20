package com.luna.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object LunaMotion {
    // Tactile checkmark spring (bouncy and responsive)
    val checkBouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Smooth card entry/exit
    val cardTransition: SpringSpec<Float> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow
    )

    // Quick responsive button/chip press
    val pressSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 600f
    )

    val fadeQuick: TweenSpec<Float> = tween(
        durationMillis = 180,
        easing = FastOutSlowInEasing
    )
}
