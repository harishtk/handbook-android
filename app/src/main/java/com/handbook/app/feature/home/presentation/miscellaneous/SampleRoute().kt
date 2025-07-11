package com.handbook.app.feature.home.presentation.miscellaneous

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
internal fun SampleRoute(
    modifier: Modifier = Modifier,
    title: String = "Sample"
) {

    SampleScreen(
        modifier = modifier,
        titleProvider = { title }
    )
}

@Composable
internal fun SampleScreen(
    modifier: Modifier = Modifier,
    titleProvider: () -> String,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = titleProvider(), style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun SampleDefaultPreview() {
    HandbookTheme {
        SampleScreen(
            titleProvider = { "Sample" }
        )
    }
}