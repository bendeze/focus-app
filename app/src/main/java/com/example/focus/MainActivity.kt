package com.example.focus

import android.app.NotificationManager
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.focus.ui.screen.FocusTimerScreen
import com.example.focus.ui.theme.FocusTheme
import com.example.focus.util.BiometricPromptManager
import com.example.focus.util.BiometricResult

class MainActivity : FragmentActivity() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var biometricManager: BiometricPromptManager

    private var previousInterruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL
    private var previousRingerMode: Int = AudioManager.RINGER_MODE_NORMAL
    private var previousRingVolume: Int = -1
    private var previousNotificationVolume: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        biometricManager = BiometricPromptManager(this)

        enableEdgeToEdge()
        setContent {
            FocusTheme {
                FocusTimerScreen(
                    onRequestLockTask = { startFocusLock() },
                    onRequestUnlockTask = { stopFocusLock() },
                    onRequestDndPermission = { requestDndPermission() },
                    hasDndPermission = { hasDndPermission() },
                    onTriggerBiometrics = { onSuccess ->
                        biometricManager.showBiometricPrompt { result ->
                            if (result is BiometricResult.AuthenticationSuccess) {
                                onSuccess()
                            }
                        }
                    },
                )
            }
        }
    }

    private fun hasDndPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestDndPermission() {
        stopFocusLock()
        if (!hasDndPermission()) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    startActivity(fallbackIntent)
                } catch (_: Exception) {
                    try {
                        val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(appDetailsIntent)
                    } catch (_: Exception) {
                        try {
                            val generalSettingsIntent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(generalSettingsIntent)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    private fun startFocusLock() {
        // Keep screen on while timer is running so the app stays visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Lock task to pin the screen and prevent leaving/switching apps
        try {
            startLockTask()
        } catch (_: Exception) {
            // Screen pinning might fail if not supported or disallowed
        }

        // Explicitly mute ringtone and notification audio streams
        try {
            previousRingerMode = audioManager.ringerMode
            previousRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            previousNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // Set Do Not Disturb mode to NONE to block all incoming calls, messages and notifications
        try {
            if (hasDndPermission()) {
                previousInterruptionFilter = notificationManager.currentInterruptionFilter
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    private fun stopFocusLock() {
        // Clear keep screen on flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Unpin screen
        try {
            stopLockTask()
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // Restore previous Do Not Disturb setting
        try {
            if (hasDndPermission()) {
                notificationManager.setInterruptionFilter(previousInterruptionFilter)
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // Restore previous ringer mode and stream volumes
        try {
            audioManager.ringerMode = previousRingerMode
            if (previousRingVolume >= 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, previousRingVolume, 0)
            }
            if (previousNotificationVolume >= 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, previousNotificationVolume, 0)
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFocusLock()
    }
}
