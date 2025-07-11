package com.handbook.app.feature.home.presentation.create

import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

class PostContentState(initialValue: String = "")
    : TextFieldState(validator = ::isValidContent, errorFor = ::contentErrorFor) {
    init {
        text = initialValue
    }
}

private fun isValidContent(content: String): Boolean {
    return content.isNotBlank() && content.length in 10..280
}

private fun contentErrorFor(content: String): String {
    return when {
        content.isBlank() -> "Post cannot be empty"
        content.length < 10 -> "Content too short"
        content.length > 280 -> "Content too long"
        else -> ""
    }
}

val PostContentStateSaver = textFieldStateSaver(PostContentState())