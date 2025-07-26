package com.handbook.app.feature.home.presentation.party

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.handbook.app.common.util.UiText
import com.handbook.app.core.di.HandbookDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.feature.home.domain.model.Party
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
class AllPartiesViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    @param:Dispatcher(HandbookDispatchers.Default)
    private val computationDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = QUERY, initialValue = "")
    val isInPickerMode = savedStateHandle.getStateFlow(key = "pickerMode", initialValue = false)

    private val _uiEvent = MutableSharedFlow<AllPartiesUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _actionStream = MutableSharedFlow<AllPartiesUiAction>()
    val accept: (AllPartiesUiAction) -> Unit

    val uiState: StateFlow<PartiesUiState> = searchQuery
        .flatMapLatest { q ->
            if (q.isNotBlank() && q.length < SEARCH_QUERY_MIN_LENGTH) {
                flowOf<PartiesUiState>(PartiesUiState.EmptyResult)
            } else {
                flowOf<PartiesUiState>(
                    PartiesUiState.Parties(
                        parties = getParties(q),
                        searchQuery = q
                    )
                )
                    .onStart { emit(PartiesUiState.Loading) }
                    .catch {
                        ifDebug { Timber.e(it) }
                        emit(parsePartiesError(it))
                    }
                    .flowOn(computationDispatcher)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PartiesUiState.Idle
        )

    init {
        _actionStream.filterIsInstance<AllPartiesUiAction.OnTypingQuery>()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { action ->
                savedStateHandle[QUERY] = action.query
            }
            .launchIn(viewModelScope)

        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: AllPartiesUiAction) {
        when (action) {
            is AllPartiesUiAction.OnItemClick -> {
                sendEvent(AllPartiesUiEvent.NavigateToEditParty(action.party.id))
            }
            is AllPartiesUiAction.OnTypingQuery -> {
                viewModelScope.launch { _actionStream.emit(action) }
            }
            AllPartiesUiAction.Reset -> {
                savedStateHandle[QUERY] = ""
            }
            AllPartiesUiAction.Retry -> {

            }
            is AllPartiesUiAction.Search -> {}
        }

    }

    private fun getParties(query: String): Flow<PagingData<Party>> {
        return accountsRepository.getPartiesPagingSource(query)
    }

    private fun parsePartiesError(t: Throwable): PartiesUiState.Error {
        return PartiesUiState.Error(t, UiText.somethingWentWrong)
    }


    private fun sendEvent(event: AllPartiesUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }
}

sealed interface AllPartiesUiAction {
    data class OnItemClick(val party: Party) : AllPartiesUiAction
    data class OnTypingQuery(val query: String) : AllPartiesUiAction
    data class Search(val query: String) : AllPartiesUiAction
    data object Retry : AllPartiesUiAction
    data object Reset : AllPartiesUiAction
}

sealed interface AllPartiesUiEvent {
    data class ShowToast(val message: UiText) : AllPartiesUiEvent
    data class ShowSnack(val message: UiText) : AllPartiesUiEvent
    data class NavigateToEditParty(val partyId: Long) : AllPartiesUiEvent
}

sealed interface PartiesUiState {
    data object Idle : PartiesUiState
    data object Loading : PartiesUiState
    data object EmptyResult : PartiesUiState
    data class Parties(
        val parties: Flow<PagingData<Party>>,
        val searchQuery: String,
    ) : PartiesUiState
    data class Error(
        val t: Throwable,
        val uiText: UiText,
    ) : PartiesUiState
}

const val QUERY = "query"
const val SEARCH_QUERY_MIN_LENGTH = 1

