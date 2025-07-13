package com.handbook.app.core.designsystem.component.text

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import kotlinx.parcelize.Parcelize

@Parcelize
open class TextFieldState(
    private val validator: (String) -> Boolean = { true },
    private val errorFor: (String) -> String = { "" },
) : Parcelable {
    var text: String by mutableStateOf("")
    // was the TextField ever focused?
    var isFocusedDirty: Boolean by mutableStateOf(false)
    var isFocused: Boolean by mutableStateOf(false)
    private var displayErrors: Boolean by mutableStateOf(false)

    open val isValid: Boolean
        get() = validator(text)

    fun onFocusChange(focused: Boolean) {
        isFocused = focused
        if (focused) isFocusedDirty = true
    }

    fun enableShowErrors() {
        // only show errors if the text was at least once focused
        if (isFocusedDirty) {
            displayErrors = true
        }
    }

    fun showErrors() = !isValid && displayErrors

    open fun getError(): String? {
        return if (showErrors()) {
            errorFor(text)
        } else {
            null
        }
    }

    fun updateText(newText: String) {
        text = newText
        enableShowErrors()
    }
}

fun textFieldStateSaver(state: TextFieldState) = listSaver<TextFieldState, Any>(
    save = { listOf(it.text, it.isFocusedDirty) },
    restore = {
        state.apply {
            text = it[0] as String
            isFocusedDirty = it[1] as Boolean
        }
    }
)

fun createTextFieldStateSaver(): Saver<TextFieldState, Any> = listSaver(
    save = { textFieldState ->
        listOf(
            textFieldState.text,
            textFieldState.isFocusedDirty
            // Save other TextFieldState properties if they exist and need saving
            // e.g., textFieldState.isFocused, textFieldState.displayErrors
        )
    },
    restore = { restoredList ->
        // Create a NEW TextFieldState instance
        val restoredState = TextFieldState(
            // Assuming TextFieldState has appropriate constructor parameters
            // or default values if not all are being restored.
            // You might need to adjust this based on TextFieldState's constructor.
            // For simplicity, let's assume it has a default constructor and we set properties.
        )
        restoredState.text = restoredList[0] as String
        restoredState.isFocusedDirty = restoredList[1] as Boolean
        // Restore other properties if saved
        // restoredState.isFocused = restoredList[2] as Boolean
        // restoredState.displayErrors = restoredList[3] as Boolean

        restoredState // IMPORTANT: Return the new instance
    }
)