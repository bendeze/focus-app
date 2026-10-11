package com.example.focus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus.data.local.SyncState
import com.example.focus.data.model.FocusSession
import com.example.focus.data.model.FocusStats
import com.example.focus.data.model.Project
import com.example.focus.data.model.TaskItem
import com.example.focus.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)

    val projects: StateFlow<List<Project>> = repository.projects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val tasks: StateFlow<List<TaskItem>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val selectedTask: StateFlow<TaskItem?> = repository.selectedTask
    val activeSession: StateFlow<FocusSession?> = repository.activeSession
    val focusStats: StateFlow<FocusStats?> = repository.focusStats
    val syncState: StateFlow<SyncState> = repository.syncEngine.syncState

    fun loadData() {
        viewModelScope.launch {
            repository.triggerSync()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerSync()
        }
    }

    fun selectTask(task: TaskItem?) {
        repository.selectTaskForFocus(task)
    }

    fun createProject(
        title: String,
        description: String? = null,
        onResult: (Result<Project>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.createProject(title, description)
            onResult(result)
        }
    }

    fun deleteProject(projectId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteProject(projectId)
            onResult(result)
        }
    }

    fun createTask(
        title: String,
        description: String? = null,
        projectId: String? = null,
        priority: String? = "medium",
        estimatedMinutes: Int? = 25,
        onResult: (Result<TaskItem>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.createTask(title, description, projectId, priority, estimatedMinutes)
            onResult(result)
        }
    }

    fun updateTaskEstimatedMinutes(taskId: String, estimatedMinutes: Int, onResult: (Result<TaskItem>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.updateTaskEstimatedMinutes(taskId, estimatedMinutes)
            onResult(result)
        }
    }

    fun updateTaskStatus(taskId: String, status: String, onResult: (Result<TaskItem>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateTaskStatus(taskId, status)
            onResult(result)
        }
    }

    fun deleteTask(taskId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)
            onResult(result)
        }
    }

    fun startFocusSession(
        taskId: String?,
        durationMinutes: Int,
        onResult: (Result<FocusSession>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.startFocusSession(taskId, durationMinutes)
            onResult(result)
        }
    }

    fun completeFocusSession(
        sessionId: String,
        actualSeconds: Int,
        notes: String? = null,
        onResult: (Result<FocusSession>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.completeFocusSession(sessionId, actualSeconds, notes)
            onResult(result)
        }
    }

    fun cancelFocusSession(sessionId: String) {
        viewModelScope.launch {
            repository.cancelFocusSession(sessionId)
        }
    }
}
