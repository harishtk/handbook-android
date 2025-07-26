package com.handbook.app.feature.home.presentation.bank

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.handbook.app.common.util.UiText
import com.handbook.app.core.di.HandbookDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AllBanksViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    @param:Dispatcher(HandbookDispatchers.Default)
    private val computationDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = QUERY, initialValue = "")
    val isInPickerMode = savedStateHandle.getStateFlow(key = "pickerMode", initialValue = false)

    private val _uiEvent = MutableSharedFlow<AllBanksUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _actionStream = MutableSharedFlow<AllBanksUiAction>()
    val accept: (AllBanksUiAction) -> Unit

    val uiState: StateFlow<BanksUiState> = searchQuery
        .flatMapLatest { q ->
            if (q.isNotBlank() && q.length < SEARCH_QUERY_MIN_LENGTH) {
                flowOf<BanksUiState>(BanksUiState.EmptyResult)
            } else {
                flowOf<BanksUiState>(
                    BanksUiState.Banks(
                        banks = getBanks(q),
                        searchQuery = q
                    )
                )
                    .onStart { emit(BanksUiState.Loading) }
                    .catch {
                        ifDebug { Timber.e(it) }
                        emit(parseBanksError(it))
                    }
                    .flowOn(computationDispatcher)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BanksUiState.Idle
        )

    init {
        _actionStream.filterIsInstance<AllBanksUiAction.OnTypingQuery>()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { action ->
                savedStateHandle[QUERY] = action.query
            }
            .launchIn(viewModelScope)

        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: AllBanksUiAction) {
        when (action) {
            is AllBanksUiAction.OnItemClick -> {
                sendEvent(AllBanksUiEvent.NavigateToEditBank(action.bank.id))
            }
            is AllBanksUiAction.OnTypingQuery -> {
                viewModelScope.launch { _actionStream.emit(action) }
            }
            AllBanksUiAction.Reset -> {
                savedStateHandle[QUERY] = ""
            }
            AllBanksUiAction.Retry -> {

            }
            is AllBanksUiAction.Search -> {}
        }

    }

    private fun getBanks(query: String): Flow<PagingData<Bank>> {
        return accountsRepository.getBanksPagingSource(query)
    }

    private fun parseBanksError(t: Throwable): BanksUiState.Error {
        return BanksUiState.Error(t, UiText.somethingWentWrong)
    }


    private fun sendEvent(event: AllBanksUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }
}

sealed interface AllBanksUiAction {
    data class OnItemClick(val bank: Bank) : AllBanksUiAction
    data class OnTypingQuery(val query: String) : AllBanksUiAction
    data class Search(val query: String) : AllBanksUiAction
    data object Retry : AllBanksUiAction
    data object Reset : AllBanksUiAction
}

sealed interface AllBanksUiEvent {
    data class ShowToast(val message: UiText) : AllBanksUiEvent
    data class ShowSnack(val message: UiText) : AllBanksUiEvent
    data class NavigateToEditBank(val bankId: Long) : AllBanksUiEvent
}

sealed interface BanksUiState {
    data object Idle : BanksUiState
    data object Loading : BanksUiState
    data object EmptyResult : BanksUiState
    data class Banks(
        val banks: Flow<PagingData<Bank>>,
        val searchQuery: String,
    ) : BanksUiState
    data class Error(
        val t: Throwable,
        val uiText: UiText,
    ) : BanksUiState
}

const val QUERY = "query"
const val SEARCH_QUERY_MIN_LENGTH = 1

