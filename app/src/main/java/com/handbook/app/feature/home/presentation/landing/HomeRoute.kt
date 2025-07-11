@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.landing

import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.handbook.app.ObserverAsEvents
import com.handbook.app.SharedViewModel
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.ui.spacerSizeTiny
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.MaterialColor.Red50

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
    onWritePostRequest: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val feedUiState by viewModel.feedUiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        feedUiState = feedUiState,
        snackbarHostState = snackbarHostState,
        uiAction = viewModel.accept,
        onWritePostRequest = onWritePostRequest,
        onNavigateToNotifications = onNavigateToNotifications
    )

    // FIXME: View model is recreating everytime
//    LaunchedEffect(sharedViewModel.feedRefreshSignal) {
//        viewModel.accept(HomeUiAction.Refresh)
//    }

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
            is HomeUiEvent.NavigateToProfile -> {
                onNavigateToProfile(
                    if (it.isSelf) "" else it.userId
                )
            }
            is HomeUiEvent.NavigateToPost -> {
                onNavigateToPost(it.postId)
            }
            is HomeUiEvent.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(it.message.asString(context))
                }
            }
            is HomeUiEvent.ShowToast -> {
                Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    feedUiState: FeedUiState = FeedUiState.Idle,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    uiAction: (HomeUiAction) -> Unit = {},
    onWritePostRequest: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {

    // This code should be called when UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { true /* Add custom conditions here. eg. !isSyncing */ }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = "Hoots", style = MaterialTheme.typography.titleLarge) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = onNavigateToNotifications
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Unread",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.TopEnd)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onWritePostRequest,
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Write post",
                        Modifier.size(16.dp))
                    Text(text = "Write", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                ),
        ) {
            PullToRefreshBox(
                isRefreshing = (feedUiState is FeedUiState.Success && feedUiState.isRefreshing),
                onRefresh = { uiAction(HomeUiAction.Refresh) },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // .verticalScroll(rememberScrollState())
                        .imePadding(),
                ) {
                    when (feedUiState) {
                        FeedUiState.Idle -> {}
                        FeedUiState.Loading -> {
                            LoadingScreen()
                        }

                        is FeedUiState.Error -> {
                            FullScreenErrorLayout(
                                errorMessage = feedUiState.errorMessage,
                                onClick = { uiAction(HomeUiAction.Refresh) }
                            )
                        }

                        is FeedUiState.Success -> {
                            FeedContent(
                                feeds = feedUiState,
                                uiAction = uiAction,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedContent(
    modifier: Modifier = Modifier,
    feeds: FeedUiState.Success,
    uiAction: (HomeUiAction) -> Unit = {},
) {
    val userMap = remember(feeds.users) {
        feeds.users.associateBy { it.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        val listState = rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .map { visible -> visible.lastOrNull()?.index ?: 0 }
                .distinctUntilChanged()
                .collect { lastVisible ->
                    val total = listState.layoutInfo.totalItemsCount
                    if (lastVisible >= total - 3) {
                        if (!feeds.endOfPaginationReached) {
                            uiAction(HomeUiAction.LoadMore)
                        }
                    }
                }
        }

        LazyColumn(
            state = listState
        ) {
            items(feeds.posts) { post ->
                val author = userMap[post.authorId]!!

                PostCard(
                    post = post,
                    author = author,
                    onLikeToggle = {
                        uiAction(HomeUiAction.LikeToggle(post.id, it))
                    },
                    onUserProfileClick = {
                        uiAction(HomeUiAction.NavigateToProfile(author.id))
                    },
                    onPostClick = {
                        uiAction(HomeUiAction.NavigateToPost(post.id))
                    }
                )
            }
            item { Spacer(Modifier.height(spacerSizeTiny)) }
        }
    }
}

@Composable
private fun PostCard(
    modifier: Modifier = Modifier,
    post: Post,
    author: UserSummary,
    onPostClick: () -> Unit = {},
    onLikeToggle: (Boolean) -> Unit = {},
    onUserProfileClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info Header
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onUserProfileClick
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
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clickable(onClick = onPostClick)
            )

            // Actions (e.g., Like button) - Simplified
            IconButton(
                onClick = {
                    onLikeToggle(post.likedByCurrentUser)
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
fun UserAvatar(
    modifier: Modifier = Modifier,
    profile: UserSummary,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!profile.profilePictureId.isBlank()) {
            // In a real app, you'd use a library like Coil or Glide here
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
            )
//            Image(
//                painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
//                contentDescription = "Profile Image",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
        } else {
            val initials = profile.displayName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
            Text(
                text = initials.ifEmpty { "?" },
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
            )
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
private fun HomeDefaultPreview() {
    Box(
        Modifier.background(Color.White)
    ) {
        HandbookTheme(
            darkTheme = false,
            disableDynamicTheming = false,
        ) {
            HomeScreen(
                feedUiState = FeedUiState.Success(
                    posts = listOf(
                        Post(
                            id = "1",
                            authorId = "1",
                            content = "This is a sample post content.",
                            createdAt = "2023-09-10T12:00:00Z",
                            updatedAt = "2023-09-10T12:00:00Z",
                            likesCount = 10,
                            likedByCurrentUser = true
                        ),
                    ),
                    users = listOf(
                        UserSummary(
                            id = "1",
                            username = "john_doe",
                            displayName = "John Doe",
                            profilePictureId = "",
                            isFollowing = false,
                        )
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun FeedContentPreview() {
    HandbookTheme {
        PostCard(
            post = Post(
                id = "1",
                authorId = "1",
                content = "This is a sample post content.",
                createdAt = "2023-09-10T12:00:00Z",
                updatedAt = "2023-09-10T12:00:00Z",
                likesCount = 10,
                likedByCurrentUser = true
            ),
            author = UserSummary(
                id = "1",
                username = "john_doe",
                displayName = "John Doe",
                profilePictureId = "",
                isFollowing = false,
            )
        )
    }
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    HandbookTheme {
        LoadingScreen()
    }
}