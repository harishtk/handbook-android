package com.handbook.app.core.util

import com.handbook.app.common.util.UiText

data class ValidationResult(
    val typedValue: String, /* for StateFlow to recognize different value */
    val successful: Boolean = false,
    val errorMessage: UiText? = null
)