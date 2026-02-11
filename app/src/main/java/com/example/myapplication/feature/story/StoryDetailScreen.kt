package com.example.myapplication.feature.story

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.UiState
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    storyId: String,
    viewModel: StoryDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Comment Input Area
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a comment...") },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.postComment(storyId, commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    val story = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Story Content
                        item {
                            Text(text = story.title, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "By ${story.author.username}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onNavigateToUser(story.author.id) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = story.category,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = story.content, style = MaterialTheme.typography.bodyLarge)

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleLike(story) }) {
                                    Icon(
                                        imageVector = if (story.isLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (story.isLiked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(text = "${story.likesCount} Likes")
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Comments List
                        when (val cState = commentsState) {
                            is UiState.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            is UiState.Error -> {
                                item {
                                    Text("Failed to load comments", color = MaterialTheme.colorScheme.error)
                                }
                            }
                            is UiState.Success -> {
                                val comments = cState.data
                                if (comments.isEmpty()) {
                                    item {
                                        Text("No comments yet. Be the first to share your thoughts!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                } else {
                                    items(comments) { comment ->
                                        CommentItem(comment, onUserClick = onNavigateToUser)
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadStory(storyId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onUserClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUserClick(comment.author.id) }
            ) {
                Text(
                    text = comment.author.username,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeAgo(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun timeAgo(createdAt: String): String {
    return try {
        val created = Instant.parse(createdAt)
        val now = Instant.now()
        val hours = ChronoUnit.HOURS.between(created, now)
        if (hours < 1) {
            val mins = ChronoUnit.MINUTES.between(created, now).coerceAtLeast(1)
            "$mins min ago"
        } else if (hours < 24) {
            "$hours hours ago"
        } else {
            val days = ChronoUnit.DAYS.between(created, now)
            "$days days ago"
        }
    } catch (_: Exception) {
        createdAt
    }
}
