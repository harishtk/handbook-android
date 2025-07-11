package com.handbook.app.feature.home.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.handbook.app.ui.theme.HandbookTheme

@Composable
internal fun CreateRoute(
    modifier: Modifier = Modifier,
) {

    CreateScreen(
        modifier = modifier,
    )
}

@Composable
internal fun CreateScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Create", style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun CreateDefaultPreview() {
    HandbookTheme {
        CreateScreen()
    }
}