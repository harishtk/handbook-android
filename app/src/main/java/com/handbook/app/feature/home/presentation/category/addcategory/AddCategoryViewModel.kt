package com.handbook.app.feature.home.presentation.category.addcategory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val uiState = viewModelState
        .map(ViewModelState::toAddCategoryUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddCategoryUiState()
        )
    val categoryId = savedStateHandle.getStateFlow<Long>("categoryId", 0L)
    val transactionType = savedStateHandle.getStateFlow("transactionType", TransactionType.EXPENSE)

    private val _uiEvent = MutableSharedFlow<AddCategoryUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddCategoryUiAction) -> Unit

    private var addCategoryJob: Job? = null
    private var deleteCategoryJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        if (categoryId.value != 0L) {
            viewModelState.update { state -> state.copy(categoryId = categoryId.value.toLong()) }
            viewModelScope.launch {
                accountsRepository.getCategory(categoryId.value.toLong()).fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { party ->
                        viewModelState.update { state ->
                            state.copy(
                                categoryId = party.id,
                                name = party.name,
                                description = party.description ?: "",
                            )
                        }
                    }
                )
            }
        }

        transactionType.onEach { type ->
            viewModelState.update { state ->
                state.copy(
                    transactionType = type
                )
            }
        }
            .launchIn(viewModelScope)
    }

    private fun onUiAction(action: AddCategoryUiAction) {
        when (action) {
            AddCategoryUiAction.ErrorShown -> {

            }

            AddCategoryUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isAddCategorySuccessful = false,
                        name = "",
                        description = "",
                        errorMessage = null
                    )
                }
            }

            is AddCategoryUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        name = action.name,
                        description = action.description,
                    )
                }
                validate()
            }

            AddCategoryUiAction.DeleteCategory -> {
                deleteCategory()
            }

            is AddCategoryUiAction.OnTransactionTypeChanged -> {
                savedStateHandle["transactionType"] = action.transactionType
            }
        }
    }

    private fun validate() {
        // No validation required.

        val party = Category.create(
            id = viewModelState.value.categoryId ?: 0,
            name = viewModelState.value.name,
            description = viewModelState.value.description,
            transactionType = viewModelState.value.transactionType,
        )
        addCategory(party)
    }

    private fun addCategory(party: Category) {
        if (addCategoryJob?.isActive == true) {
            val t = IllegalStateException("A request is already active.")
            ifDebug { Timber.w(t) }
            return
        }

        addCategoryJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        addCategoryJob = viewModelScope.launch {
            accountsRepository.addCategory(party).fold(
                onFailure = { exception ->
                    ifDebug { Timber.e(exception) }
                    val errorMessage = ErrorMessage(
                        id = 0,
                        exception = exception,
                        message = UiText.somethingWentWrong
                    )
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = errorMessage
                        )
                    }
                },
                onSuccess = {
                    setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)
                    viewModelState.update { state ->
                        state.copy(
                            isAddCategorySuccessful = true
                        )
                    }
                },
            )
        }
    }

    private fun deleteCategory() {
        val partyId = viewModelState.value.categoryId ?: return
        deleteCategoryJob = viewModelScope.launch {
            accountsRepository.getCategory(partyId).fold(
                onFailure = { exception ->
                    Timber.e(exception)
                    val errorMessage = ErrorMessage(
                        id = 0,
                        exception = exception,
                        message = UiText.somethingWentWrong
                    )
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = errorMessage
                        )
                    }
                },
                onSuccess = { party ->
                    accountsRepository.deleteCategory(partyId).fold(
                        onFailure = {},
                        onSuccess = {
                            sendEvent(AddCategoryUiEvent.ShowToast(UiText.DynamicString("Category deleted")))
                            sendEvent(AddCategoryUiEvent.OnNavUp)
                        }
                    )
                }
            )
        }
    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(event: AddCategoryUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val categoryId: Long? = null,

    val name: String = "",
    val description: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the signup is successful
     */
    val isAddCategorySuccessful: Boolean = false,
) {
    fun toAddCategoryUiState(): AddCategoryUiState {
        return if (isAddCategorySuccessful) {
            AddCategoryUiState.AddCategorySuccess
        } else {
            AddCategoryUiState.AddCategoryForm(
                name = name,
                description = description,
                transactionType = transactionType,
                errorMessage = errorMessage,
            )
        }
    }
}

sealed interface AddCategoryUiState {
    data class AddCategoryForm(
        val name: String,
        val description: String,
        val transactionType: TransactionType,
        val errorMessage: ErrorMessage? = null,
    ) : AddCategoryUiState

    data object AddCategorySuccess : AddCategoryUiState
}

sealed interface AddCategoryUiAction {
    data object ErrorShown : AddCategoryUiAction
    data class OnTransactionTypeChanged(val transactionType: TransactionType) : AddCategoryUiAction
    data class Submit(
        val name: String,
        val description: String,
        val transactionType: TransactionType,
    ) : AddCategoryUiAction

    data object Reset : AddCategoryUiAction
    data object DeleteCategory : AddCategoryUiAction
}

sealed interface AddCategoryUiEvent {
    data class ShowToast(val message: UiText) : AddCategoryUiEvent
    data object OnNavUp : AddCategoryUiEvent
}