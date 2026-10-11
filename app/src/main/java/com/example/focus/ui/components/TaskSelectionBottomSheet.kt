package com.example.focus.ui.components

import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.example.focus.data.model.Project
import com.example.focus.data.model.TaskItem

enum class SelectedSection {
    NONE,
    PROJECTS,
    PROJECT_TASKS,
    TASKS
}

@Composable
fun TaskSelectionBottomSheet(
    isVisible: Boolean,
    projects: List<Project>,
    tasks: List<TaskItem>,
    selectedTask: TaskItem?,
    remainingTimerSeconds: Int = 0,
    onDismiss: () -> Unit,
    onSelectTaskForCustomTimer: (TaskItem) -> Unit,
    onCreateProject: (title: String, description: String?) -> Unit,
    onCreateTask: (title: String, description: String?, projectId: String?, priority: String?, estimatedMinutes: Int?) -> Unit,
    onDeleteTask: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
) {
    if (!isVisible) return

    var currentSection by remember { mutableStateOf(SelectedSection.NONE) }
    var selectedProjectForTasks by remember { mutableStateOf<Project?>(null) }

    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf(false) }

    val standaloneTasks = remember(tasks) { tasks.filter { it.project_id == null } }
    val projectTasks = remember(tasks, selectedProjectForTasks) {
        if (selectedProjectForTasks != null) {
            tasks.filter { it.project_id == selectedProjectForTasks?.id }
        } else {
            emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                window.setGravity(Gravity.BOTTOM)
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                )
                window.setDimAmount(0.35f)
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(540.dp),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 16.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                        )

                        // Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (currentSection != SelectedSection.NONE) {
                                    IconButton(
                                        onClick = {
                                            if (currentSection == SelectedSection.PROJECT_TASKS) {
                                                currentSection = SelectedSection.PROJECTS
                                                selectedProjectForTasks = null
                                            } else {
                                                currentSection = SelectedSection.NONE
                                            }
                                        },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text(
                                        text = when (currentSection) {
                                            SelectedSection.NONE -> "TASKS & PROJECTS"
                                            SelectedSection.PROJECTS -> "PROJECTS"
                                            SelectedSection.PROJECT_TASKS -> "PROJECT: ${selectedProjectForTasks?.title?.uppercase() ?: ""}"
                                            SelectedSection.TASKS -> "STANDALONE TASKS"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 2.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        when (currentSection) {
                            SelectedSection.NONE -> {
                                // Show Two Squared Cards
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    // Card 1: Projects
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable { currentSection = SelectedSection.PROJECTS },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        ),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.Start,
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Folder,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = "${projects.size}",
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                Text(
                                                    text = "Projects",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }

                                    // Card 2: Standalone Tasks
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clickable { currentSection = SelectedSection.TASKS },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        ),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.Start,
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.TaskAlt,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = "${standaloneTasks.size}",
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                                Text(
                                                    text = "Standalone Tasks",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            SelectedSection.PROJECTS -> {
                                // Projects Section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${projects.size} Projects Active",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    Button(
                                        onClick = { showCreateProjectDialog = true },
                                        shape = RoundedCornerShape(20.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("New Project", style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                if (projects.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "No projects created yet.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                } else {
                                    // Transparent Border Table View for Projects
                                    OutlinedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Table Header
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text("Title", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                                Text("Tasks", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                items(projects) { project ->
                                                    // Dynamic local calculation of task counts
                                                    val projTotalTasks = tasks.count { it.project_id == project.id }
                                                    val projCompletedTasks = tasks.count { it.project_id == project.id && it.status == "completed" }

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectedProjectForTasks = project
                                                                currentSection = SelectedSection.PROJECT_TASKS
                                                            }
                                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = project.title,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(2f),
                                                        )
                                                        Text(
                                                            text = "$projCompletedTasks/$projTotalTasks",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.weight(1f),
                                                            textAlign = TextAlign.Center,
                                                        )
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Surface(
                                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                                shape = RoundedCornerShape(8.dp),
                                                            ) {
                                                                Text(
                                                                    text = project.status.uppercase(),
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = { onDeleteProject(project.id) },
                                                                modifier = Modifier.size(28.dp),
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete",
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                    modifier = Modifier.size(16.dp),
                                                                )
                                                            }
                                                        }
                                                    }
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            SelectedSection.PROJECT_TASKS -> {
                                // Tasks inside selected project
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Tasks in ${selectedProjectForTasks?.title ?: "Project"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )

                                    Button(
                                        onClick = { showCreateTaskDialog = true },
                                        shape = RoundedCornerShape(20.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("New Task", style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                if (projectTasks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "No tasks created for this project yet.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(projectTasks) { task ->
                                            val estMins = task.estimated_minutes.coerceAtLeast(1)
                                            val isCurrentTask = selectedTask?.id == task.id
                                            val activeRemMins = if (isCurrentTask && remainingTimerSeconds > 0) {
                                                kotlin.math.ceil(remainingTimerSeconds / 60.0).toInt().coerceAtLeast(1)
                                            } else {
                                                (estMins - task.total_focus_minutes).coerceAtLeast(1)
                                            }

                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = if (task.status == "completed") Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(22.dp),
                                                        )

                                                        Column {
                                                            Text(
                                                                text = task.title,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                            Text(
                                                                text = when (task.status) {
                                                                    "completed" -> "COMPLETED • ${estMins}m"
                                                                    "paused" -> "PAUSED • Rem: ${activeRemMins}m"
                                                                    "in_progress" -> "IN PROGRESS • Rem: ${activeRemMins}m"
                                                                    else -> "${task.status.uppercase()} • ${estMins}m"
                                                                },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        // Black Focus Button
                                                        Button(
                                                            onClick = {
                                                                onSelectTaskForCustomTimer(task)
                                                                onDismiss()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = MaterialTheme.colorScheme.primary,
                                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                            ),
                                                            shape = RoundedCornerShape(16.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp),
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Focus", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                        }

                                                        IconButton(
                                                            onClick = { onDeleteTask(task.id) },
                                                            modifier = Modifier.size(28.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete Task",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(16.dp),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            SelectedSection.TASKS -> {
                                // Standalone Tasks Section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${standaloneTasks.size} Standalone Tasks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    Button(
                                        onClick = { showCreateTaskDialog = true },
                                        shape = RoundedCornerShape(20.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("New Task", style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                if (standaloneTasks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "No standalone tasks found.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        items(standaloneTasks) { task ->
                                            val estMins = task.estimated_minutes.coerceAtLeast(1)
                                            val isCurrentTask = selectedTask?.id == task.id
                                            val activeRemMins = if (isCurrentTask && remainingTimerSeconds > 0) {
                                                kotlin.math.ceil(remainingTimerSeconds / 60.0).toInt().coerceAtLeast(1)
                                            } else {
                                                (estMins - task.total_focus_minutes).coerceAtLeast(1)
                                            }

                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = if (task.status == "completed") Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(22.dp),
                                                        )

                                                        Column {
                                                            Text(
                                                                text = task.title,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                            Text(
                                                                text = when (task.status) {
                                                                    "completed" -> "COMPLETED • ${estMins}m"
                                                                    "paused" -> "PAUSED • Rem: ${activeRemMins}m"
                                                                    "in_progress" -> "IN PROGRESS • Rem: ${activeRemMins}m"
                                                                    else -> "${task.status.uppercase()} • ${estMins}m"
                                                                },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        // Black Focus Button
                                                        Button(
                                                            onClick = {
                                                                onSelectTaskForCustomTimer(task)
                                                                onDismiss()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = MaterialTheme.colorScheme.primary,
                                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                            ),
                                                            shape = RoundedCornerShape(16.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp),
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Focus", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                        }

                                                        IconButton(
                                                            onClick = { onDeleteTask(task.id) },
                                                            modifier = Modifier.size(28.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete Task",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(16.dp),
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
                    }
                }
            }
        }
    }

    if (showCreateTaskDialog) {
        CreateTaskDialog(
            projects = projects,
            initialProjectId = selectedProjectForTasks?.id,
            onDismiss = { showCreateTaskDialog = false },
            onCreate = { title, desc, projId, priority, estMins ->
                onCreateTask(title, desc, projId, priority, estMins)
                showCreateTaskDialog = false
            },
        )
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateProjectDialog = false },
            onCreate = { title, desc ->
                onCreateProject(title, desc)
                showCreateProjectDialog = false
            },
        )
    }
}

@Composable
private fun CreateTaskDialog(
    projects: List<Project>,
    initialProjectId: String? = null,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String?, projectId: String?, priority: String?, estimatedMinutes: Int?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectId by remember(initialProjectId) { mutableStateOf(initialProjectId) }
    var priority by remember { mutableStateOf("medium") }
    var estimatedMinutes by remember { mutableIntStateOf(25) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Create Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                if (projects.isNotEmpty()) {
                    Text(
                        text = "Assign to Project:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProjectId = null },
                        ) {
                            RadioButton(
                                selected = selectedProjectId == null,
                                onClick = { selectedProjectId = null },
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Standalone Task (Inbox)", style = MaterialTheme.typography.bodyMedium)
                        }
                        projects.forEach { proj ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedProjectId = proj.id },
                            ) {
                                RadioButton(
                                    selected = selectedProjectId == proj.id,
                                    onClick = { selectedProjectId = proj.id },
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(proj.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), description.ifBlank { null }, selectedProjectId, priority, estimatedMinutes)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Create Project",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), description.ifBlank { null })
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
