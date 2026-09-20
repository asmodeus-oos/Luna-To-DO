package com.luna.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.theme.LunaTheme
import java.io.File

@Composable
fun UserAvatarView(
    avatarPath: String?,
    displayName: String,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f

    val bitmap = remember(avatarPath) {
        if (!avatarPath.isNullOrBlank()) {
            try {
                val file = File(avatarPath)
                if (file.exists() && file.length() > 0) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.50f else 0.85f),
            Color.White.copy(alpha = if (isDark) 0.10f else 0.25f)
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color(0x15000000),
                spotColor = Color(0x25000000)
            )
            .clip(CircleShape)
            .background(if (isDark) Color(0x2CFFFFFF) else Color(0xEEFFFFFF))
            .border(1.5.dp, borderBrush, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            val initial = displayName.trim().firstOrNull()?.uppercase() ?: "M"
            Text(
                text = initial,
                color = palette.textPrimary,
                fontSize = (size.value * 0.44f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
