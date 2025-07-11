@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.post

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.handbook.app.ObserverAsEvents
import com.handbook.app.SharedViewModel
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.landing.UserAvatar
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.MaterialColor.Red50

@Composable
internal fun PostDetailRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: PostDetailViewModel = hiltViewModel(),
    onNavUp: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.postDetailUiState.collectAsStateWithLifecycle()

    PostDetailScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = viewModel.accept,
        snackbarHostState = snackbarHostState,
        onNavUp = onNavUp
    )

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
            is PostDetailUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(it.message.asString(context))
                }
            }
            is PostDetailUiEvent.ShowToast -> {
                Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is PostDetailUiEvent.NavigateToProfile -> {
                onNavigateToProfile(
                    if (it.isSelf) "" else it.userId
                )
            }
        }
    }
}

@Composable
private fun PostDetailScreen(
    modifier: Modifier,
    uiState: PostDetailUiState,
    uiAction: (PostDetailUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onNavUp: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { snackbarHostState },
        topBar = {
            TopAppBar(
                title = { Text(text = "Post Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .systemBarsPadding()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                )
        ) {
            when (uiState) {
                PostDetailUiState.Idle -> {}
                PostDetailUiState.Loading -> LoadingScreen()
                is PostDetailUiState.Error -> {
                    FullScreenErrorLayout(
                        errorMessage = uiState.errorMessage,
                        onClick = { uiAction(PostDetailUiAction.Refresh) }
                    )
                }
                is PostDetailUiState.Success -> {
                    PostDetailContent(
                        uiState = uiState,
                        uiAction = uiAction
                    )
                }
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    modifier: Modifier = Modifier,
    uiState: PostDetailUiState.Success,
    uiAction: (PostDetailUiAction) -> Unit = {},
) {
    val userMap = remember(uiState) {
        uiState.users.associateBy { it.id }
    }

    val post = uiState.post
    val author = userMap[post.authorId]!!

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info Header
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            uiAction(PostDetailUiAction.NavigateToProfile(uiState.post.authorId))
                        }
                    )
            ) {
                Row(verticalAlignment = CenterVertically) {
                    UserAvatar(
                        modifier = Modifier.size(36.dp),
                        profile = author
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = author.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(text = "@${author.username}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Actions (e.g., Like button) - Simplified
            IconButton(
                onClick = {
                    uiAction(PostDetailUiAction.LikeToggle(post.id, post.likedByCurrentUser))
                },
            ) {
                AnimatedContent(post.likedByCurrentUser) { liked ->
                    if (liked) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Liked", tint = Red50)
                    } else {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = "Like")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun PostDetailScreenPreview() {
    HandbookTheme {
        PostDetailScreen(
            modifier = Modifier,
            uiState = PostDetailUiState.Success(
                post = Post(
                    id = "1",
                    authorId = "1",
                    content = "This is a sample post content.",
                    createdAt = "2023-09-10T12:00:00Z",
                    updatedAt = "2023-09-10T12:00:00Z",
                    likesCount = 10,
                    likedByCurrentUser = true
                ),
                users = listOf(
                    UserSummary(
                        id = "1",
                        username = "john_doe",
                        displayName = "John Doe",
                        profilePictureId = "",
                        isFollowing = false,
                    )
                ),
                isRefreshing = false,
            ),
            uiAction = {}
        )
    }
}

