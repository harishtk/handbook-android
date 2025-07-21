package com.handbook.app.feature.home.presentation.accounts.addaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@ExperimentalCoroutinesApi
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountsRepository: AccountsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val uiState = viewModelState
        .map(ViewModelState::toAddAccountUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value.toAddAccountUiState()
        )
    val accountEntryId = savedStateHandle.getStateFlow<Long>("accountEntryId", 0L)
    val transactionType = savedStateHandle.getStateFlow("transactionType", "")
    val categoryId = savedStateHandle.getStateFlow<Long>("categoryId", 0L)
    val partyId = savedStateHandle.getStateFlow<Long>("partyId", 0L)

    private val _uiEvent = MutableSharedFlow<AddAccountUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddAccountUiAction) -> Unit

    private var addAccountEntryJob: Job? = null
    private var deleteAccountEntryJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        if (accountEntryId.value != 0L) {
            Timber.d("Parsing: accountEntryId=$accountEntryId.value")
            viewModelState.update { state -> state.copy(accountEntryId = accountEntryId.value.toLong()) }
            viewModelScope.launch {
                accountsRepository.getAccountEntry(accountEntryId.value.toLong()).fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { entryWithDetails ->
                        val entry = entryWithDetails.entry
                        viewModelState.update { state ->
                            state.copy(
                                accountEntryId = entry.entryId,
                                title = entry.title,
                                description = entry.description ?: "",
                                amount = entry.amount.toString(),
                                entryType = entry.entryType,
                                transactionType = entry.transactionType,
                                transactionDate = entry.transactionDate,
                                partyId = entry.partyId,
                                categoryId = entry.categoryId
                            )
                        }
                        savedStateHandle["categoryId"] = entry.categoryId
                        savedStateHandle["partyId"] = entry.partyId
                    }
                )
            }
        } else {
            Timber.d("Parsing: No accountEntryId")
        }

        transactionType.onEach {
            viewModelState.update { state ->
                state.copy(
                    transactionType = TransactionType.fromString(it)
                )
            }
        }
            .launchIn(viewModelScope)

        categoryId.mapLatest { cid ->
            if (cid != 0L) {
                accountsRepository.getCategory(cid)
            } else {
                null
            }
        }
            .onEach { result ->
                result?.fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { category ->
                        Timber.d("Selected: category=$category")
                        viewModelState.update { state ->
                            state.copy(
                                categoryId = category.id,
                                category = category
                            )
                        }
                    }
                )
            }
            .launchIn(viewModelScope)

        partyId.mapLatest { pid ->
            if (pid != 0L) {
                accountsRepository.getParty(pid)
            } else {
                null
            }
        }.onEach { result ->
            if (result != null) {
                result?.fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { party ->
                        Timber.d("Selected: party=$party")
                        viewModelState.update { state ->
                            state.copy(
                                partyId = party.id,
                                party = party
                            )
                        }
                    }
                )
            } else {
                viewModelState.update { state ->
                    state.copy(
                        partyId = null,
                        party = null
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun onUiAction(action: AddAccountUiAction) {
        when (action) {
            AddAccountUiAction.ErrorShown -> {

            }

            AddAccountUiAction.Reset -> {
                viewModelState.update { state ->
                    state.copy(
                        loadState = LoadStates.IDLE,
                        isAddAccountSuccessful = false,
                        title = "",
                        description = "",
                        errorMessage = null
                    )
                }
            }

            is AddAccountUiAction.OnEntryTypeToggle -> {
                viewModelState.update { state ->
                    state.copy(
                        entryType = action.entryType
                    )
                }
            }

            is AddAccountUiAction.OnTransactionTypeToggle -> {
                savedStateHandle["transactionType"] = action.transactionType.name
            }

            is AddAccountUiAction.OnDatePicked -> {
                viewModelState.update { state ->
                    state.copy(
                        transactionDate = action.date
                    )
                }
            }

            is AddAccountUiAction.OnTypingTitle -> {
                viewModelState.update { state ->
                    state.copy(
                        title = action.title
                    )
                }
            }

            is AddAccountUiAction.OnTypingAmount -> {
                viewModelState.update { state ->
                    state.copy(
                        amount = action.amount
                    )
                }
            }

            is AddAccountUiAction.Submit -> {
                viewModelState.update { state ->
                    state.copy(
                        title = action.name,
                        description = action.description,
                    )
                }
                validate()
            }

            AddAccountUiAction.DeleteAccountEntry -> {
                deleteAccountEntry()
            }

            is AddAccountUiAction.OnCategoryToggle -> {
                savedStateHandle["categoryId"] = action.categoryId
            }

            is AddAccountUiAction.OnCategorySelectRequest -> {
                sendEvent(AddAccountUiEvent.NavigateToCategorySelection(action.categoryId))
            }

            is AddAccountUiAction.OnPartyToggle -> {
                savedStateHandle["partyId"] = action.partyId
            }

            is AddAccountUiAction.OnPartySelectRequest -> {
                sendEvent(AddAccountUiEvent.NavigateToPartySelection(action.partyId ?: 0L))
            }
        }
    }

    private fun validate() {
        // No validation required.
        val category = viewModelState.value.category
        if (category == null) {
            val errorMessage = ErrorMessage(
                id = 0,
                exception = null,
                message = UiText.DynamicString("Select a category")
            )
            viewModelState.update { state ->
                state.copy(
                    errorMessage = errorMessage
                )
            }
            return
        }

        val entry = AccountEntry.create(
            id = viewModelState.value.accountEntryId ?: 0,
            title = viewModelState.value.title,
            description = viewModelState.value.description,
            amount = viewModelState.value.amount.toDouble(),
            entryType = viewModelState.value.entryType,
            transactionType = viewModelState.value.transactionType,
            transactionDate = viewModelState.value.transactionDate,
            partyId = viewModelState.value.partyId,
            categoryId = viewModelState.value.categoryId
        )
        addAccountEntry(entry)
    }

    private fun addAccountEntry(entry: AccountEntry) {
        if (addAccountEntryJob?.isActive == true) {
            val t = IllegalStateException("A request is already active.")
            ifDebug { Timber.w(t) }
            return
        }

        addAccountEntryJob?.cancel(CancellationException())
        setLoading(LoadType.ACTION, LoadState.Loading())
        addAccountEntryJob = viewModelScope.launch {
            accountsRepository.addAccountEntry(entry).fold(
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
                            isAddAccountSuccessful = true
                        )
                    }
                },
            )
        }
    }

    private fun deleteAccountEntry() {
        val partyId = viewModelState.value.accountEntryId ?: return
        deleteAccountEntryJob = viewModelScope.launch {
            accountsRepository.getAccountEntry(partyId).fold(
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
                onSuccess = { entryWithDetails ->
                    accountsRepository.deleteAccountEntry(entryWithDetails.entry.entryId).fold(
                        onFailure = {},
                        onSuccess = {
                            sendEvent(AddAccountUiEvent.ShowToast(UiText.DynamicString("AccountEntry deleted")))
                            sendEvent(AddAccountUiEvent.OnNavUp)
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

    private fun sendEvent(event: AddAccountUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

}

@OptIn(ExperimentalTime::class)
private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,

    val accountEntryId: Long? = null,

    val title: String = "",
    val description: String = "",
    val amount: String = "",
    val date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val entryType: EntryType = EntryType.OTHER,
    val transactionType: TransactionType = TransactionType.TRANSFER,
    val transactionDate: Long = System.currentTimeMillis(),
    val partyId: Long? = null,
    val party: Party? = null,
    val categoryId: Long = 0L,
    val category: Category? = null,

    val errorMessage: ErrorMessage? = null,

    /**
     * Flag to indicate that the signup is successful
     */
    val isAddAccountSuccessful: Boolean = false,
) {
    fun toAddAccountUiState(): AddAccountUiState {
        return if (isAddAccountSuccessful) {
            AddAccountUiState.AddAccountSuccess
        } else {
            AddAccountUiState.AddAccountForm(
                title = title,
                description = description,
                amount = amount.toString(),
                entryType = entryType,
                transactionType = transactionType,
                transactionDate = transactionDate,
                party = party,
                category = category,
                errorMessage = errorMessage,
            )
        }
    }
}

sealed interface AddAccountUiState {
    data class AddAccountForm(
        val title: String,
        val description: String,
        val amount: String = "",
        val entryType: EntryType = EntryType.OTHER,
        val transactionType: TransactionType = TransactionType.TRANSFER,
        val transactionDate: Long = 0L,
        val party: Party? = null,
        val category: Category? = null,
        val errorMessage: ErrorMessage? = null,
    ) : AddAccountUiState

    data object AddAccountSuccess : AddAccountUiState
}

sealed interface AddAccountUiAction {
    data object ErrorShown : AddAccountUiAction
    data class OnTypingTitle(val title: String) : AddAccountUiAction
    data class OnTypingAmount(val amount: String) : AddAccountUiAction
    data class OnDatePicked(val date: Long) : AddAccountUiAction
    data class OnEntryTypeToggle(val entryType: EntryType) : AddAccountUiAction
    data class OnTransactionTypeToggle(val transactionType: TransactionType) : AddAccountUiAction
    data class OnCategoryToggle(val categoryId: Long) : AddAccountUiAction
    data class OnPartyToggle(val partyId: Long?) : AddAccountUiAction
    data class Submit(
        val name: String,
        val description: String,
        val amount: String = "",
        val entryType: EntryType = EntryType.OTHER,
        val transactionType: TransactionType = TransactionType.TRANSFER,
        val transactionDate: Long = 0L,
        val partyId: Long? = null,
    ) : AddAccountUiAction

    data object Reset : AddAccountUiAction
    data object DeleteAccountEntry : AddAccountUiAction
    data class OnCategorySelectRequest(val categoryId: Long) : AddAccountUiAction
    data class OnPartySelectRequest(val partyId: Long?) : AddAccountUiAction
}

sealed interface AddAccountUiEvent {
    data class ShowToast(val message: UiText) : AddAccountUiEvent
    data object OnNavUp : AddAccountUiEvent
    data class NavigateToCategorySelection(val categoryId: Long) : AddAccountUiEvent
    data class NavigateToPartySelection(val partyId: Long) : AddAccountUiEvent
}
