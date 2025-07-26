package com.handbook.app.feature.home.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.local.model.AccountSummaryAggregationPojo
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.FilteredSummaryResult
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface SummaryRepository {

    suspend fun getFilteredAccountEntries(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
    ): Result<FilteredSummaryResult>

    fun getFilteredSummaryPaginated(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
    ): Flow<PagingData<AccountEntryWithDetails>>

    suspend fun getSummaryAggregation(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
    ): Result<AccountSummaryAggregationPojo>

    suspend fun getAllDataForExport(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>?,
        partyIds: List<Long>?,
        bankIds: List<Long>?
    ): Result<List<AccountEntryWithDetails>>

    suspend fun getDistinctYearMonthsFromEntries(): Result<List<YearMonth>>

    suspend fun getDistinctYearsFromEntries(): Result<List<Int>>
}