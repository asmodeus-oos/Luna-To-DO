package com.luna.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.theme.LunaTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiParticle(
    val startXRatio: Float,
    val targetXRatio: Float,
    val startYRatio: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float
)

@Composable
fun LunaCelebrationOverlay(
    visible: Boolean,
    taskTitle: String,
    xpAwarded: Int = 50,
    timePerformanceText: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val palette = LunaTheme.colors
    val animProgress = remember { Animatable(0f) }
    val xpCounterAnim = remember { Animatable(0f) }

    // Generate randomized confetti particles
    val particles = remember {
        val colors = listOf(
            Color(0xFF008FFD), // Luna vibrant blue
            Color(0xFFF59E0B), // Amber gold
            Color(0xFF10B981), // Emerald green
            Color(0xFFEC4899), // Hot pink
            Color(0xFF8B5CF6), // Purple
            Color(0xFF3B82F6)  // Sky blue
        )
        List(60) {
            ConfettiParticle(
                startXRatio = Random.nextFloat(),
                targetXRatio = Random.nextFloat(),
                startYRatio = -0.1f - (Random.nextFloat() * 0.4f),
                speed = 0.8f + (Random.nextFloat() * 0.8f),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors[Random.nextInt(colors.size)],
                width = 8f + Random.nextFloat() * 8f,
                height = 14f + Random.nextFloat() * 12f
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            animProgress.snapTo(0f)
            xpCounterAnim.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            xpCounterAnim.animateTo(
                targetValue = xpAwarded.toFloat(),
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // 60FPS Native Particle Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = animProgress.value
            val canvasW = size.width
            val canvasH = size.height

            particles.forEach { p ->
                val currentY = (p.startYRatio + (progress * 1.3f * p.speed)) * canvasH
                val currentX = (p.startXRatio + ((p.targetXRatio - p.startXRatio) * progress * 0.3f)) * canvasW
                val rotation = progress * p.rotationSpeed
                val alpha = (1f - (progress * 0.9f)).coerceIn(0f, 1f)

                if (currentY in 0f..canvasH) {
                    rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
                        drawRoundRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(currentX, currentY),
                            size = Size(p.width.dp.toPx(), p.height.dp.toPx()),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }
        }

        // Celebration Card Modal
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(palette.surface)
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent dismiss inside card */ }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Party Popper Icon Burst
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(palette.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎉", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Congratulations!",
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = taskTitle,
                    color = palette.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                val encouragementMessage = remember(taskTitle) {
                    com.luna.app.data.LunaEncouragementMessages.getRandom()
                }

                // Random Dynamic Encouragement Quote
                Text(
                    text = "“$encouragementMessage”",
                    color = palette.textSecondary,
                    fontSize = 13.5.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // XP Earned Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${xpCounterAnim.value.toInt()} XP Earned",
                            color = Color(0xFFF59E0B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Time Performance message
                if (!timePerformanceText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = timePerformanceText,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dismiss / Continue Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.accent)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keep Momentum",
                        color = palette.onAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
