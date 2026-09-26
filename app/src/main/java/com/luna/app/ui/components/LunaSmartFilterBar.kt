package com.luna.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.domain.model.SmartFilter
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaSmartFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: SmartFilter,
    onFilterSelected: (SmartFilter) -> Unit,
    counts: Map<SmartFilter, Int> = emptyMap(),
    templates: List<TaskTemplateEntity> = emptyList(),
    onOpenTemplates: (() -> Unit)? = null,
    onSelectTemplate: ((TaskTemplateEntity) -> Unit)? = null,
    onEditTemplate: ((TaskTemplateEntity) -> Unit)? = null,
    onDeleteTemplate: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Area 1: Dedicated Search Bar
        LunaSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange
        )

        // Area 2: Dedicated Templates Area
        LunaTemplatesBar(
            templates = templates,
            onOpenTemplates = onOpenTemplates,
            onSelectTemplate = onSelectTemplate,
            onEditTemplate = onEditTemplate,
            onDeleteTemplate = onDeleteTemplate
        )

        // Area 3: Dedicated Smart Filters & Tags Area
        LunaFilterTagsBar(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            counts = counts
        )
    }
}

/**
 * 🔍 Area 1: Dedicated Liquid Glass Search Bar
 */
@Composable
fun LunaSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f

    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD91C1C24).copy(alpha = 0.82f)
    val glassBorderBrush = Brush.verticalGradient(
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
            .height(48.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isLight) Color(0x12000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x18000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(glassFill)
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = UntitledIcons.Search,
                contentDescription = "Search",
                tint = if (searchQuery.isNotEmpty()) palette.textPrimary else palette.textTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* done */ }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search tasks, tags, notes...",
                            color = palette.textTertiary,
                            fontSize = 13.5.sp
                        )
                    }
                    inner()
                }
            )
            if (searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isLight) Color(0x15000000) else Color(0x20FFFFFF))
                        .clickable { onSearchQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = UntitledIcons.Close,
                        contentDescription = "Clear",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 📑 Area 2: Dedicated Liquid Glass Templates Strip
 */
@Composable
fun LunaTemplatesBar(
    templates: List<TaskTemplateEntity> = emptyList(),
    onOpenTemplates: (() -> Unit)? = null,
    onSelectTemplate: ((TaskTemplateEntity) -> Unit)? = null,
    onEditTemplate: ((TaskTemplateEntity) -> Unit)? = null,
    onDeleteTemplate: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val scrollState = rememberScrollState()

    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD91C1C24).copy(alpha = 0.82f)
    val glassBorderBrush = Brush.verticalGradient(
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
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(glassFill)
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Templates Launcher Action Chip
            if (onOpenTemplates != null) {
                val templatesGlassBg = if (isLight) Color(0xFFE4E4E8) else Color(0x30FFFFFF)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(templatesGlassBg)
                        .border(
                            width = 0.8.dp,
                            color = if (isLight) Color.White.copy(alpha = 0.90f) else Color(0x25FFFFFF),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(onClick = onOpenTemplates)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = "Templates",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (templates.isNotEmpty()) "Templates (${templates.size})" else "Templates",
                            color = palette.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Template Chips
            templates.forEach { template ->
                TemplateChipItem(
                    template = template,
                    isLight = isLight,
                    onSelect = { onSelectTemplate?.invoke(template) },
                    onEdit = { onEditTemplate?.invoke(template) },
                    onDelete = { onDeleteTemplate?.invoke(template.id) }
                )
            }

            // If empty, quick hint to add template
            if (templates.isEmpty() && onOpenTemplates != null) {
                Text(
                    text = "Tap Templates to create reusable presets",
                    color = palette.textTertiary,
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .clickable(onClick = onOpenTemplates)
                        .padding(horizontal = 6.dp)
                )
            }
        }
    }
}

/**
 * 🏷️ Area 3: Dedicated Liquid Glass Tags & Smart Filters Strip
 */
@Composable
fun LunaFilterTagsBar(
    selectedFilter: SmartFilter,
    onFilterSelected: (SmartFilter) -> Unit,
    counts: Map<SmartFilter, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val isLight = palette.background.red > 0.5f
    val scrollState = rememberScrollState()

    val glassFill = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.88f) else Color(0xD91C1C24).copy(alpha = 0.82f)
    val glassBorderBrush = Brush.verticalGradient(
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
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(glassFill)
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                val count = counts[filter] ?: 0

                val unselectedFilterBg = if (isLight) Color(0xFFE4E4E8) else Color(0x24FFFFFF)
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) palette.accent else unselectedFilterBg,
                    animationSpec = tween(150),
                    label = "filterBg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) palette.chipTextSelected else (if (isLight) Color(0xFF18181B) else palette.textSecondary),
                    animationSpec = tween(150),
                    label = "filterText"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(
                            width = 0.8.dp,
                            color = if (isSelected) Color.Transparent else (if (isLight) Color(0xFFC4C4C8) else Color(0x20FFFFFF)),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onFilterSelected(filter) }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = filter.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = filter.label,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                        if (count > 0 && filter != SmartFilter.COMPLETED) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) palette.chipTextSelected.copy(alpha = 0.20f) else (if (isLight) Color(0x18000000) else Color(0x30FFFFFF)))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    color = if (isSelected) palette.chipTextSelected else palette.textSecondary,
                                    fontSize = 10.sp,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TemplateChipItem(
    template: TaskTemplateEntity,
    isLight: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val chipBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.85f) else Color(0x24FFFFFF)
    val menuGlassFill = if (isLight) Color(0xF8FFFFFF) else Color(0xFA1E1E26)
    val menuBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.35f else 0.10f)
        )
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(chipBg)
            .border(
                width = 0.8.dp,
                color = if (isLight) Color.White.copy(alpha = 0.90f) else Color(0x25FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onSelect,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                }
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📋", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = template.name,
                color = palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Apple Liquid Glass Dropdown Menu on Long Press
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .width(170.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isLight) Color(0x20000000) else Color(0x60000000),
                    spotColor = if (isLight) Color(0x30000000) else Color(0x70000000)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(menuGlassFill)
                .border(
                    width = 1.2.dp,
                    brush = menuBorderBrush,
                    shape = RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            containerColor = menuGlassFill,
            shadowElevation = 0.dp,
            border = null
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Use Template",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textPrimary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = UntitledIcons.Plus,
                        contentDescription = "Use",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    onSelect()
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp),
                thickness = 0.5.dp,
                color = if (isLight) Color(0x15000000) else Color(0x20FFFFFF)
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Edit Template",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textPrimary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = UntitledIcons.Edit,
                        contentDescription = "Edit",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp),
                thickness = 0.5.dp,
                color = if (isLight) Color(0x15000000) else Color(0x20FFFFFF)
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textPrimary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = UntitledIcons.Trash,
                        contentDescription = "Delete",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
