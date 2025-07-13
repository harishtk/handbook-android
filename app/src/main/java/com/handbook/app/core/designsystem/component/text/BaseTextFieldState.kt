package com.handbook.app.core.designsystem.component.text

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.parcelize.Parcelize

// Composable helper to create and remember the handler and its state
@Composable
fun <T : BaseTextFieldState> rememberTextFieldStateHandler(
    initialState: () -> T, // Lambda to create the initial BaseTextFieldState
    validator: (String) -> Boolean,
    errorFor: (String) -> String
): TextFieldStateHandler<T> {
    // The actual data state is saved and restored
    val savedState = rememberSaveable { initialState() }

    // The handler is remembered but re-created if its keys (validator, errorFor) change.
    // It always operates on the up-to-date savedState.
    return remember(savedState, validator, errorFor) {
        TextFieldStateHandler(
            state = savedState,
            validator = validator,
            errorFor = errorFor
        )
    }
}

@Parcelize
open class BaseTextFieldState(
    val initialText: String = "",
    val initialIsFocusedDirty: Boolean = false,
) : Parcelable {
    var text: String by mutableStateOf(initialText)
    var isFocusedDirty: Boolean by mutableStateOf(initialIsFocusedDirty)

    @delegate:Transient
    var isFocused: Boolean by mutableStateOf(false)

    @delegate:Transient
    var displayErrors: Boolean by mutableStateOf(false)
}

@Immutable
class TextFieldStateHandler<T : BaseTextFieldState>(
    val state: T,
    private val validator: (String) -> Boolean,
    private val errorFor: (String) -> String,
) {
    val text: String
        get() = state.text

    val isFocused: Boolean
        get() = state.isFocused

    // Derived UI State
    val isValid: Boolean
        get() = validator(state.text)

    private val shouldShowErrors: Boolean
        get() = !isValid && state.displayErrors

    val errorMessage: String?
        get() = if (shouldShowErrors) errorFor(state.text) else null

    // UI Actions
    fun onTextChanged(newText: String) {
        state.text = newText
    }

    fun onFocusChanged(focused: Boolean) {
        state.isFocused = focused
        if (focused) {
            state.isFocusedDirty = true
        } else {
            // When focus is lost, if it was dirty, enable error display
            if (state.isFocusedDirty) {
                state.displayErrors = true
            }
        }
    }

    fun updateText(newText: String) {
        state.text = newText
    }

    fun enableShowErrors() {
        // only show errors if the text was at least once focused
        if (state.isFocusedDirty) {
           state.displayErrors = true
        }
    }

    fun showErrors() = !isValid && state.displayErrors

}