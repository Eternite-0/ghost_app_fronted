package com.example.myapplication.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.UiState
import com.example.myapplication.core.model.UserPublicResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDetailViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<UserPublicResponse>>(UiState.Loading)
    val uiState: StateFlow<UiState<UserPublicResponse>> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getUserPublicProfile(userId)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(result.getOrThrow())
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load user")
            }
        }
    }

    fun toggleFollow(userId: String, isFollowing: Boolean) {
        viewModelScope.launch {
            val result = if (isFollowing) {
                repository.unfollowUser(userId)
            } else {
                repository.followUser(userId)
            }

            if (result.isSuccess) {
                // Refresh user data to update counts and status
                loadUser(userId)
            }
        }
    }
}

class UserDetailViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
