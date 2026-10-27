package com.example.focus.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRegister(
    @Json(name = "identifier") val identifier: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "password") val password: String,
    @Json(name = "full_name") val full_name: String? = null,
)

@JsonClass(generateAdapter = true)
data class UserLogin(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class UserUpdate(
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "full_name") val full_name: String? = null,
    @Json(name = "avatar_url") val avatar_url: String? = null,
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "full_name") val full_name: String? = null,
    @Json(name = "avatar_url") val avatar_url: String? = null,
    @Json(name = "is_active") val is_active: Boolean,
    @Json(name = "role") val role: String = "user",
    @Json(name = "is_admin") val is_admin: Boolean = false,
    @Json(name = "created_at") val created_at: String,
    @Json(name = "updated_at") val updated_at: String,
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val access_token: String,
    @Json(name = "token_type") val token_type: String = "bearer",
    @Json(name = "expires_in_seconds") val expires_in_seconds: Int,
    @Json(name = "user") val user: UserResponse,
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message") val message: String,
)
