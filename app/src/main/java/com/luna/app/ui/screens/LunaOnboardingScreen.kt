package com.luna.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.R
import com.luna.app.data.preferences.AppThemeMode
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.luna.app.ui.theme.LunaColorPalette
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaOnboardingScreen(
    initialName: String = "",
    initialGender: String = "BOY",
    initialCoverTitle: String = "Mountains",
    initialThemeMode: AppThemeMode = AppThemeMode.DARK,
    onCompleteOnboarding: (name: String, coverTitle: String, gender: String, themeMode: AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()

    var currentStep by remember { mutableIntStateOf(0) } // 0: Welcome, 1: Name, 2: Cover, 3: Gender, 4: Theme
    var nameInput by remember { mutableStateOf(initialName) }
    var selectedCover by remember { mutableStateOf(initialCoverTitle) }
    var selectedGender by remember { mutableStateOf(initialGender) }
    var selectedTheme by remember { mutableStateOf(if (systemDark) AppThemeMode.DARK else AppThemeMode.LIGHT) }

    val isDark = when (selectedTheme) {
        AppThemeMode.DARK, AppThemeMode.BLACK -> true
        AppThemeMode.LIGHT -> false
        else -> systemDark
    }

    val brandLogoRes = if (isDark) R.drawable.ic_luna_brand_logo_dark else R.drawable.ic_luna_brand_logo_light
    val mainIconRes = R.drawable.ic_main_icon_logo_removebg_black

    val totalSteps = 5

    // Pulsing logo animation scale
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    LunaTheme(themeMode = selectedTheme) {
        val palette = LunaTheme.colors
        val isDark = palette.background.red < 0.5f
        val brandLogoRes = if (isDark) R.drawable.ic_luna_brand_logo_dark else R.drawable.ic_luna_brand_logo_light

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF090D16))
                        } else {
                            listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                        }
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Progress Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(totalSteps) { idx ->
                    val isActive = idx == currentStep
                    val isPassed = idx < currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isActive -> palette.accent
                                    isPassed -> palette.accent.copy(alpha = 0.5f)
                                    else -> palette.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Step Content Container
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "onboardingStep"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (step) {
                        0 -> {
                            // Step 0: Welcome & App Identity
                            Box(
                                modifier = Modifier
                                    .scale(logoScale)
                                    .fillMaxWidth(0.85f)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                palette.accent.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = brandLogoRes),
                                    contentDescription = "Luna Brand Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Welcome to Luna",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Plan • Do • Grow",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accent,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Your time. Your tasks. In perfect harmony.",
                                fontSize = 14.sp,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // 4 Pillars Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PillarChip(icon = Icons.Rounded.CheckCircle, title = "Tasks", subtitle = "Organized")
                                PillarChip(icon = Icons.Rounded.Timer, title = "Time", subtitle = "Focus Mode")
                                PillarChip(icon = Icons.Rounded.DateRange, title = "Schedule", subtitle = "Plan Ahead")
                                PillarChip(icon = Icons.AutoMirrored.Rounded.TrendingUp, title = "Progress", subtitle = "Better Days")
                            }
                        }

                        1 -> {
                            // Step 1: Set Name
                            Icon(
                                imageVector = Icons.Rounded.WavingHand,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "What should Luna call you?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your name will personalize your dashboard & greeting.",
                                fontSize = 13.sp,
                                color = palette.textTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            LunaNameInputField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                onDone = { if (nameInput.isNotBlank()) currentStep = 2 },
                                palette = palette,
                                isDark = isDark
                            )

                            if (nameInput.isNotBlank()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Preview: \"Good day, ${nameInput.trim()}! 🌙\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.accent
                                )
                            }
                        }

                        2 -> {
                            // Step 2: Choose Cover Banner
                            Text(
                                text = "Choose Your Cover Style",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a header banner for your home screen dashboard.",
                                fontSize = 13.sp,
                                color = palette.textTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val covers = listOf(
                                Pair("Mountains", "🏔️ Classic Alpine"),
                                Pair("Cosmic", "🌌 Deep Space"),
                                Pair("Sunset", "🌅 Glowing Dusk"),
                                Pair("Minimal", "🎨 Pure Gradient"),
                                Pair("Glass", "🔮 Translucent Dark")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                covers.forEach { (title, label) ->
                                    val isSelected = selectedCover == title
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) palette.accent.copy(alpha = 0.18f) else palette.surfaceVariant
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) palette.accent else palette.borderSubtle,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedCover = title }
                                            .padding(horizontal = 18.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) palette.accent else palette.textPrimary
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = palette.accent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Step 3: Select Gender / Avatar
                            Icon(
                                imageVector = Icons.Rounded.Face,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select Your Avatar Preference",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This configures your default user avatar style.",
                                fontSize = 13.sp,
                                color = palette.textTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GenderCard(
                                    title = "BOY",
                                    emoji = "👦",
                                    label = "Boy",
                                    isSelected = selectedGender == "BOY",
                                    onSelect = { selectedGender = "BOY" },
                                    modifier = Modifier.weight(1f)
                                )
                                GenderCard(
                                    title = "GIRL",
                                    emoji = "👧",
                                    label = "Girl",
                                    isSelected = selectedGender == "GIRL",
                                    onSelect = { selectedGender = "GIRL" },
                                    modifier = Modifier.weight(1f)
                                )
                                GenderCard(
                                    title = "NEUTRAL",
                                    emoji = "✨",
                                    label = "Neutral",
                                    isSelected = selectedGender == "NEUTRAL",
                                    onSelect = { selectedGender = "NEUTRAL" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        4 -> {
                            // Step 4: Theme Mode Selection
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Choose Interface Theme",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Luna looks stunning in both light & dark modes.",
                                fontSize = 13.sp,
                                color = palette.textTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThemeCard(
                                    title = "Dark Mode",
                                    subtitle = "Sleek liquid glass dark theme",
                                    icon = Icons.Rounded.DarkMode,
                                    isSelected = selectedTheme == AppThemeMode.DARK,
                                    onSelect = { selectedTheme = AppThemeMode.DARK }
                                )
                                ThemeCard(
                                    title = "Light Mode",
                                    subtitle = "Clean bright high-contrast theme",
                                    icon = Icons.Rounded.LightMode,
                                    isSelected = selectedTheme == AppThemeMode.LIGHT,
                                    onSelect = { selectedTheme = AppThemeMode.LIGHT }
                                )
                                ThemeCard(
                                    title = "OLED Pure Black",
                                    subtitle = "True pitch black battery saver mode",
                                    icon = Icons.Rounded.Brightness1,
                                    isSelected = selectedTheme == AppThemeMode.BLACK,
                                    onSelect = { selectedTheme = AppThemeMode.BLACK }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation & Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep -= 1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = palette.textSecondary, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            if (currentStep < totalSteps - 1) {
                                currentStep += 1
                            } else {
                                onCompleteOnboarding(
                                    nameInput.trim(),
                                    selectedCover,
                                    selectedGender,
                                    selectedTheme
                                )
                            }
                        }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentStep == totalSteps - 1) "Finish Setup 🚀" else "Continue",
                            color = palette.onAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentStep < totalSteps - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Next",
                                tint = palette.onAccent,
                                modifier = Modifier.size(18.dp)
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
private fun PillarChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    val palette = LunaTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = palette.accent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = palette.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GenderCard(
    title: String,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) palette.accent.copy(alpha = 0.18f) else palette.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) palette.accent else palette.borderSubtle,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onSelect)
            .padding(vertical = 20.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) palette.accent else palette.textPrimary
            )
        }
    }
}

@Composable
private fun ThemeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val palette = LunaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) palette.accent.copy(alpha = 0.18f) else palette.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) palette.accent else palette.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) palette.accent else palette.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) palette.accent else palette.textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = palette.textTertiary
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LunaNameInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    palette: LunaColorPalette,
    isDark: Boolean
) {
    val glassBg = if (isDark) Color(0x331E293B) else Color(0xFFF1F5F9)
    val borderColor = if (value.isNotBlank()) palette.accent else (if (isDark) Color(0x40FFFFFF) else Color(0xFFCBD5E1))

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .height(58.dp)
            .shadow(
                elevation = if (value.isNotBlank()) 6.dp else 1.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isDark) Color.Black else Color(0x15000000),
                spotColor = if (value.isNotBlank()) palette.accent.copy(alpha = 0.25f) else Color.Transparent
            )
            .clip(RoundedCornerShape(18.dp))
            .background(glassBg)
            .border(
                width = if (value.isNotBlank()) 1.8.dp else 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = if (value.isNotBlank()) palette.accent else palette.textSecondary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = "Enter your name...",
                        color = palette.textSecondary.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { onDone() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x20000000))
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
