package com.handbook.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.handbook.app.R
import com.handbook.app.ui.theme.HandbookTheme

@Composable
private fun DialogContent(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LoadingDialog(
    isShowingDialog: Boolean,
    title: String = stringResource(id = R.string.please_wait),
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = true,
) {
    if (isShowingDialog) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside
            )
        ) {
            DialogContent(title)
        }
    }
}

@Composable
@Preview(device = "id:pixel_5", showBackground = true)
fun LoadingDialogPreview() {
    BoxWithConstraints(
        Modifier.background(Color.White)
    ) {
        HandbookTheme {
            LoadingDialog(isShowingDialog = true)
        }
    }
}