package com.example.myapplication.feature.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.model.StoryCategories
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import coil.compose.AsyncImage
import java.time.Instant
import java.time.temporal.ChronoUnit

private val searchCategories = StoryCategories.forumCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToStory: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var keyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadStories()
    }

    Scaffold(
        containerColor = Color(0xFFEAF2FC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF4B8ED6))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forum",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search stories...") },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {
                        viewModel.loadStories(keyword, selectedCategory)
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCreate,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                    label = { Text("Create") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToNotifications,
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFF5A9BE0),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Post"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CategoryBar(
                selected = selectedCategory,
                onSelect = { category ->
                    selectedCategory = category
                    viewModel.loadStories(keyword, category)
                }
            )
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    SearchStoryList(
                        stories = state.data,
                        onStoryClick = onNavigateToStory
                    )
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Load failed: ${state.message}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CategoryChip(
                title = "All",
                selected = selected == null,
                onClick = { onSelect(null) }
            )
        }
        items(searchCategories) { category ->
            CategoryChip(
                title = category,
                selected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF2D6FB6) else Color(0xFF6FA2D9)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SearchStoryList(
    stories: List<Story>,
    onStoryClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            StoryCard(story = story, onClick = { onStoryClick(story.id) })
        }
    }
}

@Composable
private fun StoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = story.imageUrl,
                    contentDescription = story.title,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF214D86)
                    )
                    Text(
                        text = "By: ${story.author.username} | ${timeAgo(story.createdAt)}",
                        color = Color(0xFF4A6E9A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = story.content,
                        maxLines = 2,
                        color = Color(0xFF2B3C54)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Views: ${story.viewsCount}", color = Color(0xFF437AB8))
                Text("Replies: ${story.commentsCount}", color = Color(0xFF437AB8))
                Text("Likes: ${story.likesCount}", color = Color(0xFF437AB8))
            }
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
