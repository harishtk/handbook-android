package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.handbook.app.core.designsystem.component.text.BaseTextFieldState
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.rememberTextFieldStateHandler
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

const val DisplayNameLength = 30

@Parcelize
class DisplayNameState(
    val initialValue: String = ""
) : BaseTextFieldState(initialValue), Parcelable {
    companion object {
        fun isValidName(name: String): Boolean {
            return name.isNotBlank() && name.length in 4..30
        }

        fun displayNameError(name: String): String {
            return when {
                name.isBlank() -> "Display name cannot be blank"
                name.length < 4 -> "Display name must be at least 4 characters"
                name.length > 30 -> "Display name must not exceed 30 characters"
                else -> ""
            }
        }

        @Composable
        fun createTextFieldStateHandler(value: String = "") = rememberTextFieldStateHandler(
            validator = ::isValidName,
            errorFor = ::displayNameError,
            initialState = { DisplayNameState(value) }
        )
    }
}