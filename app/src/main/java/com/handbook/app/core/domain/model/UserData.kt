package com.handbook.app.core.domain.model

data class UserData(
    val userId: String,
    val username: String,
    val profileName: String,
    val profileImage: String,
    val unreadNotificationCount: Int = 0,
    val shouldUpdateProfileOnce: Boolean = false,
    val onboardStep: String = "",
    val serverUnderMaintenance: Boolean = false,
    val lastGreetedTime: Long = 0,
    val shouldShowAppRating: Boolean = false,
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true,
    val isAppRatingShownAtLeastOnce: Boolean = false,
)