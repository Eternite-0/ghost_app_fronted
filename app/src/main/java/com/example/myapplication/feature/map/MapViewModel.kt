package com.example.myapplication.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.StoryMapMarker
import com.example.myapplication.core.model.UiState
import com.example.myapplication.feature.story.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<StoryMapMarker>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<StoryMapMarker>>> = _uiState.asStateFlow()

    fun loadStories(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getStoriesOnMap(lat, lon)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                // The "stories" key in the map is a List<StoryMapMarker> (in JSON), but GSON deserializes generic Maps as Map<String, Any>
                // We need to carefully convert it. In a real app, defining specific response types is better.
                // Assuming "stories" is a List of LinkedTreeMap or similar.
                try {
                   val storiesList = data["stories"] as? List<*>
                   val markers = storiesList?.map {
                       Gson().fromJson(Gson().toJson(it), StoryMapMarker::class.java)
                   } ?: emptyList()
                   _uiState.value = UiState.Success(markers)
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Failed to parse map data")
                }
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
