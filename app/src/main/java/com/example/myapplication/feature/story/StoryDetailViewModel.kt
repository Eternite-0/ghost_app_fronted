package com.example.myapplication.feature.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoryDetailViewModel(
    private val storyRepository: StoryRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Story>>(UiState.Loading)
    val uiState: StateFlow<UiState<Story>> = _uiState.asStateFlow()

    private val _commentsState = MutableStateFlow<UiState<List<Comment>>>(UiState.Loading)
    val commentsState: StateFlow<UiState<List<Comment>>> = _commentsState.asStateFlow()

    fun loadStory(storyId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = storyRepository.getStoryById(storyId)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(result.getOrThrow())
                loadComments(storyId)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load story")
            }
        }
    }

    private fun loadComments(storyId: String) {
        viewModelScope.launch {
            _commentsState.value = UiState.Loading
            val result = commentRepository.getComments(storyId, 1, 50)
            if (result.isSuccess) {
                _commentsState.value = UiState.Success(result.getOrThrow().items)
            } else {
                _commentsState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load comments")
            }
        }
    }

    fun postComment(storyId: String, content: String) {
        viewModelScope.launch {
            val result = commentRepository.createComment(storyId, content)
            if (result.isSuccess) {
                // Reload comments
                loadComments(storyId)
            }
        }
    }

    fun toggleLike(story: Story) {
        viewModelScope.launch {
            val currentLikes = story.likesCount
            val isLiked = story.isLiked ?: false

            // Optimistic update could happen here, but for now we wait for server
            val result = if (isLiked == true) {
                storyRepository.unlikeStory(story.id)
            } else {
                storyRepository.likeStory(story.id)
            }

            if (result.isSuccess) {
                val responseData = result.getOrThrow()
                val newLikesCount = (responseData["likes_count"] as? Number)?.toInt() ?: currentLikes
                // Reload story or update local state manually
                _uiState.value = UiState.Success(story.copy(
                    isLiked = !isLiked,
                    likesCount = newLikesCount
                ))
            }
        }
    }
}

class StoryDetailViewModelFactory(
    private val storyRepository: StoryRepository,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryDetailViewModel(storyRepository, commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
