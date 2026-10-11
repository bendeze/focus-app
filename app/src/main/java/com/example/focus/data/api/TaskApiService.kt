package com.example.focus.data.api

import com.example.focus.data.model.Project
import com.example.focus.data.model.ProjectCreate
import com.example.focus.data.model.ProjectUpdate
import com.example.focus.data.model.TaskCreate
import com.example.focus.data.model.TaskItem
import com.example.focus.data.model.TaskUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApiService {

    // Projects Endpoints
    @POST("api/v1/projects")
    suspend fun createProject(
        @Header("Authorization") token: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
        @Body request: ProjectCreate,
    ): Response<Project>

    @GET("api/v1/projects")
    suspend fun getProjects(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("updated_after") updatedAfter: String? = null,
        @Query("include_deleted") includeDeleted: Boolean? = null,
    ): Response<List<Project>>

    @GET("api/v1/projects/{id}")
    suspend fun getProject(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<Project>

    @PATCH("api/v1/projects/{id}")
    suspend fun updateProject(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ProjectUpdate,
    ): Response<Project>

    @DELETE("api/v1/projects/{id}")
    suspend fun deleteProject(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<Unit>

    // Tasks Endpoints
    @POST("api/v1/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
        @Body request: TaskCreate,
    ): Response<TaskItem>

    @GET("api/v1/tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Query("standalone") standalone: Boolean? = null,
        @Query("project_id") projectId: String? = null,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("updated_after") updatedAfter: String? = null,
        @Query("include_deleted") includeDeleted: Boolean? = null,
    ): Response<List<TaskItem>>

    @PATCH("api/v1/tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: TaskUpdate,
    ): Response<TaskItem>

    @DELETE("api/v1/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<Unit>
}
