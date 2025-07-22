package com.handbook.app.feature.onboard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.core.domain.repository.AuthSharedRepository
import com.handbook.app.core.domain.usecase.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authSharedRepository: AuthSharedRepository
) : ViewModel() {

    val uiState: StateFlow<LoginUiState> = authSharedRepository.authenticationState
        .map { authenticationState ->
            if (authenticationState.isAuthenticated()) {
                LoginUiState.LoginSuccess
            } else {
                LoginUiState.LoginRequired
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginUiState.LoginRequired
        )

    fun onLoginSuccess() {
        authSharedRepository.setAuthenticationState(AuthenticationState.AUTHENTICATED)
    }
}

sealed interface LoginUiState {
    data object LoginRequired : LoginUiState
    data object LoginSuccess : LoginUiState
}
