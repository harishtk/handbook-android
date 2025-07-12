package com.handbook.app.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.handbook.app.R
import com.handbook.app.ui.theme.HandbookTheme

enum class DialogActionType {
    NORMAL,
    DESTRUCTIVE
}

@Composable
fun CustomConfirmDialog(
    title: String = "Alert",
    description: String = "",
    confirmButtonText: String = stringResource(id = R.string.label_yes),
    dismissButtonText: String = stringResource(id = R.string.label_cancel),
    onDismiss: () -> Unit, // Simpler: just one callback for dismiss
    onConfirm: () -> Unit,
    confirmActionType: DialogActionType = DialogActionType.NORMAL,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface // Title color
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Description color
            )
        },
        confirmButton = {
            val confirmButtonColors = when (confirmActionType) {
                DialogActionType.NORMAL -> ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary // Normal action color
                )
                DialogActionType.DESTRUCTIVE -> ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error // Destructive action color
                )
            }
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss() // Often, confirm also dismisses the dialog
                },
                colors = confirmButtonColors
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant // Or primary if preferred
                )
            ) {
                Text(text = dismissButtonText)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        // You can also customize titleContentColor, textContentColor if needed
        // but styling the Text composables directly as above offers more control.
    )
}

@ThemePreviews
@Composable
private fun NormalConfirmDialogPreview() {
    HandbookTheme { // Ensure your preview is wrapped in your app's theme
        CustomConfirmDialog(
            title = "Confirm Action",
            description = "Are you sure you want to proceed with this normal action?",
            onDismiss = {},
            onConfirm = {},
            confirmActionType = DialogActionType.NORMAL
        )
    }
}

@ThemePreviews
@Composable
private fun DestructiveConfirmDialogPreview() {
    HandbookTheme {
        CustomConfirmDialog(
            title = "Delete Item?",
            description = "Are you sure you want to permanently delete this item? This action cannot be undone.",
            confirmButtonText = stringResource(id = R.string.label_delete), // Use specific text
            onDismiss = {},
            onConfirm = {},
            confirmActionType = DialogActionType.DESTRUCTIVE
        )
    }
}

// Add these to your strings.xml if they don't exist:
// <string name="label_yes">Yes</string>
// <string name="label_cancel">Cancel</string>
// <string name="label_delete">Delete</string>

