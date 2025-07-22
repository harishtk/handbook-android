package com.handbook.app.core.data.repository

import com.handbook.app.core.di.AppDependencies
import com.handbook.app.core.di.ApplicationCoroutineScope
import com.handbook.app.core.domain.repository.AuthSharedRepository
import com.handbook.app.core.domain.usecase.AuthenticationState
import com.handbook.app.core.util.AppForegroundObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultAuthSharedRepository @Inject constructor(
    @ApplicationCoroutineScope val applicationScope: CoroutineScope
) : AuthSharedRepository {

    private val _authenticationState = MutableStateFlow(AuthenticationState.NOT_AUTHENTICATED)

    override val authenticationState: StateFlow<AuthenticationState>
        = _authenticationState.asStateFlow()

    private var authExpirationJob: Job? = null

    init {
        AppDependencies.appForegroundObserver?.addListener(object : AppForegroundObserver.Listener {
            override fun onForeground() {
                authExpirationJob?.cancel(CancellationException("App is now visible"))
            }

            override fun onBackground() {
                authExpirationJob = applicationScope.launch {
                    delay(AUTH_TIMEOUT)
                    _authenticationState.update { AuthenticationState.NOT_AUTHENTICATED }
                }
            }
        })
    }

    override fun setAuthenticationState(state: AuthenticationState) {
        _authenticationState.update { state }
    }

    companion object {
        const val AUTH_TIMEOUT = 3600 * 1000L
    }
}