package com.example.focus.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Project(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val user_id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String = "active",
    @Json(name = "color") val color: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "is_deleted") val is_deleted: Boolean = false,
    @Json(name = "deleted_at") val deleted_at: String? = null,
    @Json(name = "created_at") val created_at: String? = null,
    @Json(name = "updated_at") val updated_at: String? = null,
    @Json(name = "total_tasks") val total_tasks: Int = 0,
    @Json(name = "completed_tasks") val completed_tasks: Int = 0,
    @Json(name = "total_focus_minutes") val total_focus_minutes: Int = 0,
)

@JsonClass(generateAdapter = true)
data class ProjectCreate(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "icon") val icon: String? = null,
)

@JsonClass(generateAdapter = true)
data class ProjectUpdate(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "icon") val icon: String? = null,
)

@JsonClass(generateAdapter = true)
data class TaskItem(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val user_id: String,
    @Json(name = "project_id") val project_id: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String = "todo",
    @Json(name = "priority") val priority: String = "medium",
    @Json(name = "estimated_minutes") val estimated_minutes: Int = 25,
    @Json(name = "order_index") val order_index: Int = 0,
    @Json(name = "is_deleted") val is_deleted: Boolean = false,
    @Json(name = "deleted_at") val deleted_at: String? = null,
    @Json(name = "completed_at") val completed_at: String? = null,
    @Json(name = "created_at") val created_at: String? = null,
    @Json(name = "updated_at") val updated_at: String? = null,
    @Json(name = "total_focus_minutes") val total_focus_minutes: Int = 0,
    @Json(name = "focus_session_count") val focus_session_count: Int = 0,
)

@JsonClass(generateAdapter = true)
data class TaskCreate(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "project_id") val project_id: String? = null,
    @Json(name = "priority") val priority: String? = "medium",
    @Json(name = "estimated_minutes") val estimated_minutes: Int? = 25,
)

@JsonClass(generateAdapter = true)
data class TaskUpdate(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "priority") val priority: String? = null,
    @Json(name = "estimated_minutes") val estimated_minutes: Int? = null,
    @Json(name = "project_id") val project_id: String? = null,
)

@JsonClass(generateAdapter = true)
data class FocusSession(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val user_id: String,
    @Json(name = "task_id") val task_id: String? = null,
    @Json(name = "duration_minutes") val duration_minutes: Int,
    @Json(name = "started_at") val started_at: String? = null,
    @Json(name = "ended_at") val ended_at: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "actual_focus_seconds") val actual_focus_seconds: Int = 0,
    @Json(name = "auto_complete_task") val auto_complete_task: Boolean = true,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "is_deleted") val is_deleted: Boolean = false,
    @Json(name = "deleted_at") val deleted_at: String? = null,
    @Json(name = "created_at") val created_at: String? = null,
    @Json(name = "updated_at") val updated_at: String? = null,
)

@JsonClass(generateAdapter = true)
data class FocusSessionStart(
    @Json(name = "task_id") val task_id: String? = null,
    @Json(name = "duration_minutes") val duration_minutes: Int,
    @Json(name = "auto_complete_task") val auto_complete_task: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class FocusSessionComplete(
    @Json(name = "actual_focus_seconds") val actual_focus_seconds: Int,
    @Json(name = "notes") val notes: String? = null,
)

@JsonClass(generateAdapter = true)
data class FocusStats(
    @Json(name = "total_focus_minutes") val total_focus_minutes: Int = 0,
    @Json(name = "total_sessions_count") val total_sessions_count: Int = 0,
    @Json(name = "completed_sessions_count") val completed_sessions_count: Int = 0,
    @Json(name = "completed_tasks_count") val completed_tasks_count: Int = 0,
    @Json(name = "active_session") val active_session: FocusSession? = null,
)
