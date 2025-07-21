package com.handbook.app.feature.home.presentation.party.components.form

import android.os.Parcelable
import androidx.compose.runtime.Composable
import com.handbook.app.core.designsystem.component.text.BaseTextFieldState
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.rememberTextFieldStateHandler
import com.handbook.app.core.designsystem.component.text.textFieldStateSaver
import kotlinx.parcelize.Parcelize

const val DescriptionLength = 280

@Parcelize
class DescriptionState(val initialValue: String)
    : BaseTextFieldState(initialValue), Parcelable {

        companion object {
            private fun isValidDescription(description: String): Boolean {
                return if (description.isNotEmpty()) {
                    description.length <= 280
                } else {
                    true
                }
            }

            private fun descriptionError(description: String): String {
                return when {
                    description.length > DescriptionLength -> "Description cannot exceed $DescriptionLength characters"
                    else -> ""
                }
            }

            @Composable
            fun createTextFieldStateHandler(value: String = "") = rememberTextFieldStateHandler(
                validator = ::isValidDescription,
                errorFor = ::descriptionError,
                initialState = { DescriptionState(value) }
            )
        }
}
