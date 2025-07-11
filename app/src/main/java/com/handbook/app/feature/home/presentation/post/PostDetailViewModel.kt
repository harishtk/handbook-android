package com.handbook.app.feature.home.presentation.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.domain.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(ViewModelState())

    val postDetailUiState = viewModelState
        .map(ViewModelState::toPostDetailUiState)
        .onStart { retryInternal() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostDetailUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<PostDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (PostDetailUiAction) -> Unit

    private var postFetchJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(uiAction: PostDetailUiAction) {
        when (uiAction) {
            PostDetailUiAction.Refresh -> {}
            is PostDetailUiAction.LikeToggle -> likeToggle(uiAction.postId, uiAction.liked)
            is PostDetailUiAction.NavigateToProfile -> {
                viewModelScope.launch {
                    val isSelf = userDataRepository.userData.firstOrNull()
                        ?.userId == uiAction.userId
                    sendEvent(PostDetailUiEvent.NavigateToProfile(uiAction.userId, isSelf))
                }
            }
        }
    }

    private fun retryInternal() {
        postFetchJob?.cancel()

        val postId = savedStateHandle.get<String>("postId")
        if (postId == null) {
            setError(IllegalStateException("Post ID not found in SavedStateHandle"))
            return
        }

        val loadType = LoadType.REFRESH
        setLoading(loadType, LoadState.Loading())
        postFetchJob = viewModelScope.launch {
            postRepository.getPostById(postId).fold(
                onFailure = { exception ->
                    setError(exception)
                    setLoading(loadType, LoadState.Error(exception))
                },
                onSuccess = { result ->
                    viewModelState.update { state ->
                        state.copy(
                            post = result.posts.first(),
                            users = result.users,
                            errorMessage = null
                        )
                    }
                    setLoading(loadType, LoadState.NotLoading.Complete)
                }
            )
        }
    }

    private fun likeToggle(postId: String, liked: Boolean) = viewModelScope.launch {
        val result = if (liked) {
            postRepository.unlikePost(postId)
        } else {
            postRepository.likePost(postId)
        }
        result.fold(
            onFailure = { exception ->
                setError(exception)
            },
            onSuccess = { result ->
                viewModelState.update { state ->
                    state.copy(
                        post = result.posts.first(),
                        users = result.users,
                        errorMessage = null
                    )
                }
            }
        )
    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun setError(throwable: Throwable) = viewModelScope.launch {
        val message = ErrorMessage.from(throwable)
        viewModelState.update { state -> state.copy(errorMessage = message) }
    }

    private fun sendEvent(event: PostDetailUiEvent) = viewModelScope.launch {
        _uiEvent.emit(event)
    }
}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val post: Post? = null,
    val users: List<UserSummary> = emptyList(),

    val errorMessage: ErrorMessage? = null
) {
    fun toPostDetailUiState(): PostDetailUiState {
        if (post != null) {
            return PostDetailUiState.Success(
                post = post,
                users = users,
                isRefreshing = loadState.refresh is LoadState.Loading,
            )
        } else {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    return PostDetailUiState.Loading
                }
                is LoadState.Error -> {
                    val message = errorMessage ?: ErrorMessage.unknown()
                    return PostDetailUiState.Error(message)
                }
                else -> {
                    return PostDetailUiState.Idle
                }
            }
        }
    }
}

sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    data object Idle : PostDetailUiState
    data class Error(val errorMessage: ErrorMessage) : PostDetailUiState
    data class Success(
        val post: Post,
        val users: List<UserSummary>,
        val isRefreshing: Boolean = false,
    ) : PostDetailUiState
}

sealed interface PostDetailUiAction {
    data object Refresh : PostDetailUiAction
    data class LikeToggle(val postId: String, val liked: Boolean) : PostDetailUiAction
    data class NavigateToProfile(val userId: String) : PostDetailUiAction
}

sealed interface PostDetailUiEvent {
    data class ShowSnack(val message: UiText) : PostDetailUiEvent
    data class ShowToast(val message: UiText) : PostDetailUiEvent
    data class NavigateToProfile(val userId: String, val isSelf: Boolean) : PostDetailUiEvent
}
