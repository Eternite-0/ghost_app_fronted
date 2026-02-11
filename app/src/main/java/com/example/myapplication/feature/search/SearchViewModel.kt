package com.example.myapplication.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import com.example.myapplication.feature.story.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Story>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Story>>> = _uiState.asStateFlow()

    fun loadStories(keyword: String? = null, category: String? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getPublishedStories(
                page = 1,
                limit = 50,
                keyword = keyword?.takeIf { it.isNotBlank() },
                category = category?.takeIf { it.isNotBlank() }
            )
            if (result.isSuccess) {
                _uiState.value = UiState.Success(result.getOrThrow().items)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load stories")
            }
        }
    }
}

class SearchViewModelFactory(
    private val repository: StoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
