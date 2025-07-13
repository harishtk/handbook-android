package com.handbook.app.navigation

import com.handbook.app.R
import com.handbook.app.core.designsystem.HandbookIcons

enum class NavigationDrawerDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val labelTextId: Int,
) {
    Parties(
        selectedIcon = HandbookIcons.Id_Group_Outline,
        unselectedIcon = HandbookIcons.Id_Group_Outline,
        iconTextId = R.string.parties,
        labelTextId = R.string.parties
    ),
    Category(
        selectedIcon = HandbookIcons.Id_Category_Outline,
        unselectedIcon = HandbookIcons.Id_Category_Outline,
        iconTextId = R.string.category,
        labelTextId = R.string.category
    ),
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