@file:OptIn(ExperimentalTime::class)

package com.handbook.app.feature.home.presentation.accounts // Or your screen's package

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.handbook.app.core.designsystem.component.expandable
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.SortOption
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.ui.theme.HandbookTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreenWithBottomSheetFilters(
    // This would likely come from a ViewModel
    activeFilters: AccountEntryFilters,
    onApplyFilters: (AccountEntryFilters) -> Unit,
    // Content of your screen
    screenContent: @Composable (PaddingValues) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    var temporaryFiltersInSheet by remember { mutableStateOf(TemporarySheetFilters()) }

    // When the sheet is opened, initialize temporaryFiltersInSheet from activeFilters
    LaunchedEffect(showFilterSheet, activeFilters) {
        if (showFilterSheet) {
            temporaryFiltersInSheet = TemporarySheetFilters( // Convert _root_ide_package_.com.handbook.app.feature.home.domain.model.AccountEntryFilters
                startDate = activeFilters.startDate,
                endDate = activeFilters.endDate,
                // Logic to find and set selectedDatePresetLabel if needed
                entryType = activeFilters.entryType,
                transactionType = activeFilters.transactionType,
                sortBy = activeFilters.sortBy,
                party = activeFilters.party
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Screen Title") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (activeFilters.count() > 0) {
                                Badge { Text("${activeFilters.count()}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Outlined.FilterAlt, contentDescription = "Open Filters")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        screenContent(paddingValues) // Your main screen content

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Rounded corners like image
                modifier = Modifier.fillMaxHeight(0.9f) // Adjust height as needed
            ) {
                FilterSheetContent(
                    temporaryFilters = temporaryFiltersInSheet,
                    onTemporaryFiltersChanged = { updatedFilters ->
                        temporaryFiltersInSheet = updatedFilters
                    },
                    onApply = {
                        onApplyFilters(AccountEntryFilters())
                        showFilterSheet = false
                    },
                    onResetAll = {
                        temporaryFiltersInSheet = TemporarySheetFilters(sortBy = temporaryFiltersInSheet.sortBy) // Keep sort or reset it too
                        // Optionally apply reset immediately:
                        // onApplyFilters(_root_ide_package_.com.handbook.app.feature.home.domain.model.AccountEntryFilters.None.copy(sortBy = temporaryFiltersInSheet.sortBy))
                        // showFilterSheet = false
                    },
                    onDismiss = { showFilterSheet = false },
                    onPartySelectRequest = { /* Handle party selection */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(
    temporaryFilters: TemporarySheetFilters,
    onTemporaryFiltersChanged: (TemporarySheetFilters) -> Unit,
    onApply: () -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit,
    onPartySelectRequest: (Party?) -> Unit,
) {
    // Date picker states
    val fromDatePickerState = rememberDatePickerState(initialSelectedDateMillis = temporaryFilters.startDate)
    var showFromDatePicker by remember { mutableStateOf(false) }
    val toDatePickerState = rememberDatePickerState(initialSelectedDateMillis = temporaryFilters.endDate)
    var showToDatePicker by remember { mutableStateOf(false) }

    // Update temporaryFilters when date picker selection changes
    LaunchedEffect(fromDatePickerState.selectedDateMillis) {
        fromDatePickerState.selectedDateMillis?.let {
            onTemporaryFiltersChanged(
                temporaryFilters.copy(startDate = it, selectedDatePresetLabel = null) // Clear preset if custom date chosen
            )
        }
    }
    LaunchedEffect(toDatePickerState.selectedDateMillis) {
        toDatePickerState.selectedDateMillis?.let {
            onTemporaryFiltersChanged(
                temporaryFilters.copy(endDate = it, selectedDatePresetLabel = null)
            )
        }
    }

    val datePresets = remember { generateSheetDatePresets() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp) // Adjusted padding
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Reduced vertical padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter by:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)) // Adjusted style
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close Filters")
            }
        }

        // --- Scrollable Filter Sections ---
        Column(Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())) {
            // --- Date Range Section ---
            SheetFilterSection(
                title = "Date Range",
                onReset = {
                    onTemporaryFiltersChanged(
                        temporaryFilters.copy(startDate = null, endDate = null, selectedDatePresetLabel = null)
                    )
                    fromDatePickerState.selectedDateMillis = null
                    toDatePickerState.selectedDateMillis = null
                }
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetDateInputField(
                        label = "From",
                        dateMillis = temporaryFilters.startDate,
                        onClick = { showFromDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    SheetDateInputField(
                        label = "To",
                        dateMillis = temporaryFilters.endDate,
                        onClick = { showToDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp)) // Reduced spacer
                Row(
                    modifier = Modifier.fillMaxWidth(), // No horizontal scroll, let them wrap or adjust size
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally) // Space presets
                ) {
                    datePresets.forEach { preset ->
                        val isSelected = temporaryFilters.selectedDatePresetLabel == preset.label
                        SuggestionChip(
                            onClick = {
                                onTemporaryFiltersChanged(
                                    temporaryFilters.copy(
                                        startDate = preset.startDateMillis,
                                        endDate = preset.endDateMillis,
                                        selectedDatePresetLabel = preset.label
                                    )
                                )
                                fromDatePickerState.selectedDateMillis = preset.startDateMillis
                                toDatePickerState.selectedDateMillis = preset.endDateMillis
                            },
                            label = { Text(preset.label) },
                        )
                    }
                }
            }
            SheetDivider()

            // --- Entry Type Section ---
            SheetFilterSection(
                title = "Entry Type",
                onReset = { onTemporaryFiltersChanged(temporaryFilters.copy(entryType = null)) }
            ) {
                EntryTypeDropdown(
                    selectedType = temporaryFilters.entryType,
                    onTypeSelected = {
                        onTemporaryFiltersChanged(temporaryFilters.copy(entryType = it))
                    }
                )
            }
            SheetDivider()

            // --- Transaction Type Section ---
            SheetFilterSection(
                title = "Transaction Type",
                onReset = { onTemporaryFiltersChanged(temporaryFilters.copy(transactionType = null)) }
            ) {
                TransactionTypeDropdown(
                    selectedType = temporaryFilters.transactionType,
                    onTypeSelected = {
                        onTemporaryFiltersChanged(temporaryFilters.copy(transactionType = it))
                    }
                )
            }
            SheetDivider()

            // --- Party Section ---
            SheetFilterSection(
                title = "Party",
                onReset = { onTemporaryFiltersChanged(temporaryFilters.copy(party = null)) }
            ) {
                PartyDropdown(
                    selectedParty = temporaryFilters.party,
                    onPartySelectRequest = {
                        onPartySelectRequest(it)
                    }
                )
            }

            // --- Sort By Section (Example) ---
//            SheetFilterSection(title = "Sort by", showResetButton = false) { // Sort might not need a "reset" in the same way
//                SortByDropdown(
//                    selectedSortOption = temporaryFilters.sortBy ?: SortOption.NEWEST_FIRST,
//                    onSortOptionSelected = {
//                         onTemporaryFiltersChanged(temporaryFilters.copy(sortBy = it))
//                    }
//                )
//            }
            // Add other sections like EntryType, TransactionType if needed
            // SheetDivider()
            // SheetFilterSection(title = "Entry Type", onReset = { /* ... */}) { /* ... */ }
        }

        Spacer(Modifier.height(16.dp)) // Reduced spacer

        // --- Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onResetAll,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp) // Compact button
            ) {
                Text("Reset All")
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                enabled = temporaryFilters.countSelections() > 0,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp) // Compact button
            ) {
                Text("Apply Filters (${temporaryFilters.countSelections()})")
            }
        }
    }

    // --- Date Picker Dialogs ---
    if (showFromDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = { TextButton(onClick = { showFromDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showFromDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = fromDatePickerState) }
    }
    if (showToDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = { TextButton(onClick = { showToDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showToDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = toDatePickerState) }
    }
}


@Composable
fun SheetFilterSection(
    title: String,
    showResetButton: Boolean = true,
    onReset: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) { // Reduced vertical padding
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium) // Adjusted style
            if (showResetButton && onReset != null) {
                TextButton(onClick = onReset, modifier = Modifier
                    .heightIn(min = 28.dp)
                    .padding(horizontal = 0.dp), contentPadding = PaddingValues(horizontal = 4.dp)) { // Compact reset
                    Text("Reset", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(6.dp)) // Reduced spacer
        content()
    }
}

@Composable
fun SheetDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp), // Reduced vertical padding
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDateInputField(
    label: String,
    dateMillis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) } // Adjust format as needed
    OutlinedTextField(
        value = dateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
        onValueChange = { /* Read only */ },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.CalendarToday, "Select date", Modifier.clickable(onClick = onClick)) },
        modifier = modifier.clickable(onClick = onClick), // Make the whole field clickable
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium, // Compact text
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,        )
    )
}

// --- Dropdown Composables for EntryType, TransactionType, SortOption ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryTypeDropdown(
    selectedType: EntryType?,
    onTypeSelected: (EntryType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember { EntryType.entries.toList() }
    val selectedLabel = selectedType?.name?.replace("_", " ")?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    } ?: "Select Entry Type"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Entry Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium // Compact text
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Any Entry Type") },
                onClick = {
                    onTypeSelected(null)
                    expanded = false
                }
            )
            items.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeDropdown(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = remember { TransactionType.entries.toList() }
    val selectedLabel = selectedType?.name?.replace("_", " ")?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    } ?: "Select Transaction Type"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Transaction Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium // Compact text
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Any Transaction Type") },
                onClick = {
                    onTypeSelected(null)
                    expanded = false
                }
            )
            items.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDropdown(
    selectedParty: Party?,
    onPartySelectRequest: (Party?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLabel = selectedParty?.name ?: "Select Party"
    Box(
        modifier = modifier
            .expandable(
                expanded = true,
                onExpandedChange = {
                    onPartySelectRequest(selectedParty)
                },
                expandedDescription = "Show party picker",
                collapsedDescription = "Hide party picker",
                toggleDescription = "Toggle party picker"
            )
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Party") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
            modifier = Modifier
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium // Compact text
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByDropdown(selectedSortOption: SortOption, onSortOptionSelected: (SortOption) -> Unit) {
    // Similar implementation
    var expanded by remember { mutableStateOf(false) }
    val items = remember { SortOption.entries.toList() }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSortOption.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyMedium // Compact text
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }) },
                    onClick = {
                        onSortOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


// --- Preview ---
@Preview(showBackground = true, heightDp = 700)
@Composable
fun MyScreenWithBottomSheetFiltersPreview() {
    var activeFilters by remember { mutableStateOf(AccountEntryFilters.None) }
    HandbookTheme {
        MyScreenWithBottomSheetFilters(
            activeFilters = activeFilters,
            onApplyFilters = {
                activeFilters = it
                println("Applied Filters: $it")
            },
            screenContent = { pv ->
                Box(
                    Modifier
                        .padding(pv)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Main screen content here.\nActive Filters: ${activeFilters.count()}")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterSheetContent_Preview() {
    var temporaryFilters by remember { mutableStateOf(TemporarySheetFilters(selectedDatePresetLabel = "Today")) }
     HandbookTheme {
        Surface(modifier = Modifier.fillMaxHeight()) { // Added surface for better preview of sheet content
            FilterSheetContent(
                temporaryFilters = temporaryFilters,
                onTemporaryFiltersChanged = { temporaryFilters = it},
                onApply = {},
                onResetAll = { temporaryFilters = TemporarySheetFilters() },
                onDismiss = {},
                onPartySelectRequest = {}
            )
        }
    }
}
