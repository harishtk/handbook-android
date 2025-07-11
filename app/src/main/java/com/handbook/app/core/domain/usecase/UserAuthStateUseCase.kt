package com.handbook.app.core.domain.usecase

import com.handbook.app.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class AuthenticationState {
    UNKNOWN, AUTHENTICATED, NOT_AUTHENTICATED;

    fun isAuthenticated(): Boolean {
        return this == AUTHENTICATED
    }
}

class UserAuthStateUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {

    operator fun invoke(): Flow<AuthenticationState> {
        return userDataRepository.userData
            .map {
                if (it.userId.isNotBlank()) {
                    AuthenticationState.AUTHENTICATED
                } else {
                    AuthenticationState.NOT_AUTHENTICATED
                }
            }
    }
}

