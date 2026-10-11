package com.example.focus.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus.model.TimerMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CountdownSoundStyle(val label: String) {
    SOFT_BEEP("Soft Beep"),
    GENTLE_TICK("Gentle Tick"),
    MUTE("Muted"),
}

enum class CompletionSoundStyle(val label: String) {
    NOTIFICATION("Notification"),
    ALARM("Alarm Ringtone"),
    MUTE("Muted"),
}

sealed class TimerCompletionEvent {
    data class Completed(val mode: TimerMode, val message: String) : TimerCompletionEvent()
}

class FocusTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val _timerMode = MutableStateFlow(TimerMode.FOCUS)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _totalTimeSeconds = MutableStateFlow(TimerMode.FOCUS.durationInSeconds)
    val totalTimeSeconds: StateFlow<Int> = _totalTimeSeconds.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow(TimerMode.FOCUS.durationInSeconds)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _completionEvent = MutableSharedFlow<TimerCompletionEvent>()
    val completionEvent: SharedFlow<TimerCompletionEvent> = _completionEvent.asSharedFlow()

    private val prefs by lazy {
        try {
            application.getSharedPreferences("focus_timer_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }

    private val _dontAskDndPermission = MutableStateFlow(
        try {
            prefs?.getBoolean("dont_ask_dnd_permission", false) ?: false
        } catch (_: Throwable) {
            false
        },
    )
    val dontAskDndPermission: StateFlow<Boolean> = _dontAskDndPermission.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(
        try {
            prefs?.getBoolean("vibration_enabled", true) ?: true
        } catch (_: Throwable) {
            true
        },
    )
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _countdownSoundStyle = MutableStateFlow(
        try {
            val name = prefs?.getString("countdown_sound_style", CountdownSoundStyle.SOFT_BEEP.name)
            CountdownSoundStyle.valueOf(name ?: CountdownSoundStyle.SOFT_BEEP.name)
        } catch (_: Throwable) {
            CountdownSoundStyle.SOFT_BEEP
        },
    )
    val countdownSoundStyle: StateFlow<CountdownSoundStyle> = _countdownSoundStyle.asStateFlow()

    private val _completionSoundStyle = MutableStateFlow(
        try {
            val name = prefs?.getString("completion_sound_style", CompletionSoundStyle.NOTIFICATION.name)
            CompletionSoundStyle.valueOf(name ?: CompletionSoundStyle.NOTIFICATION.name)
        } catch (_: Throwable) {
            CompletionSoundStyle.NOTIFICATION
        },
    )
    val completionSoundStyle: StateFlow<CompletionSoundStyle> = _completionSoundStyle.asStateFlow()

    private val _customMinutes = MutableStateFlow(
        try {
            prefs?.getInt("custom_minutes", 25) ?: 25
        } catch (_: Throwable) {
            25
        },
    )
    val customMinutes: StateFlow<Int> = _customMinutes.asStateFlow()

    private val _customSeconds = MutableStateFlow(
        try {
            prefs?.getInt("custom_seconds", 0) ?: 0
        } catch (_: Throwable) {
            0
        },
    )
    val customSeconds: StateFlow<Int> = _customSeconds.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activeRingtone: Ringtone? = null
    private var ringtoneStopJob: Job? = null

    init {
        val savedMin = try { prefs?.getInt("custom_minutes", 25) ?: 25 } catch (_: Throwable) { 25 }
        val savedSec = try { prefs?.getInt("custom_seconds", 0) ?: 0 } catch (_: Throwable) { 0 }
        val totalSec = (savedMin * 60) + savedSec
        TimerMode.CUSTOM.durationInSeconds = if (totalSec > 0) totalSec else 1500
    }

    private val vibrator by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            try {
                // Volume 40 for distinct and clear countdown ticks
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 40)
            } catch (_: Throwable) {
                // Fallback for non-Android / unit test environments
            }
        }
        return toneGenerator
    }

    fun triggerTickVibration() {
        if (!_isVibrationEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50L)
            }
        } catch (_: Throwable) {
            // Safe fallback for unit tests
        }
    }

    fun triggerCompletionVibration() {
        if (!_isVibrationEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 350, 150, 350, 150, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 350, 150, 350, 150, 500), -1)
            }
        } catch (_: Throwable) {
            // Safe fallback for unit tests
        }
    }

    private fun playCountdownTick(remainingSeconds: Int) {
        triggerTickVibration()
        if (_countdownSoundStyle.value == CountdownSoundStyle.MUTE) return

        try {
            val tg = getToneGenerator() ?: return
            when (_countdownSoundStyle.value) {
                CountdownSoundStyle.SOFT_BEEP -> {
                    if (remainingSeconds == 1) {
                        tg.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                    } else {
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
                    }
                }
                CountdownSoundStyle.GENTLE_TICK -> {
                    if (remainingSeconds == 1) {
                        tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 90)
                    } else {
                        tg.startTone(ToneGenerator.TONE_SUP_PIP, 50)
                    }
                }
                CountdownSoundStyle.MUTE -> {}
            }
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun setDontAskDndPermission(dontAsk: Boolean) {
        _dontAskDndPermission.value = dontAsk
        try {
            prefs?.edit()?.putBoolean("dont_ask_dnd_permission", dontAsk)?.apply()
        } catch (_: Throwable) {
            // Safe fallback for unit tests / mocked context
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        try {
            prefs?.edit()?.putBoolean("vibration_enabled", enabled)?.apply()
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun setCountdownSoundStyle(style: CountdownSoundStyle) {
        _countdownSoundStyle.value = style
        try {
            prefs?.edit()?.putString("countdown_sound_style", style.name)?.apply()
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun setCompletionSoundStyle(style: CompletionSoundStyle) {
        _completionSoundStyle.value = style
        try {
            prefs?.edit()?.putString("completion_sound_style", style.name)?.apply()
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun setCustomDuration(minutes: Int, seconds: Int) {
        pause()
        stopRingtone()
        triggerTickVibration()

        val totalSec = (minutes * 60) + seconds
        val validSec = if (totalSec > 0) totalSec else 1

        _customMinutes.value = minutes
        _customSeconds.value = seconds
        TimerMode.CUSTOM.durationInSeconds = validSec

        try {
            prefs?.edit()?.putInt("custom_minutes", minutes)?.putInt("custom_seconds", seconds)?.apply()
        } catch (_: Throwable) {
            // Safe fallback
        }

        _timerMode.value = TimerMode.CUSTOM
        _totalTimeSeconds.value = validSec
        _remainingTimeSeconds.value = validSec
    }

    fun play() {
        if (_isRunning.value) return
        stopRingtone()
        _isRunning.value = true
        triggerTickVibration()
        timerJob = viewModelScope.launch {
            while (isActive && _remainingTimeSeconds.value > 0) {
                delay(1000L)
                _remainingTimeSeconds.value -= 1
                val remaining = _remainingTimeSeconds.value
                if (remaining in 1..15) {
                    playCountdownTick(remaining)
                }
            }
            if (_remainingTimeSeconds.value <= 0 && _isRunning.value) {
                _isRunning.value = false
                onTimerCompleted()
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        stopRingtone()
        triggerTickVibration()
        timerJob?.cancel()
        timerJob = null
        _isRunning.value = false
    }

    fun togglePlayPause() {
        if (_isRunning.value) {
            pause()
        } else {
            play()
        }
    }

    fun reset() {
        pause()
        stopRingtone()
        triggerTickVibration()
        _remainingTimeSeconds.value = _timerMode.value.durationInSeconds
    }

    fun setMode(mode: TimerMode) {
        pause()
        stopRingtone()
        triggerTickVibration()
        _timerMode.value = mode
        _totalTimeSeconds.value = mode.durationInSeconds
        _remainingTimeSeconds.value = mode.durationInSeconds
    }

    private suspend fun onTimerCompleted() {
        triggerCompletionVibration()
        playNotificationSound()
        val message = when (_timerMode.value) {
            TimerMode.FOCUS -> "Focus session complete! Take a break."
            TimerMode.SHORT_BREAK -> "Short break finished! Ready to focus?"
            TimerMode.LONG_BREAK -> "Long break finished! Ready to focus?"
            TimerMode.CUSTOM -> "Custom timer session complete!"
        }
        _completionEvent.emit(TimerCompletionEvent.Completed(_timerMode.value, message))
    }

    fun playNotificationSound() {
        if (_completionSoundStyle.value == CompletionSoundStyle.MUTE) return
        try {
            stopRingtone()
            val isAlarm = _completionSoundStyle.value == CompletionSoundStyle.ALARM
            val type = if (isAlarm) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION
            var notificationUri: Uri? = RingtoneManager.getDefaultUri(type)
            if (notificationUri == null) {
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            if (notificationUri != null) {
                val context: Context = getApplication()
                val mp = MediaPlayer().apply {
                    setDataSource(context, notificationUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(if (isAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    isLooping = isAlarm
                    prepare()
                    start()
                }
                mediaPlayer = mp

                // Automatically stop alarm/ringtone after 1 minute (60 seconds)
                ringtoneStopJob = viewModelScope.launch {
                    delay(60_000L)
                    stopRingtone()
                }
            }
        } catch (_: Throwable) {
            // Fallback to RingtoneManager if MediaPlayer is unavailable
            try {
                val type = if (_completionSoundStyle.value == CompletionSoundStyle.ALARM) {
                    RingtoneManager.TYPE_ALARM
                } else {
                    RingtoneManager.TYPE_NOTIFICATION
                }
                val notificationUri = RingtoneManager.getDefaultUri(type)
                val ringtone = RingtoneManager.getRingtone(getApplication(), notificationUri)
                if (ringtone != null) {
                    activeRingtone = ringtone
                    ringtone.play()
                    ringtoneStopJob = viewModelScope.launch {
                        delay(60_000L)
                        stopRingtone()
                    }
                }
            } catch (_: Throwable) {
                // Fallback for non-Android environments (e.g. unit tests)
            }
        }
    }

    fun stopRingtone() {
        try {
            ringtoneStopJob?.cancel()
            ringtoneStopJob = null

            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null

            activeRingtone?.stop()
            activeRingtone = null
        } catch (_: Throwable) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRingtone()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Throwable) {
        }
    }
}
