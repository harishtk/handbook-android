package com.handbook.app.core.designsystem.component.forms

import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver

class ProductPriceState(price: String)
    : TextFieldState() {
        init {
            text = price
        }
    }

private fun productPriceValidationError(): String {
    return "Price Error"
}

private fun isValidPrice(price: String): Boolean {
    return price.isNotBlank()
}

val ProductPriceStateSaver = textFieldStateSaver(ProductPriceState(""))