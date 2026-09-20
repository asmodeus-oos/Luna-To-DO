package com.luna.app.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.model.TaskWithDetails
import com.luna.app.ui.components.LunaCheckbox
import com.luna.app.ui.theme.LunaShapes
import com.luna.app.ui.theme.LunaTheme

@Composable
fun LunaProjectsView(
    projects: List<ProjectEntity>,
    tasks: List<TaskWithDetails>,
    onAddProject: (String, Long?) -> Unit,
    onToggleTask: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LunaTheme.colors
    var newProjectName by remember { mutableStateOf("") }
    var expandedProjectIds by remember { mutableStateOf(setOf<Long>()) }

    // Root projects (parentId == null)
    val rootProjects = projects.filter { it.parentId == null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Quick Add Project Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📁", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = newProjectName,
                onValueChange = { newProjectName = it },
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newProjectName.isNotBlank()) {
                            onAddProject(newProjectName.trim(), null)
                            newProjectName = ""
                        }
                    }
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (newProjectName.isEmpty()) {
                        Text(
                            text = "New project or folder...",
                            color = palette.textTertiary,
                            fontSize = 13.sp
                        )
                    }
                    inner()
                }
            )
            if (newProjectName.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            onAddProject(newProjectName.trim(), null)
                            newProjectName = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (rootProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No projects yet. Create a project above to organize your tasks!",
                    color = palette.textTertiary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rootProjects, key = { it.id }) { project ->
                    val isExpanded = expandedProjectIds.contains(project.id)
                    val projectTasks = tasks.filter { it.task.projectId == project.id }
                    val childProjects = projects.filter { it.parentId == project.id }

                    ProjectFolderCard(
                        project = project,
                        tasks = projectTasks,
                        childProjects = childProjects,
                        allTasks = tasks,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedProjectIds = if (isExpanded) {
                                expandedProjectIds - project.id
                            } else {
                                expandedProjectIds + project.id
                            }
                        },
                        onToggleTask = onToggleTask,
                        onTaskClick = onTaskClick,
                        onAddSubProject = { name -> onAddProject(name, project.id) }
                    )
                }

                item(key = "projects_bottom_spacer") {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
private fun ProjectFolderCard(
    project: ProjectEntity,
    tasks: List<TaskWithDetails>,
    childProjects: List<ProjectEntity>,
    allTasks: List<TaskWithDetails>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTask: (TaskWithDetails) -> Unit,
    onTaskClick: (TaskWithDetails) -> Unit,
    onAddSubProject: (String) -> Unit
) {
    val palette = LunaTheme.colors
    val totalCount = tasks.size
    val completedCount = tasks.count { it.task.isCompleted }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val projectColor = try {
        Color(android.graphics.Color.parseColor(project.colorHex))
    } catch (_: Exception) {
        palette.accent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LunaShapes.medium)
            .background(palette.surface)
            .border(1.dp, palette.borderSubtle, LunaShapes.medium)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(projectColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = project.icon, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = project.name,
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalCount == 0) "No tasks yet" else "$completedCount / $totalCount completed (${(progress * 100).toInt()}%)",
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (totalCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = projectColor,
                    trackColor = palette.surfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Child Projects / Sub-folders
                    if (childProjects.isNotEmpty()) {
                        Text(
                            text = "SUB-FOLDERS",
                            color = palette.textTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        childProjects.forEach { child ->
                            val childTasks = allTasks.filter { it.task.projectId == child.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = child.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = child.name,
                                    color = palette.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${childTasks.size} tasks",
                                    color = palette.textTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Project Tasks
                    if (tasks.isEmpty()) {
                        Text(
                            text = "No tasks assigned to this project yet.",
                            color = palette.textTertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        // Group by section or show flat
                        val sections = tasks.groupBy { it.task.sectionName ?: "Default" }
                        sections.forEach { (section, sectionTasks) ->
                            if (section != "Default") {
                                Text(
                                    text = section.uppercase(),
                                    color = palette.accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                                )
                            }

                            sectionTasks.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onTaskClick(item) }
                                        .padding(vertical = 5.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LunaCheckbox(
                                        checked = item.task.isCompleted,
                                        onCheckedChange = { onToggleTask(item) },
                                        size = 16.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.task.title,
                                        color = if (item.task.isCompleted) palette.textTertiary else palette.textPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
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
