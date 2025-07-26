@file:OptIn(ExperimentalMaterial3Api::class) // If you use experimental Material 3 APIs

package com.handbook.app.feature.home.presentation.summary

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.handbook.app.ObserverAsEvents // Assuming you have this for one-time events
import com.handbook.app.feature.summary.presentation.SummaryScreen
// import com.handbook.app.feature.summary.presentation.components.ExportSuccessDialog // Example dialog
// import com.handbook.app.feature.summary.presentation.components.ExportErrorDialog   // Example dialog
import kotlinx.coroutines.launch

// Potentially define a UiEvent sealed interface for one-time events from ViewModel to UI if needed
// sealed interface SummaryScreenUiEvent {
//     data class ShowSnackbar(val message: String) : SummaryScreenUiEvent
//     // data class OpenExportedFile(val uri: String) : SummaryScreenUiEvent
// }

@Composable
internal fun SummaryRoute(
    modifier: Modifier = Modifier,
    navController: NavController, // If needed for navigation from this screen
    viewModel: SummaryViewModel = hiltViewModel(),
    // onNavigateToDetails: (entryId: Long) -> Unit, // Example navigation callback
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect states from ViewModel
    val filterParams by viewModel.filterParams.collectAsStateWithLifecycle()
    val pagedAccountEntries = viewModel.pagedAccountEntries.collectAsLazyPagingItems()
    val summaryAggregation by viewModel.summaryAggregation.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val isLoadingTotals by viewModel.isLoadingTotals.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    // --- Handle One-Time Export Events (Example using LaunchedEffect) ---
    // This is one way to handle events like showing a toast/dialog after export.
    // Alternatively, your `exportState` itself can drive dialog visibility directly in `SummaryScreen`.
    LaunchedEffect(exportState) {
        when (val currentExportState = exportState) {
            is ExportUiState.Success -> {
                // Option 1: Show a Snackbar
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Export successful: ${currentExportState.fileUriOrPath}",
                        actionLabel = "Open" // You'd need to implement file opening
                    )
                }
                // Option 2: Show a Toast
                // Toast.makeText(context, "Export successful: ${currentExportState.fileUriOrPath}", Toast.LENGTH_LONG).show()
                // Option 3: ViewModel could expose a separate event for showing a dialog for more complex interactions
                viewModel.resetExportState() // Reset state after handling
            }
            is ExportUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Export failed: ${currentExportState.message}",
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }
                viewModel.resetExportState() // Reset state after handling
            }
            is ExportUiState.NoDataToExport -> {
                 scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "No data available to export for the selected filters.",
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }
                viewModel.resetExportState()
            }
            else -> { /* Idle or Loading, no immediate action needed here */ }
        }
    }


    SummaryScreen(
        modifier = modifier,
        filterParameters = filterParams,
        pagedAccountEntries = pagedAccountEntries,
        summaryAggregation = summaryAggregation,
        isLoadingTotals = isLoadingTotals,
        exportState = exportState,
        snackbarHostState = snackbarHostState,
        onPrimaryTabSelected = viewModel::selectPrimaryTab,
        onCustomDateRangeSelected = viewModel::setCustomDateRange,
        onCategoryFilterChanged = viewModel::updateCategoryFilters,
        onPartyFilterChanged = viewModel::updatePartyFilters,
        onBankFilterChanged = viewModel::updateBankFilters,
        onExportAction = viewModel::startExport,
        onRetryEntriesLoad = { pagedAccountEntries.retry() },
        onRefreshEntries = { pagedAccountEntries.refresh() },
        availableMonths = availableMonths,
        availableYears = availableYears,
        onNavigateBack = {},
        onMonthSelected = viewModel::selectMonth,
        onYearSelected = viewModel::selectYear
    )

    // Example for other UI events if you had a dedicated event flow from ViewModel
    // ObserverAsEvents(viewModel.uiEvent) { event ->
    //     when (event) {
    //         is SummaryScreenUiEvent.ShowSnackbar -> {
    //             scope.launch {
    //                 snackbarHostState.showSnackbar(event.message)
    //             }
    //         }
    //     }
    // }
}
