package com.example.myapplication.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.NotificationItem
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<NotificationItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<NotificationItem>>> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val listResult = repository.getNotifications(page = 1, limit = 50)
            val unreadResult = repository.getUnreadCount()

            if (listResult.isSuccess) {
                _uiState.value = UiState.Success(listResult.getOrThrow().items)
            } else {
                _uiState.value = UiState.Error(listResult.exceptionOrNull()?.message ?: "Failed to load notifications")
            }

            if (unreadResult.isSuccess) {
                _unreadCount.value = unreadResult.getOrThrow()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead()
            if (result.isSuccess) {
                loadData()
            } else if (_uiState.value is UiState.Success) {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to mark as read")
            }
        }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
