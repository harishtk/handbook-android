@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalTime::class
)

package com.handbook.app.feature.summary.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.handbook.app.core.designsystem.component.HandbookGradientBackground
import com.handbook.app.feature.home.data.source.local.model.AccountSummaryAggregationPojo
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.presentation.summary.DateTab
import com.handbook.app.feature.home.presentation.summary.ExportType
import com.handbook.app.feature.home.presentation.summary.ExportUiState
import com.handbook.app.feature.home.presentation.summary.SelectablePeriod
import com.handbook.app.feature.home.presentation.summary.SummaryFilterParameters
import com.handbook.app.feature.home.presentation.summary.SummaryViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid

@Composable
internal fun SummaryRoute(
    modifier: Modifier = Modifier,
    // navController: NavController, // If needed for navigation
    viewModel: SummaryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit // Example for back navigation
    // onNavigateToDetails: (entryId: Long) -> Unit, // Example navigation callback
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filterParams by viewModel.filterParams.collectAsStateWithLifecycle()
    val pagedAccountEntries = viewModel.pagedAccountEntries.collectAsLazyPagingItems()
    val summaryAggregation by viewModel.summaryAggregation.collectAsStateWithLifecycle()
    val isLoadingTotals by viewModel.isLoadingTotals.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()

    LaunchedEffect(exportState) {
        when (val currentExportState = exportState) {
            is ExportUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Export successful: ${currentExportState.fileUriOrPath}",
                        actionLabel = "Open" // You'd need to implement file opening
                    )
                }
                viewModel.resetExportState()
            }
            is ExportUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Export failed: ${currentExportState.message}",
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetExportState()
            }
            is ExportUiState.NoDataToExport -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "No data available to export for the selected filters.",
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetExportState()
            }
            else -> { /* Idle or Loading */ }
        }
    }

    SummaryScreen(
        modifier = modifier,
        filterParameters = filterParams,
        pagedAccountEntries = pagedAccountEntries,
        summaryAggregation = summaryAggregation,
        isLoadingTotals = isLoadingTotals,
        availableMonths = availableMonths,
        availableYears = availableYears,
        exportState = exportState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onPrimaryTabSelected = viewModel::selectPrimaryTab,
        onMonthSelected = viewModel::selectMonth,
        onYearSelected = viewModel::selectYear,
        onCustomDateRangeSelected = viewModel::setCustomDateRange,
        onCategoryFilterChanged = viewModel::updateCategoryFilters,
        onPartyFilterChanged = viewModel::updatePartyFilters,
        onBankFilterChanged = viewModel::updateBankFilters,
        onExportAction = viewModel::startExport,
        onRetryEntriesLoad = { pagedAccountEntries.retry() },
        onRefreshEntries = { pagedAccountEntries.refresh() }
        // onEntryClicked = { entryId -> onNavigateToDetails(entryId) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SummaryScreen(
    modifier: Modifier = Modifier,
    filterParameters: SummaryFilterParameters,
    pagedAccountEntries: LazyPagingItems<AccountEntryWithDetails>,
    summaryAggregation: AccountSummaryAggregationPojo?,
    isLoadingTotals: Boolean,
    availableMonths: List<SelectablePeriod>,
    availableYears: List<SelectablePeriod>,
    exportState: ExportUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onPrimaryTabSelected: (DateTab) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onYearSelected: (Int) -> Unit,
    onCustomDateRangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    onCategoryFilterChanged: (List<Long>?) -> Unit,
    onPartyFilterChanged: (List<Long>?) -> Unit,
    onBankFilterChanged: (List<Long>?) -> Unit,
    onExportAction: (ExportType) -> Unit,
    onRetryEntriesLoad: () -> Unit,
    onRefreshEntries: () -> Unit,
    // onEntryClicked: (entryId: Long) -> Unit,
) {
    var showCustomFilterSheet by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (filterParameters.selectedPrimaryTab == DateTab.CUSTOM) {
                        IconButton(onClick = { showCustomFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export to PDF") },
                                onClick = {
                                    onExportAction(ExportType.PDF)
                                    showExportMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export to Excel") },
                                onClick = {
                                    onExportAction(ExportType.EXCEL)
                                    showExportMenu = false
                                }
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
        ) {
            PrimaryDateTabBar(
                selectedPrimaryTab = filterParameters.selectedPrimaryTab,
                onPrimaryTabSelected = onPrimaryTabSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp) // Only bottom padding here
            )

            val showTotalsAndEntries = when (filterParameters.selectedPrimaryTab) {
                DateTab.TODAY, DateTab.THIS_WEEK, DateTab.CUSTOM -> true
                DateTab.MONTH -> filterParameters.selectedMonth != null
                DateTab.YEAR -> filterParameters.selectedYear != null
            }

            when (filterParameters.selectedPrimaryTab) {
                DateTab.MONTH -> {
                    PeriodSelector(
                        label = "Select Month:",
                        items = availableMonths,
                        selectedItemDisplay = filterParameters.selectedMonth?.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                            ?: "Select a Month",
                        onItemSelected = { selectedPeriod ->
                            selectedPeriod.yearMonth?.let { onMonthSelected(it) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                DateTab.YEAR -> {
                    PeriodSelector(
                        label = "Select Year:",
                        items = availableYears,
                        selectedItemDisplay = filterParameters.selectedYear?.toString() ?: "Select a Year",
                        onItemSelected = { selectedPeriod ->
                            selectedPeriod.year?.let { onYearSelected(it) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                else -> { /* No specific selector for Today, Week, Custom here */ }
            }


            if (showTotalsAndEntries) {
                SummaryTotalsCard(
                    summary = summaryAggregation,
                    isLoading = isLoadingTotals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                PagedEntriesList(
                    pagedAccountEntries = pagedAccountEntries,
                    currentTab = filterParameters.selectedPrimaryTab,
                    onRetryEntriesLoad = onRetryEntriesLoad,
                    onRefreshEntries = onRefreshEntries,
                    modifier = Modifier.weight(1f) // Ensures list takes remaining space
                )
            } else {
                // Show message if Month/Year is selected but no specific period chosen yet
                val message = if (filterParameters.selectedPrimaryTab == DateTab.MONTH) {
                    "Please select a month to view entries."
                } else if (filterParameters.selectedPrimaryTab == DateTab.YEAR) {
                    "Please select a year to view entries."
                } else {
                    "Loading data..." // Should be covered by PagedEntriesList states generally
                }
                CenteredMessage(
                    text = message,
                    modifier = Modifier.weight(1f) // Takes remaining space
                )
            }
        }

        if (showCustomFilterSheet && filterParameters.selectedPrimaryTab == DateTab.CUSTOM) {
            CustomFiltersBottomSheet(
                currentFilters = filterParameters,
                onDismiss = { showCustomFilterSheet = false },
                onCustomDateRangeSelected = onCustomDateRangeSelected,
                onCategoryFilterChanged = onCategoryFilterChanged,
                onPartyFilterChanged = onPartyFilterChanged,
                onBankFilterChanged = onBankFilterChanged
            )
        }

        if (exportState is ExportUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Exporting data...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun PrimaryDateTabBar(
    selectedPrimaryTab: DateTab,
    onPrimaryTabSelected: (DateTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        // Add a surface for elevation or distinct background
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Example different bg
        // elevation = 2.dp // Optional elevation
    ) {
        TabRow(
            selectedTabIndex = DateTab.values().indexOf(selectedPrimaryTab),
            containerColor = Color.Transparent, // TabRow itself transparent, Surface provides bg
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[DateTab.values().indexOf(selectedPrimaryTab)]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            DateTab.values().forEach { tab ->
                Tab(
                    selected = tab == selectedPrimaryTab,
                    onClick = { onPrimaryTabSelected(tab) },
                    text = { Text(tab.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun PagedEntriesList(
    pagedAccountEntries: LazyPagingItems<AccountEntryWithDetails>,
    currentTab: DateTab,
    onRetryEntriesLoad: () -> Unit,
    onRefreshEntries: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loadState = pagedAccountEntries.loadState
    val itemCount = pagedAccountEntries.itemCount

    val itemSnapshot = pagedAccountEntries.itemSnapshotList

    LazyColumn(
        modifier = modifier.fillMaxSize(), // Ensure it can fill available space
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (val refreshState = loadState.refresh) {
            is LoadState.Loading -> {
                item { FullScreenLoadingIndicator(modifier = Modifier.fillParentMaxSize()) }
            }

            is LoadState.Error -> {
                item {
                    FullScreenErrorState(
                        errorMessage = refreshState.error.localizedMessage
                            ?: "An error occurred",
                        onRetry = onRetryEntriesLoad,
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            }

            is LoadState.NotLoading -> {
                if (itemCount == 0) {
                    item {
                        EmptyStateView(
                            message = "No transactions found for the selected filters.",
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                }
            }
        }

        items(
            count = itemSnapshot.size,
            key = { index ->
                itemSnapshot[index]?.let {
                    "${currentTab.name}_${it.entry.entryId}"
                } ?: "placeholder_${currentTab.name}_$index"
            },
            contentType = { index -> "AccountEntry" }
        ) { index ->
            val entry = itemSnapshot[index]
            if (entry != null) {
                AccountEntryRow(
                    entry = entry,
                    onClick = { /* onEntryClicked(entry.entry.entryId) */ }
                )
            } else {
                // Placeholder if needed, though typically not if placeholders are false
            }
        }

        when (val appendState = loadState.append) {
            is LoadState.Loading -> {
                item { AppendLoadingIndicator() }
            }

            is LoadState.Error -> {
                item {
                    AppendErrorState(
                        errorMessage = appendState.error.localizedMessage
                            ?: "Failed to load more",
                        onRetry = onRetryEntriesLoad
                    )
                }
            }

            is LoadState.NotLoading -> {
                if (appendState.endOfPaginationReached && itemCount > 0) {
                    item { EndOfListIndicator() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    label: String,
    items: List<SelectablePeriod>,
    selectedItemDisplay: String,
    onItemSelected: (SelectablePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedItemDisplay,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth() // Make dropdown match width
            ) {
                if (items.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No periods available", style = MaterialTheme.typography.bodyMedium) },
                        onClick = {},
                        enabled = false
                    )
                } else {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.displayValue, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTotalsCard(summary: AccountSummaryAggregationPojo?, isLoading: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading && summary == null) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp), contentAlignment = Alignment.Center) { // Fixed height for loading state
                    CircularProgressIndicator()
                }
            } else if (summary != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Income:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "₹${"%,.2f".format(summary.totalIncome)}", // Example currency format
                        color = Color(0xFF2E7D32), // Greenish
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Expenses:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "₹${"%,.2f".format(summary.totalExpenses)}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Balance:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    val balance = summary.totalIncome - summary.totalExpenses
                    Text(
                        text = "₹${"%,.2f".format(balance)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (balance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            } else { // Not loading, but summary is null (e.g., error or no data for period)
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp), contentAlignment = Alignment.Center) {
                    Text("No summary data available.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


@Composable
fun AccountEntryRow(entry: AccountEntryWithDetails, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for category icon if you have one
            // Icon(painterResource(id = entry.category.iconResId), contentDescription = null, modifier = Modifier.size(40.dp))
            // Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.entry.title ?: entry.category.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    entry.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.party?.let {
                    Text(
                        "Party: ${it.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${if (entry.entry.transactionType == TransactionType.INCOME) "+" else "-"}₹${"%,.2f".format(entry.entry.amount)}",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                color = if (entry.entry.transactionType == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
fun CustomFiltersBottomSheet(
    currentFilters: SummaryFilterParameters,
    onDismiss: () -> Unit,
    onCustomDateRangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    onCategoryFilterChanged: (List<Long>?) -> Unit,
    onPartyFilterChanged: (List<Long>?) -> Unit,
    onBankFilterChanged: (List<Long>?) -> Unit
) {
    // Remember the local state for date pickers if you want to show current selection
    // before "applying" or having them directly call the ViewModel methods.
    // For simplicity, this example assumes direct calls or a simple structure.

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding() // For gesture navigation
                .verticalScroll(rememberScrollState()) // If content might overflow
        ) {
            Text(
                "Custom Filters",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // TODO: Implement actual controls here. These are placeholders:

            // 1. Custom Date Range Picker
            // This is a complex component. You might use two buttons opening DatePickerDialogs.
            Text("Date Range (TODO: Implement Date Picker)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* TODO: Show Start Date Picker -> onCustomDateRangeSelected */ }, modifier = Modifier.weight(1f)) {
                    Text(currentFilters.customStartDate.let {
                        LocalDate.ofEpochDay(it / (1000*60*60*24)).format(DateTimeFormatter.ISO_LOCAL_DATE) // Simple format, adapt
                    })
                }
                Button(onClick = { /* TODO: Show End Date Picker -> onCustomDateRangeSelected */ }, modifier = Modifier.weight(1f)) {
                    Text(currentFilters.customEndDate.let {
                        LocalDate.ofEpochDay(it / (1000*60*60*24)).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // 2. Category Multi-Selector
            Text("Categories (TODO: Implement Selector)", style = MaterialTheme.typography.titleMedium)
            // Example: (currentFilters.categoryIds ?: emptyList()).toString() - just to show the data
            Text("Selected: ${(currentFilters.categoryIds ?: emptyList()).joinToString()}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { /* TODO: Show Category Selector -> onCategoryFilterChanged */ }) { Text("Select Categories") }
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Party Multi-Selector
            Text("Parties (TODO: Implement Selector)", style = MaterialTheme.typography.titleMedium)
            Text("Selected: ${(currentFilters.partyIds ?: emptyList()).joinToString()}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { /* TODO: Show Party Selector -> onPartyFilterChanged */ }) { Text("Select Parties") }
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Bank Multi-Selector (if applicable)
            Text("Banks (TODO: Implement Selector)", style = MaterialTheme.typography.titleMedium)
            Text("Selected: ${(currentFilters.bankIds ?: emptyList()).joinToString()}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { /* TODO: Show Bank Selector -> onBankFilterChanged */ }) { Text("Select Banks") }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss, // Filters are applied live by calling ViewModel methods from selectors
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
            Spacer(modifier = Modifier.height(8.dp)) // Padding at the bottom
        }
    }
}


// --- Loading and Error State Composables (Simplified, ensure they take modifier) ---
@Composable
fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullScreenErrorState(errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyStateView(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AppendLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
fun AppendErrorState(errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 4.dp)) { Text("Retry") }
    }
}

@Composable
fun EndOfListIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        Text("You've reached the end!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


// --- Preview Setup ---

// Mock data for preview
object MockData {
    val sampleCategory = Category.create(
        id = 1L,
        name = "Groceries",
        transactionType = TransactionType.EXPENSE,
    )
    val sampleEntry1 = AccountEntryWithDetails(
        entry = AccountEntry.create(
            entryId = 1L,
            title = "Supermarket run",
            amount = 55.75,
            transactionType = TransactionType.EXPENSE,
            categoryId = 1L,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            transactionDate = Clock.System.now().toEpochMilliseconds(),
            entryType = EntryType.CASH
        ),
        category = sampleCategory,
        party = null,
        bank = null,
        attachments = emptyList()
    )
    val sampleEntry2 = AccountEntryWithDetails(
        entry = AccountEntry.create(
            entryId = 2L,
            title = "Salary",
            amount = 2500.0,
            transactionType = TransactionType.INCOME,
            categoryId = 1L,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            transactionDate = Clock.System.now().toEpochMilliseconds(),
            entryType = EntryType.CASH,
        ),
        category = sampleCategory,
        party = null,
        bank = null,
        attachments = emptyList()
    )

    val pagedAccountEntriesFlow = flowOf(PagingData.from(listOf(sampleEntry1, sampleEntry2)))
    val emptyPagedAccountEntriesFlow = flowOf(PagingData.empty<AccountEntryWithDetails>())

    val summaryAggregationData = AccountSummaryAggregationPojo(
        totalIncome = 2500.0,
        totalExpenses = 55.75
    )

    val availableMonthsData = listOf(
        SelectablePeriod("December 2023", yearMonth = YearMonth.of(2023, 12)),
        SelectablePeriod("November 2023", yearMonth = YearMonth.of(2023, 11)),
        SelectablePeriod("October 2023", yearMonth = YearMonth.of(2023, 10)),
    )
    val availableYearsData = listOf(
        SelectablePeriod("2024", year = 2024),
        SelectablePeriod("2023", year = 2023),
    )
}

// PreviewParameterProvider for different states (Optional, but good for showcasing variations)
class SummaryScreenStateProvider : PreviewParameterProvider<SummaryScreenPreviewState> {
    override val values = sequenceOf(
        SummaryScreenPreviewState(
            description = "Default Loaded State",
            filterParameters = SummaryFilterParameters(selectedPrimaryTab = DateTab.MONTH),
            pagedAccountEntriesData = listOf(MockData.sampleEntry1, MockData.sampleEntry2),
            summaryAggregation = MockData.summaryAggregationData,
            isLoadingTotals = false,
            exportState = ExportUiState.Idle
        ),
        SummaryScreenPreviewState(
            description = "Loading Totals",
            filterParameters = SummaryFilterParameters(selectedPrimaryTab = DateTab.TODAY),
            pagedAccountEntriesData = emptyList(),
            summaryAggregation = null,
            isLoadingTotals = true,
            exportState = ExportUiState.Idle
        ),
        SummaryScreenPreviewState(
            description = "Exporting State",
            filterParameters = SummaryFilterParameters(selectedPrimaryTab = DateTab.THIS_WEEK),
            pagedAccountEntriesData = listOf(MockData.sampleEntry1),
            summaryAggregation = AccountSummaryAggregationPojo(100.0, 50.0),
            isLoadingTotals = false,
            exportState = ExportUiState.Loading
        ),
        SummaryScreenPreviewState(
            description = "Empty State",
            filterParameters = SummaryFilterParameters(selectedPrimaryTab = DateTab.CUSTOM),
            pagedAccountEntriesData = emptyList(),
            summaryAggregation = AccountSummaryAggregationPojo(0.0,0.0),
            isLoadingTotals = false,
            exportState = ExportUiState.Idle,
            overrideLoadState = LoadState.NotLoading(endOfPaginationReached = true) // To show empty
        )
        // Add more states: Paging loading, Paging error, etc.
    )
}

data class SummaryScreenPreviewState(
    val description: String, // For naming the @Preview
    val filterParameters: SummaryFilterParameters,
    val pagedAccountEntriesData: List<AccountEntryWithDetails>, // Data for the current "page"
    val summaryAggregation: AccountSummaryAggregationPojo?,
    val isLoadingTotals: Boolean,
    val exportState: ExportUiState,
    val overrideLoadState: LoadState? = null // To simulate specific paging states
)

@Preview(name = "Summary Screen - Today View", showBackground = true, widthDp = 380, heightDp = 700)
@Composable
fun SummaryScreenPreview_Today() {
    MaterialTheme {
        SummaryScreen(
            filterParameters = SummaryFilterParameters(selectedPrimaryTab = DateTab.TODAY),
            pagedAccountEntries = MockData.pagedAccountEntriesFlow.collectAsLazyPagingItems(),
            summaryAggregation = MockData.summaryAggregationData,
            isLoadingTotals = false,
            availableMonths = MockData.availableMonthsData,
            availableYears = MockData.availableYearsData,
            exportState = ExportUiState.Idle,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onPrimaryTabSelected = {},
            onMonthSelected = {},
            onYearSelected = {},
            onCustomDateRangeSelected = { _, _ -> },
            onCategoryFilterChanged = {},
            onPartyFilterChanged = {},
            onBankFilterChanged = {},
            onExportAction = {},
            onRetryEntriesLoad = {},
            onRefreshEntries = {}
        )
    }
}

@Preview(name = "Summary Screen Default", showBackground = true)
@Composable
fun SummaryScreenPreviewDefault() {
    val defaultState = SummaryScreenStateProvider().values.first()
    MaterialTheme { // Ensure a MaterialTheme is applied for previews
        SummaryScreenWithMockData(state = defaultState)
    }
}

@Preview(name = "Summary Screen - Loading Totals", showBackground = true)
@Composable
fun SummaryScreenPreviewLoadingTotals() {
    val loadingState = SummaryScreenStateProvider().values.elementAt(1)
    MaterialTheme {
        SummaryScreenWithMockData(state = loadingState)
    }
}

@Preview(name = "Summary Screen - Exporting", showBackground = true)
@Composable
fun SummaryScreenPreviewExporting() {
    val exportingState = SummaryScreenStateProvider().values.elementAt(2)
    MaterialTheme {
        SummaryScreenWithMockData(state = exportingState)
    }
}

@Preview(name = "Summary Screen - Empty", showBackground = true)
@Composable
fun SummaryScreenPreviewEmpty() {
    val emptyState = SummaryScreenStateProvider().values.elementAt(3)
    MaterialTheme {
        SummaryScreenWithMockData(state = emptyState)
    }
}

// A helper composable to setup SummaryScreen with mock PagingData
@Composable
fun SummaryScreenWithMockData(
    @PreviewParameter(SummaryScreenStateProvider::class) state: SummaryScreenPreviewState
) {
    // Mock PagingData by creating a flow that emits a single PagingData instance
    val mockPagingDataFlow = flowOf(PagingData.from(state.pagedAccountEntriesData))
    val mockLazyPagingItems = mockPagingDataFlow.collectAsLazyPagingItems()

    // If a specific load state is requested for the preview (e.g., to show empty/error)
    // This is a simplified way to influence what the LazyColumn sees.
    // In a real scenario with complex PagingData mocking, this would be more involved.
    // For this preview, we mostly rely on itemCount to show empty.
    // If state.overrideLoadState is not null, you might need a custom LazyPagingItems wrapper
    // or adjust how FullScreenErrorState/EmptyStateView are shown based on itemCount + this state.

    SummaryScreen(
        filterParameters = state.filterParameters,
        pagedAccountEntries = mockLazyPagingItems,
        summaryAggregation = state.summaryAggregation,
        isLoadingTotals = state.isLoadingTotals,
        exportState = state.exportState,
        snackbarHostState = remember { SnackbarHostState() }, // Fine for preview
        onPrimaryTabSelected = {},
        onCustomDateRangeSelected = { _, _ -> },
        onCategoryFilterChanged = {},
        onPartyFilterChanged = {},
        onBankFilterChanged = {},
        onExportAction = {},
        onRetryEntriesLoad = {},
        onRefreshEntries = {},
        onNavigateBack = {},
        onYearSelected = {},
        onMonthSelected = {},
        availableMonths = MockData.availableMonthsData,
        availableYears = MockData.availableYearsData
    )
}
