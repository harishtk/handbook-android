package com.handbook.app.feature.home.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.handbook.app.core.util.fold
import com.handbook.app.core.util.onFailure
import com.handbook.app.core.util.onSuccess
import com.handbook.app.feature.home.data.source.local.model.AccountSummaryAggregationPojo
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.repository.SummaryRepository
import com.handbook.app.feature.home.presentation.summary.util.DateRangeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// --- Enums and Data Classes ---

enum class DateTab {
    TODAY,
    THIS_WEEK,
    MONTH,
    YEAR,
    CUSTOM
}

data class SummaryFilterParameters(
    val selectedPrimaryTab: DateTab = DateTab.TODAY,
    val selectedMonth: YearMonth? = null,
    val selectedYear: Int? = null,
    val customStartDate: Long = DateRangeUtil.getTodayRange().first,
    val customEndDate: Long = DateRangeUtil.getTodayRange().second,
    val categoryIds: List<Long>? = null,
    val partyIds: List<Long>? = null,
    val bankIds: List<Long>? = null
) {
    val effectiveStartDate: Long
        get() = when (selectedPrimaryTab) {
            DateTab.TODAY -> DateRangeUtil.getTodayRange().first
            DateTab.THIS_WEEK -> DateRangeUtil.getThisWeekRange().first
            DateTab.MONTH -> selectedMonth?.let { DateRangeUtil.getMonthRange(it).first }
                ?: DateRangeUtil.getCurrentMonthRange().first // Fallback to current month if null
            DateTab.YEAR -> selectedYear?.let { DateRangeUtil.getYearRange(it).first }
                ?: DateRangeUtil.getCurrentYearRange().first // Fallback to current year if null
            DateTab.CUSTOM -> customStartDate
        }

    val effectiveEndDate: Long
        get() = when (selectedPrimaryTab) {
            DateTab.TODAY -> DateRangeUtil.getTodayRange().second
            DateTab.THIS_WEEK -> DateRangeUtil.getThisWeekRange().second
            DateTab.MONTH -> selectedMonth?.let { DateRangeUtil.getMonthRange(it).second }
                ?: DateRangeUtil.getCurrentMonthRange().second
            DateTab.YEAR -> selectedYear?.let { DateRangeUtil.getYearRange(it).second }
                ?: DateRangeUtil.getCurrentYearRange().second
            DateTab.CUSTOM -> customEndDate
        }
}

data class SelectablePeriod(
    val displayValue: String,
    val yearMonth: YearMonth? = null,
    val year: Int? = null
)

sealed interface ExportUiState {
    object Idle : ExportUiState
    object Loading : ExportUiState
    data class Success(val fileUriOrPath: String) : ExportUiState // Assuming URI for modern Android
    data class Error(val message: String) : ExportUiState
    object NoDataToExport : ExportUiState
}

enum class ExportType {
    PDF, EXCEL
}


@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository
    // TODO: Inject PDFExporter, ExcelExporter
) : ViewModel() {

    private val _filterParams = MutableStateFlow(
        SummaryFilterParameters(
            selectedMonth = YearMonth.now(), // Default to current month
            selectedYear = LocalDate.now().year // Default to current year
        )
    )
    val filterParams: StateFlow<SummaryFilterParameters> = _filterParams.asStateFlow()

    val pagedAccountEntries: Flow<PagingData<AccountEntryWithDetails>> = _filterParams
        .debounce(300) // Debounce to avoid rapid firing of queries
        .flatMapLatest { params ->
            Timber.d("Fetching entries for: StartDate=${LocalDate.ofEpochDay(params.effectiveStartDate / (1000*60*60*24))}, EndDate=${LocalDate.ofEpochDay(params.effectiveEndDate / (1000*60*60*24))}, Tab=${params.selectedPrimaryTab}, Month=${params.selectedMonth}, Year=${params.selectedYear}, Categories=${params.categoryIds}")
            summaryRepository.getFilteredSummaryPaginated(
                startDate = params.effectiveStartDate,
                endDate = params.effectiveEndDate,
                categoryIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.categoryIds else null,
                partyIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.partyIds else null,
                bankIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.bankIds else null
            )
        }
        .cachedIn(viewModelScope)

    private val _summaryAggregation = MutableStateFlow<AccountSummaryAggregationPojo?>(null)
    val summaryAggregation: StateFlow<AccountSummaryAggregationPojo?> = _summaryAggregation.asStateFlow()

    private val _isLoadingTotals = MutableStateFlow(false)
    val isLoadingTotals: StateFlow<Boolean> = _isLoadingTotals.asStateFlow()

    private val _availableMonths = MutableStateFlow<List<SelectablePeriod>>(emptyList())
    val availableMonths: StateFlow<List<SelectablePeriod>> = _availableMonths.asStateFlow()

    private val _availableYears = MutableStateFlow<List<SelectablePeriod>>(emptyList())
    val availableYears: StateFlow<List<SelectablePeriod>> = _availableYears.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    init {
        loadInitialFilterDependentData()

        viewModelScope.launch {
            _filterParams.collect { params ->
                loadSummaryTotals()
                // Auto-select first available month/year if tab changes and selection is null
                if (params.selectedPrimaryTab == DateTab.MONTH && params.selectedMonth == null && _availableMonths.value.isNotEmpty()) {
                    _availableMonths.value.firstOrNull()?.yearMonth?.let { selectMonth(it) }
                }
                if (params.selectedPrimaryTab == DateTab.YEAR && params.selectedYear == null && _availableYears.value.isNotEmpty()) {
                    _availableYears.value.firstOrNull()?.year?.let { selectYear(it) }
                }
            }
        }
    }

    private fun loadInitialFilterDependentData() {
        viewModelScope.launch {
            summaryRepository.getDistinctYearMonthsFromEntries().let { result ->
                result.fold(
                    onSuccess = { yearMonths ->
                        val selectableMonths = yearMonths
                            .sortedDescending()
                            .map { ym ->
                                SelectablePeriod(
                                    displayValue = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                    yearMonth = ym
                                )
                            }
                        _availableMonths.value = selectableMonths
                        // If current selectedMonth is not in the list (or is null), and tab is MONTH, select the latest one
                        val currentParams = _filterParams.value
                        if (currentParams.selectedPrimaryTab == DateTab.MONTH &&
                            (currentParams.selectedMonth == null || selectableMonths.none { it.yearMonth == currentParams.selectedMonth })) {
                            selectableMonths.firstOrNull()?.yearMonth?.let { defaultMonth ->
                                _filterParams.update { it.copy(selectedMonth = defaultMonth) }
                            }
                        }
                    },
                    onFailure = { Timber.e(it, "Failed to load distinct months") }
                )
            }

            summaryRepository.getDistinctYearsFromEntries().let { result ->
                result.fold(
                    onSuccess = { years ->
                        val selectableYears = years
                            .sortedDescending()
                            .map { y -> SelectablePeriod(displayValue = y.toString(), year = y) }
                        _availableYears.value = selectableYears

                        val currentParams = _filterParams.value
                        if (currentParams.selectedPrimaryTab == DateTab.YEAR &&
                            (currentParams.selectedYear == null || selectableYears.none { it.year == currentParams.selectedYear })) {
                            selectableYears.firstOrNull()?.year?.let { defaultYear ->
                                _filterParams.update { it.copy(selectedYear = defaultYear) }
                            }
                        }
                    },
                    onFailure = { Timber.e(it, "Failed to load distinct years") }
                )

            }
        }
    }

    fun selectPrimaryTab(tab: DateTab) {
        _filterParams.update { currentParams ->
            currentParams.copy(
                selectedPrimaryTab = tab,
                // Ensure a default month/year is selected if switching to those tabs and none is picked
                selectedMonth = if (tab == DateTab.MONTH) currentParams.selectedMonth ?: _availableMonths.value.firstOrNull()?.yearMonth ?: YearMonth.now() else currentParams.selectedMonth,
                selectedYear = if (tab == DateTab.YEAR) currentParams.selectedYear ?: _availableYears.value.firstOrNull()?.year ?: LocalDate.now().year else currentParams.selectedYear
            )
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _filterParams.update {
            it.copy(
                selectedPrimaryTab = DateTab.MONTH,
                selectedMonth = yearMonth
            )
        }
    }

    fun selectYear(year: Int) {
        _filterParams.update {
            it.copy(
                selectedPrimaryTab = DateTab.YEAR,
                selectedYear = year
            )
        }
    }

    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _filterParams.update {
            it.copy(
                selectedPrimaryTab = DateTab.CUSTOM,
                customStartDate = DateRangeUtil.toEpochMillisAtStartOfDay(startDate),
                customEndDate = DateRangeUtil.toEpochMillisAtEndOfDay(endDate)
            )
        }
    }

    fun updateCategoryFilters(ids: List<Long>?) {
        _filterParams.update { it.copy(categoryIds = ids?.ifEmpty { null }, selectedPrimaryTab = DateTab.CUSTOM) }
    }

    fun updatePartyFilters(ids: List<Long>?) {
        _filterParams.update { it.copy(partyIds = ids?.ifEmpty { null }, selectedPrimaryTab = DateTab.CUSTOM) }
    }

    fun updateBankFilters(ids: List<Long>?) {
        _filterParams.update { it.copy(bankIds = ids?.ifEmpty { null }, selectedPrimaryTab = DateTab.CUSTOM) }
    }

    private fun loadSummaryTotals() {
        viewModelScope.launch {
            _isLoadingTotals.value = true
            val params = _filterParams.value

            // Ensure valid period for Month/Year tabs before fetching
            if ((params.selectedPrimaryTab == DateTab.MONTH && params.selectedMonth == null) ||
                (params.selectedPrimaryTab == DateTab.YEAR && params.selectedYear == null)) {
                Timber.w("Skipping loadSummaryTotals: Month/Year tab selected but no specific period chosen.")
                _summaryAggregation.value = null // Explicitly clear if no valid period
                _isLoadingTotals.value = false
                return@launch
            }

            try {

                val result = summaryRepository.getSummaryAggregation(
                    startDate = params.effectiveStartDate,
                    endDate = params.effectiveEndDate,
                    categoryIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.categoryIds else null,
                    partyIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.partyIds else null,
                    bankIds = if (params.selectedPrimaryTab == DateTab.CUSTOM) params.bankIds else null
                )
                result.onSuccess { aggregation ->
                    _summaryAggregation.value = aggregation
                    Timber.d("Summary totals loaded: Income=${aggregation.totalIncome}, Expenses=${aggregation.totalExpenses}")
                }.onFailure { error ->
                    Timber.e(error, "Failed to load summary totals")
                    _summaryAggregation.value = null
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception loading summary totals")
                _summaryAggregation.value = null
            } finally {
                _isLoadingTotals.value = false
            }
        }
    }

    fun startExport(exportType: ExportType) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            val currentParams = _filterParams.value

            // Fetch all data for export (not paginated)
            // Note: This could be a large dataset. Consider streaming or background processing for real exports.
            val entriesToExportResult = summaryRepository.getFilteredAccountEntries(
                startDate = currentParams.effectiveStartDate,
                endDate = currentParams.effectiveEndDate,
                categoryIds = if (currentParams.selectedPrimaryTab == DateTab.CUSTOM) currentParams.categoryIds else null,
                partyIds = if (currentParams.selectedPrimaryTab == DateTab.CUSTOM) currentParams.partyIds else null,
                bankIds = if (currentParams.selectedPrimaryTab == DateTab.CUSTOM) currentParams.bankIds else null
            )

            entriesToExportResult.onSuccess { entries ->
                if (entries.entries.isEmpty()) {
                    _exportState.value = ExportUiState.NoDataToExport
                    return@onSuccess
                }
                // TODO: Implement actual export logic using injected exporters
                // For now, simulating success/failure
                when (exportType) {
                    ExportType.PDF -> {
                        // val pdfExporter = injectedPdfExporter
                        // val result = pdfExporter.export(entries, currentParams)
                        // _exportState.value = result (Success or Error)
                        _exportState.value = ExportUiState.Success("Simulated_Report.pdf") // Placeholder
                        Timber.i("PDF Export started for ${entries.entries.size} entries.")
                    }
                    ExportType.EXCEL -> {
                        // val excelExporter = injectedExcelExporter
                        // val result = excelExporter.export(entries, currentParams)
                        // _exportState.value = result
                        _exportState.value = ExportUiState.Success("Simulated_Report.xlsx") // Placeholder
                        Timber.i("Excel Export started for ${entries.entries.size} entries.")
                    }
                }
            }.onFailure { error ->
                Timber.e(error, "Failed to fetch entries for export")
                _exportState.value = ExportUiState.Error("Failed to retrieve data for export: ${error.localizedMessage}")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }
}
