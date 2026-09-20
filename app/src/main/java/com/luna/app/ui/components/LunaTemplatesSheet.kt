package com.luna.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.ui.icons.UntitledIcons
import com.luna.app.ui.theme.LunaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunaTemplatesSheet(
    templates: List<TaskTemplateEntity>,
    onSelectTemplate: (TaskTemplateEntity) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onEditTemplate: ((TaskTemplateEntity) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = palette.background.red < 0.5f
    val isLight = !isDark
    val sheetBackground = if (isDark) Color(0xEE141418) else Color(0xF4F6F6F8)
    val sheetBorderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.35f else 0.85f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.20f),
            Color.Transparent
        )
    )

    var editingTemplateTarget by remember { mutableStateOf<TaskTemplateEntity?>(null) }
    var isCreatingTemplate by remember { mutableStateOf(false) }

    var editNameInput by remember { mutableStateOf("") }
    var editChecklistItems by remember { mutableStateOf<List<String>>(emptyList()) }
    var newChecklistItemInput by remember { mutableStateOf("") }

    // Full Template Editor Dialog (Create / Edit name & checklist items)
    if (editingTemplateTarget != null || isCreatingTemplate) {
        val isNew = isCreatingTemplate
        val titleText = if (isNew) "Create Custom Template" else "Edit Template & Checklist"

        AlertDialog(
            onDismissRequest = {
                editingTemplateTarget = null
                isCreatingTemplate = false
            },
            title = {
                Text(
                    text = titleText,
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TEMPLATE NAME",
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLight) Color(0x12000000) else Color(0x24FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        BasicTextField(
                            value = editNameInput,
                            onValueChange = { editNameInput = it },
                            textStyle = TextStyle(
                                color = palette.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(palette.accent),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (editNameInput.isEmpty()) {
                                    Text("Template title (e.g. Daily Review)...", color = palette.textTertiary, fontSize = 14.sp)
                                }
                                inner()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TEMPLATE CHECKLIST ITEMS",
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (editChecklistItems.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            editChecklistItems.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• $item",
                                        color = palette.textPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove item",
                                        tint = palette.textTertiary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                editChecklistItems = editChecklistItems.filterIndexed { i, _ -> i != idx }
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Quick add checklist item to template
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) Color(0x10000000) else Color(0x20FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = newChecklistItemInput,
                            onValueChange = { newChecklistItemInput = it },
                            textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (newChecklistItemInput.isEmpty()) {
                                    Text("+ Add checklist item...", color = palette.textTertiary, fontSize = 12.sp)
                                }
                                inner()
                            }
                        )
                        Text(
                            text = "Add",
                            color = palette.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    if (newChecklistItemInput.isNotBlank()) {
                                        editChecklistItems = editChecklistItems + newChecklistItemInput.trim()
                                        newChecklistItemInput = ""
                                    }
                                }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.accent)
                        .clickable {
                            if (editNameInput.isNotBlank()) {
                                val finalItems = if (newChecklistItemInput.isNotBlank()) {
                                    editChecklistItems + newChecklistItemInput.trim()
                                } else editChecklistItems

                                if (isNew) {
                                    val newTemplate = TaskTemplateEntity(
                                        name = editNameInput.trim(),
                                        defaultTitle = editNameInput.trim(),
                                        checklistItems = finalItems
                                    )
                                    onEditTemplate?.invoke(newTemplate)
                                } else {
                                    val updated = editingTemplateTarget!!.copy(
                                        name = editNameInput.trim(),
                                        checklistItems = finalItems
                                    )
                                    onEditTemplate?.invoke(updated)
                                }
                                editingTemplateTarget = null
                                isCreatingTemplate = false
                                editNameInput = ""
                                editChecklistItems = emptyList()
                                newChecklistItemInput = ""
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Save Template",
                        color = palette.chipTextSelected,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            editingTemplateTarget = null
                            isCreatingTemplate = false
                            editNameInput = ""
                            editChecklistItems = emptyList()
                            newChecklistItemInput = ""
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = palette.textSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            containerColor = if (isDark) Color(0xFF1E1E24) else Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Templates",
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // + Add Custom Template Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.accent.copy(alpha = 0.14f))
                            .clickable {
                                editNameInput = ""
                                editChecklistItems = emptyList()
                                newChecklistItemInput = ""
                                isCreatingTemplate = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ Add Template",
                            color = palette.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x25FFFFFF) else Color(0x18000000))
                            .border(
                                width = 0.8.dp,
                                brush = sheetBorderBrush,
                                shape = CircleShape
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (templates.isEmpty()) {
                Text(
                    text = "No saved templates yet. Tap '+ Add Template' above or edit any task and choose 'Save as Template' to reuse recurring workflows.",
                    color = palette.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(templates, key = { it.id }) { template ->
                        TemplateRow(
                            template = template,
                            isLight = isLight,
                            onSelect = {
                                onSelectTemplate(template)
                                onDismiss()
                            },
                            onEdit = {
                                editNameInput = template.name
                                editChecklistItems = template.checklistItems
                                newChecklistItemInput = ""
                                editingTemplateTarget = template
                            },
                            onDelete = {
                                onDeleteTemplate(template.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TemplateRow(
    template: TaskTemplateEntity,
    isLight: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LunaTheme.colors
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val rowBg = if (isLight) Color(0xF5FFFFFF).copy(alpha = 0.90f) else Color(0xD922222A).copy(alpha = 0.80f)
    val rowBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.92f else 0.30f),
            Color.White.copy(alpha = if (isLight) 0.30f else 0.08f)
        )
    )

    val menuGlassFill = if (isLight) Color(0xF8FFFFFF) else Color(0xFA1E1E26)
    val menuBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isLight) 0.95f else 0.35f),
            Color.White.copy(alpha = if (isLight) 0.35f else 0.10f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isLight) Color(0x10000000) else Color(0x30000000),
                spotColor = if (isLight) Color(0x15000000) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onSelect,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistAddCheck,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (template.checklistItems.isNotEmpty()) {
                    Text(
                        text = "${template.checklistItems.size} checklist items",
                        color = palette.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // 3-dots Context Menu Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = UntitledIcons.MoreVertical,
                    contentDescription = "Template options",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )

                // Apple Liquid Glass Dropdown Menu
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
    }
}
