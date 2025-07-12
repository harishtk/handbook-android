package com.handbook.app.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.handbook.app.R
import com.handbook.app.ui.theme.HandbookTheme

@Composable
internal fun ConfirmBackPressDialog(
    title: String = "Alert",
    description: String = "",
    onDismiss: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss(0) },
        confirmButton = {
            TextButton(onClick = { onDismiss(1) }) {
                Text(text = stringResource(id = R.string.label_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(0) }) {
                Text(text = stringResource(id = R.string.label_cancel))
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )

}

@ThemePreviews
@Composable
private fun ConfirmBackPressDialogPreview() {
    HandbookTheme(androidTheme = true) {
        ConfirmBackPressDialog(
            description = "Confirm go back?",
            onDismiss = {}
        )
    }
}
