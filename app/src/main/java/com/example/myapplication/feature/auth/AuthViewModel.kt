package com.example.myapplication.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.AuthResponse
import com.example.myapplication.core.model.LoginRequest
import com.example.myapplication.core.model.RegisterRequest
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AuthResponse>?>(null)
    val uiState: StateFlow<UiState<AuthResponse>?> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(email, password))
            _uiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.register(RegisterRequest(username, email, password))
            _uiState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
