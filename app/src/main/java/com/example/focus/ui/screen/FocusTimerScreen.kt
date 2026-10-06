package com.example.focus.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focus.model.TimerMode
import com.example.focus.ui.theme.FocusTheme
import com.example.focus.viewmodel.FocusTimerViewModel
import com.example.focus.viewmodel.TimerCompletionEvent

@Composable
fun FocusTimerScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusTimerViewModel = viewModel(),
    onRequestLockTask: () -> Unit = {},
    onRequestUnlockTask: () -> Unit = {},
    onRequestDndPermission: () -> Unit = {},
    hasDndPermission: () -> Boolean = { true },
) {
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val remainingTimeSeconds by viewModel.remainingTimeSeconds.collectAsStateWithLifecycle()
    val totalTimeSeconds by viewModel.totalTimeSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val dontAskDndPermission by viewModel.dontAskDndPermission.collectAsStateWithLifecycle()

    var showDndDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            onRequestLockTask()
        } else {
            onRequestUnlockTask()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.completionEvent.collect { event ->
            when (event) {
                is TimerCompletionEvent.Completed -> {
                    snackbarHostState.showSnackbar(event.message)
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
                onRequestDndPermission()
                viewModel.play()
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

    FocusTimerContent(
        timerMode = timerMode,
        remainingTimeSeconds = remainingTimeSeconds,
        totalTimeSeconds = totalTimeSeconds,
        isRunning = isRunning,
        onModeSelected = { viewModel.setMode(it) },
        onPlayPauseClick = {
            if (!isRunning && !hasDndPermission() && !dontAskDndPermission) {
                showDndDialog = true
            } else {
                viewModel.togglePlayPause()
            }
        },
        onResetClick = { viewModel.reset() },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
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
    onModeSelected: (TimerMode) -> Unit,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top/Upper section: SingleChoiceSegmentedButtonRow
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                TimerMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = mode == timerMode,
                        onClick = { onModeSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TimerMode.entries.size,
                        ),
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            // Center section: Large circular progress indicator / ring with countdown time MM:SS
            val progress = if (totalTimeSeconds > 0) {
                remainingTimeSeconds.toFloat() / totalTimeSeconds.toFloat()
            } else 0f

            val minutes = remainingTimeSeconds / 60
            val seconds = remainingTimeSeconds % 60
            val locale = LocalConfiguration.current.locales[0]
            val formattedTime = String.format(locale, "%02d:%02d", minutes, seconds)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp),
            ) {
                // Background ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                )
                // Countdown progress ring
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRunning) "Running" else "Paused",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Action controls below timer: Play/Pause FAB and Reset OutlinedButton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                LargeFloatingActionButton(
                    onClick = onPlayPauseClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        modifier = Modifier.size(36.dp),
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
