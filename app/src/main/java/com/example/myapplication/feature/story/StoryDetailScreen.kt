package com.example.myapplication.feature.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import com.example.myapplication.core.network.ImageUrlResolver
import java.time.Instant
import java.time.temporal.ChronoUnit

private data class ReplyTarget(
    val rootCommentId: String,
    val mentionName: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoryDetailScreen(
    storyId: String,
    viewModel: StoryDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUser: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    val hasMoreTopComments by viewModel.hasMoreTopComments.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ReplyTarget?>(null) }
    var pendingDeleteComment by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }

    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) {
            viewModel.clearDeleteState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "故事详情",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1A2E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A2E)
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val target = replyTarget
                    if (target != null) {
                        Surface(
                            color = Color(0xFFF0F4F8),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (target.mentionName == null) "回复评论" else "回复 @${target.mentionName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4A6FA5),
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(
                                    onClick = { replyTarget = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cancel reply",
                                        tint = Color(0xFF7A8BA3),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                val hint = when {
                                    target == null -> "写下你的想法..."
                                    target.mentionName == null -> "回复评论..."
                                    else -> "回复 @${target.mentionName}..."
                                }
                                Text(hint, color = Color(0xFF9AA8B8))
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF7F9FC),
                                unfocusedContainerColor = Color(0xFFF7F9FC),
                                cursorColor = Color(0xFF4A90D9),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                val draft = commentText.trim()
                                if (draft.isNotBlank()) {
                                    val contentToSend = if (target?.mentionName != null) {
                                        val prefix = "@${target.mentionName}"
                                        if (draft.startsWith(prefix)) draft else "$prefix $draft"
                                    } else {
                                        draft
                                    }
                                    viewModel.postComment(
                                        storyId = storyId,
                                        content = contentToSend,
                                        parentId = target?.rootCommentId
                                    )
                                    commentText = ""
                                    replyTarget = null
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (commentText.isNotBlank()) Color(0xFF4A90D9)
                                    else Color(0xFFD1D9E2)
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadStory(storyId) }) { Text("Retry") }
                    }
                }
                is UiState.Success -> {
                    val story = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { StoryHeader(story = story, onNavigateToUser = onNavigateToUser) }
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC))
                            ) {
                                Text(
                                    text = story.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF243447),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                        item {
                            StoryActions(
                                story = story,
                                onToggleLike = { viewModel.toggleLike(story) },
                                onDelete = { viewModel.deleteStory(story.id) }
                            )
                        }
                        item {
                            // 评论区分隔和标题
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(18.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF4A90D9))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "评论区",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A2B3C)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${story.commentsCount})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF7A8BA3)
                                    )
                                }
                            }
                        }
                        when (val cState = commentsState) {
                            is UiState.Loading -> item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                                }
                            }
                            is UiState.Error -> item {
                                Text("评论加载失败", color = MaterialTheme.colorScheme.error)
                            }
                            is UiState.Success -> {
                                if (cState.data.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF0F4F8)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Send,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB0BEC5),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "还没有评论",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFF607D8B),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "来抢沙发吧 ~",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF90A4AE)
                                            )
                                        }
                                    }
                                } else {
                                    items(cState.data, key = { it.root.id }) { thread ->
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            CommentBubble(
                                                comment = thread.root,
                                                onUserClick = onNavigateToUser,
                                                onReplyClick = {
                                                    replyTarget = ReplyTarget(rootCommentId = thread.root.id)
                                                },
                                                onLongDelete = if (thread.root.isOwner) {
                                                    { pendingDeleteComment = thread.root }
                                                } else null
                                            )
                                            if (thread.replies.isNotEmpty()) {
                                                Surface(
                                                    modifier = Modifier.padding(start = 20.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFFF5F8FC)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        thread.replies.forEach { reply ->
                                                            CommentBubble(
                                                                comment = reply,
                                                                onUserClick = onNavigateToUser,
                                                                onReplyClick = {
                                                                    replyTarget = ReplyTarget(
                                                                        rootCommentId = thread.root.id,
                                                                        mentionName = reply.author.username
                                                                    )
                                                                },
                                                                onLongDelete = if (reply.isOwner) {
                                                                    { pendingDeleteComment = reply }
                                                                } else null,
                                                                compact = true
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (thread.repliesHasMore || (thread.root.repliesCount > 0 && thread.replies.isEmpty())) {
                                                Surface(
                                                    onClick = { viewModel.loadMoreReplies(thread.root.id) },
                                                    modifier = Modifier.padding(start = 20.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFF0F4F8)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "...",
                                                            color = Color(0xFF4A90D9),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (thread.replies.isEmpty()) {
                                                                "展开 ${thread.root.repliesCount} 条回复"
                                                            } else {
                                                                "展开更多回复"
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF4A90D9),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            HorizontalDivider(
                                                color = Color(0xFFEEF2F7),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                    if (hasMoreTopComments) {
                                        item {
                                            Surface(
                                                onClick = { viewModel.loadMoreTopComments() },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF5F8FC)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "...",
                                                        color = Color(0xFF4A90D9),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "展开更多评论",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF4A90D9),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val deleting = pendingDeleteComment
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteComment = null },
            title = { Text("删除评论") },
            text = { Text("确认删除这条评论吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteComment(storyId, deleting.id)
                        pendingDeleteComment = null
                    }
                ) { Text("删除", color = Color(0xFFC62828)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteComment = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun StoryHeader(
    story: Story,
    onNavigateToUser: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            AsyncImage(
                model = ImageUrlResolver.resolve(story.imageUrl),
                contentDescription = story.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC101621))))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onNavigateToUser(story.author.id) }
                ) {
                    UserAvatar(avatarUrl = story.author.avatarUrl, username = story.author.username, size = 28.dp)
                    Text(
                        text = "${story.author.username} · ${story.location.address ?: "未知地点"} · ${timeAgo(story.createdAt)}",
                        color = Color(0xFFE4ECF8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryActions(
    story: Story,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 播放按钮
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A90D9)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                if (story.audioUrl.isNullOrBlank()) "收听故事（即将开放）" else "收听故事",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }

        // 互动数据
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF7F9FC)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 点赞
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleLike() }
                    ) {
                        Icon(
                            imageVector = if (story.isLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (story.isLiked == true) Color(0xFFE57373) else Color(0xFF7A8BA3),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${story.likesCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A5568),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // 评论数
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Comments",
                            tint = Color(0xFF7A8BA3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${story.commentsCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A5568),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // 分享
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFF7A8BA3),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { }
                )
            }
        }

        // 删除按钮
        if (story.isOwner == true) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "删除故事",
                    color = Color(0xFFC62828),
                    fontSize = 13.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFFEEF2F7), thickness = 1.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommentBubble(
    comment: Comment,
    onUserClick: (String) -> Unit,
    onReplyClick: () -> Unit,
    onLongDelete: (() -> Unit)? = null,
    compact: Boolean = false
) {
    val avatarSize = if (compact) 28.dp else 38.dp
    val bgColor = if (compact) Color(0xFFFAFBFC) else Color.White

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { onLongDelete?.invoke() }
            ),
        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
        color = bgColor,
        shadowElevation = if (compact) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            UserAvatar(
                avatarUrl = comment.author.avatarUrl,
                username = comment.author.username,
                size = avatarSize,
                modifier = Modifier.clickable { onUserClick(comment.author.id) }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.author.username,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A2B3C),
                            modifier = Modifier.clickable { onUserClick(comment.author.id) }
                        )
                        if (comment.isOwner) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "作者",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4A90D9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = timeAgo(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9AA8B8),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2D3A4A),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onReplyClick() }
                ) {
                    Text(
                        text = "回复",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4A90D9),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(
    avatarUrl: String?,
    username: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val hasAvatar = !avatarUrl.isNullOrBlank()
    if (hasAvatar) {
        AsyncImage(
            model = ImageUrlResolver.resolve(avatarUrl),
            contentDescription = username,
            modifier = modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFCEDAE8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = Color(0xFF2E4A66),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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
            "$mins 分钟前"
        } else if (hours < 24) {
            "$hours 小时前"
        } else {
            val days = ChronoUnit.DAYS.between(created, now)
            "$days 天前"
        }
    } catch (_: Exception) {
        createdAt
    }
}
