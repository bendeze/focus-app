package com.example.focus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus.data.model.UserResponse
import com.example.focus.data.repository.AuthRepository
import com.example.focus.data.repository.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)
    val authState: StateFlow<AuthState> = repository.authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            repository.checkAuthStatus()
        }
    }

    fun register(
        identifier: String?,
        username: String?,
        email: String?,
        password: String,
        fullName: String?,
        onResult: (Result<UserResponse>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.register(identifier, username, email, password, fullName)
            onResult(result)
        }
    }

    fun login(
        identifier: String,
        password: String,
        onResult: (Result<UserResponse>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.login(identifier, password)
            onResult(result)
        }
    }

    fun loginWithBiometrics(onResult: (Result<UserResponse>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithBiometrics()
            onResult(result)
        }
    }

    fun updateProfile(
        username: String?,
        email: String?,
        fullName: String?,
        avatarUrl: String?,
        onResult: (Result<UserResponse>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = repository.updateProfile(username, email, fullName, avatarUrl)
            onResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
