package com.handbook.app.feature.home.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.handbook.app.R
import com.handbook.app.common.util.UiText
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.common.util.loadstate.LoadStates
import com.handbook.app.common.util.loadstate.LoadType
import com.handbook.app.common.util.paging.PagedRequest
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.util.fold
import com.handbook.app.feature.home.domain.model.PostSummary
import com.handbook.app.feature.home.domain.model.SearchType
import com.handbook.app.feature.home.domain.model.UserSummary
import com.handbook.app.feature.home.domain.model.request.SearchRequest
import com.handbook.app.feature.home.domain.repository.SearchRepository
import com.handbook.app.ifDebug
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val viewModelState = MutableStateFlow<ViewModelState>(ViewModelState())

    val searchResultUiState = viewModelState.map(ViewModelState::toSearchResultState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchResultState.Idle
        )

    private val _uiEvent = MutableSharedFlow<SearchUiEvent>()
    val uiEvent: SharedFlow<SearchUiEvent> = _uiEvent.asSharedFlow()

    val accept: (SearchUiAction) -> Unit

    private val actionStream = MutableSharedFlow<SearchUiAction>()

    private var searchSuggestionFetchJob: Job? = null
    private var searchResultFetchJob: Job? = null
    private var bestDealsTrendingFetchJob: Job? = null

    init {
        val searches = actionStream
            .filterIsInstance<SearchUiAction.OnTyping>()
            .distinctUntilChanged()
            .onStart {
                SearchUiAction.OnTyping(
                    query = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
                )
            }
        val scrolls = actionStream
            .filterIsInstance<SearchUiAction.Scroll>()
            .distinctUntilChanged()
            .onStart {
                emit(
                    SearchUiAction.Scroll(
                        savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY,
                        totalItemCount = viewModelState.value.searchResult.size,
                        lastVisibleItemPosition = 0,
                        visibleItemCount = 0
                    )
                )
            }

        searches
            .debounce(150)
            .distinctUntilChanged()
            .onEach { search ->
                Timber.d("Searches: $search")
                if (search.query.isBlank()) {
                    savedStateHandle[LAST_SEARCH_QUERY] = DEFAULT_QUERY
                    // _searchUsersPagedFlow.update { PagingData.empty() }
                    /*_viewModelState.update { state ->
                        state.copy(
                            searchSuggestions = emptyList()
                        )
                    }*/
                } else {
                    savedStateHandle[LAST_SEARCH_QUERY] = search.query
                    viewModelState.update { state ->
                        state.copy(
                            hasSearchedForCurrentQuery = false
                        )
                    }
                    if (search.query.length >= MIN_QUERY_LENGTH) {
                        // searchUserInternal(search.query)
                        // getSearchSuggestions(search.query)
                        retry(false)
                    }
                }
            }
            .launchIn(viewModelScope)

        scrolls.onEach { scroll ->
            if (scroll.shouldFetchMore && !viewModelState.value.endOfPaginationReached && searchResultFetchJob?.isActive == false) {
                retry(loadMore = true)
            }
        }
            .launchIn(viewModelScope)

        combine(
            searches,
            scrolls,
            ::Pair
        ).onEach { (search, scroll) ->
            viewModelState.update {
                it.copy(
                    query = search.query,
                    lastQueryScrolled = scroll.currentQuery,
                    hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery,
                )
            }
        }
            .launchIn(viewModelScope)

        accept = { uiAction -> onUiAction(uiAction) }
    }

    private fun onUiAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.ErrorShown -> {
                viewModelState.update { state ->
                    state.copy(
                        exception = null,
                        uiErrorMessage = null
                    )
                }
            }

            is SearchUiAction.ClearSearch -> {
                viewModelState.update { state ->
                    state.copy(
                        query = DEFAULT_QUERY,
                        hasNotScrolledForCurrentSearch = true,
                        lastQueryScrolled = DEFAULT_QUERY,
                        hasSearchedForCurrentQuery = false,
                        searchResult = emptyList()
                    )
                }
            }

            is SearchUiAction.Search -> {
                viewModelState.update { state ->
                    state.copy(
                        query = action.query,
                        hasNotScrolledForCurrentSearch = true,
                        hasSearchedForCurrentQuery = false,
                        searchResult = emptyList()
                    )
                }
                retry(false)
            }

            is SearchUiAction.NavigateToProfile -> {
                viewModelScope.launch {
                    val isSelf = userDataRepository.userData.firstOrNull()
                        ?.userId == action.userId
                    sendEvent(SearchUiEvent.NavigateToProfile(action.userId, isSelf))
                }
            }

            is SearchUiAction.NavigateToPostDetail -> {
                sendEvent(SearchUiEvent.NavigateToPostDetail(action.postId))
            }

            is SearchUiAction.OnTyping,
            is SearchUiAction.Scroll,
                -> {
                viewModelScope.launch { actionStream.emit(action) }
            }
        }
    }

    fun handleBackPressed(): Boolean {
        val query = viewModelState.value.query
        if (query.isNotBlank()) {
            resetSearch()
            return true
        }
        val searchResultCache = viewModelState.value.searchResult
        if (searchResultCache.isNotEmpty()) {
            resetSearch()
            return true
        }
        return false
    }

    @Deprecated("Only a temp fix")
    fun resetSearchResults() {
        getSearchResult(LoadType.REFRESH, "")
    }

    /**
     * Delegates the scroll to top signal as an event
     */
    fun scrollToTop() {
        sendEvent(SearchUiEvent.ScrollToTop)
    }

    fun setActionLoadState(loadState: LoadState) {
        setLoading(LoadType.ACTION, loadState)
    }

    /*fun exposeFeedsToSharedRepository(type: String, continuation: () -> Unit) = viewModelScope.launch {
        val feeds = when (type) {
            "trending" -> viewModelState.value.trending
            else -> viewModelState.value.topDealProducts
        }
        sharedRepository.setSharedFeeds(feeds)
        continuation()
    }*/

    private fun resetSearch() {
        viewModelState.update { state ->
            state.copy(
                loadState = LoadStates.IDLE,
                query = DEFAULT_QUERY,
                hasNotScrolledForCurrentSearch = true,
                lastQueryScrolled = DEFAULT_QUERY,
                hasSearchedForCurrentQuery = false,
                searchResult = emptyList(),
                // searchSuggestions = emptyList(),
                exception = null,
                uiErrorMessage = null,
            )
        }
        sendEvent(SearchUiEvent.ResetSearch)
    }

    fun retry(loadMore: Boolean = false) {
        if (loadMore) {
            // FIXME: temporarily disable pagination
            // getSearchResult(LoadType.APPEND, viewModelState.value.query)
        } else {
            getSearchResult(LoadType.REFRESH, viewModelState.value.query)
        }
    }

    /*private fun getSearchSuggestions(query: String) {
        searchSuggestionFetchJob?.cancel(CancellationException("New request"))

        setLoading(LoadType.ACTION, LoadState.Loading())
        searchSuggestionFetchJob = viewModelScope.launch {
            when (val result = dealsRepository.searchSuggestions(query)) {
                is Result.Loading -> Unit
                is Result.Error -> {
                    setLoading(LoadType.ACTION, LoadState.Error(result.exception))
                }

                is Result.Success -> {
                    setLoading(LoadType.ACTION, LoadState.NotLoading.Complete)
                    _viewModelState.update { state ->
                        state.copy(
                            searchSuggestions = result.data
                        )
                    }
                }
            }
        }
    }*/

    private fun getSearchResult(loadType: LoadType, query: String) {
        Timber.d("Search: query = $query")
        if (loadType == LoadType.APPEND && searchResultFetchJob?.isActive == true) {
            val t = IllegalStateException("A load more request is already in progress")
            ifDebug { Timber.w(t) }
            return
        }
        searchResultFetchJob?.cancel(CancellationException("New request"))

        val request = SearchRequest(
            query = query,
            type = SearchType.ALL,
            pagedRequest = PagedRequest.create(
                LoadType.REFRESH,
                key = 0,
                loadSize = 10
            )
        )

        setLoading(loadType, LoadState.Loading())
        searchResultFetchJob = viewModelScope.launch {
            searchRepository.search(request).fold(
                onFailure = { exception ->
                    Timber.e(exception)
                    setLoading(loadType, LoadState.Error(exception))
                    viewModelState.update { state ->
                        state.copy(
                            hasSearchedForCurrentQuery = true
                        )
                    }
                },
                onSuccess = { result ->
                    val endOfPaginationReached = true
                    val searchResult: List<SearchResultUiModel> = result.let {
                        mutableListOf<SearchResultUiModel>().apply {
                            val message = "Showing ${result.users.size + result.posts.size} of " +
                                    "${result.totalUsers + result.totalPosts}"
                            add(SearchResultUiModel.Separator(message))
                            if (it.users.isNotEmpty()) {
                                add(SearchResultUiModel.Separator("Users"))
                            }
                            add(SearchResultUiModel.UserSearchResult(request.query, it.users))
                            if (it.posts.isNotEmpty()) {
                                add(SearchResultUiModel.Separator("Posts"))
                            }
                            add(SearchResultUiModel.PostSearchResult(request.query, it.posts))

                            if (endOfPaginationReached) {
                                add(SearchResultUiModel.Footer(UiText.StringResource(R.string.no_more_results_des)))
                            }
                        }
                    }
                    viewModelState.update { state ->
                        state.copy(
                            hasSearchedForCurrentQuery = true,
                            searchResult = searchResult,
                            endOfPaginationReached = endOfPaginationReached
                        )
                    }
                    if (endOfPaginationReached) {
                        setLoading(loadType, LoadState.NotLoading.Complete)
                    } else {
                        setLoading(loadType, LoadState.NotLoading.InComplete)
                    }
                    scrollToTop()
                }
            )
        }
    }

    private fun searchUserInternal(query: String) {
        Timber.d("Search: query = $query")

        /*val params = JsonObject()
        params.addProperty("loginUserId", prefs.getUserDetailModel()?.userId)
        params.addProperty("deviceToken", prefs.getUserDeviceToken())
        params.addProperty("search", query)
        searchJob?.cancel(CancellationException("New search"))
        searchJob = viewModelScope.launch {
            repository.searchUsers(params)
                .map { pagingData -> pagingData.map { UiModel.Global(user = it) } }
                .map {
                    it.insertSeparators { before: UiModel.Global?, after: UiModel.Global? ->
                        if (after == null) {
                            *//* We've reached the end *//*
                            return@insertSeparators null
                        }

                        if (before == null) {
                            *//* We are at the very first item *//*
                            return@insertSeparators UiModel.Separator("${after.user.type}")
                        }

                        // checking between 2 items
                        if (before.user.type != after.user.type) {
                            UiModel.Separator("${after.user.type}")
                        } else {
                            null
                        }
                    }
                }
                .collect(_searchUsersPagedFlow)
        }*/
    }

    @Suppress("SameParameterValue")
    private fun setLoading(
        loadType: LoadType,
        loadState: LoadState,
    ) {
        val newLoadState = viewModelState.value.loadState.modifyState(loadType, loadState)
        viewModelState.update { state -> state.copy(loadState = newLoadState) }
    }

    private fun sendEvent(newEvent: SearchUiEvent) = viewModelScope.launch {
        _uiEvent.emit(newEvent)
    }
}

private data class ViewModelState(
    /* LoadState can be adapted from paging adapter */
    val loadState: LoadStates = LoadStates.IDLE,
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val hasSearchedForCurrentQuery: Boolean = false,
    val trendingSearches: List<String> = emptyList(),
    val searchResult: List<SearchResultUiModel> = emptyList(),
    val nextPagingKey: Int? = null,
    val endOfPaginationReached: Boolean = false,
    val exception: Exception? = null,
    val uiErrorMessage: UiText? = null,
) {
    fun toSearchResultState(): SearchResultState {
        return if (searchResult.isEmpty()) {
            SearchResultState.Idle
        } else {
            if (loadState.refresh is LoadState.Loading) {
                SearchResultState.Loading
            } else {
                SearchResultState.Success(searchResult)
            }
        }
    }
}

sealed interface SearchUiAction {
    data class ErrorShown(val id: Long) : SearchUiAction
    data class OnTyping(val query: String) : SearchUiAction
    data class Search(val query: String) : SearchUiAction
    data class Scroll(
        val currentQuery: String,
        val totalItemCount: Int,
        val visibleItemCount: Int,
        val lastVisibleItemPosition: Int,
    ) : SearchUiAction
    data object ClearSearch : SearchUiAction
    data class NavigateToProfile(val userId: String) : SearchUiAction
    data class NavigateToPostDetail(val postId: String) : SearchUiAction
}

val SearchUiAction.Scroll.shouldFetchMore
    get() = visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount

sealed interface SearchUiEvent {
    data object ResetSearch : SearchUiEvent
    data object ScrollToTop : SearchUiEvent
    data class ShowSnack(val message: UiText) : SearchUiEvent
    data class ShowToast(val message: UiText) : SearchUiEvent
    data class NavigateToProfile(val userId: String, val isSelf: Boolean) : SearchUiEvent
    data class NavigateToPostDetail(val postId: String) : SearchUiEvent
}

sealed interface SearchResultUiModel {
    data class UserSearchResult(val query: String, val users: List<UserSummary>) : SearchResultUiModel
    data class PostSearchResult(val query: String, val posts: List<PostSummary>) : SearchResultUiModel

    data class Separator(val title: String) : SearchResultUiModel
    data class Footer(val title: UiText) : SearchResultUiModel
}

sealed interface SearchResultState {
    data object Idle : SearchResultState
    data object Loading : SearchResultState
    data class Success(val data: List<SearchResultUiModel>) : SearchResultState
}

private const val MIN_QUERY_LENGTH = 1
private const val VISIBLE_THRESHOLD = 4
private const val DEFAULT_QUERY: String = ""
private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"