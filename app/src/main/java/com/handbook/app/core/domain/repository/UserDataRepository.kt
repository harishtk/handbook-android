package com.handbook.app.core.domain.repository

import com.handbook.app.core.domain.model.LoginUser
import kotlinx.coroutines.flow.Flow
import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.core.domain.model.UserData

interface UserDataRepository {

    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /**
     * Set's the [LoginUser] of the currently logged user.
     */
    suspend fun setUserData(userData: LoginUser?)

    suspend fun updateUnreadNotificationCount(count: Int)

    suspend fun setServerUnderMaintenance(underMaintenance: Boolean)

    suspend fun setLastGreetedTime(timestamp: Long)

    suspend fun updateOnboardStep(onboardStep: String)

    /**
     * Sets the desired theme brand.
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Sets the preferred dynamic color config.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    // TODO: Add other keys here.
}