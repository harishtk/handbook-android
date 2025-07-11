package com.handbook.app.navigation

import com.handbook.app.R
import com.handbook.app.core.designsystem.HandbookIcons

enum class NavigationDrawerDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val labelTextId: Int,
) {
    Support(
        selectedIcon = HandbookIcons.Id_Help_Outline,
        unselectedIcon = HandbookIcons.Id_Help_Outline,
        iconTextId = R.string.support,
        labelTextId = R.string.support
    ),
    Settings(
        selectedIcon = HandbookIcons.Id_SettingsGear_Outline,
        unselectedIcon = HandbookIcons.Id_SettingsGear_Outline,
        iconTextId = R.string.settings,
        labelTextId = R.string.settings
    ),
}