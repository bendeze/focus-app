package com.example.focus.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.focus.data.api.FocusApiService
import com.example.focus.data.api.NetworkClient
import com.example.focus.data.api.TaskApiService
import com.example.focus.data.model.FocusSessionComplete
import com.example.focus.data.model.FocusSessionStart
import com.example.focus.data.model.ProjectCreate
import com.example.focus.data.model.ProjectUpdate
import com.example.focus.data.model.TaskCreate
import com.example.focus.data.model.TaskUpdate
import com.example.focus.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

class SyncEngine(
    context: Context,
    private val database: FocusDatabase = FocusDatabase.getInstance(context),
    private val taskApi: TaskApiService = NetworkClient.taskApiService,
    private val focusApi: FocusApiService = NetworkClient.focusApiService,
) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val authRepository = AuthRepository(context)

    private val syncMutex = Mutex()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private fun getStoredToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getLastSyncedAt(): String? {
        return prefs.getString("last_synced_at", null)
    }

    private fun saveLastSyncedAt(timestamp: String) {
        prefs.edit().putString("last_synced_at", timestamp).apply()
    }

    suspend fun sync(onIdReplaced: ((oldId: String, newId: String) -> Unit)? = null): Result<Unit> = withContext(Dispatchers.IO) {
        // Prevent concurrent sync executions (prevents task duplication on sync)
        if (!syncMutex.tryLock()) {
            return@withContext Result.success(Unit)
        }

        try {
            authRepository.syncPendingAuthIfOnline()

            val token = getStoredToken() ?: run {
                _syncState.value = SyncState.Idle
                return@withContext Result.failure(Exception("Not authenticated"))
            }

            _syncState.value = SyncState.Syncing

            val projectDao = database.projectDao()
            val taskDao = database.taskDao()
            val sessionDao = database.focusSessionDao()

            // Step 1: Push Local Mutations to Remote Server
            // Push Pending Projects
            val pendingProjects = projectDao.getPendingProjects()
            for (p in pendingProjects) {
                when (p.syncStatus) {
                    SyncStatus.PENDING_INSERT -> {
                        val response = taskApi.createProject(
                            token = "Bearer $token",
                            idempotencyKey = p.id,
                            request = ProjectCreate(title = p.title, description = p.description),
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val serverP = response.body()!!
                            database.withTransaction {
                                projectDao.deleteProjectById(p.id)
                                projectDao.upsertProject(serverP.toEntity(SyncStatus.SYNCED))
                            }
                            onIdReplaced?.invoke(p.id, serverP.id)
                        }
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val response = taskApi.updateProject("Bearer $token", p.id, ProjectUpdate(title = p.title, description = p.description, status = p.status))
                        if (response.isSuccessful && response.body() != null) {
                            projectDao.upsertProject(response.body()!!.toEntity(SyncStatus.SYNCED))
                        }
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val response = taskApi.deleteProject("Bearer $token", p.id)
                        if (response.isSuccessful) {
                            projectDao.deleteProjectById(p.id)
                        }
                    }

                    else -> {}
                }
            }

            // Push Pending Tasks
            val pendingTasks = taskDao.getPendingTasks()
            for (t in pendingTasks) {
                when (t.syncStatus) {
                    SyncStatus.PENDING_INSERT -> {
                        val response = taskApi.createTask(
                            token = "Bearer $token",
                            idempotencyKey = t.id,
                            request = TaskCreate(
                                title = t.title,
                                description = t.description,
                                project_id = t.projectId,
                                priority = t.priority,
                                estimated_minutes = t.estimatedMinutes,
                            ),
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val serverT = response.body()!!
                            var finalTask = serverT
                            // If status was updated offline to in_progress or completed, update backend status!
                            if (t.status != "todo") {
                                val patchResp = taskApi.updateTask(
                                    token = "Bearer $token",
                                    id = serverT.id,
                                    request = TaskUpdate(status = t.status),
                                )
                                if (patchResp.isSuccessful && patchResp.body() != null) {
                                    finalTask = patchResp.body()!!
                                } else {
                                    finalTask = serverT.copy(status = t.status)
                                }
                            }
                            database.withTransaction {
                                taskDao.deleteTaskById(t.id)
                                taskDao.upsertTask(finalTask.toEntity(SyncStatus.SYNCED))
                            }
                            onIdReplaced?.invoke(t.id, finalTask.id)
                        }
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val response = taskApi.updateTask(
                            "Bearer $token",
                            t.id,
                            TaskUpdate(
                                title = t.title,
                                description = t.description,
                                status = t.status,
                                priority = t.priority,
                                estimated_minutes = t.estimatedMinutes,
                                project_id = t.projectId,
                            ),
                        )
                        if (response.isSuccessful && response.body() != null) {
                            taskDao.upsertTask(response.body()!!.toEntity(SyncStatus.SYNCED))
                        }
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val response = taskApi.deleteTask("Bearer $token", t.id)
                        if (response.isSuccessful) {
                            taskDao.deleteTaskById(t.id)
                        }
                    }

                    else -> {}
                }
            }

            // Push Pending Focus Sessions
            val pendingSessions = sessionDao.getPendingSessions()
            for (s in pendingSessions) {
                when (s.syncStatus) {
                    SyncStatus.PENDING_INSERT -> {
                        val response = focusApi.startSession(
                            token = "Bearer $token",
                            idempotencyKey = s.id,
                            request = FocusSessionStart(
                                task_id = s.taskId,
                                duration_minutes = s.durationMinutes,
                                auto_complete_task = s.autoCompleteTask,
                            ),
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val serverS = response.body()!!
                            database.withTransaction {
                                sessionDao.deleteSessionById(s.id)
                                sessionDao.upsertSession(serverS.toEntity(SyncStatus.SYNCED))
                            }
                            onIdReplaced?.invoke(s.id, serverS.id)
                        }
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val response = focusApi.completeSession(
                            "Bearer $token",
                            s.id,
                            FocusSessionComplete(
                                actual_focus_seconds = s.actualFocusSeconds,
                                notes = s.notes,
                            ),
                        )
                        if (response.isSuccessful && response.body() != null) {
                            sessionDao.upsertSession(response.body()!!.toEntity(SyncStatus.SYNCED))
                        }
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val response = focusApi.cancelSession("Bearer $token", s.id)
                        if (response.isSuccessful) {
                            sessionDao.deleteSessionById(s.id)
                        }
                    }

                    else -> {}
                }
            }

            // Step 2: Pull Delta Updates from Remote Server
            val lastSyncedAt = getLastSyncedAt()

            // Pull Projects Delta
            val projResponse = taskApi.getProjects(
                token = "Bearer $token",
                updatedAfter = lastSyncedAt,
                includeDeleted = true,
            )
            if (projResponse.isSuccessful && projResponse.body() != null) {
                database.withTransaction {
                    for (p in projResponse.body()!!) {
                        val localItem = projectDao.getProjectById(p.id)
                        if (p.is_deleted) {
                            projectDao.deleteProjectById(p.id)
                        } else if (localItem == null || localItem.syncStatus == SyncStatus.SYNCED) {
                            projectDao.upsertProject(p.toEntity(SyncStatus.SYNCED))
                        }
                    }
                }
            }

            // Pull Tasks Delta
            val taskResponse = taskApi.getTasks(
                token = "Bearer $token",
                updatedAfter = lastSyncedAt,
                includeDeleted = true,
            )
            if (taskResponse.isSuccessful && taskResponse.body() != null) {
                database.withTransaction {
                    for (t in taskResponse.body()!!) {
                        val localItem = taskDao.getTaskById(t.id)
                        if (t.is_deleted) {
                            taskDao.deleteTaskById(t.id)
                        } else if (localItem == null || localItem.syncStatus == SyncStatus.SYNCED) {
                            taskDao.upsertTask(t.toEntity(SyncStatus.SYNCED))
                        }
                    }
                }
            }

            // Pull Focus Sessions Delta
            val sessResponse = focusApi.getSessions(
                token = "Bearer $token",
                updatedAfter = lastSyncedAt,
                includeDeleted = true,
            )
            if (sessResponse.isSuccessful && sessResponse.body() != null) {
                database.withTransaction {
                    for (s in sessResponse.body()!!) {
                        val localItem = sessionDao.getSessionById(s.id)
                        if (s.is_deleted) {
                            sessionDao.deleteSessionById(s.id)
                        } else if (localItem == null || localItem.syncStatus == SyncStatus.SYNCED) {
                            sessionDao.upsertSession(s.toEntity(SyncStatus.SYNCED))
                        }
                    }
                }
            }

            // Save Sync Time
            val currentIsoTimestamp = getCurrentIsoTimestamp()
            saveLastSyncedAt(currentIsoTimestamp)

            _syncState.value = SyncState.Success("Synced at ${formatDisplayTime(currentIsoTimestamp)}")
            Result.success(Unit)
        } catch (e: Throwable) {
            _syncState.value = SyncState.Error(e.localizedMessage ?: "Sync error")
            Result.failure(e)
        } finally {
            syncMutex.unlock()
        }
    }

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun formatDisplayTime(isoString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString) ?: return isoString
            val outFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outFormat.format(date)
        } catch (_: Throwable) {
            isoString
        }
    }
}
