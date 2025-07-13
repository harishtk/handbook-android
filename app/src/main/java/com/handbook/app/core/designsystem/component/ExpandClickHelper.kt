package com.handbook.app.core.designsystem.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

internal fun Modifier.expandable(
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    expandedDescription: String,
    collapsedDescription: String,
    toggleDescription: String,
) =
    pointerInput(onExpandedChange) {
        awaitEachGesture {
            // Modifier.clickable makes the ExposedDropdownMenuBox capture focus first instead
            // of the text field, which would be a confusing user experience, so we use
            // Modifier.pointerInput in the Initial pass to observe events before the text field
            // consumes them in the Main pass.
            val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) {
                onExpandedChange()
            }
        }
    }
        .onPreviewKeyEvent {
            // Make sure keyboard input works like if Modifier.clickable was set.
            if (it.isClick) {
               if (it.isEnterMinusSpacebar) {
                    // Primary editable shouldn't expand menu via spacebar.
                    // TODO: First menu item shouldn't have darker background before gaining focus.
                    onExpandedChange()
                    return@onPreviewKeyEvent true
                }
            }

            return@onPreviewKeyEvent false
        }
        .semantics {
            role = Role.Button
            stateDescription = if (expanded) expandedDescription else collapsedDescription
            contentDescription = toggleDescription
            onClick {
                onExpandedChange()
                true
            }
        }

private val KeyEvent.isClick: Boolean
    get() = type == KeyUp && (isEnterMinusSpacebar || key == Key.Spacebar)

private val KeyEvent.isEnterMinusSpacebar: Boolean
    get() =
        when (key) {
            Key.DirectionCenter,
            Key.Enter,
            Key.NumPadEnter -> true
            else -> false
        }