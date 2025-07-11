package com.handbook.app

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.domain.usecase.UserAuthStateUseCase
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.feature.home.navigation.createNavigationRoute
import com.handbook.app.feature.home.navigation.homeNavigationRoute
import com.handbook.app.feature.home.navigation.profileNavigationRoute
import com.handbook.app.feature.home.navigation.searchNavigationRoute
import timber.log.Timber
import javax.inject.Inject

/**
 * TODO: 1. Configure current destination with bottom bar state visibility
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
    private val userDataRepository: UserDataRepository,
    userAuthStateUseCase: UserAuthStateUseCase,
) : ViewModel() {

    private val _jumpToDestination = MutableSharedFlow<Int?>()
    val jumpToDestination = _jumpToDestination
        .filterNotNull()
        .shareIn(
            scope = viewModelScope,
            replay = 0,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun jumpToPage(@IdRes destinationId: Int?) = viewModelScope.launch {
        if (currentDestination.value != destinationId) {
            _jumpToDestination.emit(destinationId)
        }
    }

    /* Navigation current destination */
    val currentDestination: MutableStateFlow<Int> = MutableStateFlow(-1)

    fun setCurrentDestination(destinationId: Int) {
        currentDestination.update { destinationId }
    }

    /* Connection Listener */
    /*val isOffline =
        combine(
            networkMonitor.isOnline
                .map(Boolean::not),
            currentDestination
                .map { destinationId ->
                    destinationId !in noOfflineAlertDestinations
                },
            Boolean::and
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val shopsBottomAppBarState = currentDestination
        .map { destinationId ->
            val hidden = destinationId !in bottomBarDestinations
            HandbookBottomAppBarVisibilityState(
                hidden = hidden
            )
        }
        .distinctUntilChanged()
        .debounce {
            if (it.hidden) { 0 } else { 150 }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HandbookBottomAppBarVisibilityState.Default
        )*/

    /* Home feeds scroll to top signal */
    private val _scrollToTopSignal = MutableStateFlow(false)
    val scrollToTopSignal = _scrollToTopSignal.asStateFlow()

    fun setHomeScrollToTop(scrollToTop: Boolean) {
        _scrollToTopSignal.update { scrollToTop }
    }

    /* Feed refresh signal */
    private val _feedRefreshSignal = MutableSharedFlow<Unit>(replay = 0)
    val feedRefreshSignal = _feedRefreshSignal.asSharedFlow()

    fun setRefreshFeeds(refresh: Boolean) {
        viewModelScope.launch {
            if (refresh) {
                _feedRefreshSignal.emit(Unit)
            }
        }
    }

    private val _bottomBarReSelectSignal = MutableSharedFlow<Int>(replay = 0)
    /**
     * Used to get a callback on bottom bar item reselect
     */
    val bottomBarReselectSignal = _bottomBarReSelectSignal.asSharedFlow()

    fun setBottomBarReselected(id: Int) {
        Timber.d("Reselect: sending $id..")
        viewModelScope.launch { _bottomBarReSelectSignal.emit(id) }
    }
}

val bottomBarDestinations: List<String> =
    listOf(
        homeNavigationRoute,
        profileNavigationRoute,
        createNavigationRoute,
        searchNavigationRoute,
    )

val onboardDestinations: List<Int> = listOf()

val noOfflineAlertDestinations: List<String> = listOf()
