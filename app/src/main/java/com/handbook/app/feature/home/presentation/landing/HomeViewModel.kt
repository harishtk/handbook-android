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
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.feature.home.domain.repository.PostRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
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
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userDataRepository: UserDataRepository,
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _filtersUiState = MutableStateFlow(AccountEntryFilters.None)
    val filtersUiState = _filtersUiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountEntryFilters.None
        )

    private val accountEntriesPagingSource = filtersUiState
        .flatMapLatest { filters ->
            accountsRepository.getAccountEntriesPagingSource(filters)
                .map { pagingData ->
                    pagingData.map { AccountEntryUiModel.Item(it) }
                        .insertSeparators { before, after ->
                            // Simplified: Only add separator if 'after' is an Item and it's the start of the list OR if 'before' is an Item.
                            // This is a placeholder and likely needs more sophisticated logic for actual date separation.
                            getSeparatorForItems(before, after)
                        }
                }
        }
        .cachedIn(viewModelScope)

    val accountEntriesUiState: StateFlow<AccountEntryUiState> = _filtersUiState
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountEntryUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (HomeUiAction) -> Unit

    private var feedFetchJob: Job? = null
    private var likeJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            HomeUiAction.Refresh -> {

            }

            HomeUiAction.LoadMore -> {
                // retryInternal(true)
            }
        }
    }

    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    /**
     * Returns true if the separator should be shown
     *
     * there is always a separator for today, and for other remaining days as past, if any
     */
    private fun getSeparatorForItems(before: AccountEntryUiModel?, after: AccountEntryUiModel?): AccountEntryUiModel.Separator? {
        if (after == null) {
            // we're at the end of the list
            return null
        }

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (before == null) {
            // we're at the beginning of the list
            // Always show "Today" separator at the beginning
            return AccountEntryUiModel.Separator("Today", today.date)
        }

        if (before is AccountEntryUiModel.Item && after is AccountEntryUiModel.Item) {
            val beforeDate = before.accountEntryWithDetails.entry.createdAt.toLocalDateTime().date
            val afterDate = after.accountEntryWithDetails.entry.createdAt.toLocalDateTime().date
            val todayDate = today.date

            return when {
                // If the item before is today and the item after is not today, show "Past"
                beforeDate.compareTo(todayDate) == 0 && afterDate.compareTo(todayDate) != 0 -> {
                    AccountEntryUiModel.Separator("Past", afterDate)
                }
                // If the item before is not today and the item after is not today, and their dates are different, show "Past"
                beforeDate.compareTo(todayDate) != 0 && afterDate.compareTo(todayDate) != 0 && beforeDate.compareTo(afterDate) != 0 -> {
                    AccountEntryUiModel.Separator("Past", afterDate)
                }
                beforeDate.compareTo(afterDate) != 0 -> AccountEntryUiModel.Separator(afterDate.toString(), afterDate) // Or format as needed
                else -> null
            }
        }

        return null
    }

    private fun parseAccountEntriesError(t: Throwable): AccountEntryUiState.Error {
        return AccountEntryUiState.Error(ErrorMessage.from(t))
    }

    private fun sendEvent(newEvent: HomeUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

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
    data class Separator(val text: String, val date: LocalDate) : AccountEntryUiModel
}

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
    data object LoadMore : HomeUiAction
}

sealed interface HomeUiEvent {
    data class ShowToast(val message: UiText) : HomeUiEvent
    data class ShowSnackbar(val message: UiText) : HomeUiEvent
}
