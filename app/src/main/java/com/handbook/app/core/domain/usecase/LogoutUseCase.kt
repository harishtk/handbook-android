package com.handbook.app.core.domain.usecase

import com.handbook.app.core.di.AppDependencies
import com.handbook.app.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * **CAUTION** This is test use case for demonstration, not written fully
 */
class LogoutUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        val userId = userDataRepository.userData.firstOrNull()?.userId ?: ""
        // database.clearAllTables()
        userDataRepository.setUserData(null) /* Logs out the user */
        // accountsRepository.logout(LogoutRequest(userId))
        AppDependencies.persistentStore?.logout()
    }
}