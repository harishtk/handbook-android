package com.handbook.app.feature.home.presentation.accounts.components

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.handbook.app.ui.theme.HandbookTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SimpleDropDownPicker(
    modifier: Modifier = Modifier,
    label: String = "Select an Option",
    options: List<String>,
    selectedOption: String,
    selectedOptionContent: @Composable () -> Unit = {},
    onOptionSelected: (String) -> Unit,
    dropDownContentForOption: @Composable (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
//        OutlinedTextField(
//            value = selectedOption,
//            onValueChange = {},
//            readOnly = true,
//            label = { Text(label) },
//            trailingIcon = {
//                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//            },
//            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
//                focusedContainerColor = MaterialTheme.colorScheme.surface,
//                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
//                focusedBorderColor = MaterialTheme.colorScheme.primary,
//                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
//            ),
//            modifier = Modifier
//                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
//                .fillMaxWidth()
//        )
        BasicTextField(
            value = selectedOption, // Or a TextFieldValue
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = true
                )
                .fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = {
                // Mimic OutlinedTextField's structure
                // This is a simplified version. Real OutlinedTextField has complex label logic, padding, etc.
                Box(
                    Modifier
                        .border(
                            width = 1.dp, // TextFieldDefaults.UnfocusedIndicatorThickness,
                            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.extraSmall // Or TextFieldDefaults.shape
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Adjust padding
                ) {
                    // Simplified Label (doesn't float, always above or placeholder like)
                    if (selectedOption.isEmpty()) {
                        Text(
                            text = label,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (selectedOption.isNotEmpty()) {
                            selectedOptionContent()
                        } else {
                            // Spacer to push trailing icon to the end if no content and no label shown here
                            Spacer(Modifier.weight(1f))
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize(matchAnchorWidth = true)
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        dropDownContentForOption(selectionOption)
                    },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview(showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "components")
@Composable
private fun SimpleDropDownPickerPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf(options[0]) }

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
            SimpleDropDownPicker(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = {
                    selectedOption = it
                },
                dropDownContentForOption = {
                    Text(it)
                }
            )
            Spacer(Modifier.height(16.dp))
            Text("Selected: $selectedOption")
        }
    }
}