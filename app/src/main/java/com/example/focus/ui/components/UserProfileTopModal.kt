package com.example.focus.ui.components

import android.graphics.Color
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.example.focus.data.local.SyncState
import com.example.focus.data.model.UserResponse
import com.example.focus.data.repository.AuthState

@Composable
fun UserProfileTopModal(
    isVisible: Boolean,
    authState: AuthState,
    syncState: SyncState = SyncState.Idle,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onTriggerSync: () -> Unit = {},
    onLogin: (identifier: String, password: String) -> Unit,
    onRegister: (identifier: String, password: String, fullName: String?) -> Unit,
    onUpdateProfile: (username: String?, email: String?, fullName: String?, avatarUrl: String?) -> Unit,
    onLogout: () -> Unit,
) {
    if (!isVisible) return

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
                window.setGravity(Gravity.TOP)
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                )
                window.setDimAmount(0.35f)
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 16.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
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
                                    text = if (authState is AuthState.Authenticated) "PROFILE" else "ACCOUNT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    IconButton(onClick = {
                                        onDismiss()
                                        onOpenSettings()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "App Settings",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
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
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        when (authState) {
                            is AuthState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            is AuthState.Authenticated -> {
                                AuthenticatedProfileContent(
                                    user = authState.user,
                                    syncState = syncState,
                                    onTriggerSync = onTriggerSync,
                                    onUpdateProfile = onUpdateProfile,
                                    onOpenSettings = {
                                        onDismiss()
                                        onOpenSettings()
                                    },
                                    onLogout = onLogout,
                                )
                            }

                            else -> {
                                val errorMessage = if (authState is AuthState.Error) authState.message else null
                                UnauthenticatedAuthContent(
                                    errorMessage = errorMessage,
                                    onLogin = onLogin,
                                    onRegister = onRegister,
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
private fun AuthenticatedProfileContent(
    user: UserResponse,
    syncState: SyncState,
    onTriggerSync: () -> Unit,
    onUpdateProfile: (username: String?, email: String?, fullName: String?, avatarUrl: String?) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }

    var fullNameInput by remember(user.full_name) { mutableStateOf(user.full_name ?: "") }
    var usernameInput by remember(user.username) { mutableStateOf(user.username ?: "") }
    var emailInput by remember(user.email) { mutableStateOf(user.email ?: "") }
    var avatarUrlInput by remember(user.avatar_url) { mutableStateOf(user.avatar_url ?: "") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // User Avatar / Initials Circle
        val avatar = user.avatar_url
        if (!avatar.isNullOrBlank()) {
            AsyncImage(
                model = avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape),
            )
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(88.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = (user.full_name ?: user.username ?: user.email ?: "F")
                        .take(2)
                        .uppercase()
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        if (!isEditing) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = user.full_name ?: user.username ?: "Focus Member",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                val emailText = user.email
                if (!emailText.isNullOrBlank()) {
                    Text(
                        text = emailText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val usernameText = user.username
                if (!usernameText.isNullOrBlank()) {
                    Text(
                        text = "@$usernameText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Sync Status Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text(
                                text = "Offline-First Sync",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = when (syncState) {
                                    is SyncState.Syncing -> "Syncing with cloud..."
                                    is SyncState.Success -> syncState.message
                                    is SyncState.Error -> "Offline Mode (Saved locally)"
                                    else -> "Ready to sync"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(onClick = onTriggerSync) {
                        if (syncState is SyncState.Syncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Now",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings")
                    }
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = fullNameInput,
                    onValueChange = { fullNameInput = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onUpdateProfile(
                                usernameInput.ifBlank { null },
                                emailInput.ifBlank { null },
                                fullNameInput.ifBlank { null },
                                avatarUrlInput.ifBlank { null },
                            )
                            isEditing = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedAuthContent(
    errorMessage: String?,
    onLogin: (identifier: String, password: String) -> Unit,
    onRegister: (identifier: String, password: String, fullName: String?) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Minimalist Tab Selector: Sign In | Create Account
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (selectedTab == 0) {
            // Sign In Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = identifierInput,
                    onValueChange = { identifierInput = it },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                Button(
                    onClick = {
                        if (identifierInput.isNotBlank() && passwordInput.isNotBlank()) {
                            onLogin(identifierInput.trim(), passwordInput)
                        }
                    },
                    enabled = identifierInput.isNotBlank() && passwordInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        } else {
            // Register Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = fullNameInput,
                    onValueChange = { fullNameInput = it },
                    label = { Text("Full Name (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                OutlinedTextField(
                    value = identifierInput,
                    onValueChange = { identifierInput = it },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password (min 6 chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                )

                Button(
                    onClick = {
                        if (identifierInput.isNotBlank() && passwordInput.length >= 6) {
                            onRegister(
                                identifierInput.trim(),
                                passwordInput,
                                fullNameInput.ifBlank { null },
                            )
                        }
                    },
                    enabled = identifierInput.isNotBlank() && passwordInput.length >= 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
