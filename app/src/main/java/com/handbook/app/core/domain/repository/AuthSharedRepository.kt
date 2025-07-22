package com.handbook.app.core.domain.repository

import com.handbook.app.core.domain.usecase.AuthenticationState
import kotlinx.coroutines.flow.StateFlow

interface AuthSharedRepository {

    val authenticationState: StateFlow<AuthenticationState>

    fun setAuthenticationState(state: AuthenticationState)
}