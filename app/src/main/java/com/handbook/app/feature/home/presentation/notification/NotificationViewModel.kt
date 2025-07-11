package com.handbook.app.feature.home.presentation.notification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.di.AiaDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.HandbookNotification
import com.handbook.app.feature.home.domain.model.request.NotificationRequest
import com.handbook.app.feature.home.domain.repository.NotificationRepository
import com.handbook.app.ifDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.handbook.app.common.util.paging.PagedRequest
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(AiaDispatchers.Default) private val computationDispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val notificationUiState = viewModelState
        .map(ViewModelState::toNotificationUiState)
        .onStart { refreshNotifications(false) }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<NotificationUiEvent>()
    val uiEvent: SharedFlow<NotificationUiEvent> = _uiEvent.asSharedFlow()

    private val actionStream = MutableSharedFlow<NotificationUiAction>()
    val accept: (NotificationUiAction) -> Unit

    private var notificationFetchJob: Job? = null

    init {
        accept = { action -> onUiAction(action) }

        val remoteNotifications = notificationRepository.notificationStream()
            .distinctUntilChanged()
            .map { notifications ->
                notifications.map { notification -> NotificationUiModel.Notification(notification) }
            }

        val appendLoadState = viewModelState.map { it.loadState.append }
            .distinctUntilChanged()
        combine(
            remoteNotifications,
            appendLoadState,
            ::Pair
        ).onEach { (notifications, append) ->
            val newUiModels = if (!append.endOfPaginationReached && append is LoadState.Loading) {
                notifications + NotificationUiModel.Footer(append)
            } else {
                notifications
            }

            viewModelState.update { state ->
                state.copy(
                    notifications = newUiModels,
                    endOfPaginationReached = append.endOfPaginationReached
                )
            }
        }
            .flowOn(computationDispatcher)
            .launchIn(viewModelScope)

        actionStream
            .filterIsInstance<NotificationUiAction.Scroll>()
            .onEach { action ->
                val endOfPaginationReached = viewModelState.value.endOfPaginationReached
                if (action.shouldFetchMore && !endOfPaginationReached && notificationFetchJob?.isActive != true) {
                    refreshNotifications(true)
                }
            }
            .launchIn(viewModelScope)

    }

    private fun onUiAction(action: NotificationUiAction) {
        when (action) {
            is NotificationUiAction.ErrorShown -> {
                viewModelState.update { state ->
                    state.copy(
                        errorMessage = null
                    )
                }
            }

            is NotificationUiAction.Scroll -> {
                viewModelState.update { state ->
                    state.copy(
                        nextPagingKey = action.lastVisibleItemPosition
                    )
                }
            }

            is NotificationUiAction.Refresh -> {
                refreshNotifications(false)
            }

            is NotificationUiAction.NavigateToProfile -> {
                viewModelScope.launch {
                    val isSelf = userDataRepository.userData.firstOrNull()
                        ?.userId == action.userId
                    sendEvent(NotificationUiEvent.NavigateToProfile(action.userId, isSelf))
                }
            }

            is NotificationUiAction.NavigateToPostDetail -> {
                sendEvent(NotificationUiEvent.NavigateToPostDetail(action.postId))
            }
        }
    }

    private fun refreshNotifications(loadMore: Boolean) {
        if (loadMore) {
            getNotifications(LoadType.APPEND)
        } else {
            getNotifications(LoadType.REFRESH)
        }
    }

    private fun getNotifications(loadType: LoadType) {
        if (loadType == LoadType.APPEND && notificationFetchJob?.isActive == true) {
            val t = IllegalStateException("A load more request is already in progress")
            ifDebug { Timber.w(t) }
            return
        }

        notificationFetchJob?.cancel(CancellationException("New request"))

        setLoading(loadType, LoadState.Loading())

        val request = PagedRequest.create<Int>(
            loadType,
            key = if (loadType == LoadType.REFRESH) {
                0
            } else {
                viewModelState.value.nextPagingKey ?: 0
            },
            loadSize = if (loadType == LoadType.REFRESH) {
                NOTIFICATIONS_PAGE_SIZE * 2
            } else {
                NOTIFICATIONS_PAGE_SIZE
            }
        )

        notificationFetchJob = viewModelScope.launch {
            notificationRepository.refreshNotifications(NotificationRequest(request)).fold(
                onFailure = { exception ->
                    Timber.e(exception)
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = ErrorMessage(
                                id = 0,
                                exception = exception,
                                message = UiText.somethingWentWrong
                            )
                        )
                    }
                    setLoading(loadType, LoadState.Error(exception))
                },
                onSuccess = { result ->
                    val endOfPaginationReached = result.nextKey == null

                    viewModelState.update { state ->
                        state.copy(
                            nextPagingKey = result.nextKey,
                            endOfPaginationReached = endOfPaginationReached,
                            errorMessage = null
                        )
                    }

                    if (endOfPaginationReached) {
                        setLoading(loadType, LoadState.NotLoading.Complete)
                    } else {
                        setLoading(loadType, LoadState.NotLoading.InComplete)
                    }
                }
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(newEvent: NotificationUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val notifications: List<NotificationUiModel> = emptyList(),

    val nextPagingKey: Int? = null,
    val endOfPaginationReached: Boolean = false,
    val errorMessage: ErrorMessage? = null
) {
    fun toNotificationUiState(): NotificationUiState {
        if (notifications.isNotEmpty()) {
            return NotificationUiState.Success(notifications)
        } else {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    return NotificationUiState.Loading
                }
                is LoadState.Error -> {
                    val message = errorMessage ?: ErrorMessage.unknown()
                    return NotificationUiState.Error(message)
                }
                else -> {
                    return NotificationUiState.Idle
                }
            }
        }
    }
}

sealed interface NotificationUiAction {
    data class ErrorShown(val id: Long) : NotificationUiAction
    data class Scroll(
        val visibleItemCount: Int,
        val lastVisibleItemPosition: Int,
        val totalItemCount: Int,
    ) : NotificationUiAction
    data object Refresh : NotificationUiAction
    data class NavigateToProfile(val userId: String) : NotificationUiAction
    data class NavigateToPostDetail(val postId: String) : NotificationUiAction
}

val NotificationUiAction.Scroll.shouldFetchMore
    get() = visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount

sealed interface NotificationUiEvent {
    data class ShowSnack(val message: UiText) : NotificationUiEvent
    data class ShowToast(val message: UiText) : NotificationUiEvent
    data class NavigateToProfile(val userId: String, val isSelf: Boolean) : NotificationUiEvent
    data class NavigateToPostDetail(val postId: String) : NotificationUiEvent
}

sealed interface NotificationUiModel {
    data class Notification(val banterboxNotification: HandbookNotification) : NotificationUiModel
    data class Separator(val title: String) : NotificationUiModel
    data class Footer(val loadState: LoadState) : NotificationUiModel
}

sealed interface NotificationUiState {
    object Idle : NotificationUiState
    object Loading : NotificationUiState
    data class Error(val errorMessage: ErrorMessage) : NotificationUiState
    data class Success(val notifications: List<NotificationUiModel>) : NotificationUiState
}

private const val VISIBLE_THRESHOLD = 3
private const val NOTIFICATIONS_PAGE_SIZE = 10


