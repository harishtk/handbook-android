@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package com.handbook.app.feature.home.presentation.landing

import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
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
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.component.CustomConfirmDialog
import com.handbook.app.core.designsystem.component.HandbookTopAppBar
import com.handbook.app.core.designsystem.shimmerBackground
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.presentation.accounts.FilterSheetContent
import com.handbook.app.feature.home.presentation.accounts.TemporarySheetFilters
import com.handbook.app.feature.home.presentation.accounts.components.ExpandableAccountEntryCard
import com.handbook.app.feature.home.presentation.accounts.components.FabOption
import com.handbook.app.feature.home.presentation.accounts.components.OnMainFabClickBehavior
import com.handbook.app.feature.home.presentation.accounts.components.SpeedDialFab
import com.handbook.app.feature.home.presentation.accounts.components.SpeedDialState
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.filteredDelay
import com.handbook.app.ui.defaultSpacerSize
import com.handbook.app.ui.theme.DarkGreen
import com.handbook.app.ui.theme.DarkRed
import com.handbook.app.ui.theme.HandbookTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
    onAddEntryRequest: (Long, TransactionType) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onSelectPartyRequest: (selectedPartyId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val accountEntriesUiState by viewModel.accountEntriesUiState.collectAsStateWithLifecycle()
    val filterUiState by viewModel.filterUiState.collectAsStateWithLifecycle()

    var confirmDeleteItem by remember { mutableStateOf<AccountEntry?>(null) }

    HomeScreen(
        modifier = modifier,
        entriesUiState = accountEntriesUiState,
        filterUiState = filterUiState,
        filterUiAction = viewModel.acceptFilterAction,
        snackbarHostState = snackbarHostState,
        uiAction = viewModel.accept,
        onFabClick = { onAddEntryRequest(0, it) },
        onNavigateToNotifications = onNavigateToNotifications,
        onSelectPartyRequest = onSelectPartyRequest,
        onNavigationIconClick = {
            sharedViewModel.setNavigationDrawerSignal(true)
        }
    )

    // FIXME: View model is recreating everytime
//    LaunchedEffect(sharedViewModel.feedRefreshSignal) {
//        viewModel.accept(HomeUiAction.Refresh)
//    }

    if (confirmDeleteItem != null) {
        val entry = confirmDeleteItem!!
        CustomConfirmDialog(
            title = "Delete Entry",
            description = "Are you sure you want to delete this entry?\n${entry.title}",
            onDismiss = { confirmDeleteItem = null },
            onConfirm = {
                viewModel.accept(HomeUiAction.DeleteEntry(entry))
                confirmDeleteItem = null
            }
        )
    }

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
                onAddEntryRequest(event.entry.entryId, event.entry.transactionType)
            }

            is HomeUiEvent.NavigateToDeleteEntry -> {
                // Confirm delete
                confirmDeleteItem = event.entry
            }

            is HomeUiEvent.OnEntryDeleted -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Entry deleted")
                }
            }
        }
    }

    val currentNavController by rememberUpdatedState(navController)
    DisposableEffect(currentNavController) {
        val navBackStackEntry = currentNavController.currentBackStackEntry
        val savedStateHandle = navBackStackEntry?.savedStateHandle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { // Or another appropriate lifecycle event
                // Extract selected Party Id
                savedStateHandle?.get<Long?>("partyId")?.let { result ->
                    viewModel.acceptFilterAction(FilterUiAction.OnSelectedParty(result))
                    savedStateHandle.remove<Long>("partyId")
                }
            }
        }
        navBackStackEntry?.lifecycle?.addObserver(observer)

        onDispose {
            navBackStackEntry?.lifecycle?.removeObserver(observer)
        }
    }
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    entriesUiState: AccountEntryUiState = AccountEntryUiState.Idle,
    filterUiState: FilterUiState = FilterUiState(),
    filterUiAction: (FilterUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    uiAction: (HomeUiAction) -> Unit = {},
    onFabClick: (TransactionType) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onSelectPartyRequest: (selectedPartyId: Long) -> Unit = {},
    onNavigationIconClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // This code should be called when UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { true /* Add custom conditions here. eg. !isSyncing */ }

    // val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val speedDialState = SpeedDialState.rememberSpeedDialState()

    val filterSheetState = rememberModalBottomSheetState()

    LaunchedEffect(filterUiState.showFilterSheet, filterSheetState) {
        if (filterUiState.showFilterSheet) {
            // Ensure any previous dismiss actions are complete before trying to show
            // This can sometimes help if the sheet was in the process of hiding.
            if (!filterSheetState.isVisible) { // Only call show if it's not already trying to be visible
                filterSheetState.show()
            }
        } else {
            if (filterSheetState.isVisible) { // Only call hide if it's currently visible or about to be
                filterSheetState.hide()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize(),
            //.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HandbookTopAppBar(
                    modifier = Modifier,
                    title = @Composable {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleLarge
                                .copy(fontWeight = FontWeight.W400)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigationIconClick) {
                            Icon(
                                painter = painterResource(id = HandbookIcons.Id_Breadcrumbs),
                                contentDescription = "Open Drawer",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = HandbookIcons.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            },
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
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeFilterCount = filterUiState.activeFilters.count()
                        if (activeFilterCount > 0) {
                            AssistChip(
                                onClick = { filterUiAction(FilterUiAction.ApplyAndResetFilters) },
                                label = { Text("Clear Filters ($activeFilterCount)") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear Filters",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        } else {
                            // Empty spacer to keep the filter icon to the right
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .padding(0.dp) // Surface border
                                            .offset(x = (-4).dp, y = 4.dp)
                                    ) {
                                        Text(
                                            "$activeFilterCount"
                                        )
                                    }
                                }
                            }
                        ) {
                            // Pass the action to the ViewModel's acceptor
                            IconButton(onClick = { filterUiAction(FilterUiAction.OpenFilterSheet) }) {
                                Icon(Icons.Outlined.FilterAlt, contentDescription = "Open Filters")
                            }
                        }

                    }

                    AnimatedContent(
                        targetState = entriesUiState,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(220, delayMillis = 90)))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        },
                        label = "AnimatedContent"
                    ) { targetState ->
                        when (targetState) {
                            is AccountEntryUiState.Error -> {
                                FullScreenErrorLayout(
                                    errorMessage = targetState.errorMessage,
                                    onClick = { uiAction(HomeUiAction.Refresh) }
                                )
                            }

                            AccountEntryUiState.Idle -> {}
                            AccountEntryUiState.Loading -> {
                                LoadingScreen()
                            }

                            is AccountEntryUiState.Success -> {
                                SearchResultContent(
                                    uiState = targetState,
                                    onUiAction = uiAction,
                                )
                            }
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

        if (filterUiState.showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { filterUiAction(FilterUiAction.DismissFilterSheet) },
                sheetState = filterSheetState
            ) {
                FilterSheetContent(
                    temporaryFilters = filterUiState.temporaryFilters,
                    onTemporaryFiltersChanged = { updatedFilters ->
                        filterUiAction(FilterUiAction.UpdateTemporaryFilters(updatedFilters))
                    },
                    onApply = {
                        filterUiAction(FilterUiAction.ApplyFilters)
                    },
                    onResetAll = {
                        // Decide if "Reset All" only resets temporary or also applies the reset
                        // filterUiAction(FilterUiAction.ResetTemporaryFilters)
                        // OR:
                        filterUiAction(FilterUiAction.ApplyAndResetFilters)

                    },
                    onDismiss = { filterUiAction(FilterUiAction.DismissFilterSheet) },
                    onPartySelectRequest = { party: Party? ->
                        onSelectPartyRequest(party?.id ?: 0L)
                    }
                )
            }
        }
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

        var delayedRefreshState by remember {
            mutableStateOf<LoadState>(LoadState.NotLoading(endOfPaginationReached = false))
        }

        LaunchedEffect(lazyPagingItems.loadState) {

            snapshotFlow { lazyPagingItems.loadState }
                .map { it.refresh }
                .distinctUntilChanged()
                .filteredDelay(
                    loadingItemPredicate = { it is LoadState.Loading },
                    minDelayFromLoadingItem = 700L
                )
                .collect { processedRefreshState ->
                    delayedRefreshState = processedRefreshState
                }
        }

        if (lazyPagingItems.itemCount == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(defaultSpacerSize))
                if (uiState.filters != AccountEntryFilters.None) {
                    Text("No entries found", style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text("No entries yet", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = listState) {
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
                                        onEditRequest = {
                                            onUiAction(HomeUiAction.OnEditEntry(item.accountEntryWithDetails.entry))
                                        },
                                        onDeleteRequest = {
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
                                item {
                                    Spacer(modifier = Modifier.height(defaultSpacerSize))
                                }
                            }
                        }
                    }
                }

//                when (val refreshState = delayedRefreshState) {
//                    is LoadState.Loading -> {
//                        LoadingScreen()
//                    }
//                    is LoadState.Error -> {
//                        ErrorRetryItem(Modifier.align(Alignment.BottomCenter)) {
//                            lazyPagingItems.retry()
//                        }
//                    }
//
//                    is LoadState.NotLoading -> {
//                        if (lazyPagingItems.itemCount == 0 && !refreshState.endOfPaginationReached &&
//                            lazyPagingItems.loadState.refresh is LoadState.NotLoading /* Ensure raw refresh is also done */) {
//                            Text("No items yet.", modifier = Modifier.align(Alignment.Center))
//                        }
//                    }
//                }
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
                modifier = Modifier
                    .fillMaxWidth()
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
        val tomorrowMillis = today.date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

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
                createdAt = tomorrowMillis,
                updatedAt = tomorrowMillis,
                transactionDate = tomorrowMillis,
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

        val temporarySheetFilters = TemporarySheetFilters(entryType = EntryType.BANK)

        HomeScreen(
            entriesUiState = successState,
            filterUiState = FilterUiState(
                showFilterSheet = false,
                temporaryFilters = temporarySheetFilters,
                activeFilters = temporarySheetFilters.toAccountEntryFilters()
            ),
            filterUiAction = {},
            uiAction = {},
            onNavigationIconClick = {}
        )
    }
}