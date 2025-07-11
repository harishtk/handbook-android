package com.handbook.app.core.designsystem.component.forms

import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

class ProductNameState(name: String)
    : TextFieldState(validator = ::isValidName) {
        init {
            text = name
        }
}

private fun productNameValidationError(name: String): String {
    return "Cannot contain special characters"
}

private fun isValidName(name: String): Boolean {
    return name.isNotBlank()
}

val ProductNameStateSaver = textFieldStateSaver(ProductNameState(""))