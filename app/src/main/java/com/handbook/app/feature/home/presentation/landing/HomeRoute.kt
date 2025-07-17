@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)


package com.handbook.app.feature.home.presentation.landing

import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.SharedViewModel
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.accounts.components.ExpandableAccountEntryCard
import com.handbook.app.feature.home.presentation.accounts.components.FabOption
import com.handbook.app.feature.home.presentation.accounts.components.OnMainFabClickBehavior
import com.handbook.app.feature.home.presentation.accounts.components.SpeedDialFab
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.ui.spacerSizeTiny
import com.handbook.app.ui.theme.DarkGreen
import com.handbook.app.ui.theme.DarkRed
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.MaterialColor.Red50
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
    onWritePostRequest: () -> Unit,
    onAddEntryRequest: (TransactionType) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val accountEntriesUiState by viewModel.accountEntriesUiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        entriesUiState = accountEntriesUiState,
        snackbarHostState = snackbarHostState,
        uiAction = viewModel.accept,
        onFabClick = onAddEntryRequest,
        onNavigateToNotifications = onNavigateToNotifications
    )

    // FIXME: View model is recreating everytime
//    LaunchedEffect(sharedViewModel.feedRefreshSignal) {
//        viewModel.accept(HomeUiAction.Refresh)
//    }

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
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
    entriesUiState: AccountEntryUiState = AccountEntryUiState.Idle,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    uiAction: (HomeUiAction) -> Unit = {},
    onFabClick: (TransactionType) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {

    // This code should be called when UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { true /* Add custom conditions here. eg. !isSyncing */ }

    // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
            //.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            val fabOptions = listOf(
                FabOption(Icons.Default.SouthWest, DarkGreen,  "Add Income") {
                    onFabClick(TransactionType.INCOME)
                },
                FabOption(Icons.Default.NorthEast, DarkRed, "Add Expense") {
                    onFabClick(TransactionType.EXPENSE)
                },
            )

            SpeedDialFab(
                initialIcon = Icons.Default.Add,
                expandedIcon = Icons.Default.Close,
                options = fabOptions,
                onMainFabClickBehavior = OnMainFabClickBehavior.EXPAND_ONLY_OR_EXECUTE_PRIMARY_WHEN_COLLAPSED,
                onMainFabClickWhileCollapsed = {
                        
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
            ) {
                when (entriesUiState) {
                    is AccountEntryUiState.Error -> {
                        FullScreenErrorLayout(
                            errorMessage = entriesUiState.errorMessage,
                            onClick = { uiAction(HomeUiAction.Refresh) }
                        )
                    }
                    AccountEntryUiState.Idle -> {}
                    AccountEntryUiState.Loading -> {
                        LoadingScreen()
                    }
                    is AccountEntryUiState.Success -> {
                        SearchResultContent(
                            uiState = entriesUiState,
                        )
                    }
                }
            }
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
                        Text(
                            text = "@${author.username}",
                            style = MaterialTheme.typography.bodySmall
                        )
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
private fun SearchResultContent(
    modifier: Modifier = Modifier,
    uiState: AccountEntryUiState.Success,
    listState: LazyListState = rememberLazyListState(),
) {
    LocalContext.current

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        val lazyPagingItems: LazyPagingItems<AccountEntryUiModel> = uiState.accountEntries.collectAsLazyPagingItems()

        // After the initial load or a refresh, itemCount will reflect the loaded items.
        // It's important to also consider the load states for a complete picture.
        LaunchedEffect(lazyPagingItems.loadState) {
            if (lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
                lazyPagingItems.itemCount == 0) {
                // PagingData is effectively empty after the initial load/refresh
                // Show "No items found" message or similar UI
            }
        }

        if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
            LoadingIndicator()
        } else if (lazyPagingItems.itemCount == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (uiState.filters != AccountEntryFilters.None) {
                    Text("No entries found", style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text("No entries yet", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(state = listState,) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey {
                        when (it) {
                            is AccountEntryUiModel.Item -> it.accountEntryWithDetails.entry.entryId
                            is AccountEntryUiModel.Separator -> it.date
                        }
                    },
                    contentType = lazyPagingItems.itemContentType { "accountEntry" }, // Generic content type
                ) { index ->
                    val item = lazyPagingItems[index]
                    if (item != null) {
                        when (item) {
                            is AccountEntryUiModel.Item -> {
                                ExpandableAccountEntryCard(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    entryDetails = item.accountEntryWithDetails,
                                    onEdit = {},
                                    onDelete = {}
                                )
                            }

                            is AccountEntryUiModel.Separator -> {
                                Box(
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(text = item.text, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }


                when (lazyPagingItems.loadState.append) {
                    is LoadState.Error -> {
                        item { ErrorRetryItem(Modifier.animateItem()) { lazyPagingItems.retry() } }
                    }
                    LoadState.Loading -> {
                        item { LoadingIndicator(Modifier.animateItem()) }
                    }
                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.loadState.append.endOfPaginationReached) {
                            // Also empty if pagination ended and count is 0
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(
            modifier = modifier.size(24.dp),
            color = Color.Gray,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ErrorRetryItem(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth()) {
        ElevatedButton(
            onClick = onRetry,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun UserAvatar(modifier: Modifier, profile: UserSummary) {
    TODO("Not yet implemented")
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
private fun LoadingScreenPreview() {
    HandbookTheme {
        LoadingScreen()
    }
}