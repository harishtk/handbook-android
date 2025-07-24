package com.handbook.app.feature.home.presentation.party.addparty

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.Party
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
class AddPartyViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val uiState = viewModelState
        .map(ViewModelState::toAddPartyUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddPartyUiState()
        )
    val partyId = savedStateHandle.getStateFlow("partyId", 0L)

    private val _uiEvent = MutableSharedFlow<AddPartyUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddPartyUiAction) -> Unit

    private var addPartyJob: Job? = null
    private var deletePartyJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        if (partyId.value != 0L) {
            viewModelState.update { state -> state.copy(partyId = partyId.value.toLong()) }
            viewModelScope.launch {
                accountsRepository.getParty(partyId.value.toLong()).fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { party ->
                        viewModelState.update { state ->
                            state.copy(
                                partyId = party.id,
                                name = party.name,
                                contact = party.contactNumber ?: "",
                                description = party.description ?: "",
                                address = party.address ?: "",
                            )
                        }
                    }
                )
            }
        }
    }

    private fun onUiAction(action: AddPartyUiAction) {
        when (action) {
            AddPartyUiAction.ErrorShown -> {

            }

            AddPartyUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isAddPartySuccessful = false,
                        name = "",
                        contact = "",
                        description = "",
                        address = "",
                        errorMessage = null
                    )
                }
            }

            is AddPartyUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        name = action.name,
                        contact = action.contact,
                        description = action.description,
                        address = action.address
                    )
                }
                validate()
            }

            AddPartyUiAction.DeleteParty -> {
                deleteParty()
            }
        }
    }

    private fun validate() {
        // No validation required.

        val party = Party.create(
            id = viewModelState.value.partyId ?: 0,
            name = viewModelState.value.name,
            contactNumber = viewModelState.value.contact,
            description = viewModelState.value.description,
            address = viewModelState.value.address,
        )
        addParty(party)
    }

    private fun addParty(party: Party) {
        if (addPartyJob?.isActive == true) {
            val t = IllegalStateException("A request is already active.")
            ifDebug { Timber.w(t) }
            return
        }

        addPartyJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        addPartyJob = viewModelScope.launch {
            accountsRepository.addParty(party).fold(
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
                            isAddPartySuccessful = true
                        )
                    }
                },
            )
        }
    }

    private fun deleteParty() {
        val partyId = viewModelState.value.partyId ?: return
        deletePartyJob = viewModelScope.launch {
            accountsRepository.getParty(partyId).fold(
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
                    accountsRepository.deleteParty(partyId).fold(
                        onFailure = {},
                        onSuccess = {
                            sendEvent(AddPartyUiEvent.ShowToast(UiText.DynamicString("Party deleted")))
                            sendEvent(AddPartyUiEvent.OnNavUp)
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

    private fun sendEvent(event: AddPartyUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val partyId: Long? = null,

    val name: String = "",
    val contact: String = "",
    val description: String = "",
    val address: String = "",
    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the signup is successful
     */
    val isAddPartySuccessful: Boolean = false,
) {
    fun toAddPartyUiState(): AddPartyUiState {
        return if (isAddPartySuccessful) {
            AddPartyUiState.AddPartySuccess
        } else {
            AddPartyUiState.AddPartyForm(
                name = name,
                contact = contact,
                description = description,
                address = address,
                errorMessage = errorMessage,
            )
        }
    }
}

sealed interface AddPartyUiState {
    data class AddPartyForm(
        val name: String,
        val contact: String,
        val description: String,
        val address: String,
        val errorMessage: ErrorMessage? = null,
    ) : AddPartyUiState

    data object AddPartySuccess : AddPartyUiState
}

sealed interface AddPartyUiAction {
    data object ErrorShown : AddPartyUiAction
    data class Submit(
        val name: String,
        val contact: String,
        val description: String,
        val address: String,
    ) : AddPartyUiAction

    data object Reset : AddPartyUiAction
    data object DeleteParty : AddPartyUiAction
}

sealed interface AddPartyUiEvent {
    data class ShowToast(val message: UiText) : AddPartyUiEvent
    data object OnNavUp : AddPartyUiEvent
}