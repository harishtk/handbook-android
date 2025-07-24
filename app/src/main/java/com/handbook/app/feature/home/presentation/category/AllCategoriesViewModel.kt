package com.handbook.app.feature.home.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.handbook.app.common.util.UiText
import com.handbook.app.core.di.AiaDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.CategoryFilters
import com.handbook.app.feature.home.domain.model.TransactionType
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    @param:Dispatcher(AiaDispatchers.Default)
    private val computationDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(key = QUERY, initialValue = "")
    val isInPickerMode = savedStateHandle.getStateFlow(key = "pickerMode", initialValue = false)

    private val _uiEvent = MutableSharedFlow<AllCategoriesUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _actionStream = MutableSharedFlow<AllCategoriesUiAction>()
    val accept: (AllCategoriesUiAction) -> Unit

    val transactionType = savedStateHandle.getStateFlow<String?>("transactionType", null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        searchQuery,
        transactionType,
        ::Pair
    )
        .flatMapLatest { (q, transactionType) ->
            if (q.isNotBlank() && q.length < SEARCH_QUERY_MIN_LENGTH) {
                flowOf<CategoriesUiState>(CategoriesUiState.EmptyResult)
            } else {
                flowOf<CategoriesUiState>(
                    CategoriesUiState.Categories(
                        categories = getCategories(
                            CategoryFilters(
                                query = q,
                                transactionType = if (!(transactionType.isNullOrBlank())) TransactionType.fromString(transactionType) else null
                            )
                        ),
                        searchQuery = q
                    )
                )
                    .onStart { emit(CategoriesUiState.Loading) }
                    .catch {
                        ifDebug { Timber.e(it) }
                        emit(parseCategoriesError(it))
                    }
                    .flowOn(computationDispatcher)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoriesUiState.Idle
        )

    init {
        _actionStream.filterIsInstance<AllCategoriesUiAction.OnTypingQuery>()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { action ->
                savedStateHandle[QUERY] = action.query
            }
            .launchIn(viewModelScope)

        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: AllCategoriesUiAction) {
        when (action) {
            is AllCategoriesUiAction.OnItemClick -> {
                sendEvent(AllCategoriesUiEvent.NavigateToEditCategory(
                    action.category.id, action.category.transactionType))
            }
            is AllCategoriesUiAction.OnTypingQuery -> {
                viewModelScope.launch { _actionStream.emit(action) }
            }
            AllCategoriesUiAction.Reset -> {
                savedStateHandle[QUERY] = ""
            }
            AllCategoriesUiAction.Retry -> {

            }
            is AllCategoriesUiAction.Search -> {}
        }

    }

    private fun getCategories(filters: CategoryFilters): Flow<PagingData<Category>> {
        return accountsRepository.getCategoriesPagingSource(filters)
    }

    private fun parseCategoriesError(t: Throwable): CategoriesUiState.Error {
        return CategoriesUiState.Error(t, UiText.somethingWentWrong)
    }


    private fun sendEvent(event: AllCategoriesUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }
}

sealed interface AllCategoriesUiAction {
    data class OnItemClick(val category: Category) : AllCategoriesUiAction
    data class OnTypingQuery(val query: String) : AllCategoriesUiAction
    data class Search(val query: String) : AllCategoriesUiAction
    data object Retry : AllCategoriesUiAction
    data object Reset : AllCategoriesUiAction
}

sealed interface AllCategoriesUiEvent {
    data class ShowToast(val message: UiText) : AllCategoriesUiEvent
    data class ShowSnack(val message: UiText) : AllCategoriesUiEvent
    data class NavigateToEditCategory(val categoryId: Long, val transactionType: TransactionType) : AllCategoriesUiEvent
}

sealed interface CategoriesUiState {
    data object Idle : CategoriesUiState
    data object Loading : CategoriesUiState
    data object EmptyResult : CategoriesUiState
    data class Categories(
        val categories: Flow<PagingData<Category>>,
        val searchQuery: String,
    ) : CategoriesUiState
    data class Error(
        val t: Throwable,
        val uiText: UiText,
    ) : CategoriesUiState
}

const val QUERY = "query"
const val SEARCH_QUERY_MIN_LENGTH = 1

