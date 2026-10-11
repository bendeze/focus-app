package com.example.focus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.focus.data.model.TaskItem

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val projectId: String? = null,
    val title: String,
    val description: String? = null,
    val status: String = "todo",
    val priority: String = "medium",
    val estimatedMinutes: Int = 25,
    val orderIndex: Int = 0,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val totalFocusMinutes: Int = 0,
    val focusSessionCount: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

fun TaskEntity.toTaskItem(): TaskItem {
    return TaskItem(
        id = id,
        user_id = userId,
        project_id = projectId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        estimated_minutes = estimatedMinutes,
        order_index = orderIndex,
        is_deleted = isDeleted,
        deleted_at = deletedAt,
        completed_at = completedAt,
        created_at = createdAt,
        updated_at = updatedAt,
        total_focus_minutes = totalFocusMinutes,
        focus_session_count = focusSessionCount,
    )
}

fun TaskItem.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): TaskEntity {
    return TaskEntity(
        id = id,
        userId = user_id,
        projectId = project_id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        estimatedMinutes = estimated_minutes,
        orderIndex = order_index,
        isDeleted = is_deleted,
        deletedAt = deleted_at,
        completedAt = completed_at,
        createdAt = created_at,
        updatedAt = updated_at,
        totalFocusMinutes = total_focus_minutes,
        focusSessionCount = focus_session_count,
        syncStatus = syncStatus,
    )
}
