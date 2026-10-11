package com.example.focus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.focus.data.model.Project

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = "active",
    val color: String? = null,
    val icon: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalFocusMinutes: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

fun ProjectEntity.toProject(): Project {
    return Project(
        id = id,
        user_id = userId,
        title = title,
        description = description,
        status = status,
        color = color,
        icon = icon,
        is_deleted = isDeleted,
        deleted_at = deletedAt,
        created_at = createdAt,
        updated_at = updatedAt,
        total_tasks = totalTasks,
        completed_tasks = completedTasks,
        total_focus_minutes = totalFocusMinutes,
    )
}

fun Project.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): ProjectEntity {
    return ProjectEntity(
        id = id,
        userId = user_id,
        title = title,
        description = description,
        status = status,
        color = color,
        icon = icon,
        isDeleted = is_deleted,
        deletedAt = deleted_at,
        createdAt = created_at,
        updatedAt = updated_at,
        totalTasks = total_tasks,
        completedTasks = completed_tasks,
        totalFocusMinutes = total_focus_minutes,
        syncStatus = syncStatus,
    )
}
