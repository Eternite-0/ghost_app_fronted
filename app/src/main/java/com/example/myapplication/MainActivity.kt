package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.core.network.AuthService
import com.example.myapplication.core.network.CommentService
import com.example.myapplication.core.network.NotificationService
import com.example.myapplication.core.network.RetrofitClient
import com.example.myapplication.core.network.StoryService
import com.example.myapplication.core.network.UploadService
import com.example.myapplication.core.network.UserService
import com.example.myapplication.core.storage.TokenManager
import com.example.myapplication.feature.auth.AuthRepository
import com.example.myapplication.feature.notification.NotificationRepository
import com.example.myapplication.feature.profile.UserRepository
import com.example.myapplication.feature.story.CommentRepository
import com.example.myapplication.feature.story.StoryRepository
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        val tokenManager = TokenManager(applicationContext)
        val retrofit = RetrofitClient.getClient(applicationContext)

        val authService = retrofit.create(AuthService::class.java)
        val storyService = retrofit.create(StoryService::class.java)
        val commentService = retrofit.create(CommentService::class.java)
        val userService = retrofit.create(UserService::class.java)
        val notificationService = retrofit.create(NotificationService::class.java)
        val uploadService = retrofit.create(UploadService::class.java)

        val authRepository = AuthRepository(authService, tokenManager)
        val storyRepository = StoryRepository(storyService, uploadService)
        val commentRepository = CommentRepository(commentService)
        val userRepository = UserRepository(userService, uploadService)
        val notificationRepository = NotificationRepository(notificationService)

        // Determine start destination based on token existence
        val startDestination = runBlocking {
            if (!tokenManager.accessToken.first().isNullOrEmpty()) "map" else "login"
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        authRepository = authRepository,
                        storyRepository = storyRepository,
                        commentRepository = commentRepository,
                        userRepository = userRepository,
                        notificationRepository = notificationRepository,
                        tokenManager = tokenManager,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
