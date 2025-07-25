package com.handbook.app.feature.home.presentation.accounts.addaccount

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.StorageUtil
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.designsystem.component.forms.MediaType
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.Attachment
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@ExperimentalCoroutinesApi
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
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
    val bankId = savedStateHandle.getStateFlow<Long>("bankId", 0L)

    private val _uiEvent = MutableSharedFlow<AddAccountUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (AddAccountUiAction) -> Unit

    private var addAccountEntryJob: Job? = null
    private var deleteAccountEntryJob: Job? = null

    private val deletedPhotoIds = mutableListOf<Long>()

    init {
        accept = { uiAction -> onUiAction(uiAction) }

        if (accountEntryId.value != 0L) {
            Timber.d("Parsing: accountEntryId=${accountEntryId.value}")
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
                                categoryId = entry.categoryId,
                                isPinned = entry.isPinned,
                                bankId = entry.bankId,
                            )
                        }
                        savedStateHandle["categoryId"] = entry.categoryId
                        savedStateHandle["partyId"] = entry.partyId ?: 0L
                        savedStateHandle["bankId"] = entry.bankId ?: 0L
                        setPickedMediaUris(
                            mediaType = MediaType.Unknown,
                            newPreviewModelList = entryWithDetails.attachments
                                .map { attachment -> UploadPreviewUiModel.Item(attachment) },
                            append = false
                        )
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

        bankId.mapLatest { bid ->
            if (bid != 0L) {
                accountsRepository.getBank(bid)
            } else {
                null
            }
        }.onEach { result ->
            if (result != null) {
                result.fold(
                    onFailure = { exception ->
                    },
                    onSuccess = { bank ->
                        Timber.d("Selected: bank=$bank")
                        viewModelState.update { state ->
                            state.copy(
                                bankId = bank.id,
                                bank = bank
                            )
                        }
                    }
                )
            } else {
                viewModelState.update { state ->
                    state.copy(
                        bankId = null,
                        bank = null
                    )
                }
            }
        }
            .launchIn(viewModelScope)

        // Initializes the place holder for media pickers.
        setPickedMediaUris(MediaType.Unknown, emptyList())
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
                sendEvent(AddAccountUiEvent.NavigateToCategorySelection(
                    action.categoryId, viewModelState.value.transactionType))
            }

            is AddAccountUiAction.OnPartyToggle -> {
                savedStateHandle["partyId"] = action.partyId
            }

            is AddAccountUiAction.OnPartySelectRequest -> {
                sendEvent(AddAccountUiEvent.NavigateToPartySelection(action.partyId ?: 0L))
            }

            is AddAccountUiAction.OnBankToggle -> {
                savedStateHandle["bankId"] = action.bankId
            }

            is AddAccountUiAction.OnBankSelectRequest -> {
                sendEvent(AddAccountUiEvent.NavigateToBankSelection(action.bankId ?: 0L))
            }

            is AddAccountUiAction.OnPinnedChange -> {
                viewModelState.update { state ->
                    state.copy(
                        isPinned = action.pinned
                    )
                }
            }
        }
    }

    private fun validate() {
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
            entryId = viewModelState.value.accountEntryId ?: 0,
            title = viewModelState.value.title,
            description = viewModelState.value.description,
            amount = viewModelState.value.amount.toDouble(),
            entryType = viewModelState.value.entryType,
            transactionType = viewModelState.value.transactionType,
            transactionDate = viewModelState.value.transactionDate,
            partyId = viewModelState.value.partyId,
            categoryId = viewModelState.value.categoryId,
            isPinned = viewModelState.value.isPinned,
            bankId = viewModelState.value.bankId
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
        setLoadState(LoadType.ACTION, LoadState.Loading())
        addAccountEntryJob = viewModelScope.launch {
            val result = if (entry.entryId != 0L) {
                accountsRepository.updateAccountEntry(entry)
            } else {
                accountsRepository.addAccountEntry(entry)
            }
            result.fold(
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
                onSuccess = { accountEntryId ->
                    setLoadState(LoadType.ACTION, LoadState.NotLoading.Complete)

                    // Delete any attachments
                    if (deletedPhotoIds.isNotEmpty()) {
                        accountsRepository.deleteAttachments(deletedPhotoIds)
                            .fold(
                                onFailure = { exception -> },
                                onSuccess = {
                                    deletedPhotoIds.clear()
                                }
                            )
                    }

                    // Check if any pending attachments needs to be inserted
                    val pendingAttachments = viewModelState.value.mediaFiles
                        .filterIsInstance<UploadPreviewUiModel.Item>()
                        .filter { it.attachment.attachmentId == 0L }
                        .map { it.attachment.copy(entryId = accountEntryId) }
                    Timber.d("Total attachments: size=${viewModelState.value.mediaFiles
                        .filterIsInstance<UploadPreviewUiModel.Item>().count()}")
                    Timber.d("Pending attachments: size=${pendingAttachments.size} $pendingAttachments")

                    if (pendingAttachments.isNotEmpty()) {
                        startUpload(applicationContext, pendingAttachments).join()
                    }

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

    private fun setLoadState(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(event: AddAccountUiEvent) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

    /* Upload related */
    fun getMaxAttachments(): Int {
        val max = MAX_ATTACHMENTS_LIMIT -
                viewModelState.value.mediaFiles.count { it !is UploadPreviewUiModel.Placeholder }
        return max.coerceAtLeast(0)
    }

    fun removeDuplicateMedia(
        mediaType: MediaType,
        pickedUris: List<Uri>,
        completion: (removed: Int, normalizedList: List<Uri>) -> Unit,
    ) = viewModelScope.launch(Dispatchers.Default) {
        val formData = viewModelState.value
        val originalList = formData.mediaFiles.filterIsInstance<UploadPreviewUiModel.Item>()
            .map { it.attachment.uri }
        val combinedUris = originalList.toMutableList().apply {
            addAll(pickedUris)
        }

        val normalizedList = combinedUris.distinct().toMutableList()
        val removed = (combinedUris.size - normalizedList.size).coerceAtLeast(0)
        normalizedList.removeAll(originalList)
        withContext(Dispatchers.Main) {
            completion(removed, normalizedList)
        }
    }

    fun deleteMedia(mediaType: MediaType, uri: Uri) {
        Timber.d("deletePhoto: $uri type=$mediaType")
        val formData = viewModelState.value
        val newPreviewModelList = formData.mediaFiles
            .filterIsInstance<UploadPreviewUiModel.Item>()
            .filterNot { uiModel ->
                val deleted = uiModel.attachment.uri == uri
                if (deleted) {
                    uiModel.attachment.attachmentId?.let { imageId ->
                        deletedPhotoIds.add(imageId)
                    }
                }
                deleted
            }

        setPickedMediaUris(mediaType, newPreviewModelList, append = false)
    }

    fun setPickedMediaUris(
        mediaType: MediaType,
        newPreviewModelList: List<UploadPreviewUiModel.Item>,
        append: Boolean = true,
    ) {
        // TODO: clean this sheet!
        val formData = viewModelState.value

        val newModelList = formData.mediaFiles
            .filterNot { it is UploadPreviewUiModel.Placeholder }
            .toMutableList().apply {
                if (!append) {
                    clear()
                }
                addAll(newPreviewModelList)
                val selectedCount = count { model ->
                    model is UploadPreviewUiModel.Item
                }
                if (selectedCount < MAX_ATTACHMENTS_LIMIT) {
                    add(0, UploadPreviewUiModel.Placeholder(size))
                }
            }

        viewModelState.update { state ->
            state.copy(
                mediaFiles = newModelList
            )
        }
    }

    private var tempCaptureFile: File? = null
    fun getTempCaptureFile(): File? {
        return tempCaptureFile ?: StorageUtil.getTempCaptureImageFile(applicationContext)
            .also { tempCaptureFile = it }
    }

    // We will just move a copy of those files to app's private directory.
    fun startUpload(context: Context, attachments: List<Attachment>): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            StorageUtil.cleanUp(context)

            val pendingAttachments = attachments
                .mapNotNull { attachment ->
                    val tempFile = StorageUtil.getAttachmentFile(context, MediaType.getFileExtension(attachment.contentMediaType))
                    tempFile ?: return@mapNotNull null
                    val result = StorageUtil.saveAttachmentToFolder(
                        context,
                        attachment.uri,
                        tempFile
                    )
                    attachment.copy(
                        filePath = tempFile.absolutePath,
                        uri = tempFile.toUri()
                    )
                }

            Timber.d("Pending attachments: after $pendingAttachments")

            accountsRepository.addAttachments(pendingAttachments)
                .fold(
                    onFailure = { t ->
                        Timber.e(t, "Failed to add attachments")
                    },
                    onSuccess = {
                        Timber.d("Attachment added successfully")
                    }
                )
        }
    }

    /*fun startUpload(context: Context): Job {
        setLoadState(loadType = LoadType.ACTION, loadState = LoadState.Loading())
        return viewModelScope.launch {
            StorageUtil.cleanUp(context)

            val productFormData = viewModelState.value
            val uploadCallables = (productFormData.videos + productFormData.images)
                .filterIsInstance<UploadPreviewUiModel.Item>()
                .filter {
                    *//* remoteFileName null means it is from local file, and needs to be uploaded *//*
                    it.attachment.remoteFileName == null
                }
                .mapNotNull { model ->
                    val mimeType = getMimeType(context, model.attachment.uri)
                    val tempFile = when (model.attachment.contentMediaType) {
                        MediaType.Image -> {
                            StorageUtil.getTempUploadFile(context)
                        }
                        MediaType.Video -> {
                            StorageUtil.getTempUploadFile(
                                context,
                                "VID_",
                                EXTENSION_MP4
                            )
                        }
                        else -> null
                    }
                    tempFile ?: return@mapNotNull null
                    StorageUtil.saveFilesToFolder(
                        context,
                        model.attachment.uri,
                        tempFile
                    )
                    model.attachment.copy(cachedFile = tempFile)
                }
                .map { sellerMediaFile ->
                    FileUploaderCallable(
                        request = sellerMediaFile,
                        type = "product",
                        file = sellerMediaFile.cachedFile!!,
                        onProgress = { _ -> }
                    )
                }

            val jobPool = uploadCallables.map { callable ->
                viewModelScope.async(workerContext) {
                    withContext(Dispatchers.IO) {
                        callable.call()
                    }
                }
            }
            jobPool.awaitAll().let { callback ->
                callback.count { it.response?.data == null }.let { failedUploadCount ->
                    if (failedUploadCount > 0) {
                        Timber.w("$failedUploadCount upload(s) failed")
                    }
                }
                callback
            }.mapNotNull { callback ->
                Timber.d("Callback: $callback")
                // TODO: process success uploads
                val response = callback.response
                if (response?.data != null) {
                    callback.request.copy(
                        remoteFileName = response.data.fileName
                    )
                } else {
                    null
                }
            }.let { mediaFiles ->
                Timber.d("Upload response=$mediaFiles")
                val images = mediaFiles.filter { it.mediaType == MediaType.Image }
                val videos = mediaFiles.filter { it.mediaType == MediaType.Video }
                setPickedMediaUris(
                    MediaType.Image,
                    images.map(UploadPreviewUiModel::Item),
                    false
                )
                setPickedMediaUris(
                    MediaType.Video,
                    videos.map(UploadPreviewUiModel::Item),
                    false
                )
            }
            *//*jobPool.count { it.data == null }.let { failedUploadCount ->
                if (failedUploadCount > 0) {
                    Timber.w("$failedUploadCount upload(s) failed")
                }
            }*//*
            // submitReviewInternal()
            setLoadState(loadType = LoadType.ACTION, loadState = LoadState.NotLoading.Complete)
            // TODO: Handle upload complete.
            viewModelState.update { state ->
                state.copy(
                    uploadComplete = true,
                )
            }
        }
    }*/
    /* END - Upload related */

    private val threadCount: Int = Runtime.getRuntime().availableProcessors() * 2
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, t ->
            Timber.e(t)
        }
    private val backgroundDispatcher = newFixedThreadPoolContext(threadCount, "Upload photos pool")
    private val workerContext =
        backgroundDispatcher.limitedParallelism(MAX_PARALLEL_THREADS) + SupervisorJob() + coroutineExceptionHandler

    companion object {
        private const val MAX_PARALLEL_THREADS: Int = 2
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
    val bankId: Long? = null,
    val bank: Bank? = null,
    val categoryId: Long = 0L,
    val category: Category? = null,
    val isPinned: Boolean = false,
    val mediaFiles: List<UploadPreviewUiModel> = emptyList(),
    // This is not for uploading to remote, just copy the files to app's private directory.
    val uploadComplete: Boolean = false,

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
                bank = bank,
                errorMessage = errorMessage,
                isPinned = isPinned,
                mediaFiles = mediaFiles,
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
        val bank: Bank? = null,
        val isPinned: Boolean = false,
        val mediaFiles: List<UploadPreviewUiModel> = emptyList(),
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
    data class OnBankToggle(val bankId: Long?) : AddAccountUiAction
    data class OnPinnedChange(val pinned: Boolean) : AddAccountUiAction
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
    data class OnBankSelectRequest(val bankId: Long?) : AddAccountUiAction
}

sealed interface AddAccountUiEvent {
    data class ShowToast(val message: UiText) : AddAccountUiEvent
    data object OnNavUp : AddAccountUiEvent
    data class NavigateToCategorySelection(val categoryId: Long, val transactionType: TransactionType) : AddAccountUiEvent
    data class NavigateToPartySelection(val partyId: Long) : AddAccountUiEvent
    data class NavigateToBankSelection(val bankId: Long) : AddAccountUiEvent
}

interface UploadPreviewUiModel {
    data class Item(val attachment: Attachment) : UploadPreviewUiModel
    data class Placeholder(val position: Int) : UploadPreviewUiModel
}