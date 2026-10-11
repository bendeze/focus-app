package com.example.focus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.focus.data.model.FocusSession

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val taskId: String? = null,
    val durationMinutes: Int,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val status: String,
    val actualFocusSeconds: Int = 0,
    val autoCompleteTask: Boolean = true,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

fun FocusSessionEntity.toFocusSession(): FocusSession {
    return FocusSession(
        id = id,
        user_id = userId,
        task_id = taskId,
        duration_minutes = durationMinutes,
        started_at = startedAt,
        ended_at = endedAt,
        status = status,
        actual_focus_seconds = actualFocusSeconds,
        auto_complete_task = autoCompleteTask,
        notes = notes,
        is_deleted = isDeleted,
        deleted_at = deletedAt,
        created_at = createdAt,
        updated_at = updatedAt,
    )
}

fun FocusSession.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        userId = user_id,
        taskId = task_id,
        durationMinutes = duration_minutes,
        startedAt = started_at,
        endedAt = ended_at,
        status = status,
        actualFocusSeconds = actual_focus_seconds,
        autoCompleteTask = auto_complete_task,
        notes = notes,
        isDeleted = is_deleted,
        deletedAt = deleted_at,
        createdAt = created_at,
        updatedAt = updated_at,
        syncStatus = syncStatus,
    )
}
