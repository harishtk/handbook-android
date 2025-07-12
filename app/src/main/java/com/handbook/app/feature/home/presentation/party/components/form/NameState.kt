package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

const val DisplayNameLength = 30

@Parcelize
class DisplayNameState(
    val initialValue: String = ""
) : TextFieldState(validator = ::isValidName, errorFor = ::displayNameError), Parcelable {

    init {
        text = initialValue
    }
}

private fun isValidName(name: String): Boolean {
    return name.isNotBlank() && name.length in 4..30
}

private fun displayNameError(name: String): String {
    return when {
        name.isBlank() -> "Display name cannot be blank"
        name.length < 4 -> "Display name must be at least 4 characters"
        name.length > 30 -> "Display name must not exceed 30 characters"
        else -> ""
    }
}

val DisplayNameStateSaver = textFieldStateSaver(DisplayNameState())