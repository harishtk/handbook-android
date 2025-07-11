package com.handbook.app.core.data.repository

import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.LoginUser
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.core.domain.model.UserData
import com.handbook.app.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class NoopUserDataRepository @Inject constructor(): UserDataRepository {
    /**
     * Stream of [UserData]
     */
    override val userData: Flow<UserData>
        get() = flowOf(
            UserData(
                userId = "dummy-id",
                username = "",
                profileName = "",
                profileImage = "",
                unreadNotificationCount = 0,
                shouldUpdateProfileOnce = false,
            )
        )

    /**
     * Set's the [LoginUser] of the currently logged user.
     */
    override suspend fun setUserData(userData: LoginUser?) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateUnreadNotificationCount(count: Int) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateUserPinCode(pinCode: String, areaName: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateProfileName(name: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateProfileThumb(imageName: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateProfileImage(imageName: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateCartCount(count: Int) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateVideoMuteStatus(muted: Boolean) {
        // TODO("Not yet implemented")
    }

    override suspend fun setShouldUpdateProfileOnce(shouldUpdate: Boolean) {
        // TODO("Not yet implemented")
    }

    override suspend fun setServerUnderMaintenance(underMaintenance: Boolean) {
        // TODO("Not yet implemented")
    }

    override suspend fun setLastGreetedTime(timestamp: Long) {
        // TODO("Not yet implemented")
    }

    override suspend fun setShowAppRating(show: Boolean) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateOnboardStep(onboardStep: String) {
        // TODO("Not yet implemented")
    }

    /**
     * Sets the desired theme brand.
     */
    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        // TODO("Not yet implemented")
    }

    /**
     * Sets the desired dark theme config.
     */
    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        // TODO("Not yet implemented")
    }

    /**
     * Sets the preferred dynamic color config.
     */
    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        // TODO("Not yet implemented")
    }
}