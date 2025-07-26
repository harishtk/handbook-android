package com.handbook.app.feature.home.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.handbook.app.core.di.HandbookDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.local.dao.AttachmentDao
import com.handbook.app.feature.home.data.source.local.dao.SummaryDao
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.toAccountEntry
import com.handbook.app.feature.home.data.source.local.entity.toAttachment
import com.handbook.app.feature.home.data.source.local.entity.toBank
import com.handbook.app.feature.home.data.source.local.entity.toCategory
import com.handbook.app.feature.home.data.source.local.entity.toParty
import com.handbook.app.feature.home.data.source.local.model.AccountSummaryAggregationPojo
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.FilteredSummaryResult
import com.handbook.app.feature.home.domain.repository.SummaryRepository
import com.handbook.app.ifDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.YearMonth
import java.time.format.DateTimeParseException
import javax.inject.Inject

class LocalSummaryRepository @Inject constructor(
    private val summaryDao: SummaryDao,
    private val attachmentDao: AttachmentDao,
    @Dispatcher(HandbookDispatchers.Io) private val ioDispatcher: CoroutineDispatcher
): SummaryRepository {

    override suspend fun getFilteredAccountEntries(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>?,
        partyIds: List<Long>?,
        bankIds: List<Long>?
    ): Result<FilteredSummaryResult> {
        return try {
            val result = withContext(ioDispatcher) {
                val detailedEntriesDeferred = async {
                    summaryDao.getFilteredAccountEntries(
                        startDate = startDate,
                        endDate = endDate,
                        categoryIds = categoryIds,
                        partyIds = partyIds,
                        bankIds = bankIds
                    )
                }

                val aggregationDeferred = async {
                    summaryDao.getAccountSummaryAggregation(
                        startDate = startDate,
                        endDate = endDate,
                        categoryIds = categoryIds,
                        partyIds = partyIds,
                        bankIds = bankIds
                    )
                }

                val detailedEntriesPojo = detailedEntriesDeferred.await()
                val aggregationPojo = aggregationDeferred.await()

                val domainEntries = detailedEntriesPojo.map { pojo ->
                    AccountEntryWithDetails(
                        entry = pojo.entry.toAccountEntry(),
                        category = pojo.category.toCategory(),
                        party = pojo.party?.toParty(),
                        bank = pojo.bank?.toBank(),
                        attachments = attachmentDao.getAttachmentsByEntryId(pojo.entry.entryId)
                            .map(AttachmentEntity::toAttachment)
                    )
                }

                val income = aggregationPojo?.totalIncome ?: 0.0
                val expenses = aggregationPojo?.totalExpenses ?: 0.0
                val balance = income - expenses

                FilteredSummaryResult(
                    entries = domainEntries,
                    totalIncome = income,
                    totalExpenses = expenses,
                    balance = balance
                )
            }

            Result.Success(result)
        } catch (e: Exception) {
            ifDebug { Timber.e(e, "Error fetching filtered account entries") }
            Result.Error(e)
        }
    }

    override fun getFilteredSummaryPaginated(
        startDate: Long, endDate: Long,
        categoryIds: List<Long>?, partyIds: List<Long>?, bankIds: List<Long>?
    ): Flow<PagingData<AccountEntryWithDetails>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                summaryDao.getFilteredAccountEntriesPagingSource(
                    startDate = startDate,
                    endDate = endDate,
                    categoryIds = categoryIds,
                    partyIds = partyIds,
                    bankIds = bankIds
                )
            }
        ).flow
            .map { pagingDataPojo -> // This map is on PagingData
                pagingDataPojo.map { pojo -> // This map is on each item in the page
                    // Your existing mapping logic, including fetching attachments
                    AccountEntryWithDetails(
                        entry = pojo.entry.toAccountEntry(),
                        category = pojo.category.toCategory(),
                        party = pojo.party?.toParty(),
                        bank = pojo.bank?.toBank(),
                        attachments = attachmentDao.getAttachmentsByEntryId(pojo.entry.entryId)
                            .map(AttachmentEntity::toAttachment)
                    )
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getSummaryAggregation(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>?,
        partyIds: List<Long>?,
        bankIds: List<Long>?
    ): Result<AccountSummaryAggregationPojo> {
        return try {
            val result = withContext(ioDispatcher) {
                summaryDao.getAccountSummaryAggregation(
                    startDate = startDate,
                    endDate = endDate,
                    categoryIds = categoryIds,
                    partyIds = partyIds,
                    bankIds = bankIds,
                ) ?: throw NullPointerException()
            }
            Result.Success(result)
        } catch (e: Exception) {
            ifDebug { Timber.e(e, "Error fetching summary aggregation") }
            Result.Error(e)
        }
    }

    override suspend fun getAllDataForExport(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>?,
        partyIds: List<Long>?,
        bankIds: List<Long>?
    ): Result<List<AccountEntryWithDetails>> { // Returns a simple List of domain models
        return try {
            val pojoList = withContext(Dispatchers.IO) { // Run on IO dispatcher
                summaryDao.getFilteredAccountEntries(
                    startDate = startDate,
                    endDate = endDate,
                    categoryIds = categoryIds,
                    partyIds = partyIds,
                    bankIds = bankIds
                )
            }

            // Map POJOs to Domain Models, including attachments
            val domainEntries = pojoList.map { pojo ->
                AccountEntryWithDetails(
                    entry = pojo.entry.toAccountEntry(),
                    category = pojo.category.toCategory(),
                    party = pojo.party?.toParty(),
                    bank = pojo.bank?.toBank(),
                    attachments = attachmentDao.getAttachmentsByEntryId(pojo.entry.entryId)
                        .map(AttachmentEntity::toAttachment)
                )
            }
            Result.Success(domainEntries)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching all data for export")
            Result.Error(e)
        }
    }

    override suspend fun getDistinctYearMonthsFromEntries(): Result<List<YearMonth>> {
        return try {
            // If using getDistinctYearMonthStrings:
            val yearMonthStrings = summaryDao.getDistinctYearMonthStrings()
            val yearMonths = yearMonthStrings.mapNotNull { ymString ->
                try { YearMonth.parse(ymString) } catch (e: DateTimeParseException) { null }
            }
            Result.Success(yearMonths.distinct().sortedDescending())

            // If using getAllEntryCreationTimestamps:
            // val timestamps = summaryDao.getAllEntryCreationTimestamps()
            // val yearMonths = timestamps.map { timestamp ->
            //    YearMonth.from(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))
            // }.distinct().sortedDescending()
            // Result.success(yearMonths)

        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getDistinctYearsFromEntries(): Result<List<Int>> {
        return try {
            val yearStrings = summaryDao.getDistinctYearStrings()
            val years = yearStrings.mapNotNull { it.toIntOrNull() }
            Result.Success(years.distinct().sortedDescending())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}