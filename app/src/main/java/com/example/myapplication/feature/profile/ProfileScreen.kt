package com.example.myapplication.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.model.Comment
import com.example.myapplication.core.model.Story
import com.example.myapplication.core.model.UiState
import com.example.myapplication.core.model.User
import com.example.myapplication.core.model.UserPublicResponse
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToStory: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val myStoriesState by viewModel.myStories.collectAsState()
    val myFavoritesState by viewModel.myFavorites.collectAsState()
    val myCommentsState by viewModel.myComments.collectAsState()
    val myFollowersState by viewModel.myFollowers.collectAsState()
    val myFollowingState by viewModel.myFollowing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
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
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
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
                    ProfileContent(
                        user = state.data,
                        selectedTab = selectedTab,
                        onTabSelected = viewModel::onTabSelected,
                        myStoriesState = myStoriesState,
                        myFavoritesState = myFavoritesState,
                        myCommentsState = myCommentsState,
                        myFollowersState = myFollowersState,
                        myFollowingState = myFollowingState,
                        onNavigateToStory = onNavigateToStory,
                        onLogout = {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    myStoriesState: UiState<List<Story>>,
    myFavoritesState: UiState<List<Story>>,
    myCommentsState: UiState<List<Comment>>,
    myFollowersState: UiState<List<UserPublicResponse>>,
    myFollowingState: UiState<List<UserPublicResponse>>,
    onNavigateToStory: (String) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header Section
        item {
            ProfileHeader(user, onLogout)
        }

        // Tabs Section
        item {
            ProfileTabs(selectedTab, onTabSelected)
        }

        // List Content
        when (selectedTab) {
            0 -> storiesList(myStoriesState, onNavigateToStory)
            1 -> storiesList(myFavoritesState, onNavigateToStory)
            2 -> commentsList(myCommentsState)
            3 -> usersList(myFollowersState)
            4 -> usersList(myFollowingState)
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.storiesList(
    state: UiState<List<Story>>,
    onNavigateToStory: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> item { LoadingItem() }
        is UiState.Error -> item { ErrorItem(state.message) }
        is UiState.Success -> {
            val items = state.data
            if (items.isEmpty()) {
                item { EmptyItem("No stories found") }
            } else {
                items(items) { story ->
                    ProfileStoryItem(story, onNavigateToStory)
                }
            }
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.commentsList(state: UiState<List<Comment>>) {
    when (state) {
        is UiState.Loading -> item { LoadingItem() }
        is UiState.Error -> item { ErrorItem(state.message) }
        is UiState.Success -> {
            val items = state.data
            if (items.isEmpty()) {
                item { EmptyItem("No comments found") }
            } else {
                items(items) { comment ->
                    ProfileCommentItem(comment)
                }
            }
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.usersList(state: UiState<List<UserPublicResponse>>) {
    when (state) {
        is UiState.Loading -> item { LoadingItem() }
        is UiState.Error -> item { ErrorItem(state.message) }
        is UiState.Success -> {
            val items = state.data
            if (items.isEmpty()) {
                item { EmptyItem("No users found") }
            } else {
                items(items) { user ->
                    ProfileUserItem(user)
                }
            }
        }
    }
}

@Composable
fun LoadingItem() {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorItem(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text("Failed to load: $message", color = Color.Gray)
    }
}

@Composable
fun EmptyItem(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray)
    }
}

@Composable
fun ProfileHeader(user: User, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            AsyncImage(
                model = user.avatarUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        if (!user.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.bio,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* TODO: Navigate to Edit Profile */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B8ED6)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }

            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Tab(selected = selectedTab == 0, onClick = { onTabSelected(0) }, text = { Text("Stories") })
        Tab(selected = selectedTab == 1, onClick = { onTabSelected(1) }, text = { Text("Favorites") })
        Tab(selected = selectedTab == 2, onClick = { onTabSelected(2) }, text = { Text("Comments") })
        Tab(selected = selectedTab == 3, onClick = { onTabSelected(3) }, text = { Text("Followers") })
        Tab(selected = selectedTab == 4, onClick = { onTabSelected(4) }, text = { Text("Following") })
    }
}

@Composable
fun ProfileCommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Commented on a story",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeAgo(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProfileUserItem(user: UserPublicResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarUrl ?: "https://via.placeholder.com/150",
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (!user.bio.isNullOrBlank()) {
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileStoryItem(story: Story, onNavigateToStory: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onNavigateToStory(story.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeAgo(story.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${story.viewsCount} views",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
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
