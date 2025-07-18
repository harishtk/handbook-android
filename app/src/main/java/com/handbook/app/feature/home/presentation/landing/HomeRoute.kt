@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)


package com.handbook.app.feature.home.presentation.landing

import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.paging.insertSeparators
import androidx.paging.map
import com.handbook.app.ObserverAsEvents
import com.handbook.app.SharedViewModel
import com.handbook.app.core.designsystem.shimmerBackground
import com.handbook.app.debugToast
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.accounts.components.ExpandableAccountEntryCard
import com.handbook.app.feature.home.presentation.accounts.components.FabOption
import com.handbook.app.feature.home.presentation.accounts.components.OnMainFabClickBehavior
import com.handbook.app.feature.home.presentation.accounts.components.SpeedDialFab
import com.handbook.app.feature.home.presentation.accounts.components.SpeedDialState
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.ui.theme.DarkGreen
import com.handbook.app.ui.theme.DarkRed
import com.handbook.app.ui.theme.HandbookTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
    onAddEntryRequest: (Long, TransactionType) -> Unit,
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
        onFabClick = { onAddEntryRequest(0, it) },
        onNavigateToNotifications = onNavigateToNotifications
    )

    // FIXME: View model is recreating everytime
//    LaunchedEffect(sharedViewModel.feedRefreshSignal) {
//        viewModel.accept(HomeUiAction.Refresh)
//    }

    ObserverAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is HomeUiEvent.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }

            is HomeUiEvent.ShowToast -> {
                Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
            }

            is HomeUiEvent.NavigateToEditEntry -> {
                context.debugToast("Navigate to edit entry: ${event.entry.entryId} ${event.entry.transactionType}")
                onAddEntryRequest(event.entry.entryId, event.entry.transactionType)
            }

            is HomeUiEvent.NavigateToDeleteEntry -> {
                // Confirm delete
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
    val speedDialState = SpeedDialState.rememberSpeedDialState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize(),
            //.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
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
                                onUiAction = uiAction,
                            )
                        }
                    }
                }
            }
        }

        val targetScrimAlpha = speedDialState.uiState.scrimAlpha
        AnimatedVisibility(
            visible = speedDialState.uiState.isExpanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = targetScrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { speedDialState.collapse() }
                    )
            )
        }

        // Custom Fab
        val fabOptions = listOf(
            FabOption(Icons.Default.SouthWest, DarkGreen, "Add Income") {
                onFabClick(TransactionType.INCOME)
            },
            FabOption(Icons.Default.NorthEast, DarkRed, "Add Expense") {
                onFabClick(TransactionType.EXPENSE)
            },
        )

        SpeedDialFab(
            state = speedDialState,
            initialIcon = Icons.Default.Add,
            expandedIcon = Icons.Default.Close,
            options = fabOptions,
            onMainFabClickBehavior = OnMainFabClickBehavior.EXPAND_ONLY_OR_EXECUTE_PRIMARY_WHEN_COLLAPSED,
            onMainFabClickWhileCollapsed = {

            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun SearchResultContent(
    modifier: Modifier = Modifier,
    uiState: AccountEntryUiState.Success,
    onUiAction: (HomeUiAction) -> Unit = {},
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
                            else -> "Footer"
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
                                    onEdit = {
                                        onUiAction(HomeUiAction.OnEditEntry(item.accountEntryWithDetails.entry))
                                    },
                                    onDelete = {
                                        onUiAction(HomeUiAction.OnDeleteEntry(item.accountEntryWithDetails.entry))
                                    }
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

                            is AccountEntryUiModel.Footer -> {

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
    // Shimmer effect for loading state
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(24.dp) // Approximate height of a title
                .shimmerBackground(
                    RoundedCornerShape(12.dp)
                )
        )
        repeat(3) { // Show 3 shimmer items for ExpandableAccountEntryCard
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 4.dp)
                    .shimmerBackground(
                        RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true,
    ) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val emptySourceLoadStatesComplete = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true), // Refresh is complete, and the list is empty, so end of pagination
            append = LoadState.NotLoading(endOfPaginationReached = true),  // Append is complete, no more data
            prepend = LoadState.NotLoading(endOfPaginationReached = true) // Prepend is complete, no more data
        )

        val sampleEntry = AccountEntryWithDetails(
            entry = AccountEntry(
                entryId = 2,
                title = "Utility Bill Payment",
                amount = 300.0,
                transactionType = TransactionType.EXPENSE,
                createdAt = today.date.plus(1, DateTimeUnit.DAY).toEpochDays(),
                updatedAt = today.date.plus(1, DateTimeUnit.DAY).toEpochDays(),
                transactionDate = today.date.plus(1, DateTimeUnit.DAY).toEpochDays(),
                partyId = null, // No party
                categoryId = 2,
                entryType = EntryType.BANK,
                description = null // No description
            ),
            category = Category(
                id = 2,
                name = "Utilities",
                description = null,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
            party = null, // No party
        )
        val pagingData = PagingData.from(listOf(sampleEntry), sourceLoadStates = emptySourceLoadStatesComplete, null)
        val entriesFlow: Flow<PagingData<AccountEntryUiModel>> = flowOf(pagingData)
            .map { pagingData -> pagingData
                .map(AccountEntryUiModel::Item)
                .insertSeparators { before, after ->
                    if (before == null) {
                        return@insertSeparators AccountEntryUiModel.Separator("Today", today.date)
                    } else {
                        return@insertSeparators null
                    }
                }
            }

        val successState = AccountEntryUiState.Success(
            accountEntries = entriesFlow,
            filters = AccountEntryFilters.None
        )

        HomeScreen(
            entriesUiState = successState
        )
    }
}