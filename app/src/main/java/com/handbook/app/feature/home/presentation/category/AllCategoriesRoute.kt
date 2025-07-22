@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.category

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.handbook.app.ObserverAsEvents
import com.handbook.app.core.designsystem.component.HandbookBackground
import com.handbook.app.core.designsystem.component.HandbookGradientBackground
import com.handbook.app.core.designsystem.component.ThemePreviews
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.ui.theme.Gray60
import com.handbook.app.ui.theme.HandbookTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
internal fun AllCategoriesRoute(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AllCategoriesViewModel = hiltViewModel(),
    onNavUp: () -> Unit,
    onAddCategoryRequest: (categoryId: Long?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isInPickerMode by viewModel.isInPickerMode.collectAsStateWithLifecycle()

    AllCategoriesScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = viewModel.accept,
        snackbarHostState = snackbarHostState,
        listState = listState,
        onNavUp = onNavUp,
        onAddCategoryRequest = onAddCategoryRequest,
    )

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
            is AllCategoriesUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(it.message.asString(context))
                }
            }
            is AllCategoriesUiEvent.ShowToast -> {
                Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is AllCategoriesUiEvent.NavigateToEditCategory -> {
                if (isInPickerMode) {
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set("categoryId", it.categoryId)
                    navController.popBackStack()
                } else {
                    onAddCategoryRequest(it.categoryId)
                }
            }
        }
    }

}

@Composable
private fun AllCategoriesScreen(
    modifier: Modifier,
    uiState: CategoriesUiState,
    uiAction: (AllCategoriesUiAction) -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    listState: LazyListState = LazyListState(),
    onNavUp: () -> Unit = {},
    onAddCategoryRequest: (categoryId: Long?) -> Unit = {},
) {
    TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var fabCenter by remember { mutableStateOf(Offset.Zero) }
    var fabSize by remember { mutableStateOf(IntSize.Zero) }
    var isRevealed by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { snackbarHostState },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Categories", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddCategoryRequest(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.onGloballyPositioned { coords ->
                    fabSize = coords.size
                    fabCenter = coords.localToRoot(Offset.Zero) + Offset(
                        coords.size.width / 2f,
                        coords.size.height / 2f
                    )
                }
                    .imePadding()
                    .systemBarsPadding()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    // Trigger search action if needed, e.g., uiAction(AllCategoriesUiAction.Search(it))
                    uiAction(AllCategoriesUiAction.OnTypingQuery(it))
                },
                placeholder = {
                    Text("Search name, phone..")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                uiAction(AllCategoriesUiAction.OnTypingQuery(""))
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Gray60,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "Animated Content"
            ) { targetState ->
                when (targetState) {
                    is CategoriesUiState.Idle -> {}
                    is CategoriesUiState.Loading -> {
                        LoadingIndicator()
                    }
                    is CategoriesUiState.Categories -> SearchResultContent(
                        uiState = targetState,
                        uiAction = uiAction,
                        listState = listState,
                    )

                    CategoriesUiState.EmptyResult -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            if (searchQuery.isNotBlank()) {
                                Text("No categories found", style = MaterialTheme.typography.headlineSmall)
                            } else {
                                Text("No categories yet", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }

                    is CategoriesUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Text("Error: ${targetState.uiText.asString(LocalContext.current)}")
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun SearchResultContent(
    modifier: Modifier = Modifier,
    uiState: CategoriesUiState.Categories,
    uiAction: (AllCategoriesUiAction) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    LocalContext.current

    Column(
        modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        val lazyPagingItems: LazyPagingItems<Category> = uiState.categories.collectAsLazyPagingItems()

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
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (uiState.searchQuery.isNotBlank()) {
                    Text("No categories found", style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text("No categories yet", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(state = listState,) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.id },
                    contentType = lazyPagingItems.itemContentType { "party" },
                ) { index ->
                    val item = lazyPagingItems[index]
                    if (item != null) {
                        CategoryItem(
                            party = item,
                            onClick = { uiAction(AllCategoriesUiAction.OnItemClick(item)) },
                            modifier = Modifier.animateItem()
                        )
                        HorizontalDivider()
                    } else {
                        // Placeholder
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
private fun CategoryItem(
    modifier: Modifier = Modifier,
    party: Category,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        UserAvatar(
//            name = party.name,
//            modifier = Modifier.size(32.dp)
//        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = party.name,
                style = MaterialTheme.typography.titleMedium
            )
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

@ThemePreviews
@Composable
private fun AllCategoriesScreenPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        val sampleCategoriesList = listOf<Category>(
            Category.create(
                id = 0,
                name = "Category Name",
            ),
            Category.create(
                id = 1,
                name = "Category Name",
            ),
            Category.create(
                id = 2,
                name = "Category Name",
            ),
            Category.create(
                id = 3,
                name = "Category Name",
            ),
        )

        AllCategoriesScreen(
            modifier = Modifier,
            uiState = CategoriesUiState.Categories(
                flowOf(PagingData.from(sampleCategoriesList)),
                "",
            ),
            uiAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryItemPreview() {
    HandbookBackground {
        HandbookGradientBackground() {
            HandbookTheme(androidTheme = true) {
                val sampleCategoriesList = listOf<Category>(
                    Category.create(
                        name = "Category Name",
                    ),
                    Category.create(
                        name = "Category Name",
                    ),
                    Category.create(
                        name = "Category Name",
                    ),
                    Category.create(
                        name = "Category Name",
                    ),
                )
                Column {
                    sampleCategoriesList.forEach { party ->
                        CategoryItem(party = party)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

