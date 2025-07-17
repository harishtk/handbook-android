@file:OptIn(ExperimentalTime::class)

package com.handbook.app.feature.home.presentation.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.ui.theme.HandbookTheme
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Data class for representing a specific date range filter option
data class DateRangeFilter(
    val startDateMillis: Long,
    val endDateMillis: Long,
    val displayLabel: String // e.g., "Today", "Mon"
)

//region Main FilterBar Composable
@Composable
fun FilterBar(
    currentFilters: AccountEntryFilters,
    onFiltersChanged: (AccountEntryFilters) -> Unit,
    modifier: Modifier = Modifier,
    // Example: For dynamic filters like categories/parties, you might pass them as parameters
    // allCategories: List<FilterChipOption<Long>> = emptyList(),
    // allParties: List<FilterChipOption<Long>> = emptyList()
) {
    val dateFilterOptions = remember { generatePast7DaysDateOptions() }

    // Static lists for Enums - In a real app, dynamic lists (categories, parties)
    // might be fetched and mapped to FilterChipOption in a ViewModel or passed as parameters.
    val allEntryTypes: List<EntryType> = remember { EntryType.entries.toList() }
    val entryTypeOptions = remember(allEntryTypes) {
        allEntryTypes.map { entryType ->
            FilterChipOption(
                label = entryType.name.replace("_", " ").lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                value = entryType
            )
        }
    }

    val allTransactionTypes: List<TransactionType> = remember { TransactionType.entries.toList() }
    val transactionTypeOptions = remember(allTransactionTypes) {
        allTransactionTypes.map { transactionType ->
            FilterChipOption(
                label = transactionType.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                value = transactionType
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // --- Date Filters ---
        DateFilterChips(
            dateOptions = dateFilterOptions,
            currentFilters = currentFilters,
            onDateRangeSelected = { startDate, endDate ->
                onFiltersChanged(
                    currentFilters.copy(
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp)) // Spacing between filter groups

        // --- Entry Type Filters ---
        GenericFilterChipRow(
            title = "Entry Type",
            options = entryTypeOptions,
            selectedSingleValue = currentFilters.entryType,
            onOptionSelected = { selectedEntryType ->
                onFiltersChanged(currentFilters.copy(entryType = selectedEntryType))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Transaction Type Filters ---
        GenericFilterChipRow(
            title = "Transaction Type",
            options = transactionTypeOptions,
            selectedSingleValue = currentFilters.transactionType,
            onOptionSelected = { selectedTransactionType ->
                onFiltersChanged(currentFilters.copy(transactionType = selectedTransactionType))
            }
        )

        // TODO: Add more GenericFilterChipRow sections for other filter types
        // e.g., for Categories, Parties.
        // If these options are dynamic (e.g., from a database):
        // 1. Fetch them in your ViewModel.
        // 2. Map them to List<FilterChipOption<YourIdType>>.
        // 3. Pass this list to the FilterBar composable as a parameter.
        // 4. Use it in another GenericFilterChipRow.
        /*
        if (allCategories.isNotEmpty()) { // Assuming allCategories is List<FilterChipOption<Long>>
            Spacer(modifier = Modifier.height(12.dp))
            GenericFilterChipRow(
                title = "Category",
                options = allCategories,
                selectedSingleValue = currentFilters.categoryId,
                onOptionSelected = { selectedCategoryId ->
                    onFiltersChanged(currentFilters.copy(categoryId = selectedCategoryId))
                }
            )
        }
        */
    }
}
//endregion

/**
 * Composable that displays a row of filter chips for selecting a date range
 * from the past 7 days, including today.
 */
@Composable
fun DateFilterChips(
    currentFilters: AccountEntryFilters,
    onDateRangeSelected: (startDateMillis: Long?, endDateMillis: Long?) -> Unit,
    modifier: Modifier = Modifier,
    dateOptions: List<DateRangeFilter> = remember { generatePast7DaysDateOptions() } // Allow pre-generated options
) {
    // Determine the initially selected index based on currentFilters
    var selectedIndex by remember(currentFilters.startDate, currentFilters.endDate, dateOptions) {
        mutableStateOf(
            dateOptions.indexOfFirst {
                it.startDateMillis == currentFilters.startDate && it.endDateMillis == currentFilters.endDate
            }.takeIf { it != -1 } // Result is -1 if not found, takeIf makes it null
        )
    }

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex ?: -1,
        modifier = modifier.fillMaxWidth(),
        edgePadding = 8.dp, // Still applicable and useful
        indicator = { /* No indicator by default for chip-like style */ },
        divider = { /* No divider by default */ }
    ) {
        dateOptions.forEachIndexed { index, dateOption ->
            FilterChipTab(
                text = dateOption.displayLabel,
                selected = selectedIndex == index,
                onClick = {
                    if (selectedIndex == index) {
                        selectedIndex = null
                        onDateRangeSelected(null, null)
                    } else {
                        selectedIndex = index
                        onDateRangeSelected(dateOption.startDateMillis, dateOption.endDateMillis)
                    }
                },
            )
        }
    }
}

/**
 * Generates a list of [DateRangeFilter] options for the past 7 days, including today.
 */
fun generatePast7DaysDateOptions(): List<DateRangeFilter> {
    val options = mutableListOf<DateRangeFilter>()
    val systemTimeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTimeZone).date

    // Today
    options.add(
        DateRangeFilter(
            startDateMillis = today.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone)
                .toEpochMilliseconds() - 1,
            displayLabel = "Today"
        )
    )

    // Yesterday
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    options.add(
        DateRangeFilter(
            startDateMillis = yesterday.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = yesterday.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone)
                .toEpochMilliseconds() - 1,
            displayLabel = "Yesterday"
        )
    )

    // Last 5 days before yesterday (total of 7 days)
    for (i in 2..6) { // This loop runs 5 times for days 2 to 6 days ago
        val date = today.minus(i, DateTimeUnit.DAY)
        options.add(
            DateRangeFilter(
                startDateMillis = date.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
                endDateMillis = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone)
                    .toEpochMilliseconds() - 1,
                displayLabel = date.format(
                    kotlinx.datetime.LocalDate.Format {
                        day()
                        char('-')
                        monthNumber()
                        char('-')
                        year()
                    }
                )
                    /*date.dayOfWeek.getShortDayName()*/
            )
        )
    }
    return options
}

//region Data Models (Some might be defined elsewhere in your project)
// --- Generic Filter Option ---
data class FilterChipOption<T>(
    val label: String,
    val value: T
)

//endregion
/**
 * A generic composable to display a scrollable row of filter chips for any type T.
 * Allows single selection.
 */
@Composable
fun <T> GenericFilterChipRow(
    title: String?, // Optional title for this filter section
    options: List<FilterChipOption<T>>,
    selectedSingleValue: T?, // The currently selected value for this filter type from the parent state
    onOptionSelected: (T?) -> Unit, // Callback when an option is selected (null if deselected)
    modifier: Modifier = Modifier
) {
    // This internal 'selectedLocalValue' helps manage the visual selection state
    // and decide when to call 'onOptionSelected'.
    // It's reset if the 'selectedSingleValue' from the parent changes externally.
    var selectedLocalValue by remember(selectedSingleValue) { mutableStateOf(selectedSingleValue) }

    Column(modifier = modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp, bottom = 4.dp) // Added top padding
            )
        }
        SecondaryScrollableTabRow(
            selectedTabIndex = options.indexOfFirst { it.value == selectedLocalValue }
                .takeIf { it != -1 } ?: -1,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp,
            indicator = { /* No indicator */ },
            divider = { /* No divider */ }
        ) {
            options.forEach { option ->
                FilterChipTab(
                    text = option.label,
                    selected = option.value == selectedLocalValue,
                    onClick = {
                        val newValue = if (selectedLocalValue == option.value) {
                            null // Deselect if clicking the same item
                        } else {
                            option.value // Select new item
                        }
                        selectedLocalValue = newValue // Update local visual state
                        onOptionSelected(newValue)    // Notify parent
                    },
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

//endregion
//region Reusable FilterChipTab (Visual element for each chip)
@Composable
fun FilterChipTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
            border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .defaultMinSize(minHeight = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

//endregion

//region Preview for GenericFilterChipRow
@Preview(showBackground = true, widthDp = 380)
@Composable
fun GenericFilterChipRowPreview() {
    // Sample data for EntryType
    val allEntryTypes = EntryType.entries.toList()
    val entryTypeOptions = remember(allEntryTypes) {
        allEntryTypes.map { entryType ->
            FilterChipOption(
                label = entryType.name.replace("_", " ").lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                value = entryType
            )
        }
    }
    var selectedEntryType by remember { mutableStateOf<EntryType?>(null) }

    // Sample data for TransactionType
    val allTransactionTypes = TransactionType.entries.toList()
    val transactionTypeOptions = remember(allTransactionTypes) {
        allTransactionTypes.map { tt ->
            FilterChipOption(
                label = tt.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
                value = tt
            )
        }
    }
    var selectedTransactionType by remember { mutableStateOf<TransactionType?>(TransactionType.INCOME) }


    HandbookTheme { // Assuming your theme is HandbookTheme
        Column(Modifier.padding(16.dp)) {
            GenericFilterChipRow(
                title = "Entry Type",
                options = entryTypeOptions,
                selectedSingleValue = selectedEntryType,
                onOptionSelected = { newSelection ->
                    selectedEntryType = newSelection
                    println("Selected Entry Type: $newSelection")
                }
            )

            Spacer(Modifier.height(16.dp))

            GenericFilterChipRow(
                title = "Transaction Type",
                options = transactionTypeOptions,
                selectedSingleValue = selectedTransactionType,
                onOptionSelected = { newSelection ->
                    selectedTransactionType = newSelection
                    println("Selected Transaction Type: $newSelection")
                }
            )

            Spacer(Modifier.height(16.dp))
            Text("Current selected EntryType: ${selectedEntryType?.name ?: "None"}")
            Text("Current selected TransactionType: ${selectedTransactionType?.name ?: "None"}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { selectedEntryType = EntryType.CASH }) { Text("Select CASH") }
                Button(onClick = { selectedEntryType = null }) { Text("Clear EntryType") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    selectedTransactionType = TransactionType.EXPENSE
                }) { Text("Select EXPENSE") }
                Button(onClick = { selectedTransactionType = null }) { Text("Clear TxType") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun DateFilterChipsPreview() {
    var currentFilters by remember { mutableStateOf(AccountEntryFilters.None) }
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DateFilterChips(
                currentFilters = currentFilters,
                onDateRangeSelected = { startDate, endDate ->
                    currentFilters = currentFilters.copy(startDate = startDate, endDate = endDate)
                    println("Selected range: $startDate to $endDate")
                }
            )

            Spacer(Modifier.height(20.dp))
            Text("Current startDate: ${currentFilters.startDate}")
            Text("Current endDate: ${currentFilters.endDate}")

            // Button to test selecting "Today" externally
            Button(onClick = {
                val todayRange = generatePast7DaysDateOptions().first()
                currentFilters = currentFilters.copy(
                    startDate = todayRange.startDateMillis,
                    endDate = todayRange.endDateMillis
                )
            }) {
                Text("Select Today Externally")
            }
            // Button to test clearing selection
            Button(onClick = {
                currentFilters = currentFilters.copy(
                    startDate = null,
                    endDate = null
                )
            }) {
                Text("Clear Selection Externally")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun FilterBarPreview() {
    var currentFilters by remember { mutableStateOf(AccountEntryFilters.None) }

    // Example dynamic options for preview (normally from ViewModel/Repo)
    // val previewCategoryOptions = remember {
    //     listOf(
    //         FilterChipOption("Food", 1L),
    //         FilterChipOption("Transport", 2L),
    //         FilterChipOption("Shopping", 3L)
    //     )
    // }

    HandbookTheme {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            FilterBar(
                currentFilters = currentFilters,
                onFiltersChanged = { updatedFilters ->
                    currentFilters = updatedFilters
                    println("FilterBarPreview: Filters updated to: $updatedFilters")
                }
                // allCategories = previewCategoryOptions // Example of passing dynamic options
            )

            Spacer(Modifier.height(20.dp))
            Text("Current Filters State:", style = MaterialTheme.typography.titleMedium)
            Text("Start Date: ${currentFilters.startDate}")
            Text("End Date: ${currentFilters.endDate}")
            Text("Entry Type: ${currentFilters.entryType}")
            Text("Transaction Type: ${currentFilters.transactionType}")
            Text("Category ID: ${currentFilters.categoryId}")

            // Buttons to test clearing filters externally
            Spacer(Modifier.height(10.dp))
            Button(onClick = { currentFilters = AccountEntryFilters.None }) {
                Text("Clear All Filters Externally")
            }
        }
    }
}
//endregion

fun DayOfWeek.getShortDayName(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}