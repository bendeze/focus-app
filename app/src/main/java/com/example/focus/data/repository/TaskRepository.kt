package com.example.focus.data.repository

import android.content.Context
import com.example.focus.data.local.FocusDatabase
import com.example.focus.data.local.FocusSessionEntity
import com.example.focus.data.local.ProjectEntity
import com.example.focus.data.local.SyncEngine
import com.example.focus.data.local.SyncStatus
import com.example.focus.data.local.TaskEntity
import com.example.focus.data.local.toEntity
import com.example.focus.data.local.toFocusSession
import com.example.focus.data.local.toProject
import com.example.focus.data.local.toTaskItem
import com.example.focus.data.model.FocusSession
import com.example.focus.data.model.FocusStats
import com.example.focus.data.model.Project
import com.example.focus.data.model.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class TaskRepository(
    context: Context,
    private val database: FocusDatabase = FocusDatabase.getInstance(context),
    val syncEngine: SyncEngine = SyncEngine(context, database),
) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private fun getCurrentUserId(): String {
        return prefs.getString("user_id", "local_user") ?: "local_user"
    }

    private val currentUserIdFlow = MutableStateFlow(getCurrentUserId())
    private val selectedTaskIdFlow = MutableStateFlow<String?>(null)

    fun refreshUserContext() {
        currentUserIdFlow.value = getCurrentUserId()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val projects: Flow<List<Project>> = currentUserIdFlow.flatMapLatest { userId ->
        database.projectDao().getProjectsForUser(userId)
    }.map { list ->
        list.map { it.toProject() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: Flow<List<TaskItem>> = currentUserIdFlow.flatMapLatest { userId ->
        database.taskDao().getTasksForUser(userId)
    }.map { list ->
        list.map { it.toTaskItem() }
    }

    val selectedTask: StateFlow<TaskItem?> = combine(tasks, selectedTaskIdFlow) { taskList, selectedId ->
        if (selectedId == null) null else taskList.find { it.id == selectedId }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val _activeSession = MutableStateFlow<FocusSession?>(null)
    val activeSession: StateFlow<FocusSession?> = _activeSession.asStateFlow()

    private val _focusStats = MutableStateFlow<FocusStats?>(null)
    val focusStats: StateFlow<FocusStats?> = _focusStats.asStateFlow()

    fun selectTaskForFocus(task: TaskItem?) {
        selectedTaskIdFlow.value = task?.id
    }

    fun triggerSync() {
        refreshUserContext()
        repositoryScope.launch {
            syncEngine.sync(
                onIdReplaced = { oldId, newId ->
                    if (selectedTaskIdFlow.value == oldId) {
                        selectedTaskIdFlow.value = newId
                    }
                },
            )
        }
    }

    suspend fun createProject(title: String, description: String? = null): Result<Project> {
        val currentUserId = getCurrentUserId()
        val projectId = UUID.randomUUID().toString()
        val currentIsoTime = getCurrentIsoTimestamp()
        val localProject = Project(
            id = projectId,
            user_id = currentUserId,
            title = title,
            description = description,
            status = "active",
            created_at = currentIsoTime,
            updated_at = currentIsoTime,
        )

        val entity = ProjectEntity(
            id = projectId,
            userId = currentUserId,
            title = title,
            description = description,
            status = "active",
            createdAt = currentIsoTime,
            updatedAt = currentIsoTime,
            syncStatus = SyncStatus.PENDING_INSERT,
        )

        database.projectDao().upsertProject(entity)
        triggerSync()
        return Result.success(localProject)
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        val existing = database.projectDao().getProjectById(projectId)
        if (existing != null) {
            val softDeletedEntity = existing.copy(
                isDeleted = true,
                deletedAt = getCurrentIsoTimestamp(),
                syncStatus = SyncStatus.PENDING_DELETE,
            )
            database.projectDao().upsertProject(softDeletedEntity)
            triggerSync()
        }
        return Result.success(Unit)
    }

    suspend fun createTask(
        title: String,
        description: String? = null,
        projectId: String? = null,
        priority: String? = "medium",
        estimatedMinutes: Int? = 25,
    ): Result<TaskItem> {
        val currentUserId = getCurrentUserId()
        val taskId = UUID.randomUUID().toString()
        val currentIsoTime = getCurrentIsoTimestamp()
        val localTask = TaskItem(
            id = taskId,
            user_id = currentUserId,
            project_id = projectId,
            title = title,
            description = description,
            status = "todo",
            priority = priority ?: "medium",
            estimated_minutes = estimatedMinutes ?: 25,
            created_at = currentIsoTime,
            updated_at = currentIsoTime,
        )

        val entity = TaskEntity(
            id = taskId,
            userId = currentUserId,
            projectId = projectId,
            title = title,
            description = description,
            status = "todo",
            priority = priority ?: "medium",
            estimatedMinutes = estimatedMinutes ?: 25,
            createdAt = currentIsoTime,
            updatedAt = currentIsoTime,
            syncStatus = SyncStatus.PENDING_INSERT,
        )

        database.taskDao().upsertTask(entity)
        triggerSync()
        return Result.success(localTask)
    }

    suspend fun updateTaskEstimatedMinutes(taskId: String, estimatedMinutes: Int): Result<TaskItem> {
        val activeSelectedId = selectedTaskIdFlow.value
        var existing = database.taskDao().getTaskById(taskId)
        if (existing == null && activeSelectedId != null) {
            existing = database.taskDao().getTaskById(activeSelectedId)
        }

        if (existing != null) {
            val updatedEntity = existing.copy(
                estimatedMinutes = estimatedMinutes,
                updatedAt = getCurrentIsoTimestamp(),
                syncStatus = if (existing.syncStatus == SyncStatus.PENDING_INSERT) SyncStatus.PENDING_INSERT else SyncStatus.PENDING_UPDATE,
            )
            database.taskDao().upsertTask(updatedEntity)
            triggerSync()
            return Result.success(updatedEntity.toTaskItem())
        }
        return Result.failure(Exception("Task not found locally"))
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Result<TaskItem> {
        val activeSelectedId = selectedTaskIdFlow.value
        var existing = database.taskDao().getTaskById(taskId)
        if (existing == null && activeSelectedId != null) {
            existing = database.taskDao().getTaskById(activeSelectedId)
        }

        if (existing != null) {
            val isCompleted = status == "completed"
            val updatedEntity = existing.copy(
                status = status,
                completedAt = if (isCompleted) getCurrentIsoTimestamp() else existing.completedAt,
                updatedAt = getCurrentIsoTimestamp(),
                syncStatus = if (existing.syncStatus == SyncStatus.PENDING_INSERT) SyncStatus.PENDING_INSERT else SyncStatus.PENDING_UPDATE,
            )
            database.taskDao().upsertTask(updatedEntity)
            triggerSync()
            return Result.success(updatedEntity.toTaskItem())
        }
        return Result.failure(Exception("Task not found locally"))
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        val existing = database.taskDao().getTaskById(taskId)
        if (existing != null) {
            val softDeletedEntity = existing.copy(
                isDeleted = true,
                deletedAt = getCurrentIsoTimestamp(),
                syncStatus = SyncStatus.PENDING_DELETE,
            )
            database.taskDao().upsertTask(softDeletedEntity)
            if (selectedTaskIdFlow.value == taskId) {
                selectedTaskIdFlow.value = null
            }
            triggerSync()
        }
        return Result.success(Unit)
    }

    suspend fun startFocusSession(taskId: String?, durationMinutes: Int): Result<FocusSession> {
        val currentUserId = getCurrentUserId()
        val sessionId = UUID.randomUUID().toString()
        val currentIsoTime = getCurrentIsoTimestamp()
        val localSession = FocusSession(
            id = sessionId,
            user_id = currentUserId,
            task_id = taskId,
            duration_minutes = durationMinutes,
            started_at = currentIsoTime,
            status = "active",
            created_at = currentIsoTime,
            updated_at = currentIsoTime,
        )

        val entity = FocusSessionEntity(
            id = sessionId,
            userId = currentUserId,
            taskId = taskId,
            durationMinutes = durationMinutes,
            startedAt = currentIsoTime,
            status = "active",
            createdAt = currentIsoTime,
            updatedAt = currentIsoTime,
            syncStatus = SyncStatus.PENDING_INSERT,
        )

        database.focusSessionDao().upsertSession(entity)
        _activeSession.value = localSession

        if (taskId != null) {
            updateTaskStatus(taskId, "in_progress")
        }

        triggerSync()
        return Result.success(localSession)
    }

    suspend fun completeFocusSession(sessionId: String, actualSeconds: Int, notes: String? = null): Result<FocusSession> {
        val existing = database.focusSessionDao().getSessionById(sessionId)
        val currentIsoTime = getCurrentIsoTimestamp()

        if (existing != null) {
            val updatedEntity = existing.copy(
                status = "completed",
                endedAt = currentIsoTime,
                actualFocusSeconds = actualSeconds,
                notes = notes,
                updatedAt = currentIsoTime,
                syncStatus = if (existing.syncStatus == SyncStatus.PENDING_INSERT) SyncStatus.PENDING_INSERT else SyncStatus.PENDING_UPDATE,
            )
            database.focusSessionDao().upsertSession(updatedEntity)
            _activeSession.value = null

            val targetTaskId = existing.taskId ?: selectedTaskIdFlow.value
            if (targetTaskId != null) {
                val focusedMinsAdded = (actualSeconds / 60).coerceAtLeast(1)
                val taskEntity = database.taskDao().getTaskById(targetTaskId)
                if (taskEntity != null) {
                    val updatedTaskEntity = taskEntity.copy(
                        status = "completed",
                        completedAt = currentIsoTime,
                        totalFocusMinutes = taskEntity.totalFocusMinutes + focusedMinsAdded,
                        focusSessionCount = taskEntity.focusSessionCount + 1,
                        updatedAt = currentIsoTime,
                        syncStatus = if (taskEntity.syncStatus == SyncStatus.PENDING_INSERT) SyncStatus.PENDING_INSERT else SyncStatus.PENDING_UPDATE,
                    )
                    database.taskDao().upsertTask(updatedTaskEntity)
                } else {
                    updateTaskStatus(targetTaskId, "completed")
                }
            }

            triggerSync()
            return Result.success(updatedEntity.toFocusSession())
        } else {
            val targetTaskId = selectedTaskIdFlow.value
            if (targetTaskId != null) {
                updateTaskStatus(targetTaskId, "completed")
            }
            triggerSync()
            return Result.failure(Exception("Session completed locally"))
        }
    }

    suspend fun cancelFocusSession(sessionId: String): Result<FocusSession> {
        val existing = database.focusSessionDao().getSessionById(sessionId)
        if (existing != null) {
            val updatedEntity = existing.copy(
                status = "cancelled",
                endedAt = getCurrentIsoTimestamp(),
                syncStatus = SyncStatus.PENDING_DELETE,
            )
            database.focusSessionDao().upsertSession(updatedEntity)
            _activeSession.value = null
            triggerSync()
            return Result.success(updatedEntity.toFocusSession())
        }
        return Result.failure(Exception("Session cancelled locally"))
    }

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
