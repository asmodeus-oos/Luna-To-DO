package com.luna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaTagChip(
    tag: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val palette = LunaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$tag",
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onRemove
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove tag",
                        tint = palette.textTertiary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
