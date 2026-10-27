package com.example.focus.ui.screen

import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.focus.data.model.TaskItem
import com.example.focus.data.repository.AuthState
import com.example.focus.model.TimerMode
import com.example.focus.ui.components.TaskSelectionBottomSheet
import com.example.focus.ui.components.UserProfileTopModal
import com.example.focus.ui.theme.FocusTheme
import com.example.focus.viewmodel.AuthViewModel
import com.example.focus.viewmodel.CompletionSoundStyle
import com.example.focus.viewmodel.CountdownSoundStyle
import com.example.focus.viewmodel.FocusTimerViewModel
import com.example.focus.viewmodel.TaskViewModel
import com.example.focus.viewmodel.TimerCompletionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FocusTimerScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusTimerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    onRequestLockTask: () -> Unit = {},
    onRequestUnlockTask: () -> Unit = {},
    onRequestDndPermission: () -> Unit = {},
    hasDndPermission: () -> Boolean = { true },
    onTriggerBiometrics: (onSuccess: () -> Unit) -> Unit = {},
) {
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val remainingTimeSeconds by viewModel.remainingTimeSeconds.collectAsStateWithLifecycle()
    val totalTimeSeconds by viewModel.totalTimeSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val dontAskDndPermission by viewModel.dontAskDndPermission.collectAsStateWithLifecycle()

    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val countdownSoundStyle by viewModel.countdownSoundStyle.collectAsStateWithLifecycle()
    val completionSoundStyle by viewModel.completionSoundStyle.collectAsStateWithLifecycle()
    val customMinutes by viewModel.customMinutes.collectAsStateWithLifecycle()
    val customSeconds by viewModel.customSeconds.collectAsStateWithLifecycle()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val projects by taskViewModel.projects.collectAsStateWithLifecycle()
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val selectedTask by taskViewModel.selectedTask.collectAsStateWithLifecycle()
    val activeSession by taskViewModel.activeSession.collectAsStateWithLifecycle()
    val syncState by taskViewModel.syncState.collectAsStateWithLifecycle()

    var showDndDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var showCustomTimerPickerSheet by remember { mutableStateOf(false) }
    var showTaskSelectionSheet by remember { mutableStateOf(false) }

    var pendingTaskForCustomTimer by remember { mutableStateOf<TaskItem?>(null) }

    val isAnyModalOpen = showProfileModal || showSettingsDialog || showCustomTimerPickerSheet || showTaskSelectionSheet
    val blurRadius by animateDpAsState(
        targetValue = if (isAnyModalOpen) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "BackgroundBlurAnimation",
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            taskViewModel.loadData()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            onRequestLockTask()
        } else {
            onRequestUnlockTask()
        }
    }

    // Auto-clear completed task banner after 3.5 seconds
    LaunchedEffect(selectedTask?.id, selectedTask?.status) {
        val task = selectedTask
        if (task != null && task.status == "completed") {
            delay(3500L)
            taskViewModel.selectTask(null)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.completionEvent.collect { event ->
            when (event) {
                is TimerCompletionEvent.Completed -> {
                    snackbarHostState.showSnackbar(event.message)
                    val taskToComplete = selectedTask
                    val activeSess = activeSession

                    if (activeSess != null) {
                        taskViewModel.completeFocusSession(
                            sessionId = activeSess.id,
                            actualSeconds = totalTimeSeconds,
                        ) { _ -> }
                    }

                    if (taskToComplete != null) {
                        taskViewModel.updateTaskStatus(taskToComplete.id, "completed") { result ->
                            if (result.isSuccess) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Task '${taskToComplete.title}' completed!")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDndDialog) {
        DndPermissionDialog(
            onDismissRequest = {
                showDndDialog = false
                viewModel.play()
            },
            onGrantPermission = { dontAskAgain ->
                if (dontAskAgain) {
                    viewModel.setDontAskDndPermission(true)
                }
                showDndDialog = false
                onRequestUnlockTask()
                onRequestDndPermission()
            },
            onSkip = { dontAskAgain ->
                if (dontAskAgain) {
                    viewModel.setDontAskDndPermission(true)
                }
                showDndDialog = false
                viewModel.play()
            },
        )
    }

    if (showSettingsDialog) {
        SoundSettingsDialog(
            isVibrationEnabled = isVibrationEnabled,
            countdownSoundStyle = countdownSoundStyle,
            completionSoundStyle = completionSoundStyle,
            customMinutes = customMinutes,
            customSeconds = customSeconds,
            onVibrationToggle = { viewModel.setVibrationEnabled(it) },
            onCountdownStyleSelected = { viewModel.setCountdownSoundStyle(it) },
            onCompletionStyleSelected = { viewModel.setCompletionSoundStyle(it) },
            onCustomDurationSet = { min, sec ->
                viewModel.setCustomDuration(min, sec)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        String.format(
                            java.util.Locale.US,
                            "Custom timer set to %02d:%02d",
                            min,
                            sec,
                        ),
                    )
                }
            },
            onDismissRequest = { showSettingsDialog = false },
        )
    }

    if (showCustomTimerPickerSheet) {
        CustomTimerPickerBottomSheet(
            initialMinutes = customMinutes,
            initialSeconds = customSeconds,
            onDurationSet = { min, sec ->
                viewModel.setCustomDuration(min, sec)
                val totalSec = (min * 60) + sec
                val estMins = kotlin.math.ceil(totalSec / 60.0).toInt().coerceAtLeast(1)

                val taskToFocus = pendingTaskForCustomTimer ?: selectedTask
                if (taskToFocus != null) {
                    taskViewModel.updateTaskEstimatedMinutes(taskToFocus.id, estMins)
                    taskViewModel.updateTaskStatus(taskToFocus.id, "in_progress") { _ -> }
                    taskViewModel.startFocusSession(taskToFocus.id, estMins) { _ -> }
                    viewModel.play()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Timing task '${taskToFocus.title}' (${estMins}m)")
                    }
                    pendingTaskForCustomTimer = null
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            String.format(
                                java.util.Locale.US,
                                "Timer duration set to %02d:%02d",
                                min,
                                sec,
                            ),
                        )
                    }
                }
            },
            onDismissRequest = { showCustomTimerPickerSheet = false },
        )
    }

    TaskSelectionBottomSheet(
        isVisible = showTaskSelectionSheet,
        projects = projects,
        tasks = tasks,
        selectedTask = selectedTask,
        remainingTimerSeconds = remainingTimeSeconds,
        onDismiss = { showTaskSelectionSheet = false },
        onSelectTaskForCustomTimer = { task ->
            pendingTaskForCustomTimer = task
            taskViewModel.selectTask(task)
            taskViewModel.updateTaskStatus(task.id, "in_progress") { _ -> }
            showTaskSelectionSheet = false
            showCustomTimerPickerSheet = true
        },
        onCreateProject = { title, desc ->
            taskViewModel.createProject(title, desc) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Project created!")
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to create project")
                    }
                }
            }
        },
        onCreateTask = { title, desc, projId, priority, estMins ->
            taskViewModel.createTask(title, desc, projId, priority, estMins) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Task created!")
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to create task")
                    }
                }
            }
        },
        onDeleteTask = { id ->
            taskViewModel.deleteTask(id) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Task deleted!")
                    }
                }
            }
        },
        onDeleteProject = { id ->
            taskViewModel.deleteProject(id) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Project deleted!")
                    }
                }
            }
        },
    )

    UserProfileTopModal(
        isVisible = showProfileModal,
        authState = authState,
        syncState = syncState,
        onDismiss = { showProfileModal = false },
        onOpenSettings = { showSettingsDialog = true },
        onTriggerSync = { taskViewModel.triggerSync() },
        onLogin = { identifier, password ->
            authViewModel.login(identifier, password) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        val name = result.getOrNull()?.full_name ?: result.getOrNull()?.username ?: "Focus Member"
                        snackbarHostState.showSnackbar("Welcome back, $name!")
                        showProfileModal = false
                        taskViewModel.loadData()
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Login failed")
                    }
                }
            }
        },
        onBiometricLogin = {
            onTriggerBiometrics {
                authViewModel.loginWithBiometrics { result ->
                    coroutineScope.launch {
                        if (result.isSuccess) {
                            val name = result.getOrNull()?.full_name ?: result.getOrNull()?.username ?: "Focus Member"
                            snackbarHostState.showSnackbar("Authenticated with Fingerprint. Welcome, $name!")
                            showProfileModal = false
                            taskViewModel.loadData()
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Fingerprint login failed")
                        }
                    }
                }
            }
        },
        onRegister = { identifier, password, fullName ->
            authViewModel.register(
                identifier = identifier,
                username = identifier,
                email = if (identifier.contains("@")) identifier else null,
                password = password,
                fullName = fullName,
            ) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Account created successfully!")
                        showProfileModal = false
                        taskViewModel.loadData()
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Registration failed")
                    }
                }
            }
        },
        onUpdateProfile = { username, email, fullName, avatarUrl ->
            authViewModel.updateProfile(username, email, fullName, avatarUrl) { result ->
                coroutineScope.launch {
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("Profile updated!")
                    } else {
                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Update failed")
                    }
                }
            }
        },
        onLogout = {
            authViewModel.logout()
            taskViewModel.selectTask(null)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Signed out successfully.")
            }
        },
    )

    FocusTimerContent(
        timerMode = timerMode,
        remainingTimeSeconds = remainingTimeSeconds,
        totalTimeSeconds = totalTimeSeconds,
        isRunning = isRunning,
        authState = authState,
        selectedTask = selectedTask,
        onModeSelected = { viewModel.setMode(it) },
        onPlayPauseClick = {
            if (!isRunning && !hasDndPermission() && !dontAskDndPermission) {
                showDndDialog = true
            } else {
                val taskToFocus = selectedTask
                if (!isRunning) {
                    if (taskToFocus != null) {
                        taskViewModel.updateTaskStatus(taskToFocus.id, "in_progress") { _ -> }
                        taskViewModel.startFocusSession(taskToFocus.id, remainingTimeSeconds / 60) { _ -> }
                    }
                    viewModel.play()
                } else {
                    if (taskToFocus != null) {
                        taskViewModel.updateTaskStatus(taskToFocus.id, "paused") { _ -> }
                    }
                    viewModel.pause()
                }
            }
        },
        onResetClick = { viewModel.reset() },
        onSettingsClick = { showSettingsDialog = true },
        onProfileClick = { showProfileModal = true },
        onTimerCircleClick = { showCustomTimerPickerSheet = true },
        onTaskSelectClick = { showTaskSelectionSheet = true },
        snackbarHostState = snackbarHostState,
        modifier = modifier.blur(blurRadius),
    )
}

@Composable
fun CustomTimerPickerBottomSheet(
    initialMinutes: Int,
    initialSeconds: Int,
    onDurationSet: (minutes: Int, seconds: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedMinutes by remember(initialMinutes) { mutableIntStateOf(initialMinutes) }
    var selectedSeconds by remember(initialSeconds) { mutableIntStateOf(initialSeconds) }

    Dialog(
        onDismissRequest = onDismissRequest,
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
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                        // Drag Indicator Handle
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
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    text = "SET TIMER DURATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                IconButton(onClick = onDismissRequest) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // Apple Wheel Time Picker
                        AppleWheelTimePicker(
                            initialMinutes = selectedMinutes,
                            initialSeconds = selectedSeconds,
                            onTimeSelected = { min, sec ->
                                selectedMinutes = min
                                selectedSeconds = sec
                            },
                        )

                        // Confirm Action Button
                        Button(
                            onClick = {
                                onDurationSet(selectedMinutes, selectedSeconds)
                                onDismissRequest()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                        ) {
                            Text(
                                text = "Set Duration (${selectedMinutes}m ${selectedSeconds}s)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundSettingsDialog(
    isVibrationEnabled: Boolean,
    countdownSoundStyle: CountdownSoundStyle,
    completionSoundStyle: CompletionSoundStyle,
    customMinutes: Int,
    customSeconds: Int,
    onVibrationToggle: (Boolean) -> Unit,
    onCountdownStyleSelected: (CountdownSoundStyle) -> Unit,
    onCompletionStyleSelected: (CompletionSoundStyle) -> Unit,
    onCustomDurationSet: (minutes: Int, seconds: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedMinutes by remember(customMinutes) { mutableIntStateOf(customMinutes) }
    var selectedSeconds by remember(customSeconds) { mutableIntStateOf(customSeconds) }
    var showCustomPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
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
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 16.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Drag Indicator Handle
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
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    text = "SETTINGS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp),
                            ) {
                                IconButton(onClick = onDismissRequest) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // Setting 1: Custom Duration Setter Card
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCustomPicker = !showCustomPicker },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = "Custom Timer",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Text(
                                        text = String.format(
                                            java.util.Locale.US,
                                            "%02dm %02ds",
                                            selectedMinutes,
                                            selectedSeconds,
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                if (showCustomPicker) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AppleWheelTimePicker(
                                        initialMinutes = selectedMinutes,
                                        initialSeconds = selectedSeconds,
                                        onTimeSelected = { min, sec ->
                                            selectedMinutes = min
                                            selectedSeconds = sec
                                        },
                                    )
                                    Button(
                                        onClick = {
                                            onCustomDurationSet(selectedMinutes, selectedSeconds)
                                            showCustomPicker = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                    ) {
                                        Text(
                                            text = "Set Custom Timer (${selectedMinutes}m ${selectedSeconds}s)",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        // Hide the other 3 cards when Custom Timer wheel picker is open
                        if (!showCustomPicker) {
                            // Setting 2: Vibration
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onVibrationToggle(!isVibrationEnabled) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = "Vibration",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Switch(
                                        checked = isVibrationEnabled,
                                        onCheckedChange = onVibrationToggle,
                                    )
                                }
                            }

                            // Setting 3: Countdown Sound (15s)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = "15s Countdown",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }

                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        CountdownSoundStyle.entries.forEachIndexed { index, style ->
                                            SegmentedButton(
                                                selected = style == countdownSoundStyle,
                                                onClick = { onCountdownStyleSelected(style) },
                                                shape = SegmentedButtonDefaults.itemShape(
                                                    index = index,
                                                    count = CountdownSoundStyle.entries.size,
                                                ),
                                            ) {
                                                Text(
                                                    text = when (style) {
                                                        CountdownSoundStyle.SOFT_BEEP -> "Beep"
                                                        CountdownSoundStyle.GENTLE_TICK -> "Tick"
                                                        CountdownSoundStyle.MUTE -> "Off"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Setting 4: Timer End Sound
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = "Timer End Alarm",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }

                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        CompletionSoundStyle.entries.forEachIndexed { index, style ->
                                            SegmentedButton(
                                                selected = style == completionSoundStyle,
                                                onClick = { onCompletionStyleSelected(style) },
                                                shape = SegmentedButtonDefaults.itemShape(
                                                    index = index,
                                                    count = CompletionSoundStyle.entries.size,
                                                ),
                                            ) {
                                                Text(
                                                    text = when (style) {
                                                        CompletionSoundStyle.NOTIFICATION -> "Default"
                                                        CompletionSoundStyle.ALARM -> "Alarm"
                                                        CompletionSoundStyle.MUTE -> "Off"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                        ) {
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppleWheelTimePicker(
    initialMinutes: Int,
    initialSeconds: Int,
    onTimeSelected: (minutes: Int, seconds: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedMinutes by remember(initialMinutes) { mutableIntStateOf(initialMinutes) }
    var selectedSeconds by remember(initialSeconds) { mutableIntStateOf(initialSeconds) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Minutes Wheel (0 to 99)
        WheelNumberColumn(
            range = 0..99,
            initialValue = selectedMinutes,
            onValueChange = {
                selectedMinutes = it
                onTimeSelected(selectedMinutes, selectedSeconds)
            },
            label = "min",
            modifier = Modifier.weight(1f),
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        // Seconds Wheel (0 to 59)
        WheelNumberColumn(
            range = 0..59,
            initialValue = selectedSeconds,
            onValueChange = {
                selectedSeconds = it
                onTimeSelected(selectedMinutes, selectedSeconds)
            },
            label = "sec",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WheelNumberColumn(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val items = remember(range) { range.toList() }
    val initialIndex = remember(initialValue) { items.indexOf(initialValue).coerceAtLeast(0) }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
        }
    }

    LaunchedEffect(selectedIndex) {
        items.getOrNull(selectedIndex)?.let { onValueChange(it) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.height(160.dp),
    ) {
        // Apple-style center selection highlight bar
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(40.dp),
        ) {}

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items.size) { index ->
                val number = items[index]
                val isSelected = index == selectedIndex
                val distance = kotlin.math.abs(index - selectedIndex)

                val alpha = when (distance) {
                    0 -> 1.0f
                    1 -> 0.45f
                    else -> 0.2f
                }

                val scale = when (distance) {
                    0 -> 1.15f
                    1 -> 0.9f
                    else -> 0.75f
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%02d", number),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = (20 * scale).sp,
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                        },
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DndPermissionDialog(
    onDismissRequest: () -> Unit,
    onGrantPermission: (dontAskAgain: Boolean) -> Unit,
    onSkip: (dontAskAgain: Boolean) -> Unit,
) {
    var dontAskAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        },
        title = {
            Text(
                text = "Block All Distractions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "To keep your focus sessions completely silent, grant Do Not Disturb access so incoming calls and app notifications are muted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FeatureRow(
                            icon = Icons.AutoMirrored.Filled.VolumeOff,
                            text = "Mute calls, SMS & app alerts",
                        )
                        FeatureRow(
                            icon = Icons.Default.Lock,
                            text = "Pin app to prevent quitting",
                        )
                        FeatureRow(
                            icon = Icons.Default.Check,
                            text = "Timer completion sound still plays",
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontAskAgain = !dontAskAgain }
                        .padding(vertical = 4.dp),
                ) {
                    Checkbox(
                        checked = dontAskAgain,
                        onCheckedChange = { dontAskAgain = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Don't ask me again",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGrantPermission(dontAskAgain) },
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Access")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onSkip(dontAskAgain) },
            ) {
                Text("Skip for Now")
            }
        },
    )
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun FocusTimerContent(
    timerMode: TimerMode,
    remainingTimeSeconds: Int,
    totalTimeSeconds: Int,
    isRunning: Boolean,
    authState: AuthState = AuthState.Unauthenticated,
    selectedTask: TaskItem? = null,
    onModeSelected: (TimerMode) -> Unit,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onTimerCircleClick: () -> Unit = {},
    onTaskSelectClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val homePresets = remember { listOf(TimerMode.FOCUS, TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK) }

    // Smooth UI/UX Animations when Focus session starts / pauses
    val topControlsAlpha by animateFloatAsState(
        targetValue = if (isRunning) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "TopControlsAlphaAnimation",
    )

    val topControlsScale by animateFloatAsState(
        targetValue = if (isRunning) 0.7f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "TopControlsScaleAnimation",
    )

    val heroCircleSize by animateDpAsState(
        targetValue = if (isRunning) 320.dp else 290.dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "HeroCircleSizeAnimation",
    )

    val heroTimeFontSize by animateFloatAsState(
        targetValue = if (isRunning) 68f else 62f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "HeroTimeFontSizeAnimation",
    )

    var showStatusPill by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning, timerMode, selectedTask) {
        showStatusPill = true
        delay(3000L)
        showStatusPill = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Minimalist Header Top Bar: Profile Avatar/Icon at Left, Brand in Center, Triangle (Tasks & Projects) at Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Top-Left Profile Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer {
                            alpha = topControlsAlpha
                            scaleX = topControlsScale
                            scaleY = topControlsScale
                        },
                ) {
                    IconButton(
                        onClick = onProfileClick,
                        enabled = !isRunning,
                    ) {
                        if (authState is AuthState.Authenticated) {
                            val user = authState.user
                            val avatar = user.avatar_url
                            if (!avatar.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatar,
                                    contentDescription = "User Profile",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape),
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initial = (user.full_name ?: user.username ?: user.email ?: "F")
                                            .take(1)
                                            .uppercase()
                                        Text(
                                            text = initial,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }

                // Brand Title
                Text(
                    text = "FOCUS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )

                // Top-Right Triangle Button (Opens Tasks and Projects Page)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer {
                            alpha = topControlsAlpha
                            scaleX = topControlsScale
                            scaleY = topControlsScale
                        },
                ) {
                    IconButton(
                        onClick = onTaskSelectClick,
                        enabled = !isRunning,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChangeHistory,
                            contentDescription = "Tasks and Projects",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            // Task Selection Banner (Visible when logged in)
            if (authState is AuthState.Authenticated) {
                AnimatedVisibility(
                    visible = !isRunning,
                    enter = fadeIn(tween(500, easing = FastOutSlowInEasing)) + expandVertically(tween(500, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(500, easing = FastOutSlowInEasing)) + shrinkVertically(tween(500, easing = FastOutSlowInEasing)),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskSelectClick() },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = if (selectedTask != null) {
                                        if (selectedTask.status == "completed") Icons.Default.CheckCircle else Icons.Default.TaskAlt
                                    } else {
                                        Icons.AutoMirrored.Filled.PlaylistAddCheck
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = selectedTask?.title ?: "+ Select Task / Project to Focus",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTask != null) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (selectedTask != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = when {
                                    selectedTask == null -> "TASKS"
                                    selectedTask.status == "completed" -> "COMPLETED"
                                    selectedTask.status == "paused" -> "PAUSED"
                                    isRunning -> "IN PROGRESS"
                                    else -> "PAUSED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Upper section: Minimalist Preset Selector (smoothly shrinks and fades away during focus session)
            AnimatedVisibility(
                visible = !isRunning,
                enter = fadeIn(tween(500, easing = FastOutSlowInEasing)) + expandVertically(tween(500, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(500, easing = FastOutSlowInEasing)) + shrinkVertically(tween(500, easing = FastOutSlowInEasing)),
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    homePresets.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = mode == timerMode,
                            onClick = { onModeSelected(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = homePresets.size,
                            ),
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (mode == timerMode) FontWeight.Bold else FontWeight.Normal,
                                ),
                            )
                        }
                    }
                }
            }

            // Hero Center Section: Minimalist Interactive Timer Progress Display (smoothly expands when session runs)
            val progress = if (totalTimeSeconds > 0) {
                remainingTimeSeconds.toFloat() / totalTimeSeconds.toFloat()
            } else 0f

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "TimerProgressAnimation",
            )

            val minutes = remainingTimeSeconds / 60
            val seconds = remainingTimeSeconds % 60
            val locale = LocalConfiguration.current.locales[0]
            val formattedTime = String.format(locale, "%02d:%02d", minutes, seconds)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(heroCircleSize)
                    .clip(CircleShape)
                    .clickable { if (!isRunning) onTimerCircleClick() },
            ) {
                // Outer background track ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                )
                // Smooth animated countdown progress ring
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = heroTimeFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedVisibility(
                        visible = showStatusPill,
                        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(400, easing = FastOutSlowInEasing)),
                    ) {
                        Surface(
                            color = if (isRunning) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = if (isRunning) {
                                    if (selectedTask != null) "FOCUSING ON TASK" else "SESSION IN PROGRESS"
                                } else if (selectedTask?.status == "paused") {
                                    "TASK PAUSED • TAP TO RESUME"
                                } else if (timerMode == TimerMode.CUSTOM) {
                                    "TAP TO ADJUST • PAUSED"
                                } else {
                                    "TAP TO SET • PAUSED"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = if (isRunning) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }

            // Minimalist Control Action Bar at Bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                LargeFloatingActionButton(
                    onClick = onPlayPauseClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp),
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FocusTimerContentPreview() {
    FocusTheme {
        FocusTimerContent(
            timerMode = TimerMode.FOCUS,
            remainingTimeSeconds = 25 * 60,
            totalTimeSeconds = 25 * 60,
            isRunning = false,
            onModeSelected = {},
            onPlayPauseClick = {},
            onResetClick = {},
            onSettingsClick = {},
            onProfileClick = {},
            onTimerCircleClick = {},
            onTaskSelectClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FocusTimerContentRunningPreview() {
    FocusTheme {
        FocusTimerContent(
            timerMode = TimerMode.SHORT_BREAK,
            remainingTimeSeconds = (3 * 60) + 15,
            totalTimeSeconds = 5 * 60,
            isRunning = true,
            onModeSelected = {},
            onPlayPauseClick = {},
            onResetClick = {},
            onSettingsClick = {},
            onProfileClick = {},
            onTimerCircleClick = {},
            onTaskSelectClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DndPermissionDialogPreview() {
    FocusTheme {
        DndPermissionDialog(
            onDismissRequest = {},
            onGrantPermission = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SoundSettingsDialogPreview() {
    FocusTheme {
        SoundSettingsDialog(
            isVibrationEnabled = true,
            countdownSoundStyle = CountdownSoundStyle.SOFT_BEEP,
            completionSoundStyle = CompletionSoundStyle.ALARM,
            customMinutes = 25,
            customSeconds = 0,
            onVibrationToggle = {},
            onCountdownStyleSelected = {},
            onCompletionStyleSelected = {},
            onCustomDurationSet = { _, _ -> },
            onDismissRequest = {},
        )
    }
}
