package com.handbook.app.feature.home.presentation.landing

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
import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.Post
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.domain.repository.PostRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow(ViewModelState())

    val feedUiState = viewModelState
        .map(ViewModelState::toFeedUiState)
        .onStart { retryInternal(false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeedUiState.Idle
        )

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val accept: (HomeUiAction) -> Unit

    private var feedFetchJob: Job? = null
    private var likeJob: Job? = null

    init {
        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            HomeUiAction.Refresh -> {
                retryInternal(false)
            }

            HomeUiAction.LoadMore -> retryInternal(true)

            is HomeUiAction.LikeToggle -> {
                likeUnlikePost(uiAction.postId, !(uiAction.liked))
            }

            is HomeUiAction.NavigateToProfile -> {
                viewModelScope.launch {
                    val isSelf = userDataRepository.userData.firstOrNull()
                        ?.userId == uiAction.userId
                    sendEvent(HomeUiEvent.NavigateToProfile(uiAction.userId, isSelf))
                }
            }

            is HomeUiAction.NavigateToPost -> {
                sendEvent(HomeUiEvent.NavigateToPost(uiAction.postId))
            }
        }
    }

    private fun retryInternal(loadMore: Boolean) {
        if (loadMore) {
            if (!viewModelState.value.endOfPaginationReached) {
                getGlobalFeed(LoadType.APPEND)
            }
        } else {
            getGlobalFeed(LoadType.REFRESH)
        }
    }

    private fun getGlobalFeed(loadType: LoadType) {
        if (feedFetchJob?.isActive == true) {
            val t = IllegalStateException("Feed fetch job is already active, cancelling")
            Timber.w(t)
        }

        feedFetchJob?.cancel(CancellationException("New request"))
        setLoading(loadType, LoadState.Loading())

            val request = PagedRequest.create<Int>(
                loadType,
                key = if (loadType == LoadType.REFRESH) {
                    0
                } else {
                    viewModelState.value.nextPagingKey ?: 0
                },
                loadSize = if (loadType == LoadType.REFRESH) {
                    FEED_PAGE_SIZE * 2
                } else {
                    FEED_PAGE_SIZE
                }
            )

        feedFetchJob = viewModelScope.launch {
            when (val result = postRepository.globalFeed(request)) {
                Result.Loading -> {}
                is Result.Error -> {
                    Timber.e(result.exception)
                    viewModelState.update { state ->
                        state.copy(
                            errorMessage = ErrorMessage(
                                id = 0,
                                exception = result.exception,
                                message = UiText.somethingWentWrong
                            )
                        )
                    }
                    setLoading(loadType, LoadState.Error(result.exception))
                }

                is Result.Success -> {
                    viewModelState.update { state ->
                        val newPosts = if (loadType == LoadType.REFRESH) {
                            result.data.posts
                        } else {
                            result.data.posts + state.posts
                        }
                        val newUsers = if (loadType == LoadType.REFRESH) {
                            result.data.users
                        } else {
                            result.data.users + state.users
                        }

                        state.copy(
                            posts = newPosts,
                            users = newUsers,
                            nextPagingKey = result.data.nextPagingKey,
                            endOfPaginationReached = result.data.nextPagingKey == null,
                            errorMessage = null
                        )
                    }
                    if (result.data.nextPagingKey != null) {
                        setLoading(loadType, LoadState.NotLoading.InComplete)
                    } else {
                        setLoading(loadType, LoadState.NotLoading.Complete)
                    }
                }
            }
        }
    }

    private fun likeUnlikePost(postId: String, liked: Boolean) {
        likeJob = viewModelScope.launch {
            val result = if (liked) {
                postRepository.likePost(postId)
            } else {
                postRepository.unlikePost(postId)
            }
            when (result) {
                Result.Loading -> {}
                is Result.Error -> {
                    Timber.e(result.exception)
                    sendEvent(HomeUiEvent.ShowSnackbar(UiText.somethingWentWrong))
                }
                is Result.Success -> {
                    viewModelState.update { state ->
                        val newPosts = state.posts.mapNotNull { post ->
                            if (post.id == postId) {
                                result.data.posts.firstOrNull()
                            } else {
                                post
                            }
                        }
                        state.copy(posts = newPosts)
                    }
                }
            }
        }
    }

    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(newEvent: HomeUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

private data class ViewModelState(
    val loadState: LoadStates = LoadStates.IDLE,
    val posts: List<Post> = emptyList(),
    val users: List<UserSummary> = emptyList(),

    val nextPagingKey: Int? = null,
    val endOfPaginationReached: Boolean = false,

    val errorMessage: ErrorMessage? = null,
) {
    fun toFeedUiState(): FeedUiState {
        if (posts.isNotEmpty()) {
            return FeedUiState.Success(
                posts = posts,
                users = users,
                isRefreshing = loadState.refresh is LoadState.Loading,
                endOfPaginationReached = endOfPaginationReached,
            )
        } else {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    return FeedUiState.Loading
                }

                is LoadState.Error -> {
                    val message = errorMessage ?: ErrorMessage.unknown()
                    return FeedUiState.Error(message)
                }

                else -> {
                    return FeedUiState.Idle
                }
            }
        }
    }
}

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data object Idle : FeedUiState
    data class Error(val errorMessage: ErrorMessage) : FeedUiState
    data class Success(
        val posts: List<Post>,
        val users: List<UserSummary>,
        val isRefreshing: Boolean = false,
        val endOfPaginationReached: Boolean = false
    ) : FeedUiState
}

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
    data object LoadMore : HomeUiAction
    data class LikeToggle(val postId: String, val liked: Boolean) : HomeUiAction
    data class NavigateToProfile(val userId: String) : HomeUiAction
    data class NavigateToPost(val postId: String) : HomeUiAction
}

sealed interface HomeUiEvent {
    data class ShowToast(val message: UiText) : HomeUiEvent
    data class ShowSnackbar(val message: UiText) : HomeUiEvent
    data class NavigateToProfile(val userId: String, val isSelf: Boolean) : HomeUiEvent
    data class NavigateToPost(val postId: String) : HomeUiEvent
}

private const val FEED_PAGE_SIZE = 10
