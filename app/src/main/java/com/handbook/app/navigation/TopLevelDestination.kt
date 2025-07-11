package com.handbook.app.navigation

import com.handbook.app.R
import com.handbook.app.core.designsystem.HandbookIcons

/**
 * Type for top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val iconTextId: Int,
    val titleTextId: Int?,
) {
    HOME(
        selectedIcon = HandbookIcons.Id_Home_Filled,
        unselectedIcon = HandbookIcons.Id_Home_Outline,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
    ),
    SEARCH(
        selectedIcon = HandbookIcons.Id_Search_Outline,
        unselectedIcon = HandbookIcons.Id_Search_Outline,
        iconTextId = R.string.search,
        titleTextId = R.string.search,
    ),
    PROFILE(
        selectedIcon = HandbookIcons.Id_Admin_Outline,
        unselectedIcon = HandbookIcons.Id_Admin_Outline,
        iconTextId = R.string.profile,
        titleTextId = R.string.profile,
    ),
}