package com.handbook.app.feature.home.presentation.accounts.components.form

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.core.text.isDigitsOnly
import com.handbook.app.core.designsystem.component.text.BaseTextFieldState
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.rememberTextFieldStateHandler
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

@Parcelize
class AmountState(val initialValue: String = "")
    : BaseTextFieldState(initialValue), Parcelable {
        companion object {
            private fun isValidAmount(amt: String): Boolean {
                if (!amt.matches(Regex("^-?\\d*\\.?\\d+$"))) return false
                return (amt.toDoubleOrNull() ?: 0.0) in 1.0..1000000000.0
            }

            private fun errorFor(amt: String): String {
                return when {
                    amt.isBlank() -> "Amount cannot be blank"
                    !isValidAmount(amt) -> "Enter a valid amount"
                    else -> ""
                }
            }

            @Composable
            fun createTextFieldStateHandler(value: String = "") = rememberTextFieldStateHandler(
                validator = ::isValidAmount,
                errorFor = ::errorFor,
                initialState = { AmountState(value) }
            )
        }
}