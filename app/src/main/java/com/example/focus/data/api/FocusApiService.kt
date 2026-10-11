package com.example.focus.data.api

import com.example.focus.data.model.FocusSession
import com.example.focus.data.model.FocusSessionComplete
import com.example.focus.data.model.FocusSessionStart
import com.example.focus.data.model.FocusStats
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FocusApiService {

    @GET("api/v1/focus/current")
    suspend fun getCurrentSession(
        @Header("Authorization") token: String,
    ): Response<FocusSession?>

    @GET("api/v1/focus/sessions")
    suspend fun getSessions(
        @Header("Authorization") token: String,
        @Query("task_id") taskId: String? = null,
        @Query("status") status: String? = null,
        @Query("updated_after") updatedAfter: String? = null,
        @Query("include_deleted") includeDeleted: Boolean? = null,
    ): Response<List<FocusSession>>

    @POST("api/v1/focus/start")
    suspend fun startSession(
        @Header("Authorization") token: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
        @Body request: FocusSessionStart,
    ): Response<FocusSession>

    @POST("api/v1/focus/{id}/pause")
    suspend fun pauseSession(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<FocusSession>

    @POST("api/v1/focus/{id}/resume")
    suspend fun resumeSession(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<FocusSession>

    @POST("api/v1/focus/{id}/complete")
    suspend fun completeSession(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: FocusSessionComplete,
    ): Response<FocusSession>

    @POST("api/v1/focus/{id}/cancel")
    suspend fun cancelSession(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<FocusSession>

    @GET("api/v1/focus/stats")
    suspend fun getStats(
        @Header("Authorization") token: String,
    ): Response<FocusStats>
}
