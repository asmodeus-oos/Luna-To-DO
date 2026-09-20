package com.luna.app.ui.views

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.preferences.AppThemeMode
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme
import java.util.Locale
import java.util.TimeZone

/**
 * Luna Liquid Glass Settings & Preferences Screen
 * Integrated as a full primary navigation tab.
 */
@Composable
fun LunaSettingsView(
    themeMode: AppThemeMode,
    isAmoledEnabled: Boolean,
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    showTabLabels: Boolean,
    autoDeleteCompletedDaily: Boolean,
    userCountry: String,
    userTimezone: String,
    onToggleTheme: () -> Unit,
    onToggleAmoled: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleShowTabLabels: (Boolean) -> Unit,
    onToggleAutoDeleteDaily: (Boolean) -> Unit,
    onSelectCountry: (String) -> Unit,
    onSelectTimezone: (String) -> Unit,
    onClearCompleted: () -> Unit,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val scrollState = rememberScrollState()
    val isDark = themeMode == AppThemeMode.DARK || themeMode == AppThemeMode.BLACK

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp)
        ) {
            // Top Header
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)) {
                Text(
                    text = "PREFERENCES",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Settings & Style",
                    color = palette.textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // APPEARANCE & THEME SECTION
            SettingsSectionHeader(title = "APPEARANCE & THEME", icon = UntitledIcons.Layers)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassSettingsCard {
                // Dark / Light Mode Toggle
                SettingRow(
                    icon = if (isDark) UntitledIcons.Moon else UntitledIcons.Sun,
                    iconTint = palette.textPrimary,
                    title = "Dark Mode",
                    subtitle = if (isDark) "Active: Moon nocturnal theme" else "Active: Sun diurnal theme",
                    isChecked = isDark,
                    onCheckedChange = { onToggleTheme() }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                // AMOLED Mode Toggle
                SettingRow(
                    icon = UntitledIcons.Moon,
                    iconTint = palette.textPrimary,
                    title = "AMOLED Mode",
                    subtitle = "Pitch-black #000000 background for pure contrast",
                    isChecked = isAmoledEnabled,
                    badge = "OLED",
                    onCheckedChange = { onToggleAmoled(it) }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                // Bottom Bar Tab Names Toggle
                SettingRow(
                    icon = UntitledIcons.Layers,
                    iconTint = palette.textPrimary,
                    title = "Show Tab Names",
                    subtitle = "Display text labels under bottom navigation icons",
                    isChecked = showTabLabels,
                    onCheckedChange = { onToggleShowTabLabels(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TIMEZONE & DAILY CLEANUP SECTION
            SettingsSectionHeader(title = "TIMEZONE & DAILY CLEANUP", icon = UntitledIcons.Clock)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassSettingsCard {
                // Country Selection
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

                // Timezone Selection
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

                // Daily Auto-Clean Completed Tasks
                SettingRow(
                    icon = UntitledIcons.Trash,
                    iconTint = palette.textPrimary,
                    title = "Auto-Delete Daily",
                    subtitle = "Automatically remove yesterday's completed tasks at midnight in your timezone",
                    isChecked = autoDeleteCompletedDaily,
                    onCheckedChange = { onToggleAutoDeleteDaily(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TACTILE FEEDBACK & AUDIO
            SettingsSectionHeader(title = "TACTILE FEEDBACK & AUDIO", icon = UntitledIcons.Sparkles)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassSettingsCard {
                // Sound Effects Toggle
                SettingRow(
                    icon = if (isSoundEnabled) UntitledIcons.Volume2 else UntitledIcons.VolumeX,
                    iconTint = palette.textPrimary,
                    title = "Sound Effects",
                    subtitle = "Subtle auditory feedback and celebration fanfare on completing tasks",
                    isChecked = isSoundEnabled,
                    onCheckedChange = { onToggleSound() }
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x14000000),
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp)
                )

                // Haptic Feedback Toggle
                SettingRow(
                    icon = UntitledIcons.Vibrate,
                    iconTint = palette.textPrimary,
                    title = "Haptic Vibration",
                    subtitle = "Sensory tactile pulses on interactions",
                    isChecked = isHapticsEnabled,
                    onCheckedChange = { onToggleHaptics() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TASK DATA SECTION
            SettingsSectionHeader(title = "TASK DATA & STORAGE", icon = UntitledIcons.Trash)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassSettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Completed: $completedCount / $totalCount tasks",
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clean up completed items to declutter lists",
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = Color(0x20000000),
                                spotColor = Color(0x30000000)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.accent)
                            .clickable { onClearCompleted() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Clear All",
                            color = palette.chipTextSelected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ABOUT SECTION
            SettingsSectionHeader(title = "ABOUT LUNA", icon = UntitledIcons.AlertCircle)
            Spacer(modifier = Modifier.height(8.dp))

            AppleGlassSettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Luna 2.0 • Liquid Glass Edition",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crafted with pure monochromatic Apple liquid glass physics, frosted blur, dynamic specular specular rim lighting, and fluid gesture physics.",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Country Picker Dialog
        if (showCountryPicker) {
            LunaItemSelectionDialog(
                title = "Select Country",
                items = countriesList,
                selectedItem = userCountry,
                onItemSelected = onSelectCountry,
                onDismiss = { showCountryPicker = false }
            )
        }

        // Timezone Picker Dialog
        if (showTimezonePicker) {
            LunaItemSelectionDialog(
                title = "Select Timezone",
                items = timezonesList,
                selectedItem = userTimezone,
                onItemSelected = onSelectTimezone,
                onDismiss = { showTimezonePicker = false }
            )
        }
    }
}

@Composable
private fun LunaItemSelectionDialog(
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
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(palette.textPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isLight) Color(0x0C000000) else Color(0x15FFFFFF))
                                .border(
                                    1.dp,
                                    if (isLight) Color(0x18000000) else Color(0x1FFFFFFF),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = UntitledIcons.Search,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search...",
                                        color = palette.textSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = UntitledIcons.Close,
                                    contentDescription = "Clear",
                                    tint = palette.textSecondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredItems) { item ->
                        val isSelected = item.equals(selectedItem, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) (if (isLight) Color(0x1A000000) else Color(0x28FFFFFF)) else Color.Transparent)
                                .clickable {
                                    onItemSelected(item)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) palette.textPrimary else palette.textSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = UntitledIcons.Check,
                                    contentDescription = "Selected",
                                    tint = palette.textPrimary,
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
private fun AppleGlassSettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    val cardBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.90f) else Color(0xD91E1E26).copy(alpha = 0.85f)
    val cardBorder = Brush.verticalGradient(
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
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
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    val palette = LunaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textTertiary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
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
private fun SettingRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    badge: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    badge?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.accent.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = it,
                                color = palette.accent,
                                fontSize = 9.sp,
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
                checkedTrackColor = if (isLight) Color(0xFF34C759) else Color(0xFF30D158),
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = if (isLight) Color.White else Color(0xFFD4D4D8),
                uncheckedTrackColor = if (isLight) Color(0xFFE4E4E8) else Color(0xFF323238),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
