package com.example.myapplication.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.StoryMapMarker
import com.example.myapplication.core.model.UiState
import com.example.myapplication.feature.story.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<StoryMapMarker>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<StoryMapMarker>>> = _uiState.asStateFlow()

    fun loadStories(lat: Double, lon: Double, radiusMeters: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getStoriesOnMap(lat, lon, radiusMeters)
            if (result.isSuccess) {
                val markers = result.getOrThrow().stories
                _uiState.value = UiState.Success(markers)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load stories")
            }
        }
    }
}

class MapViewModelFactory(private val repository: StoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
