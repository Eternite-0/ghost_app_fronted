package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import com.example.myapplication.feature.profile.UserDetailScreen
import com.example.myapplication.feature.profile.UserDetailViewModel
import com.example.myapplication.feature.profile.UserDetailViewModelFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.feature.auth.AuthRepository
import com.example.myapplication.feature.auth.AuthViewModel
import com.example.myapplication.feature.auth.AuthViewModelFactory
import com.example.myapplication.feature.auth.LoginScreen
import com.example.myapplication.feature.auth.RegisterScreen
import com.example.myapplication.feature.map.MapScreen
import com.example.myapplication.feature.map.MapViewModel
import com.example.myapplication.feature.map.MapViewModelFactory
import com.example.myapplication.feature.notification.AlertsScreen
import com.example.myapplication.feature.notification.NotificationRepository
import com.example.myapplication.feature.notification.NotificationViewModel
import com.example.myapplication.feature.notification.NotificationViewModelFactory
import com.example.myapplication.feature.profile.ProfileScreen
import com.example.myapplication.feature.profile.ProfileViewModel
import com.example.myapplication.feature.profile.ProfileViewModelFactory
import com.example.myapplication.feature.profile.UserRepository
import com.example.myapplication.feature.search.SearchScreen
import com.example.myapplication.feature.search.SearchViewModel
import com.example.myapplication.feature.search.SearchViewModelFactory
import com.example.myapplication.feature.story.CreateStoryScreen
import com.example.myapplication.feature.story.CreateStoryViewModel
import com.example.myapplication.feature.story.CreateStoryViewModelFactory
import com.example.myapplication.feature.story.CommentRepository
import com.example.myapplication.feature.story.StoryDetailScreen
import com.example.myapplication.feature.story.StoryDetailViewModel
import com.example.myapplication.feature.story.StoryDetailViewModelFactory
import com.example.myapplication.feature.story.StoryRepository
import com.example.myapplication.core.storage.TokenManager

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    storyRepository: StoryRepository,
    commentRepository: CommentRepository,
    userRepository: UserRepository,
    notificationRepository: NotificationRepository,
    tokenManager: TokenManager,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable("login") {
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("map") {
            val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(storyRepository))
            MapScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToCreate = { navController.navigate("create_story?mapStory=true") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToStory = { storyId -> navController.navigate("story_detail/$storyId") }
            )
        }

        composable("search") {
            val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(storyRepository))
            SearchScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("map") {
                        popUpTo("map") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToCreate = { navController.navigate("create_story?mapStory=false") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToStory = { storyId -> navController.navigate("story_detail/$storyId") }
            )
        }

        composable("notifications") {
            val viewModel: NotificationViewModel = viewModel(
                factory = NotificationViewModelFactory(notificationRepository)
            )
            AlertsScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("map") {
                        popUpTo("map") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToCreate = { navController.navigate("create_story?mapStory=false") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable(
            "create_story?mapStory={mapStory}",
            arguments = listOf(navArgument("mapStory") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val isMapStory = backStackEntry.arguments?.getBoolean("mapStory") ?: false
            val viewModel: CreateStoryViewModel = viewModel(factory = CreateStoryViewModelFactory(storyRepository))
            CreateStoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                isMapStory = isMapStory
            )
        }

        composable("story_detail/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            val viewModel: StoryDetailViewModel = viewModel(
                factory = StoryDetailViewModelFactory(storyRepository, commentRepository)
            )
            StoryDetailScreen(
                storyId = storyId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { userId -> navController.navigate("user_detail/$userId") }
            )
        }

        composable(
            "user_detail/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val viewModel: UserDetailViewModel = viewModel(
                factory = UserDetailViewModelFactory(userRepository)
            )
            UserDetailScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(userRepository, tokenManager)
            )
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("map") {
                        popUpTo("map") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToCreate = { navController.navigate("create_story?mapStory=false") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToStory = { storyId -> navController.navigate("story_detail/$storyId") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("map") { inclusive = true }
                    }
                }
            )
        }
    }
}
