@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class) // Added ExperimentalMaterial3Api

package com.handbook.app.feature.home.presentation.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll // Added for expanded content
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.ui.theme.HandbookTheme
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.text.SimpleDateFormat // For formatting dates in input fields
import java.util.Date // For formatting dates
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime


// Data class for representing a specific date range filter option
data class DateRangeFilterOption( // Renamed for clarity vs DateRangeFilter from previous example
    val startDateMillis: Long?, // Nullable for custom ranges not yet set
    val endDateMillis: Long?,   // Nullable
    val displayLabel: String,
    val isPreset: Boolean = true // To differentiate presets from custom input display
)


// Helper to generate date options
fun generateDatePresetOptions(): List<DateRangeFilterOption> {
    val options = mutableListOf<DateRangeFilterOption>()
    val systemTimeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTimeZone).date

    options.add(
        DateRangeFilterOption(
            startDateMillis = today.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() - 1,
            displayLabel = "Today"
        )
    )
    val startOfWeek = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    options.add(
        DateRangeFilterOption(
            startDateMillis = startOfWeek.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = startOfWeek.plus(6, DateTimeUnit.DAY).plus(DatePeriod(days = 1)).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() -1,
            displayLabel = "This Week"
        )
    )
    val startOfMonth = LocalDate(today.year, today.month, 1)
    options.add(
        DateRangeFilterOption(
            startDateMillis = startOfMonth.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(DatePeriod(days = 1)).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() -1,
            displayLabel = "This Month"
        )
    )
    return options
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredFilterBar(
    currentFilters: AccountEntryFilters,
    onFiltersChanged: (AccountEntryFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filtersExpanded by remember { mutableStateOf(false) }
    val datePresetOptions = remember { generateDatePresetOptions() }

    // Date picker states
    val fromDatePickerState = rememberDatePickerState(initialSelectedDateMillis = currentFilters.startDate)
    var showFromDatePicker by remember { mutableStateOf(false) }
    val toDatePickerState = rememberDatePickerState(initialSelectedDateMillis = currentFilters.endDate)
    var showToDatePicker by remember { mutableStateOf(false) }

    // Update currentFilters when date picker selection changes
    LaunchedEffect(fromDatePickerState.selectedDateMillis) {
        if (filtersExpanded) { // Only update if the filter bar is open, to avoid changing on initial load
            onFiltersChanged(currentFilters.copy(startDate = fromDatePickerState.selectedDateMillis))
        }
    }
    LaunchedEffect(toDatePickerState.selectedDateMillis) {
        if (filtersExpanded) {
            onFiltersChanged(currentFilters.copy(endDate = toDatePickerState.selectedDateMillis))
        }
    }


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

    val activeFilterCount = remember(currentFilters) {
        var count = 0
        if (currentFilters.startDate != null || currentFilters.endDate != null) count++ // Date range counts as one
        if (currentFilters.entryType != null) count++
        if (currentFilters.transactionType != null) count++
        count
    }
    val anyFilterActive = activeFilterCount > 0

    val surfaceColor = when {
        filtersExpanded -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        anyFilterActive -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = surfaceColor,
                shape = MaterialTheme.shapes.small,
            )
            .clip(MaterialTheme.shapes.small)
            // .animateContentSize(animationSpec = tween(300))
    ) {
        // --- Collapsed Filter Bar Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { filtersExpanded = !filtersExpanded }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter Icon",
                tint = if (anyFilterActive) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (anyFilterActive) "Filters ($activeFilterCount)" else "Filters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (anyFilterActive) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (anyFilterActive && !filtersExpanded) {
                TextButton(
                    onClick = {
                        onFiltersChanged(AccountEntryFilters.None)
                        // filtersExpanded = false // Keep it expanded if user clears from header
                    },
                    modifier = Modifier.heightIn(min = 32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Clear, "Clear all filters", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(4.dp))
            }
            val rotationAngle by animateFloatAsState(targetValue = if (filtersExpanded) 180f else 0f, label = "expand_icon_rotation", animationSpec = tween(300))
            Icon(Icons.Default.ExpandMore, if (filtersExpanded) "Collapse filters" else "Expand filters", modifier = Modifier.rotate(rotationAngle))
        }

        // --- Expandable Filter Sections ---
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)) + fadeIn(animationSpec = tween(150, delayMillis = 150)),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300)) + fadeOut(animationSpec = tween(150))
        ) {
            if (surfaceColor != Color.Transparent) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Only horizontal padding for the scrollable column
                    .verticalScroll(rememberScrollState()) // Allow content to scroll if it overflows
                    .heightIn(max = 450.dp) // Constrain max height of expanded section
            ) {
                Spacer(Modifier.height(16.dp)) // Top padding for scrollable content

                // --- Date Range Section ---
                FilterSectionWithReset(
                    title = "Date Range",
                    onReset = {
                        onFiltersChanged(currentFilters.copy(startDate = null, endDate = null))
                        fromDatePickerState.selectedDateMillis = null // Clear picker state
                        toDatePickerState.selectedDateMillis = null
                    }
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateInputField(
                            label = "From",
                            dateMillis = currentFilters.startDate,
                            onClick = { showFromDatePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                        DateInputField(
                            label = "To",
                            dateMillis = currentFilters.endDate,
                            onClick = { showToDatePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        datePresetOptions.forEach { preset ->
                            val isSelected = currentFilters.startDate == preset.startDateMillis &&
                                    currentFilters.endDate == preset.endDateMillis
                            SuggestionChip(
                                onClick = {
                                    onFiltersChanged(currentFilters.copy(startDate = preset.startDateMillis, endDate = preset.endDateMillis))
                                    fromDatePickerState.selectedDateMillis = preset.startDateMillis // Sync picker
                                    toDatePickerState.selectedDateMillis = preset.endDateMillis
                                },
                                label = { Text(preset.displayLabel) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))


                // --- Entry Type Section ---
                FilterSectionWithReset(
                    title = "Entry Type",
                    onReset = { onFiltersChanged(currentFilters.copy(entryType = null)) }
                ) {
                    ChipGroupSingleSelect(
                        options = entryTypeOptions,
                        selectedOptionValue = currentFilters.entryType,
                        onOptionSelected = { onFiltersChanged(currentFilters.copy(entryType = it)) }
                    )
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // --- Transaction Type Section ---
                FilterSectionWithReset(
                    title = "Transaction Type",
                    onReset = { onFiltersChanged(currentFilters.copy(transactionType = null)) }
                ) {
                    ChipGroupSingleSelect(
                        options = transactionTypeOptions,
                        selectedOptionValue = currentFilters.transactionType,
                        onOptionSelected = { onFiltersChanged(currentFilters.copy(transactionType = it)) }
                    )
                }
                Spacer(Modifier.height(24.dp)) // Space before action buttons

                // --- Action Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), // Padding for buttons
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (anyFilterActive) {
                        OutlinedButton(
                            onClick = { onFiltersChanged(AccountEntryFilters.None) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FilterAltOff, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Reset All")
                        }
                    } else {
                        Spacer(Modifier.weight(1f)) // Keep spacing consistent
                    }
                    Button(
                        onClick = { filtersExpanded = false }, // "Done" simply collapses
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Done, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(if (anyFilterActive) "Done ($activeFilterCount)" else "Done")
                    }
                }
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
fun FilterSectionWithReset(
    title: String,
    onReset: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onReset, enabled = true /* Enable based on whether this section has active filter */) {
                Text("Reset", fontSize = 13.sp)
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChipGroupSingleSelect(
    options: List<FilterChipOption<T>>,
    selectedOptionValue: T?,
    onOptionSelected: (T?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = selectedOptionValue == option.value
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) onOptionSelected(null) else onOptionSelected(option.value)
                },
                label = { Text(option.label, fontSize = 13.sp) },
                shape = CircleShape,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField( // Copied from your previous example, seems fine
    label: String,
    dateMillis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    OutlinedTextField(
        value = dateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
        onValueChange = { /* Read only */ },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.CalendarToday, "Select date", Modifier.clickable(onClick = onClick)) },
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium, // Softer corners
        singleLine = true
    )
}

// --- Previews (Adjust to use StructuredFilterBar) ---
@Preview(showBackground = true, widthDp = 380, name = "Structured Collapsed - No Filters")
@Composable
fun StructuredFilterBarPreview_Collapsed_NoFilters() {
    HandbookTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            StructuredFilterBar(currentFilters = AccountEntryFilters.None, onFiltersChanged = {})
            Text("Content below", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Structured Collapsed - Active")
@Composable
fun StructuredFilterBarPreview_Collapsed_Active() {
    HandbookTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            StructuredFilterBar(
                currentFilters = AccountEntryFilters(entryType = EntryType.OTHER),
                onFiltersChanged = {}
            )
            Text("Content below", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 380, name = "Structured Expanded - Active")
@Composable
fun StructuredFilterBarPreview_Expanded_Active() {
    var filters by remember {
        mutableStateOf(
            AccountEntryFilters(
                startDate = generateDatePresetOptions().first().startDateMillis,
                endDate = generateDatePresetOptions().first().endDateMillis,
                entryType = EntryType.OTHER
            )
        )
    }
    HandbookTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            StructuredFilterBar(
                currentFilters = filters,
                onFiltersChanged = { filters = it }
            )
            Text("Content below", modifier = Modifier.padding(16.dp))
        }
    }
}
