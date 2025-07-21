package com.handbook.app.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.HandbookTopAppBarState
import com.handbook.app.ui.theme.HandbookTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandbookSimpleTopAppBar(
    modifier: Modifier = Modifier,
    state: HandbookTopAppBarState,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        navigationIcon = {
            if (state.showNavigationIcon) {
                IconButton(onClick = state.onNavigationIconClick ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
            }
        },
        actions = {
            if (state.showMoreOptions) {
                IconButton(onClick = state.onMoreOptionsClick) {
                    Icon(
                        imageVector = HandbookIcons.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    )
}

@Composable
// @Preview(device = "id:pixel_3a", showBackground = true)
@ThemePreviews
private fun BestDealsTopAppBarPreview() {
    val appBarState = HandbookTopAppBarState(
        title = "Sample Title",
        showNavigationIcon = true,
        onNavigationIconClick = {},
        showMoreOptions = true,
        onMoreOptionsClick = {}
    )

    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = false
    ) {
        HandbookSimpleTopAppBar(state = appBarState)
    }
}