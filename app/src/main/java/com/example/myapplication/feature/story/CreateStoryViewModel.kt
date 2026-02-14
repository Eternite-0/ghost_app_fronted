package com.example.myapplication.feature.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.CreateStoryRequest
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CreateStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Boolean>?>(null)
    val uiState: StateFlow<UiState<Boolean>?> = _uiState.asStateFlow()

    fun createStory(
        title: String,
        content: String,
        category: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        placeName: String?,
        image: File?,
        mapStory: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val request = CreateStoryRequest(
                title = title,
                content = content,
                category = category,
                latitude = latitude,
                longitude = longitude,
                address = address,
                placeName = placeName,
                mapStory = mapStory
            )
            val result = repository.createStory(request, image)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(true)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create story")
            }
        }
    }

    fun resetState() {
        _uiState.value = null
    }
}

class CreateStoryViewModelFactory(private val repository: StoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateStoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateStoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
