package com.handbook.app.core.domain.model

data class UserData(
    val userId: String,
    val unreadNotificationCount: Int = 0,
    val onboardStep: String = "",
    val serverUnderMaintenance: Boolean = false,
    val lastGreetedTime: Long = 0,
    val themeBrand: ThemeBrand = ThemeBrand.ANDROID,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = false,
)