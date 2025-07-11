package com.handbook.app.core.designsystem.component

import androidx.compose.runtime.Stable

@Stable
data class HandbookBottomAppBarVisibilityState(
    val hidden: Boolean
) {
    companion object {
        internal val Default = HandbookBottomAppBarVisibilityState(
            hidden = true
        )
    }
}