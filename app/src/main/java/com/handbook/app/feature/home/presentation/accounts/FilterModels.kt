package com.handbook.app.feature.home.presentation.accounts

import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.SortOption
import com.handbook.app.feature.home.domain.model.TransactionType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class TemporarySheetFilters(
    var startDate: Long? = null,
    var endDate: Long? = null,
    var selectedDatePresetLabel: String? = null,
    var entryType: EntryType? = null,             // CORRECTED
    var transactionType: TransactionType? = null, // CORRECTED
    var party: Party? = null,
    var sortBy: SortOption? = SortOption.NEWEST_FIRST
) {
    fun toAccountEntryFilters(): AccountEntryFilters {
        return AccountEntryFilters(
            startDate = startDate,
            endDate = endDate,
            entryType = entryType,
            transactionType = transactionType,
            party = party,
            sortBy = sortBy
        )
    }

    fun countSelections(): Int {
        var i = 0
        if (startDate != null || endDate != null) i++
        if (entryType != null) i++
        if (transactionType != null) i++
        if (party != null) i++
        return i
    }

    // Helper to create TemporarySheetFilters from AccountEntryFilters
    companion object {
        fun fromActive(activeFilters: AccountEntryFilters): TemporarySheetFilters {
            return TemporarySheetFilters(
                startDate = activeFilters.startDate,
                endDate = activeFilters.endDate,
                // selectedDatePresetLabel logic might need to be smarter
                // if you want to re-select a preset based on start/end dates.
                // For now, it won't auto-select a preset label.
                entryType = activeFilters.entryType,
                transactionType = activeFilters.transactionType,
                sortBy = activeFilters.sortBy,
                party = activeFilters.party,
            )
        }
    }
}

// DatePreset and generateSheetDatePresets remain the same as before
data class DatePreset(
    val label: String,
    val startDateMillis: Long,
    val endDateMillis: Long
)

@ExperimentalTime
fun generateSheetDatePresets(): List<DatePreset> {
    val options = mutableListOf<DatePreset>()
    val systemTimeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(systemTimeZone).date

    options.add(
        DatePreset(
            label = "Today",
            startDateMillis = today.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() - 1
        )
    )
    val startOfWeek = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    options.add(
        DatePreset(
            label = "This Week",
            startDateMillis = startOfWeek.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = startOfWeek.plus(6, DateTimeUnit.DAY).plus(DatePeriod(days = 1)).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() -1
        )
    )
    val startOfMonth = LocalDate(today.year, today.month, 1)
    options.add(
        DatePreset(
            label = "This Month",
            startDateMillis = startOfMonth.atStartOfDayIn(systemTimeZone).toEpochMilliseconds(),
            endDateMillis = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(DatePeriod(days = 1)).atStartOfDayIn(systemTimeZone).toEpochMilliseconds() -1
        )
    )
    return options
}