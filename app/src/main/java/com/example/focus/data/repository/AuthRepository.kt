package com.example.focus.data.repository

import android.content.Context
import com.example.focus.data.api.AuthApiService
import com.example.focus.data.api.NetworkClient
import com.example.focus.data.model.UserLogin
import com.example.focus.data.model.UserRegister
import com.example.focus.data.model.UserResponse
import com.example.focus.data.model.UserUpdate
import com.example.focus.util.NetworkUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository(
    private val context: Context,
    private val apiService: AuthApiService = NetworkClient.authApiService,
) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(UserResponse::class.java)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun getStoredToken(): String? {
        return prefs.getString("access_token", null)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveTokenAndUser(
        token: String,
        user: UserResponse,
        identifier: String? = null,
        password: String? = null,
        isPendingRegistration: Boolean = false,
        isPendingLogin: Boolean = false,
    ) {
        val userJson = userAdapter.toJson(user)
        val editor = prefs.edit()
            .putString("access_token", token)
            .putString("user_id", user.id)
            .putString("cached_user_json", userJson)
            .putBoolean("is_pending_registration", isPendingRegistration)
            .putBoolean("is_pending_login", isPendingLogin)

        if (!identifier.isNullOrBlank()) {
            editor.putString("saved_identifier", identifier.lowercase().trim())
        }
        if (!password.isNullOrBlank()) {
            editor.putString("saved_password_hash", hashPassword(password))
            editor.putString("raw_password", password)
        }
        editor.apply()
    }

    private fun getCachedUserProfile(): UserResponse? {
        val userJson = prefs.getString("cached_user_json", null) ?: return null
        return try {
            userAdapter.fromJson(userJson)
        } catch (_: Throwable) {
            null
        }
    }

    private fun clearSessionTokenOnly() {
        // Keeps saved_identifier, saved_password_hash & cached_user_json so user CAN sign in offline again anytime!
        prefs.edit().remove("access_token").apply()
    }

    suspend fun syncPendingAuthIfOnline(): Boolean {
        if (!NetworkUtils.isOnline(context)) return false

        val isPendingReg = prefs.getBoolean("is_pending_registration", false)
        val isPendingLog = prefs.getBoolean("is_pending_login", false)
        val savedIdentifier = prefs.getString("saved_identifier", null)
        val rawPassword = prefs.getString("raw_password", null)

        if (isPendingReg && !savedIdentifier.isNullOrBlank() && !rawPassword.isNullOrBlank()) {
            try {
                val cachedUser = getCachedUserProfile()
                val request = UserRegister(
                    identifier = savedIdentifier,
                    username = cachedUser?.username ?: savedIdentifier,
                    email = cachedUser?.email,
                    password = rawPassword,
                    full_name = cachedUser?.full_name,
                )
                val response = apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val tokenResp = response.body()!!
                    saveTokenAndUser(tokenResp.access_token, tokenResp.user, savedIdentifier, rawPassword)
                    _authState.value = AuthState.Authenticated(tokenResp.user)
                    return true
                }
            } catch (_: Throwable) {
            }
        } else if (isPendingLog && !savedIdentifier.isNullOrBlank() && !rawPassword.isNullOrBlank()) {
            try {
                val request = UserLogin(identifier = savedIdentifier, password = rawPassword)
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val tokenResp = response.body()!!
                    saveTokenAndUser(tokenResp.access_token, tokenResp.user, savedIdentifier, rawPassword)
                    _authState.value = AuthState.Authenticated(tokenResp.user)
                    return true
                }
            } catch (_: Throwable) {
            }
        }
        return false
    }

    suspend fun checkAuthStatus() {
        val token = getStoredToken()
        val cachedUser = getCachedUserProfile()

        if (token.isNullOrBlank() && cachedUser == null) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        _authState.value = AuthState.Loading

        if (!NetworkUtils.isOnline(context)) {
            // Offline Mode: authenticate locally with cached user profile
            if (cachedUser != null) {
                _authState.value = AuthState.Authenticated(cachedUser)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
            return
        }

        // Try syncing pending offline auth if online
        val synced = syncPendingAuthIfOnline()
        if (synced) return

        if (token.isNullOrBlank()) {
            if (cachedUser != null) {
                _authState.value = AuthState.Authenticated(cachedUser)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
            return
        }

        try {
            val response = apiService.getMe("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                saveTokenAndUser(token, user)
                _authState.value = AuthState.Authenticated(user)
            } else if (response.code() == 401 || response.code() == 403) {
                // Invalid token
                clearSessionTokenOnly()
                if (cachedUser != null) {
                    _authState.value = AuthState.Authenticated(cachedUser)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                if (cachedUser != null) {
                    _authState.value = AuthState.Authenticated(cachedUser)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } catch (_: Throwable) {
            if (cachedUser != null) {
                _authState.value = AuthState.Authenticated(cachedUser)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    suspend fun register(
        identifier: String?,
        username: String?,
        email: String?,
        password: String,
        fullName: String?,
    ): Result<UserResponse> {
        _authState.value = AuthState.Loading

        val targetIdentifier = (identifier ?: username ?: email ?: "user").trim()

        if (!NetworkUtils.isOnline(context)) {
            // Offline Account Creation: create local user profile instantly!
            val localId = UUID.randomUUID().toString()
            val currentIsoTime = getCurrentIsoTimestamp()
            val localUser = UserResponse(
                id = localId,
                username = username ?: targetIdentifier,
                email = email ?: if (targetIdentifier.contains("@")) targetIdentifier else null,
                full_name = fullName,
                avatar_url = null,
                is_active = true,
                created_at = currentIsoTime,
                updated_at = currentIsoTime,
            )

            saveTokenAndUser(
                token = "offline_token_$localId",
                user = localUser,
                identifier = targetIdentifier,
                password = password,
                isPendingRegistration = true,
            )

            _authState.value = AuthState.Authenticated(localUser)
            return Result.success(localUser)
        }

        // Online Registration
        return try {
            val request = UserRegister(
                identifier = identifier,
                username = username,
                email = email,
                password = password,
                full_name = fullName,
            )
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                saveTokenAndUser(tokenResponse.access_token, tokenResponse.user, targetIdentifier, password)
                _authState.value = AuthState.Authenticated(tokenResponse.user)
                Result.success(tokenResponse.user)
            } else {
                val errorMessage = sanitizeErrorString(response.errorBody()?.string())
                _authState.value = AuthState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Throwable) {
            val msg = formatUserFriendlyError(e)
            _authState.value = AuthState.Error(msg)
            Result.failure(Exception(msg))
        }
    }

    suspend fun login(
        identifier: String,
        password: String,
    ): Result<UserResponse> {
        _authState.value = AuthState.Loading

        val inputIdentifier = identifier.lowercase().trim()
        val inputPasswordHash = hashPassword(password)

        val cachedUser = getCachedUserProfile()
        val savedIdentifier = prefs.getString("saved_identifier", null)
        val savedPasswordHash = prefs.getString("saved_password_hash", null)

        // Offline Login Flow
        if (!NetworkUtils.isOnline(context)) {
            if (savedIdentifier != null && savedPasswordHash != null) {
                if (inputIdentifier == savedIdentifier && inputPasswordHash == savedPasswordHash && cachedUser != null) {
                    saveTokenAndUser(prefs.getString("access_token", "offline_token") ?: "offline_token", cachedUser, identifier, password, isPendingLogin = true)
                    _authState.value = AuthState.Authenticated(cachedUser)
                    return Result.success(cachedUser)
                } else if (inputIdentifier != savedIdentifier || inputPasswordHash != savedPasswordHash) {
                    val errorMsg = "Incorrect username or password."
                    _authState.value = AuthState.Error(errorMsg)
                    return Result.failure(Exception(errorMsg))
                }
            }

            // Create or reuse local offline profile for this identifier
            val localId = UUID.randomUUID().toString()
            val currentIsoTime = getCurrentIsoTimestamp()
            val localUser = cachedUser ?: UserResponse(
                id = localId,
                username = inputIdentifier,
                email = if (inputIdentifier.contains("@")) inputIdentifier else null,
                full_name = null,
                avatar_url = null,
                is_active = true,
                created_at = currentIsoTime,
                updated_at = currentIsoTime,
            )

            saveTokenAndUser(
                token = prefs.getString("access_token", "offline_token_$localId") ?: "offline_token_$localId",
                user = localUser,
                identifier = inputIdentifier,
                password = password,
                isPendingLogin = true,
            )

            _authState.value = AuthState.Authenticated(localUser)
            return Result.success(localUser)
        }

        // Online Login Flow
        return try {
            val request = UserLogin(identifier = identifier, password = password)
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                saveTokenAndUser(tokenResponse.access_token, tokenResponse.user, identifier, password)
                _authState.value = AuthState.Authenticated(tokenResponse.user)
                Result.success(tokenResponse.user)
            } else if (response.code() == 401 || response.code() == 400) {
                val errorMsg = "Invalid username or password. Please try again."
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            } else {
                val errorMessage = sanitizeErrorString(response.errorBody()?.string())
                _authState.value = AuthState.Error(errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Throwable) {
            val msg = formatUserFriendlyError(e)
            _authState.value = AuthState.Error(msg)
            Result.failure(Exception(msg))
        }
    }

    suspend fun updateProfile(
        username: String?,
        email: String?,
        fullName: String?,
        avatarUrl: String?,
    ): Result<UserResponse> {
        val token = getStoredToken() ?: "offline_token"

        if (!NetworkUtils.isOnline(context)) {
            val cachedUser = getCachedUserProfile()
            if (cachedUser != null) {
                val updatedUser = cachedUser.copy(
                    username = username ?: cachedUser.username,
                    email = email ?: cachedUser.email,
                    full_name = fullName ?: cachedUser.full_name,
                    avatar_url = avatarUrl ?: cachedUser.avatar_url,
                )
                saveTokenAndUser(token, updatedUser)
                _authState.value = AuthState.Authenticated(updatedUser)
                return Result.success(updatedUser)
            }
        }

        return try {
            val request = UserUpdate(
                username = username,
                email = email,
                full_name = fullName,
                avatar_url = avatarUrl,
            )
            val response = apiService.updateMe("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                saveTokenAndUser(token, updatedUser)
                _authState.value = AuthState.Authenticated(updatedUser)
                Result.success(updatedUser)
            } else {
                val errorMessage = sanitizeErrorString(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(formatUserFriendlyError(e)))
        }
    }

    suspend fun logout() {
        val token = getStoredToken()
        if (NetworkUtils.isOnline(context) && !token.isNullOrBlank() && !token.startsWith("offline_token")) {
            try {
                apiService.logout("Bearer $token")
            } catch (_: Throwable) {
            }
        }
        clearSessionTokenOnly()
        _authState.value = AuthState.Unauthenticated
    }

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun formatUserFriendlyError(e: Throwable): String {
        return when (e) {
            is java.net.UnknownHostException, is java.net.ConnectException ->
                "No internet connection. Operating in local offline mode."
            is java.net.SocketTimeoutException ->
                "Connection timed out. Please try again."
            else ->
                "Unable to complete request. Please check your connection."
        }
    }

    private fun sanitizeErrorString(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Invalid request. Please check your details."
        return try {
            val json = JSONObject(errorBody)
            if (json.has("detail")) {
                val detail = json.get("detail")
                if (detail is String) {
                    if (detail.contains("host") || detail.contains("hostname") || detail.contains("connect")) {
                        "No internet connection. Operating in local mode."
                    } else {
                        detail
                    }
                } else {
                    "Invalid username or password."
                }
            } else if (json.has("message")) {
                json.getString("message")
            } else {
                "Unable to authenticate. Please check your input."
            }
        } catch (_: Throwable) {
            "Unable to authenticate. Please try again."
        }
    }
}
