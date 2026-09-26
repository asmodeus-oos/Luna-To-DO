package com.luna.app.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.luna.app.R
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.io.File
import java.io.FileOutputStream

/**
 * 🏔️ Settable Cover Banner Card
 * Styled identically to the mountain cover card:
 * - Rounded rectangle banner with landscape cover image (defaults to Mountains)
 * - Centered title ("Mountains", or user-customizable), hideable via showTitle toggle
 * - Bottom-right circular dark translucent glass button to set any image the user likes
 */
@Composable
fun LunaCoverBannerCard(
    coverPath: String?,
    title: String = "Mountains",
    showTitle: Boolean = true,
    onSetCoverPath: (String) -> Unit,
    onSetTitle: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    var showTitleDialog by remember { mutableStateOf(false) }
    var tempTitleInput by remember(title) { mutableStateOf(title) }

    var coverVersion by remember { mutableIntStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val time = System.currentTimeMillis()
                val newCoverFile = File(context.filesDir, "user_cover_$time.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(newCoverFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (newCoverFile.exists() && newCoverFile.length() > 0) {
                    // Clean up any older cover photos to free storage
                    context.filesDir.listFiles()?.forEach { f ->
                        if (f.name.startsWith("user_cover") && f.name != newCoverFile.name) {
                            try { f.delete() } catch (_: Exception) {}
                        }
                    }
                    coverVersion++
                    onSetCoverPath(newCoverFile.absolutePath)
                }
            } catch (_: Throwable) {}
        }
    }

    val coverBitmap = remember(coverPath, coverVersion) {
        if (!coverPath.isNullOrBlank()) {
            try {
                val file = File(coverPath)
                if (file.exists() && file.length() > 0) {
                    // Safe downsampled decoding to handle camera/gallery photos without OOM
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, bounds)
                    if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                        var sampleSize = 1
                        val reqW = 1200
                        val reqH = 700
                        while ((bounds.outWidth / (sampleSize * 2)) >= reqW ||
                            (bounds.outHeight / (sampleSize * 2)) >= reqH
                        ) {
                            sampleSize *= 2
                        }
                        val decodeOpts = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                        }
                        BitmapFactory.decodeFile(file.absolutePath, decodeOpts)?.asImageBitmap()
                    } else null
                } else null
            } catch (_: Throwable) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 145.dp, max = 210.dp)
            .aspectRatio(16f / 7f)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (isLight) Color(0x15000000) else Color(0x40000000),
                spotColor = if (isLight) Color(0x25000000) else Color(0x60000000)
            )
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isLight) 0.35f else 0.20f),
                        Color.White.copy(alpha = if (isLight) 0.08f else 0.04f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        // Background Image: custom user photo or default Mountains landscape
        if (coverBitmap != null) {
            Image(
                bitmap = coverBitmap,
                contentDescription = "Cover Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_cover),
                contentDescription = "Default Mountain Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Cinematic glass vignette gradient for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Centered Title Text (tap to edit) with edit affordance
        if (showTitle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        tempTitleInput = title
                        showTitleDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = title.ifBlank { "Mountains" },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.80f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                            blurRadius = 12f
                        )
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = UntitledIcons.Edit,
                    contentDescription = "Edit cover title",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Bottom-Right Circular Dark Translucent Disc Button (Set button)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .size(50.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x40000000),
                    spotColor = Color(0x60000000)
                )
                .clip(CircleShape)
                .background(Color(0x9924201D)) // Dark frosted disc matching the screenshot
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.12f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    photoPickerLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = UntitledIcons.Sparkles,
                contentDescription = "Set Cover Image",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Modal: Edit Title or Reset Cover
    if (showTitleDialog) {
        Dialog(onDismissRequest = { showTitleDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isLight) Color(0xFFFFFFFF) else Color(0xFF1E1E24))
                    .border(
                        1.dp,
                        if (isLight) Color(0x20000000) else Color(0x25FFFFFF),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp)
            ) {
                Column {
                    Text(
                        text = "Customize Cover",
                        color = palette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set a custom title or change the cover photo.",
                        color = palette.textSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Input Box
                    BasicTextField(
                        value = tempTitleInput,
                        onValueChange = { tempTitleInput = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(palette.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLight) Color(0xFFF2F2F7) else Color(0xFF282830))
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset to Default Mountain button
                        if (!coverPath.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isLight) Color(0xFFE5E5EA) else Color(0xFF2C2C34))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        try {
                                            context.filesDir.listFiles()?.forEach { f ->
                                                if (f.name.startsWith("user_cover")) {
                                                    try { f.delete() } catch (_: Exception) {}
                                                }
                                            }
                                        } catch (_: Exception) {}
                                        coverVersion++
                                        onSetCoverPath("")
                                        showTitleDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Reset Photo",
                                    color = palette.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Save button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLight) Color(0xFF18181B) else Color.White)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetTitle(tempTitleInput.trim().ifBlank { "Mountains" })
                                    showTitleDialog = false
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save",
                                color = if (isLight) Color.White else Color(0xFF18181B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
