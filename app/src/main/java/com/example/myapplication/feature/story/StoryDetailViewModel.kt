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

data class CommentThread(
    val root: Comment,
    val replies: List<Comment> = emptyList(),
    val repliesHasMore: Boolean = false
)

class StoryDetailViewModel(
    private val storyRepository: StoryRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Story>>(UiState.Loading)
    val uiState: StateFlow<UiState<Story>> = _uiState.asStateFlow()

    private val _commentsState = MutableStateFlow<UiState<List<CommentThread>>>(UiState.Loading)
    val commentsState: StateFlow<UiState<List<CommentThread>>> = _commentsState.asStateFlow()

    private val _hasMoreTopComments = MutableStateFlow(false)
    val hasMoreTopComments: StateFlow<Boolean> = _hasMoreTopComments.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Boolean>?>(null)
    val deleteState: StateFlow<UiState<Boolean>?> = _deleteState.asStateFlow()

    private val threadsByRootId = linkedMapOf<String, CommentThread>()
    private val replyPageByRootId = mutableMapOf<String, Int>()
    private val replyHasMoreByRootId = mutableMapOf<String, Boolean>()
    private var currentStoryId: String? = null
    private var topCommentsPage = 1

    fun loadStory(storyId: String) {
        viewModelScope.launch {
            currentStoryId = storyId
            _uiState.value = UiState.Loading
            val result = storyRepository.getStoryById(storyId)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(result.getOrThrow())
                loadTopComments(storyId, reset = true)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load story")
            }
        }
    }

    private fun loadTopComments(storyId: String, reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                _commentsState.value = UiState.Loading
                threadsByRootId.clear()
                replyPageByRootId.clear()
                replyHasMoreByRootId.clear()
                topCommentsPage = 1
            }
            val result = commentRepository.getComments(storyId = storyId, page = topCommentsPage, limit = 5)
            if (result.isSuccess) {
                val pageData = result.getOrThrow()
                val newlyAddedRoots = mutableListOf<Comment>()
                val roots = pageData.items.filter { it.parentId == null }
                val replies = pageData.items.filter { it.parentId != null }

                roots.forEach { comment ->
                    if (!threadsByRootId.containsKey(comment.id)) {
                        threadsByRootId[comment.id] = CommentThread(
                            root = comment,
                            replies = emptyList(),
                            repliesHasMore = comment.repliesCount > 0
                        )
                        newlyAddedRoots.add(comment)
                    }
                }

                // Defensive path: in case backend returns mixed comments, don't treat replies as root comments.
                replies.forEach { reply ->
                    val parentId = reply.parentId ?: return@forEach
                    val thread = threadsByRootId[parentId] ?: return@forEach
                    val merged = (thread.replies + reply)
                        .distinctBy { it.id }
                        .sortedBy { it.createdAt }
                    threadsByRootId[parentId] = thread.copy(replies = merged)
                }
                _hasMoreTopComments.value = pageData.hasMore
                if (pageData.hasMore) {
                    topCommentsPage += 1
                }

                // Keep thread view stable after re-entering detail: preload first page of replies for visible roots.
                newlyAddedRoots
                    .filter { it.repliesCount > 0 }
                    .forEach { root ->
                        val replyResult = commentRepository.getComments(
                            storyId = storyId,
                            page = 1,
                            limit = 5,
                            parentId = root.id
                        )
                        if (replyResult.isSuccess) {
                            val response = replyResult.getOrThrow()
                            val current = threadsByRootId[root.id] ?: return@forEach
                            val merged = (current.replies + response.items)
                                .distinctBy { it.id }
                                .sortedBy { it.createdAt }
                            threadsByRootId[root.id] = current.copy(
                                replies = merged,
                                repliesHasMore = response.hasMore
                            )
                            replyHasMoreByRootId[root.id] = response.hasMore
                            replyPageByRootId[root.id] = if (response.hasMore) 2 else 1
                        }
                    }

                _commentsState.value = UiState.Success(threadsByRootId.values.toList())
            } else {
                _commentsState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load comments")
            }
        }
    }

    fun loadMoreTopComments() {
        val storyId = currentStoryId ?: return
        if (!_hasMoreTopComments.value) return
        loadTopComments(storyId, reset = false)
    }

    fun loadMoreReplies(rootCommentId: String) {
        val storyId = currentStoryId ?: return
        val thread = threadsByRootId[rootCommentId] ?: return
        if (replyHasMoreByRootId[rootCommentId] == false) return

        viewModelScope.launch {
            val page = replyPageByRootId[rootCommentId] ?: 1
            val result = commentRepository.getComments(
                storyId = storyId,
                page = page,
                limit = 5,
                parentId = rootCommentId
            )
            if (result.isSuccess) {
                val response = result.getOrThrow()
                val merged = (thread.replies + response.items)
                    .distinctBy { it.id }
                    .sortedBy { it.createdAt }
                val hasMore = response.hasMore
                threadsByRootId[rootCommentId] = thread.copy(replies = merged, repliesHasMore = hasMore)
                replyHasMoreByRootId[rootCommentId] = hasMore
                if (hasMore) {
                    replyPageByRootId[rootCommentId] = page + 1
                } else {
                    replyPageByRootId[rootCommentId] = page
                }
                _commentsState.value = UiState.Success(threadsByRootId.values.toList())
            }
        }
    }

    fun postComment(storyId: String, content: String, parentId: String? = null) {
        viewModelScope.launch {
            val result = commentRepository.createComment(storyId, content, parentId)
            if (result.isSuccess) {
                val created = result.getOrThrow()
                if (parentId == null) {
                    loadTopComments(storyId, reset = true)
                } else {
                    val thread = threadsByRootId[parentId]
                    if (thread != null) {
                        val merged = (thread.replies + created)
                            .distinctBy { it.id }
                            .sortedBy { it.createdAt }
                        threadsByRootId[parentId] = thread.copy(replies = merged)
                        _commentsState.value = UiState.Success(threadsByRootId.values.toList())
                    } else {
                        loadTopComments(storyId, reset = true)
                    }
                }
            }
        }
    }

    fun deleteComment(storyId: String, commentId: String) {
        viewModelScope.launch {
            val result = commentRepository.deleteComment(storyId, commentId)
            if (result.isSuccess) {
                loadTopComments(storyId, reset = true)
                val storyState = _uiState.value
                if (storyState is UiState.Success) {
                    val latest = storyRepository.getStoryById(storyId)
                    if (latest.isSuccess) {
                        _uiState.value = UiState.Success(latest.getOrThrow())
                    }
                }
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

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = storyRepository.deleteStory(storyId)
            _deleteState.value = if (result.isSuccess) {
                UiState.Success(true)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "删除失败")
            }
        }
    }

    fun clearDeleteState() {
        _deleteState.value = null
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
