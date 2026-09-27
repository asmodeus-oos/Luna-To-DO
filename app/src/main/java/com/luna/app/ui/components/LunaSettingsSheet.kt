package com.luna.app.ui.components

import com.luna.app.data.LunaAttribution
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.luna.app.data.preferences.AppThemeMode
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunaSettingsSheet(
    themeMode: AppThemeMode,
    isAmoledEnabled: Boolean,
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    showTabLabels: Boolean,
    autoDeleteCompletedDaily: Boolean,
    userCountry: String,
    userTimezone: String,
    userName: String = "",
    userAvatarPath: String = "",
    userGender: String = "BOY",
    showCoverBannerText: Boolean = true,
    coverTitle: String = "Mountains",
    userCoverPath: String = "",
    onSetUserName: (String) -> Unit = {},
    onSetUserAvatarPath: (String) -> Unit = {},
    onSetUserGender: (String) -> Unit = {},
    onToggleShowCoverBannerText: (Boolean) -> Unit = {},
    onSetCoverTitle: (String) -> Unit = {},
    onSetUserCoverPath: (String) -> Unit = {},
    onToggleTheme: () -> Unit,
    onToggleAmoled: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleShowTabLabels: (Boolean) -> Unit,
    onToggleAutoDeleteDaily: (Boolean) -> Unit,
    onSelectCountry: (String) -> Unit,
    onSelectTimezone: (String) -> Unit,
    onClearCompleted: () -> Unit,
    onClearAllData: () -> Unit = {},
    onImportTimetable: () -> Unit = {},
    completedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val isDark = themeMode == AppThemeMode.DARK || themeMode == AppThemeMode.BLACK
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val file = java.io.File(context.filesDir, "user_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onSetUserAvatarPath(file.absolutePath)
            } catch (_: Exception) {}
        }
    }

    val coverPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val time = System.currentTimeMillis()
                val newCover = java.io.File(context.filesDir, "user_cover_$time.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    newCover.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (newCover.exists() && newCover.length() > 0) {
                    context.filesDir.listFiles()?.forEach { f ->
                        if (f.name.startsWith("user_cover") && f.name != newCover.name) {
                            try { f.delete() } catch (_: Exception) {}
                        }
                    }
                    onSetUserCoverPath(newCover.absolutePath)
                }
            } catch (_: Throwable) {}
        }
    }

    var isEditingName by remember { mutableStateOf(false) }
    var tempNameInput by remember(userName) { mutableStateOf(userName) }
    var isEditingCoverTitle by remember { mutableStateOf(false) }
    var tempCoverTitleInput by remember(coverTitle) { mutableStateOf(coverTitle) }
    var isClearedRecently by remember { mutableStateOf(false) }
    var isAllWipedRecently by remember { mutableStateOf(false) }
    var isTimetableImported by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    var showCountryPicker by remember { mutableStateOf(false) }
    var showTimezonePicker by remember { mutableStateOf(false) }

    val countriesList = remember {
        Locale.getISOCountries()
            .map { Locale("", it).displayCountry }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val timezonesList = remember {
        TimeZone.getAvailableIDs()
            .filter { it.contains("/") && !it.startsWith("Etc/") }
            .sorted()
    }

    val sheetBackground = if (isDark) Color(0xEE141418) else Color(0xF4F6F6F8)
    val sheetBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.35f else 0.85f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.20f),
            Color.Transparent
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.5.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x40FFFFFF) else Color(0x28000000))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = modifier.border(
            width = 1.dp,
            brush = sheetBorderBrush,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Apple Liquid Glass Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SETTINGS",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Preferences",
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Liquid Glass Circular Close Orb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = Color(0x15000000),
                            spotColor = Color(0x20000000)
                        )
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x28FFFFFF) else Color(0xDDFFFFFF))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = if (isDark) 0.45f else 0.95f),
                                    Color.White.copy(alpha = if (isDark) 0.10f else 0.25f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UntitledIcons.Close,
                        contentDescription = "Close settings",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // PROFILE & IDENTITY SECTION
            SectionHeader(title = "PROFILE & IDENTITY", icon = UntitledIcons.Sparkles)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatarView(
                            avatarPath = userAvatarPath,
                            displayName = userName,
                            size = 50.dp,
                            onClick = { photoLauncher.launch("image/*") }
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            if (isEditingName) {
                                BasicTextField(
                                    value = tempNameInput,
                                    onValueChange = { tempNameInput = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = palette.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    cursorBrush = SolidColor(palette.accent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isDark) Color(0x25FFFFFF) else Color(0x10000000), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            } else {
                                Text(
                                    text = userName.ifBlank { "Set your name" },
                                    color = palette.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap Photo or Edit to customize",
                                    color = palette.textTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEditingName) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.accent)
                                    .clickable {
                                        if (tempNameInput.isNotBlank()) {
                                            onSetUserName(tempNameInput.trim())
                                        }
                                        isEditingName = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    color = palette.chipTextSelected,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                    .clickable { isEditingName = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Edit",
                                    color = palette.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                    .clickable { photoLauncher.launch("image/*") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Photo",
                                    color = palette.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Gender Selection (Boy / Girl)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Gender",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Personalize your identity",
                            color = palette.textTertiary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isBoy = userGender.equals("BOY", ignoreCase = true)
                        // Boy Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBoy) palette.accent else if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                .border(1.dp, if (isBoy) palette.accent else palette.borderSubtle, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetUserGender("BOY")
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "👦", fontSize = 14.sp)
                                Text(
                                    text = "Boy",
                                    color = if (isBoy) palette.onAccent else palette.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isBoy) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        val isGirl = userGender.equals("GIRL", ignoreCase = true)
                        // Girl Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGirl) palette.accent else if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                .border(1.dp, if (isGirl) palette.accent else palette.borderSubtle, RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetUserGender("GIRL")
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "👧", fontSize = 14.sp)
                                Text(
                                    text = "Girl",
                                    color = if (isGirl) palette.onAccent else palette.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isGirl) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HOME COVER BANNER SECTION
            SectionHeader(title = "HOME COVER BANNER", icon = UntitledIcons.Sparkles)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                // Show / Hide Banner Title Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Banner Title",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (showCoverBannerText) "Title text is visible over the cover photo" else "Title text is hidden (clean landscape view)",
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = showCoverBannerText,
                        onCheckedChange = { isChecked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleShowCoverBannerText(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (isDark) Color(0xFF30D158) else Color(0xFF34C759),
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = if (isDark) Color(0xFFD4D4D8) else Color.White,
                            uncheckedTrackColor = if (isDark) Color(0xFF323238) else Color(0xFFE4E4E8),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Cover Title text edit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cover Title",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isEditingCoverTitle) {
                            Spacer(modifier = Modifier.height(6.dp))
                            BasicTextField(
                                value = tempCoverTitleInput,
                                onValueChange = { tempCoverTitleInput = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = palette.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(palette.accent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) Color(0x25FFFFFF) else Color(0x10000000), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        } else {
                            Text(
                                text = if (coverTitle.isBlank() || coverTitle == "Mountains") "Today's Date (Default)" else coverTitle,
                                color = palette.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    if (isEditingCoverTitle) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.accent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetCoverTitle(tempCoverTitleInput.trim().ifBlank { "" })
                                    isEditingCoverTitle = false
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Save",
                                color = palette.chipTextSelected,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                .clickable { isEditingCoverTitle = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Change",
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Cover Image actions: Choose Photo / Reset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cover Image",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (userCoverPath.isNotBlank()) "Custom photo active" else "Default Mountain wallpaper",
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (userCoverPath.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x20FFFFFF) else Color(0x10000000))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        try {
                                            context.filesDir.listFiles()?.forEach { f ->
                                                if (f.name.startsWith("user_cover")) {
                                                    f.delete()
                                                }
                                            }
                                        } catch (_: Exception) {}
                                        onSetUserCoverPath("")
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Reset",
                                    color = palette.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.accent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    coverPhotoLauncher.launch("image/*")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Choose Photo",
                                color = palette.chipTextSelected,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // APPEARANCE & THEME SECTION
            SectionHeader(title = "APPEARANCE & THEME", icon = UntitledIcons.Layers)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                // Dark / Light Mode Toggle
                SettingToggleRow(
                    icon = if (isDark) UntitledIcons.Moon else UntitledIcons.Sun,
                    iconTint = palette.textPrimary,
                    title = "Dark Mode",
                    subtitle = if (isDark) "Active: Moon appearance" else "Active: Sun appearance",
                    isChecked = isDark,
                    onCheckedChange = { onToggleTheme() }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                // AMOLED Mode Toggle
                SettingToggleRow(
                    icon = UntitledIcons.Moon,
                    iconTint = palette.textPrimary,
                    title = "AMOLED Mode",
                    subtitle = "Pitch-black #000000 background for OLED displays",
                    isChecked = isAmoledEnabled,
                    badge = "OLED",
                    onCheckedChange = { onToggleAmoled(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NAVIGATION & TABS SECTION
            SectionHeader(title = "NAVIGATION BAR", icon = UntitledIcons.Layers)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                SettingToggleRow(
                    icon = UntitledIcons.Layers,
                    iconTint = palette.textPrimary,
                    title = "Show Tab Labels",
                    subtitle = "Display text descriptions below bottom navigation icons",
                    isChecked = showTabLabels,
                    onCheckedChange = { onToggleShowTabLabels(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AUTOMATED DAILY MAINTENANCE SECTION
            SectionHeader(title = "AUTOMATED MAINTENANCE", icon = UntitledIcons.Clock)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                SettingActionRow(
                    icon = UntitledIcons.MapPin,
                    iconTint = palette.textPrimary,
                    title = "Country",
                    value = userCountry,
                    onClick = { showCountryPicker = true }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                SettingActionRow(
                    icon = UntitledIcons.Clock,
                    iconTint = palette.textPrimary,
                    title = "Timezone",
                    value = userTimezone,
                    onClick = { showTimezonePicker = true }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                SettingToggleRow(
                    icon = UntitledIcons.Trash,
                    iconTint = palette.textPrimary,
                    title = "Auto-Delete Daily",
                    subtitle = "Automatically clean yesterday's completed tasks at midnight in your timezone",
                    isChecked = autoDeleteCompletedDaily,
                    onCheckedChange = { onToggleAutoDeleteDaily(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACADEMIC TIMETABLE SECTION
            SectionHeader(title = "ACADEMIC TIMETABLE", icon = UntitledIcons.Calendar)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dentistry · Level 4 Timetable",
                                color = palette.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTimetableImported) "✓ 13 Sessions loaded (Lectures + Clinics)" else "Import Fall 2026-2027 weekly schedule (13 sessions, 20h)",
                                color = if (isTimetableImported) Color(0xFF10B981) else palette.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.accent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onImportTimetable()
                                    isTimetableImported = true
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isTimetableImported) "✓ Imported" else "Import Now",
                                color = palette.chipTextSelected,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DATA MANAGEMENT
            SectionHeader(title = "DATA MANAGEMENT", icon = UntitledIcons.Trash)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Option 1: Clear Completed Tasks Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isClearedRecently) "Completed Tasks: 0 / $totalCount" else "Completed Tasks: $completedCount / $totalCount",
                                color = palette.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isClearedRecently) "✓ Completed tasks cleaned up" else "Remove finished and archived tasks",
                                color = if (isClearedRecently) Color(0xFF10B981) else palette.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        val canClear = completedCount > 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (canClear) palette.surfaceVariant else palette.surfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    if (canClear) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onClearCompleted()
                                        isClearedRecently = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isClearedRecently) "✓ Cleared" else "Clear Done",
                                color = if (canClear) palette.textPrimary else palette.textTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = palette.borderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Option 2: Remove All Tasks, Routines & Habits (Full Wipe)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAllWipedRecently) "✓ All Data Cleared" else "Remove All Tasks & Routines",
                                color = if (isAllWipedRecently) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAllWipedRecently) "All tasks, routines, and habits deleted" else "Permanently wipe all active and completed tasks, routines & habits",
                                color = palette.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showWipeConfirmDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Remove All",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ABOUT
            SectionHeader(title = "ABOUT LUNA", icon = UntitledIcons.AlertCircle)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = LunaAttribution.APP_NAME,
                            color = palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "v${LunaAttribution.APP_VERSION}",
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LunaAttribution.APP_SUBTITLE,
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer & Legal Info Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "DEVELOPER & COPYRIGHT",
                                color = palette.textTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = LunaAttribution.AUTHOR_NAME,
                                color = palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = LunaAttribution.COPYRIGHT_NOTICE,
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showCountryPicker) {
        LunaItemSelectionModal(
            title = "Select Country",
            items = countriesList,
            selectedItem = userCountry,
            onItemSelected = {
                onSelectCountry(it)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    if (showTimezonePicker) {
        LunaItemSelectionModal(
            title = "Select Timezone",
            items = timezonesList,
            selectedItem = userTimezone,
            onItemSelected = {
                onSelectTimezone(it)
                showTimezonePicker = false
            },
            onDismiss = { showTimezonePicker = false }
        )
    }

    if (showWipeConfirmDialog) {
        Dialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showWipeConfirmDialog = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF1E1E26) else Color(0xFFFFFFFF))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = UntitledIcons.Trash,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Wipe All Tasks & Routines?",
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This will permanently remove all your tasks, subtasks, routines, and habits from the local database.\n\nThis action cannot be undone.",
                            color = palette.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.surfaceVariant)
                                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
                                    .clickable { showWipeConfirmDialog = false }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = palette.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFEF4444))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showWipeConfirmDialog = false
                                        isAllWipedRecently = true
                                        onClearAllData()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Wipe All",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LunaItemSelectionModal(
    title: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) items else items.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    val glassFill = if (isLight) Color(0xF8FFFFFF) else Color(0xF21C1C24)
    val glassBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.35f else 0.10f),
            Color.Transparent
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(480.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0x30000000),
                        spotColor = Color(0x50000000)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(glassFill)
                    .border(1.2.dp, glassBorder, RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = UntitledIcons.Close,
                                contentDescription = "Close",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search field
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(palette.accent),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isLight) Color(0x0C000000) else Color(0x18FFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = UntitledIcons.Search,
                                    contentDescription = null,
                                    tint = palette.textTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search...", color = palette.textTertiary, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Items List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredItems) { item ->
                            val isSelected = item == selectedItem
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) palette.accent.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable { onItemSelected(item) }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    color = if (isSelected) palette.accent else palette.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = UntitledIcons.Check,
                                        contentDescription = null,
                                        tint = palette.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLight) Color(0x12000000) else Color(0x20FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = palette.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = UntitledIcons.ChevronRight,
            contentDescription = null,
            tint = palette.textTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AppleGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f

    val cardBg = if (isDark) Color(0x28272730) else Color(0x75FFFFFF)
    val cardBorder = if (isDark) Color(0x25FFFFFF) else Color(0x60FFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0C000000),
                spotColor = Color(0x12000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    val palette = LunaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textTertiary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = title,
            color = palette.textTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null
) {
    val palette = LunaTheme.colors
    val isDark = palette.background.red < 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x60FFFFFF))
                    .border(
                        1.dp,
                        if (isDark) Color(0x18FFFFFF) else Color(0x40FFFFFF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.accent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                color = palette.chipTextSelected,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Color(0xFF30D158) else Color(0xFF34C759),
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = if (isDark) Color(0xFFD4D4D8) else Color.White,
                uncheckedTrackColor = if (isDark) Color(0xFF323238) else Color(0xFFE4E4E8),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
