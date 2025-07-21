package com.handbook.app.core.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import com.handbook.app.common.util.UiText

@Stable
class HandbookTopAppBarState(
    val title: String,
    val showNavigationIcon: Boolean = false,
    val onNavigationIconClick: () -> Unit = {},
    val showMoreOptions: Boolean = false,
    val onMoreOptionsClick: () -> Unit = {},
)
