package com.handbook.app.feature.home.presentation.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import com.handbook.app.common.util.UiText

@Stable
data class SettingsItem(
    val settingsListType: SettingsListType,
    val id: String,
    val title: UiText,
    @DrawableRes val icon: Int?,
    val description: UiText?,
    val hasMore: Boolean = false
)

enum class SettingsListType {
    SIMPLE
}