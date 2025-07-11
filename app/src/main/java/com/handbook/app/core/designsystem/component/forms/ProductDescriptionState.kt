package com.handbook.app.core.designsystem.component.forms

import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

class ProductDescriptionState(val description: String)
    : TextFieldState() {
        init {
            text = description
        }
}

private fun productDescriptionValidationError(): String {
    return "Description Error"
}

private fun isValidDescription(description: String): Boolean {
    return description.isNotBlank()
}

val ProductDescriptionStateSaver = textFieldStateSaver(ProductDescriptionState(""))