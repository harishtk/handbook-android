package com.handbook.app.core.util

import com.handbook.app.common.util.UiText

data class ErrorMessage(
    val id: Long,
    val exception: Throwable?,
    val message: UiText?
) {
    companion object {
        fun from(t: Throwable) = ErrorMessage(
            id = 0,
            exception = t,
            message = UiText.somethingWentWrong
        )

        fun unknown() = ErrorMessage(
            id = 0,
            exception = null,
            message = UiText.somethingWentWrong
        )
    }
}
