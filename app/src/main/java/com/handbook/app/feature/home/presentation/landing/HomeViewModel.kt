@file:OptIn(ExperimentalTime::class)

package com.handbook.app.feature.home.presentation.landing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.handbook.app.common.util.UiText
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.SortOption
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.feature.home.presentation.accounts.TemporarySheetFilters
import com.handbook.app.filteredDelay
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _filterUiState: MutableStateFlow<FilterUiState> = MutableStateFlow(FilterUiState())
    val filterUiState = _filterUiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FilterUiState()
        )

    private val accountEntriesPagingSource = _filterUiState.map { it.activeFilters }
        .distinctUntilChanged()
        .flatMapLatest { filters ->
            accountsRepository.getAccountEntriesPagingSource(filters)
                .map { pagingData ->
                    pagingData.map { AccountEntryUiModel.Item(it) }
                        .insertSeparators { before, after ->
                            // Simplified: Only add separator if 'after' is an Item and it's the start of the list OR if 'before' is an Item.
                            // This is a placeholder and likely needs more sophisticated logic for actual date separation.
                            getSeparatorForItems3(before, after)
                        }
                }
        }
        .cachedIn(viewModelScope)

    val accountEntriesUiState: StateFlow<AccountEntryUiState> = _filterUiState.map { it.activeFilters }
        .distinctUntilChanged()
        .flatMapLatest { filters ->
            flowOf<AccountEntryUiState>(
                AccountEntryUiState.Success(
                    accountEntries = accountEntriesPagingSource,
                    filters = filters
                )
            )
                .onStart { emit(AccountEntryUiState.Loading) }
                .catch {
                    ifDebug { Timber.e(it) }
                    emit(parseAccountEntriesError(it))
                }
        }
        .filteredDelay(
            loadingItemPredicate = { it is AccountEntryUiState.Loading },
            minDelayFromLoadingItem = 700L,
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountEntryUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (HomeUiAction) -> Unit

    // Public acceptor for filter UI actions
    val acceptFilterAction: (FilterUiAction) -> Unit

    init {
        accept = { uiAction -> onUiAction(uiAction) }
        acceptFilterAction = { action ->
            handleFilterUiAction(action)
        }
    }

    private fun handleFilterUiAction(action: FilterUiAction) {
        when (action) {
            FilterUiAction.OpenFilterSheet -> onOpenFilterSheet()
            FilterUiAction.DismissFilterSheet -> onDismissFilterSheet()
            is FilterUiAction.UpdateTemporaryFilters -> onTemporaryFiltersChanged(action.newFilters)
            FilterUiAction.ApplyFilters -> onApplyFilters()
            FilterUiAction.ResetTemporaryFilters -> onResetAllTemporaryFilters()
            FilterUiAction.ApplyAndResetFilters -> onApplyResetFilters()
            is FilterUiAction.OnSelectedParty -> {
                viewModelScope.launch {
                    onPartySelect(action.partyId)
                }
            }
        }
    }

    private fun onUiAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> {

            }

            HomeUiAction.LoadMore -> {
                // retryInternal(true)
            }

            is HomeUiAction.OnEditEntry -> {
                sendEvent(HomeUiEvent.NavigateToEditEntry(action.entry))
            }

            is HomeUiAction.OnDeleteEntry -> {
                sendEvent(HomeUiEvent.NavigateToDeleteEntry(action.entry))
            }

            is HomeUiAction.DeleteEntry -> {
                handleDeleteEntry(action.entry)
            }

            is HomeUiAction.OnFilterChange -> {
                // _filterUiState.update { action.filters }
            }
        }
    }

    private fun handleDeleteEntry(entry: AccountEntry) {
        viewModelScope.launch {
            accountsRepository.deleteAccountEntry(entry.entryId)
                .fold(
                    onSuccess = {
                        sendEvent(HomeUiEvent.OnEntryDeleted(entry.copy(entryId = 0L)))
                    },
                    onFailure = { t ->
                        ifDebug { Timber.e(t) }
                        sendEvent(HomeUiEvent.ShowToast(
                            UiText.DynamicString("Failed to delete entry")
                        ))
                    }
                )
        }
    }

    private fun onOpenFilterSheet() {
        _filterUiState.update { currentState ->
            currentState.copy(
                showFilterSheet = true,
                temporaryFilters = TemporarySheetFilters.fromActive(
                    activeFiltersToActiveScreenFilters(currentState.activeFilters)
                )
            )
        }
    }

    private fun onDismissFilterSheet() {
        _filterUiState.update { it.copy(showFilterSheet = false) }
    }

    private fun onTemporaryFiltersChanged(newTemporaryFilters: TemporarySheetFilters) {
        _filterUiState.update {
            it.copy(temporaryFilters = newTemporaryFilters)
        }
    }

    private fun onApplyFilters() {
        _filterUiState.update { currentState ->
            val newActiveFilters = currentState.temporaryFilters.toAccountEntryFilters()
            currentState.copy(
                showFilterSheet = false,
                activeFilters = activeScreenFiltersToAccountEntryFilters(newActiveFilters)
            )
        }
    }

    private fun onResetAllTemporaryFilters() {
        _filterUiState.update { currentState ->
            val defaultSort = currentState.temporaryFilters.sortBy
            currentState.copy(
                temporaryFilters = TemporarySheetFilters(sortBy = defaultSort)
            )
        }
    }

    private fun onApplyResetFilters() {
        _filterUiState.update { currentState ->
            val defaultSort = currentState.temporaryFilters.sortBy
            currentState.copy(
                showFilterSheet = false,
                activeFilters = AccountEntryFilters.None.copy(
                    // sortBy = defaultSort // if your AccountEntryFilters supports this
                ),
                temporaryFilters = TemporarySheetFilters(sortBy = defaultSort)
            )
        }
    }

    private suspend fun onPartySelect(partyId: Long) {
        if (partyId == 0L) {
            onTemporaryFiltersChanged(
                _filterUiState.value.temporaryFilters.copy(
                    party = null
                )
            )
        } else {
            accountsRepository.getParty(partyId)
                .fold(
                    onFailure = { t ->
                        ifDebug { Timber.e(t) }
                        null
                    },
                    onSuccess = {
                        onTemporaryFiltersChanged(
                            _filterUiState.value.temporaryFilters.copy(
                                party = it
                            )
                        )
                    }
                )
        }
    }

    // --- Helper Conversion Functions (Keep these as they are) ---
    private fun activeFiltersToActiveScreenFilters(accountFilters: AccountEntryFilters): AccountEntryFilters {
        return AccountEntryFilters(
            startDate = accountFilters.startDate,
            endDate = accountFilters.endDate,
            entryType = accountFilters.entryType,
            transactionType = accountFilters.transactionType,
            party = accountFilters.party,
            sortBy = accountFilters.sortBy ?: SortOption.NEWEST_FIRST,
            isPinned = accountFilters.isPinned,
        )
    }

    private fun activeScreenFiltersToAccountEntryFilters(screenFilters: AccountEntryFilters): AccountEntryFilters {
        return AccountEntryFilters(
            startDate = screenFilters.startDate,
            endDate = screenFilters.endDate,
            entryType = screenFilters.entryType,
            transactionType = screenFilters.transactionType,
            party = screenFilters.party,
            sortBy = screenFilters.sortBy,
            isPinned = screenFilters.isPinned,
        )
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun getSeparatorForItems3(before: AccountEntryUiModel?, after: AccountEntryUiModel?): AccountEntryUiModel.Separator? {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        if (after == null) { // We're at the end of the list
            return null
        }

        // Only consider inserting a separator if 'after' is an actual data item
        val afterItem = after as? AccountEntryUiModel.Item ?: return null
        val afterDateTime = afterItem.accountEntryWithDetails.entry.createdAt.toLocalDateTime()
        val afterDate = afterDateTime.date

        if (before == null) { // We're at the beginning of the list
            return AccountEntryUiModel.Separator("Today", today)
        }

        // Only consider inserting a separator if 'before' was also an actual data item
        val beforeItem = before as? AccountEntryUiModel.Item ?: return null
        val beforeDate = beforeItem.accountEntryWithDetails.entry.createdAt.toLocalDateTime().date

        // If the date of 'after' is different from 'before', we need to insert a separator
        return if (beforeDate.compareTo(afterDate) != 0) {
            val format = LocalDate.Format { day(); char('-'); monthNumber(); char('-'); year() }
            // The date of the 'after' item is different from the 'before' item.
            // The text for this separator is based on the date of the 'after' item.
            AccountEntryUiModel.Separator(afterDate.format(format), afterDateTime)
        } else {
            // Dates are the same, no separator needed between these two items
            null
        }

    }

    private fun parseAccountEntriesError(t: Throwable): AccountEntryUiState.Error {
        return AccountEntryUiState.Error(ErrorMessage.from(t))
    }

    private fun sendEvent(newEvent: HomeUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

data class FilterUiState(
    val showFilterSheet: Boolean = false,
    val temporaryFilters: TemporarySheetFilters = TemporarySheetFilters(),
    val activeFilters: AccountEntryFilters = AccountEntryFilters.None,
)

sealed interface AccountEntryUiState {
    data object Loading : AccountEntryUiState
    data object Idle : AccountEntryUiState
    data class Error(val errorMessage: ErrorMessage) : AccountEntryUiState
    data class Success(
        val accountEntries: Flow<PagingData<AccountEntryUiModel>>,
        val filters: AccountEntryFilters,
    ) : AccountEntryUiState
}

sealed interface AccountEntryUiModel {
    data class Item(val accountEntryWithDetails: AccountEntryWithDetails) : AccountEntryUiModel
    data class Separator(val text: String, val date: LocalDateTime) : AccountEntryUiModel
    data class Footer(val uiText: UiText) : AccountEntryUiModel
}

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
    data object LoadMore : HomeUiAction
    data class OnFilterChange(val filters: AccountEntryFilters) : HomeUiAction
    data class OnEditEntry(val entry: AccountEntry) : HomeUiAction
    data class OnDeleteEntry(val entry: AccountEntry) : HomeUiAction
    data class DeleteEntry(val entry: AccountEntry) : HomeUiAction
}

sealed interface FilterUiAction {
    data object OpenFilterSheet : FilterUiAction
    data object DismissFilterSheet : FilterUiAction
    data class UpdateTemporaryFilters(val newFilters: TemporarySheetFilters) : FilterUiAction
    data object ApplyFilters : FilterUiAction
    data object ResetTemporaryFilters : FilterUiAction
    data object ApplyAndResetFilters : FilterUiAction // If "Reset All" also applies
    data class OnSelectedParty(val partyId: Long) : FilterUiAction
}

sealed interface HomeUiEvent {
    data class ShowToast(val message: UiText) : HomeUiEvent
    data class ShowSnackbar(val message: UiText) : HomeUiEvent
    data class NavigateToEditEntry(val entry: AccountEntry) : HomeUiEvent
    data class NavigateToDeleteEntry(val entry: AccountEntry) : HomeUiEvent
    data class OnEntryDeleted(val entry: AccountEntry) : HomeUiEvent
}
