@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.search

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
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
import com.handbook.app.feature.home.domain.model.PostSummary
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.landing.UserAvatar
import com.handbook.app.ui.theme.HandbookTheme

@Composable
internal fun SearchRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavUp: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val uiState by viewModel.searchResultUiState.collectAsStateWithLifecycle()

    SearchScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = viewModel.accept,
        snackbarHostState = snackbarHostState,
        listState = listState,
        onNavUp = onNavUp
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visible -> visible.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                viewModel.accept(
                    SearchUiAction.Scroll(
                        currentQuery = "",
                        totalItemCount = total,
                        visibleItemCount = listState.layoutInfo.visibleItemsInfo.size,
                        lastVisibleItemPosition = lastVisible
                    )
                )
            }
    }

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
            is SearchUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(it.message.asString(context))
                }
            }
            is SearchUiEvent.ShowToast -> {
                Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is SearchUiEvent.NavigateToProfile -> {
                onNavigateToProfile(
                    if (it.isSelf) "" else it.userId
                )
            }
            is SearchUiEvent.NavigateToPostDetail -> {
                onNavigateToPostDetail(it.postId)
            }

            SearchUiEvent.ResetSearch -> TODO()
            SearchUiEvent.ScrollToTop -> {
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    uiState: SearchResultState,
    uiAction: (SearchUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    listState: LazyListState = LazyListState(),
    onNavUp: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { snackbarHostState },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    // Trigger search action if needed, e.g., uiAction(SearchUiAction.Search(it))
                    uiAction(SearchUiAction.OnTyping(it))
                },
                placeholder = {
                    Text("Search users, posts..")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            when (uiState) {
                SearchResultState.Idle -> {}
                SearchResultState.Loading -> LoadingScreen()
                is SearchResultState.Success -> {
                    SearchResultContent(
                        uiState = uiState,
                        uiAction = uiAction,
                        listState = listState,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultContent(
    modifier: Modifier = Modifier,
    uiState: SearchResultState.Success,
    uiAction: (SearchUiAction) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize()
            .padding(8.dp),
    ) {
        LazyColumn(state = listState) {
            uiState.data.forEach { uiModel ->
                when (uiModel) {
                    is SearchResultUiModel.Separator -> {
                        item {
                            Text(
                                text = uiModel.title,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    is SearchResultUiModel.Footer -> {
                        item {
                            Text(
                                text = uiModel.title.asString(context),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    is SearchResultUiModel.PostSearchResult -> {
                        items(uiModel.posts) {
                            PostResultCard(
                                post = it,
                                author = it.author,
                                onPostClick = {
                                    uiAction(SearchUiAction.NavigateToPostDetail(it.id))
                                },
                                onLikeToggle = { liked ->

                                },
                                onUserProfileClick = {
                                    uiAction(SearchUiAction.NavigateToProfile(it.authorId))
                                }
                            )
                        }
                    }

                    is SearchResultUiModel.UserSearchResult -> {
                        items(uiModel.users) {
                            UserResultCard(
                                user = it,
                                onUserProfileClick = {
                                    uiAction(SearchUiAction.NavigateToProfile(it.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostResultCard(
    modifier: Modifier = Modifier,
    post: PostSummary,
    author: UserSummary,
    onPostClick: () -> Unit = {},
    onLikeToggle: (Boolean) -> Unit = {},
    onUserProfileClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = true, onClick = onPostClick)
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
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Actions (e.g., Like button) - Simplified
//            IconButton(
//                onClick = {
//                    onLikeToggle(post.likedByCurrentUser)
//                },
//            ) {
//                AnimatedContent(post.likedByCurrentUser) { liked ->
//                    if (liked) {
//                        Icon(Icons.Filled.Favorite, contentDescription = "Liked", tint = Red50)
//                    } else {
//                        Icon(Icons.Filled.FavoriteBorder, contentDescription = "Like")
//                    }
//                }
//            }
        }
    }
}

@Composable
private fun UserResultCard(
    modifier: Modifier = Modifier,
    user: UserSummary,
    onUserProfileClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onUserProfileClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = CenterVertically
        ) {
            UserAvatar(
                modifier = Modifier.size(40.dp),
                profile = user
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = user.displayName, style = MaterialTheme.typography.titleMedium)
                Text(text = "@${user.username}", style = MaterialTheme.typography.bodyMedium)
                // Optionally, add a follow button or other user-specific actions here
            }
        }
    }
}

@Composable
fun LoadingScreen(
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
private fun SearchScreenPreview() {
    HandbookTheme {
        SearchScreen(
            modifier = Modifier,
            uiState = SearchResultState.Success(
                listOf(
                    SearchResultUiModel.Separator("Showing 10 of 100 results"),
                    SearchResultUiModel.Separator("Posts"),
                    SearchResultUiModel.PostSearchResult(
                        query = "query",
                        posts = listOf(
                            PostSummary(
                                id = "1",
                                authorId = "1",
                                content = "This is a sample post content.",
                                createdAt = "2023-09-10T12:00:00Z",
                                likesCount = 10,
                                likedByCurrentUser = true,
                                author = UserSummary(
                                    id = "1",
                                    username = "john_doe",
                                    displayName = "John Doe",
                                    profilePictureId = "",
                                    isFollowing = false,
                                )
                            )
                        ),
                    ),
                    SearchResultUiModel.Separator("Users"),
                    SearchResultUiModel.UserSearchResult(
                        query = "query",
                        users = listOf(
                            UserSummary(
                                id = "1",
                                username = "john_doe",
                                displayName = "John Doe",
                                profilePictureId = "",
                                isFollowing = false,
                            )
                        ),
                    ),
                )
            ),
            uiAction = {},
            snackbarHostState = SnackbarHostState(),
            listState = rememberLazyListState(),
            onNavUp = {}
        )
    }
}

