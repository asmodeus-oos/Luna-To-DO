package com.luna.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luna.app.ui.theme.LunaTheme
import kotlinx.coroutines.launch

@Composable
fun LunaCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    enabled: Boolean = true
) {
    val scale = remember { Animatable(1f) }
    val checkProgress = remember { Animatable(if (checked) 1f else 0f) }
    val fillProgress = remember { Animatable(if (checked) 1f else 0f) }
    val rippleProgress = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val hasMounted = remember { mutableStateOf(false) }

    LaunchedEffect(checked) {
        if (!hasMounted.value) {
            hasMounted.value = true
            // If already checked on first mount, ensure full check state
            if (checked) {
                checkProgress.snapTo(1f)
                fillProgress.snapTo(1f)
            } else {
                checkProgress.snapTo(0f)
                fillProgress.snapTo(0f)
            }
            return@LaunchedEffect
        }

        if (checked) {
            // Snappy tactile scale bounce: contract to 0.78f, spring bounce to 1.15f, settle to 1.0f
            launch {
                scale.animateTo(
                    targetValue = 0.78f,
                    animationSpec = tween(durationMillis = 65, easing = FastOutSlowInEasing)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            // Ripple burst ring around checkbox
            launch {
                rippleAlpha.snapTo(0.45f)
                rippleProgress.snapTo(0f)
                launch {
                    rippleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    rippleAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                }
            }
            // Fill progress animation
            launch {
                fillProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )
            }
            // Animated checkmark stroke drawing
            launch {
                checkProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, delayMillis = 40, easing = FastOutSlowInEasing)
                )
            }
        } else {
            launch {
                scale.animateTo(
                    targetValue = 0.88f,
                    animationSpec = tween(durationMillis = 50)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                )
            }
            launch {
                checkProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 90)
                )
            }
            launch {
                fillProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 110)
                )
            }
        }
    }

    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size + 14.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size + 8.dp)) {
            val canvasSize = this.size
            val boxSize = size.toPx()
            val left = (canvasSize.width - boxSize) / 2f
            val top = (canvasSize.height - boxSize) / 2f
            val cornerRadius = CornerRadius(7.5.dp.toPx(), 7.5.dp.toPx())

            // Ripple glow ring on check
            if (rippleAlpha.value > 0f) {
                val rippleRadius = (boxSize / 2f) + (rippleProgress.value * 6.dp.toPx())
                drawCircle(
                    color = palette.accent.copy(alpha = rippleAlpha.value),
                    radius = rippleRadius,
                    center = Offset(canvasSize.width / 2f, canvasSize.height / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Squircle checkbox background
            val currentFill = fillProgress.value
            val currentBgColor = lerp(
                palette.checkboxUnchecked,
                palette.checkboxChecked,
                currentFill
            )

            drawRoundRect(
                color = currentBgColor,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = cornerRadius,
                style = Fill
            )

            // Crisp outline border when unchecked or transitioning
            if (currentFill < 0.98f) {
                val borderAlpha = (1f - currentFill).coerceIn(0f, 1f)
                val borderColor = palette.textSecondary.copy(alpha = 0.35f * borderAlpha)
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(left, top),
                    size = Size(boxSize, boxSize),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Draw checkmark
            if (checkProgress.value > 0.02f) {
                val checkPath = Path().apply {
                    moveTo(left + boxSize * 0.27f, top + boxSize * 0.50f)
                    lineTo(left + boxSize * 0.44f, top + boxSize * 0.67f)
                    lineTo(left + boxSize * 0.73f, top + boxSize * 0.33f)
                }

                val checkmarkColor = if (palette.checkboxChecked.luminance() > 0.5f && currentFill > 0.5f) {
                    Color(0xFF16181B)
                } else {
                    Color.White
                }

                if (checkProgress.value >= 0.98f) {
                    // Fully rendered checkmark for maximum crispness and zero clipping
                    drawPath(
                        path = checkPath,
                        color = checkmarkColor,
                        style = Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else {
                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(checkPath, false)
                    val totalLength = pathMeasure.length
                    val drawLength = totalLength * checkProgress.value

                    val subPath = Path()
                    pathMeasure.getSegment(0f, drawLength, subPath, true)

                    drawPath(
                        path = subPath,
                        color = checkmarkColor,
                        style = Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
