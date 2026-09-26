package com.luna.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaBulkActionBar(
    selectedCount: Int,
    onCompleteSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSetDueDate: () -> Unit,
    onSetPriority: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors

    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        val isLight = palette.background.red > 0.5f
        val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD91C1C24).copy(alpha = 0.90f)
        val glassBorderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = if (isLight) {
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.15f)
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = if (isLight) Color(0x20000000) else Color(0x50000000),
                        spotColor = if (isLight) Color(0x30000000) else Color(0x60000000)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(glassFill)
                    .border(
                        width = 1.2.dp,
                        brush = glassBorderBrush,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cancel / Deselect
                BulkActionButton(
                    icon = Icons.Rounded.Close,
                    tint = palette.textSecondary,
                    contentDescription = "Deselect all tasks",
                    onClick = onClearSelection
                )

                // Count badge
                Text(
                    text = "$selectedCount selected",
                    color = palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Bulk Complete
                BulkActionButton(
                    icon = Icons.Rounded.CheckCircle,
                    tint = palette.textPrimary,
                    contentDescription = "Complete selected tasks",
                    onClick = onCompleteSelected
                )

                // Bulk Due Date
                BulkActionButton(
                    icon = Icons.Rounded.CalendarToday,
                    tint = palette.textPrimary,
                    contentDescription = "Set due date",
                    onClick = onSetDueDate
                )

                // Bulk Priority
                BulkActionButton(
                    icon = Icons.Rounded.Flag,
                    tint = palette.textPrimary,
                    contentDescription = "Set priority",
                    onClick = onSetPriority
                )

                // Bulk Delete
                BulkActionButton(
                    icon = Icons.Rounded.Delete,
                    tint = palette.textPrimary,
                    contentDescription = "Delete selected tasks",
                    onClick = onDeleteSelected
                )
            }
        }
    }
}

@Composable
private fun BulkActionButton(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    val palette = LunaTheme.colors

    Box(
        modifier = Modifier
            .size(38.dp)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(palette.surfaceVariant)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
