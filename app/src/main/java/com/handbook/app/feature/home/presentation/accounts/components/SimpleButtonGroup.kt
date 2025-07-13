package com.handbook.app.feature.home.presentation.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.handbook.app.ui.theme.HandbookTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SimpleButtonGroup(
    modifier: Modifier = Modifier,
    label: String = "Select an Option",
    options: List<String>,
    unCheckedIcons: List<ImageVector>,
    checkedIcons: List<ImageVector>,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
) {
    FlowRow(
        Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedOptionIndex
            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onOptionSelected(index) },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                contentPadding = ButtonDefaults.contentPaddingFor(36.dp)
            ) {
                Icon(
                    if (isSelected) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview(showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "components")
@Composable
private fun SimpleButtonGroupPreview() {
    val options = listOf("Work", "Restaurant", "Coffee")
    val unCheckedIcons =
        listOf(Icons.Outlined.Work, Icons.Outlined.Restaurant, Icons.Outlined.Coffee)
    val checkedIcons = listOf(Icons.Filled.Work, Icons.Filled.Restaurant, Icons.Filled.Coffee)
    var selectedIndex by remember { mutableIntStateOf(0) }

    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SimpleButtonGroup(
                options = options,
                unCheckedIcons = unCheckedIcons,
                checkedIcons = checkedIcons,
                selectedOptionIndex = selectedIndex,
                onOptionSelected = { selectedIndex = it }
            )
            Spacer(Modifier.height(16.dp))
            Text("Selected: ${options[selectedIndex]}")
        }
    }
}