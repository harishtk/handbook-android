@file:OptIn(ExperimentalTime::class)

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
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
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
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Data class for representing a specific date range filter option (from your original code)
data class DateRangeFilter(
    val startDateMillis: Long,
    val endDateMillis: Long,
    val displayLabel: String
)

// FilterChipOption (from your original code - ensure it's defined or imported)
data class FilterChipOption<T>(
    val label: String,
    val value: T,
    val isSelected: Boolean = false // Could be useful if managing selection state outside
)



//region Main FilterBar Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    currentFilters: AccountEntryFilters,
    onFiltersChanged: (AccountEntryFilters) -> Unit,
    initialExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var filtersExpanded by remember { mutableStateOf(initialExpanded) }
    val dateFilterOptions = remember { generatePast7DaysDateOptions() }

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
        if (currentFilters.startDate != null) count++
        if (currentFilters.entryType != null) count++
        if (currentFilters.transactionType != null) count++
        count
    }
    val anyFilterActive = activeFilterCount > 0

    // Determine the background color and elevation based on state
    val surfaceColor = when {
        filtersExpanded -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp) // Elevated when expanded
        anyFilterActive -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp) // Slightly elevated if filters are active but collapsed
        else -> Color.Transparent // Transparent if collapsed and no filters active
    }
    val tonalElevation = when {
        filtersExpanded -> 2.dp
        anyFilterActive -> 1.dp
        else -> 0.dp
    }

    Column( // Changed from Surface to Column as the outer container for more control
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = surfaceColor,
                shape = MaterialTheme.shapes.small,
            ) // Apply background here
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
                // More subtle clear button for collapsed state
                TextButton(
                    onClick = {
                        onFiltersChanged(AccountEntryFilters.None)
                        filtersExpanded = false
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear all filters",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Clear", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(4.dp)) // Reduced spacer
            }

            val rotationAngle by animateFloatAsState(targetValue = if (filtersExpanded) 180f else 0f, label = "expand_icon_rotation", animationSpec = tween(300))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (filtersExpanded) "Collapse filters" else "Expand filters",
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        // --- Expandable Filter Options ---
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(300)) + fadeIn(animationSpec = tween(150, delayMillis = 150)),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(300)) + fadeOut(animationSpec = tween(150))
        ) {
            // Divider only if the surfaceColor isn't transparent (i.e., when expanded or active)
            if (surfaceColor != Color.Transparent) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (anyFilterActive) {
                    Button(
                        // Changed to Filled button for more prominence when expanded
                        onClick = {
                            onFiltersChanged(AccountEntryFilters.None)
                            filtersExpanded = false // Collapse after clearing
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAltOff,
                            contentDescription = "Clear all filters",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Clear All Filters ($activeFilterCount)")
                    }
                }

                DateFilterChipsScrollable(
                    currentFilters = currentFilters,
                    onDateRangeSelected = { startDate, endDate ->
                        onFiltersChanged(currentFilters.copy(startDate = startDate, endDate = endDate))
                    },
                    dateOptions = dateFilterOptions
                )
                CompactFilterChipGroup(
                    title = "Entry Type",
                    options = entryTypeOptions,
                    selectedSingleValue = currentFilters.entryType,
                    onOptionSelected = { onFiltersChanged(currentFilters.copy(entryType = it)) }
                )
                CompactFilterChipGroup(
                    title = "Transaction Type",
                    options = transactionTypeOptions,
                    selectedSingleValue = currentFilters.transactionType,
                    onOptionSelected = { onFiltersChanged(currentFilters.copy(transactionType = it)) }
                )
            }
        }
    }
}
//endregion

//region Refined Chip Groups

/**
 * A horizontally scrollable row of date filter chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterChipsScrollable(
    currentFilters: AccountEntryFilters,
    onDateRangeSelected: (startDateMillis: Long?, endDateMillis: Long?) -> Unit,
    modifier: Modifier = Modifier,
    dateOptions: List<DateRangeFilter>
) {
    Column(modifier.fillMaxWidth()) {
        Text("Date Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val selectedDateRangeKey = currentFilters.startDate?.let { start -> currentFilters.endDate?.let { end -> "$start-$end" } }

            dateOptions.forEach { dateOption ->
                val isSelected = selectedDateRangeKey == "${dateOption.startDateMillis}-${dateOption.endDateMillis}"
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onDateRangeSelected(null, null)
                        } else {
                            onDateRangeSelected(dateOption.startDateMillis, dateOption.endDateMillis)
                        }
                    },
                    label = { Text(dateOption.displayLabel) },
                    shape = CircleShape // More compact chip shape
                )
            }
        }
    }
}


/**
 * A more compact filter chip group with a title and horizontally scrollable chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CompactFilterChipGroup(
    title: String,
    options: List<FilterChipOption<T>>,
    selectedSingleValue: T?,
    onOptionSelected: (T?) -> Unit, // Allow deselecting by passing null
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = selectedSingleValue == option.value
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onOptionSelected(null) // Deselect if clicked again
                        } else {
                            onOptionSelected(option.value)
                        }
                    },
                    label = { Text(option.label, fontSize = 13.sp) }, // Slightly smaller font
                    shape = CircleShape // More compact chip shape
                )
            }
        }
    }
}

//endregion

// Helper to generate date options (from your original code - ensure it's accessible)
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
            endDateMillis = today.atStartOfDayIn(systemTimeZone).toEpochMilliseconds() - 1,
            displayLabel = "Yesterday"
        )
    )
    // Past 7 Days (as individual days for more granular selection if desired, or keep as ranges)
    for (i in 2..6) {
        val date = today.minus(i, DateTimeUnit.DAY)
        options.add(
            DateRangeFilter(
                startDateMillis = date.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
                endDateMillis = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone)
                    .toEpochMilliseconds() - 1,
                displayLabel = date.format(LocalDate.Format {
                    day(); char('-'); monthNumber(); char('-'); year()
                })
            )
        )
    }
    return options.reversed() // Show "Today" first
}


//region Preview
@Preview(showBackground = true, widthDp = 380)
@Composable
fun FilterBarPreview_Collapsed_NoFilters() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        FilterBar(
            currentFilters = AccountEntryFilters.None,
            onFiltersChanged = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun FilterBarPreview_Collapsed_ActiveFilters() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        FilterBar(
            currentFilters = AccountEntryFilters(
                startDate = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000), // Yesterday
                endDate = Clock.System.now().toEpochMilliseconds(),
                entryType = EntryType.OTHER
            ),
            onFiltersChanged = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun FilterBarPreview_Expanded_NoFilters() {
    val (filters, setFilters) = remember { mutableStateOf(AccountEntryFilters.None) }
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column {
            FilterBar(
                currentFilters = filters,
                onFiltersChanged = setFilters
            )
            // Add some dummy content below to see interaction
            Text("Content Below Filters", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun FilterBarPreview_Expanded_ActiveFilters() {
    val (filters, setFilters) = remember {
        mutableStateOf(
            AccountEntryFilters(
                startDate = generatePast7DaysDateOptions().first().startDateMillis,
                endDate = generatePast7DaysDateOptions().first().endDateMillis,
                entryType = EntryType.OTHER,
                transactionType = TransactionType.EXPENSE
            )
        )
    }
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column {
            FilterBar(
                currentFilters = filters,
                onFiltersChanged = setFilters
            )
            Text("Content Below Filters", modifier = Modifier.padding(16.dp))
        }
    }
}
//endregion

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun FilterBarPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true,
    ) {
        var filters by remember { mutableStateOf(AccountEntryFilters.None) }

        FilterBar(
            currentFilters = filters,
            onFiltersChanged = {
                filters = it
            },
            initialExpanded = true,
        )
    }
}



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