package com.handbook.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.core.domain.model.UserData
import com.handbook.app.core.domain.repository.AuthSharedRepository
import com.handbook.app.core.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    authSharedRepository: AuthSharedRepository,
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val userData = userDataRepository.userData
        .distinctUntilChanged { old, new ->
            old.userId == new.userId &&
                    old.darkThemeConfig == new.darkThemeConfig &&
                    old.useDynamicColor == new.useDynamicColor &&
                    old.themeBrand == new.themeBrand
        }

    val uiState: StateFlow<MainActivityUiState> = combine(
        userData,
        authSharedRepository.authenticationState,
        ::Pair
    )
        .map { (userData, authState) ->
            if (!(authState.isAuthenticated())) {
                MainActivityUiState.Login(userData)
            } else if (userData.serverUnderMaintenance) {
                MainActivityUiState.Maintenance
            } else {
                MainActivityUiState.Success(userData)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    @Deprecated("Not yet implemented")
    data object Maintenance : MainActivityUiState
    data class Login(val data: UserData) : MainActivityUiState
    data class Success(val data: UserData) : MainActivityUiState
}