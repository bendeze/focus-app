package com.example.focus.viewmodel

import android.app.Application
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
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
        }
    )
    val dontAskDndPermission: StateFlow<Boolean> = _dontAskDndPermission.asStateFlow()

    private var timerJob: Job? = null

    fun setDontAskDndPermission(dontAsk: Boolean) {
        _dontAskDndPermission.value = dontAsk
        try {
            prefs?.edit()?.putBoolean("dont_ask_dnd_permission", dontAsk)?.apply()
        } catch (_: Throwable) {
            // Safe fallback for unit tests / mocked context
        }
    }

    fun play() {
        if (_isRunning.value) return
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (isActive && _remainingTimeSeconds.value > 0) {
                delay(1000L)
                _remainingTimeSeconds.value -= 1
            }
            if (_remainingTimeSeconds.value <= 0 && _isRunning.value) {
                _isRunning.value = false
                onTimerCompleted()
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
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
        _remainingTimeSeconds.value = _timerMode.value.durationInSeconds
    }

    fun setMode(mode: TimerMode) {
        pause()
        _timerMode.value = mode
        _totalTimeSeconds.value = mode.durationInSeconds
        _remainingTimeSeconds.value = mode.durationInSeconds
    }

    private suspend fun onTimerCompleted() {
        playNotificationSound()
        val message = when (_timerMode.value) {
            TimerMode.FOCUS -> "Focus session complete! Take a break."
            TimerMode.SHORT_BREAK -> "Short break finished! Ready to focus?"
            TimerMode.LONG_BREAK -> "Long break finished! Ready to focus?"
        }
        _completionEvent.emit(TimerCompletionEvent.Completed(_timerMode.value, message))
    }

    fun playNotificationSound() {
        try {
            val notificationUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (notificationUri != null) {
                val ringtone = RingtoneManager.getRingtone(getApplication(), notificationUri)
                ringtone?.play()
            }
        } catch (_: Throwable) {
            // Fallback for non-Android environments (e.g. unit tests)
        }
    }
}
