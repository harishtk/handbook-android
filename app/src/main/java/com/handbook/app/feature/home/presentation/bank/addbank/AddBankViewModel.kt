package com.handbook.app.feature.home.presentation.bank.addbank

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddBankViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val uiState = viewModelState
        .map(ViewModelState::toAddBankUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddBankUiState()
        )
    val bankId = savedStateHandle.getStateFlow<Long>("bankId", 0L)

    private val _uiEvent = MutableSharedFlow<AddBankUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddBankUiAction) -> Unit

    private var addBankJob: Job? = null
    private var deleteBankJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        if (bankId.value != 0L) {
            viewModelState.update { state -> state.copy(bankId = bankId.value.toLong()) }
            viewModelScope.launch {
                accountsRepository.getBank(bankId.value.toLong()).fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { party ->
                        viewModelState.update { state ->
                            state.copy(
                                bankId = party.id,
                                name = party.name,
                                description = party.description ?: "",
                            )
                        }
                    }
                )
            }
        }
    }

    private fun onUiAction(action: AddBankUiAction) {
        when (action) {
            AddBankUiAction.ErrorShown -> {

            }

            AddBankUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isAddBankSuccessful = false,
                        name = "",
                        description = "",
                        errorMessage = null
                    )
                }
            }

            is AddBankUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        name = action.name,
                        description = action.description,
                    )
                }
                validate()
            }

            AddBankUiAction.DeleteBank -> {
                deleteBank()
            }
        }
    }

    private fun validate() {
        // No validation required.

        val party = Bank.create(
            id = viewModelState.value.bankId ?: 0,
            name = viewModelState.value.name,
            description = viewModelState.value.description,
        )
        addBank(party)
    }

    private fun addBank(party: Bank) {
        if (addBankJob?.isActive == true) {
            val t = IllegalStateException("A request is already active.")
            ifDebug { Timber.w(t) }
            return
        }

        addBankJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        addBankJob = viewModelScope.launch {
            accountsRepository.addBank(party).fold(
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
                            isAddBankSuccessful = true
                        )
                    }
                },
            )
        }
    }

    private fun deleteBank() {
        val partyId = viewModelState.value.bankId ?: return
        deleteBankJob = viewModelScope.launch {
            accountsRepository.getBank(partyId).fold(
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
                    accountsRepository.deleteBank(partyId).fold(
                        onFailure = {},
                        onSuccess = {
                            sendEvent(AddBankUiEvent.ShowToast(UiText.DynamicString("Bank deleted")))
                            sendEvent(AddBankUiEvent.OnNavUp)
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

    private fun sendEvent(event: AddBankUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val bankId: Long? = null,

    val name: String = "",
    val description: String = "",
    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the signup is successful
     */
    val isAddBankSuccessful: Boolean = false,
) {
    fun toAddBankUiState(): AddBankUiState {
        return if (isAddBankSuccessful) {
            AddBankUiState.AddBankSuccess
        } else {
            AddBankUiState.AddBankForm(
                name = name,
                description = description,
                errorMessage = errorMessage,
            )
        }
    }
}

sealed interface AddBankUiState {
    data class AddBankForm(
        val name: String,
        val description: String,
        val errorMessage: ErrorMessage? = null,
    ) : AddBankUiState

    data object AddBankSuccess : AddBankUiState
}

sealed interface AddBankUiAction {
    data object ErrorShown : AddBankUiAction
    data class Submit(
        val name: String,
        val description: String,
    ) : AddBankUiAction

    data object Reset : AddBankUiAction
    data object DeleteBank : AddBankUiAction
}

sealed interface AddBankUiEvent {
    data class ShowToast(val message: UiText) : AddBankUiEvent
    data object OnNavUp : AddBankUiEvent
}