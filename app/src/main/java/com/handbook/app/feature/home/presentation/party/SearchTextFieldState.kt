package com.handbook.app.feature.home.presentation.party

import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

class SearchTextFieldState(val initialText: String = "")
    : TextFieldState(validator = ::isValidQuery, errorFor = ::getQueryErrorFor) {

        init {
            text = initialText
        }
}

private fun isValidQuery(q: String): Boolean {
    return q.isNotBlank()
}

private fun getQueryErrorFor(q: String): String {
    return "Query cannot be empty"
}

val SearchTextFieldStateSaver = textFieldStateSaver(SearchTextFieldState())