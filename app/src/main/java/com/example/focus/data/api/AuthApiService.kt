package com.example.focus.data.api

import com.example.focus.data.model.MessageResponse
import com.example.focus.data.model.TokenResponse
import com.example.focus.data.model.UserLogin
import com.example.focus.data.model.UserRegister
import com.example.focus.data.model.UserResponse
import com.example.focus.data.model.UserUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: UserRegister): Response<TokenResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: UserLogin): Response<TokenResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserResponse>

    @PATCH("api/v1/auth/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: UserUpdate,
    ): Response<UserResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<MessageResponse>
}
