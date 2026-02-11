package com.example.myapplication.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import com.example.myapplication.core.model.User
import com.example.myapplication.core.model.UserPublicResponse
import com.example.myapplication.core.storage.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    private val _myStories = MutableStateFlow<UiState<List<Story>>>(UiState.Loading)
    val myStories: StateFlow<UiState<List<Story>>> = _myStories.asStateFlow()

    private val _myFavorites = MutableStateFlow<UiState<List<Story>>>(UiState.Loading)
    val myFavorites: StateFlow<UiState<List<Story>>> = _myFavorites.asStateFlow()

    private val _myComments = MutableStateFlow<UiState<List<Comment>>>(UiState.Loading)
    val myComments: StateFlow<UiState<List<Comment>>> = _myComments.asStateFlow()

    private val _myFollowers = MutableStateFlow<UiState<List<UserPublicResponse>>>(UiState.Loading)
    val myFollowers: StateFlow<UiState<List<UserPublicResponse>>> = _myFollowers.asStateFlow()

    private val _myFollowing = MutableStateFlow<UiState<List<UserPublicResponse>>>(UiState.Loading)
    val myFollowing: StateFlow<UiState<List<UserPublicResponse>>> = _myFollowing.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrThrow()
                _uiState.value = UiState.Success(user)
                // Load initial tab content
                refreshCurrentTab()
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load profile")
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        refreshCurrentTab()
    }

    private fun refreshCurrentTab() {
        when (_selectedTab.value) {
            0 -> if (_myStories.value !is UiState.Success) loadMyStories()
            1 -> if (_myFavorites.value !is UiState.Success) loadMyFavorites()
            2 -> if (_myComments.value !is UiState.Success) loadMyComments()
            3 -> if (_myFollowers.value !is UiState.Success) loadMyFollowers()
            4 -> if (_myFollowing.value !is UiState.Success) loadMyFollowing()
        }
    }

    private fun loadMyStories() {
        viewModelScope.launch {
            _myStories.value = UiState.Loading
            val result = repository.getMyStories(1, 20)
            if (result.isSuccess) {
                _myStories.value = UiState.Success(result.getOrThrow().items)
            } else {
                _myStories.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load stories")
            }
        }
    }

    private fun loadMyFavorites() {
        viewModelScope.launch {
            _myFavorites.value = UiState.Loading
            val result = repository.getMyFavorites(1, 20)
            if (result.isSuccess) {
                _myFavorites.value = UiState.Success(result.getOrThrow().items)
            } else {
                _myFavorites.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load favorites")
            }
        }
    }

    private fun loadMyComments() {
        val userId = (_uiState.value as? UiState.Success)?.data?.id ?: return
        viewModelScope.launch {
            _myComments.value = UiState.Loading
            val result = repository.getUserComments(userId, 1, 20)
            if (result.isSuccess) {
                _myComments.value = UiState.Success(result.getOrThrow().items)
            } else {
                _myComments.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load comments")
            }
        }
    }

    private fun loadMyFollowers() {
        val userId = (_uiState.value as? UiState.Success)?.data?.id ?: return
        viewModelScope.launch {
            _myFollowers.value = UiState.Loading
            val result = repository.getUserFollowers(userId, 1, 20)
            if (result.isSuccess) {
                _myFollowers.value = UiState.Success(result.getOrThrow().items)
            } else {
                _myFollowers.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load followers")
            }
        }
    }

    private fun loadMyFollowing() {
        val userId = (_uiState.value as? UiState.Success)?.data?.id ?: return
        viewModelScope.launch {
            _myFollowing.value = UiState.Loading
            val result = repository.getUserFollowing(userId, 1, 20)
            if (result.isSuccess) {
                _myFollowing.value = UiState.Success(result.getOrThrow().items)
            } else {
                _myFollowing.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load following")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}

class ProfileViewModelFactory(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
