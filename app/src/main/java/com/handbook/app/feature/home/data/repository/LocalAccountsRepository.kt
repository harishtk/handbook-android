package com.handbook.app.feature.home.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.handbook.app.core.di.HandbookDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.local.dao.AccountEntryDao
import com.handbook.app.feature.home.data.source.local.dao.AttachmentDao
import com.handbook.app.feature.home.data.source.local.dao.CategoryDao
import com.handbook.app.feature.home.data.source.local.dao.BankDao
import com.handbook.app.feature.home.data.source.local.dao.PartyDao
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.BankEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity
import com.handbook.app.feature.home.data.source.local.entity.asEntity
import com.handbook.app.feature.home.data.source.local.entity.toAttachment
import com.handbook.app.feature.home.data.source.local.entity.toAttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.toBank
import com.handbook.app.feature.home.data.source.local.entity.toCategory
import com.handbook.app.feature.home.data.source.local.entity.toParty
import com.handbook.app.feature.home.data.source.local.model.AccountEntryWithDetailsEntity
import com.handbook.app.feature.home.data.source.local.model.toAccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Attachment
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.CategoryFilters
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LocalAccountsRepository @Inject constructor(
    private val accountsDao: AccountEntryDao,
    private val categoriesDao: CategoryDao,
    private val partiesDao: PartyDao,
    private val banksDao: BankDao,
    private val attachmentsDao: AttachmentDao,
    @Dispatcher(HandbookDispatchers.Io)
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AccountsRepository {

    override fun getAccountEntriesPagingSource(filters: AccountEntryFilters): Flow<PagingData<AccountEntryWithDetails>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                accountsDao.getFilteredEntriesPagingSource(
                    categoryId = filters.categoryId,
                    partyId = filters.party?.id,
                    entryType = filters.entryType?.name,
                    transactionType = filters.transactionType?.name,
                    isPinned = filters.isPinned,
                    startDate = filters.startDate,
                    endDate = filters.endDate,
                    sortBy = filters.sortBy?.name
                )
            }
        ).flow
            .map { pagingData ->
                pagingData.map(AccountEntryWithDetailsEntity::toAccountEntryWithDetails)
            }
            .flowOn(dispatcher)
    }

    override fun getAccountEntriesStream(): Flow<List<AccountEntryWithDetails>> {
        return accountsDao.getAllAccountEntriesWithDetails()
            .map { entries -> entries.map(AccountEntryWithDetailsEntity::toAccountEntryWithDetails) }
            .flowOn(dispatcher)
    }

    override fun getAccountEntriesStream(id: Long): Flow<AccountEntryWithDetails?> {
        return accountsDao.observeAccountEntryWithDetails(id)
            .mapNotNull { it?.toAccountEntryWithDetails() }
            .flowOn(dispatcher)
    }

    override suspend fun getAccountEntry(accountEntryId: Long): Result<AccountEntryWithDetails> {
        return withContext(dispatcher) {
            try {
                val accountEntry = accountsDao.getAccountEntryWithDetails(accountEntryId)
                    ?.toAccountEntryWithDetails()
                if (accountEntry != null) {
                    Result.Success(accountEntry)
                } else {
                    Result.Error(Exception("Account entry not found"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun addAccountEntry(account: AccountEntry): Result<Long> {
        return withContext(dispatcher) {
            try {
                val id = accountsDao.insertAccountEntry(account.asEntity())
                Result.Success(id)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateAccountEntry(account: AccountEntry): Result<Long> {
        return withContext(dispatcher) {
            try {
                accountsDao.upsertAccountEntry(account.asEntity())
                Result.Success(account.entryId)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteAccountEntry(accountEntryId: Long): Result<Unit> {
        return withContext(dispatcher) {
            try {
                accountsDao.delete(accountEntryId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override fun getCategoriesPagingSource(filters: CategoryFilters): Flow<PagingData<Category>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { categoriesDao.categoriesPagingSource(filters.query, filters.transactionType) }
        ).flow
            .map { pagingData -> pagingData.map(CategoryEntity::toCategory) }
            .flowOn(dispatcher)
    }

    override fun getCategoriesStream(): Flow<List<Category>> {
        return categoriesDao.categoriesStream()
            .map { categories -> categories.map(CategoryEntity::toCategory) }
            .flowOn(dispatcher)
    }

    override fun getCategoryStream(id: Long): Flow<Category?> {
        return categoriesDao.observeCategory(id)
            .mapNotNull { it?.toCategory() }
            .flowOn(dispatcher)
    }

    override suspend fun addCategory(category: Category): Result<Long> {
        return withContext(dispatcher) {
            try {
                categoriesDao.insert(category.asEntity())
                Result.Success(0)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getCategory(categoryId: Long): Result<Category> {
        return withContext(dispatcher) {
            try {
                val category = categoriesDao.getCategory(categoryId)?.toCategory()
                if (category != null) {
                    Result.Success(category)
                } else {
                    Result.Error(Exception("Category not found"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        return withContext(dispatcher) {
            try {
                categoriesDao.upsertAll(listOf(category).map(Category::asEntity))
                Result.Success(category)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteCategory(categoryId: Long): Result<Unit> {
        return withContext(dispatcher) {
            try {
                categoriesDao.delete(categoryId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override fun getPartiesPagingSource(query: String): Flow<PagingData<Party>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { partiesDao.partiesPagingSource(query) }
        ).flow.map { pagingData ->
            pagingData.map(PartyEntity::toParty)
        }
            .flowOn(dispatcher)
    }

    override fun getPartiesStream(): Flow<List<Party>> {
        return partiesDao.partiesStream()
            .map { parties -> parties.map(PartyEntity::toParty) }
            .flowOn(dispatcher)
    }

    override fun getPartyStream(id: Long): Flow<Party> {
        return partiesDao.observeParty(id)
            .mapNotNull { it?.toParty() }
            .flowOn(dispatcher)
    }

    override suspend fun getParty(partyId: Long): Result<Party> {
        return withContext(dispatcher) {
            try {
                val party = partiesDao.getParty(partyId)?.toParty()
                if (party != null) {
                    Result.Success(party)
                } else {
                    Result.Error(Exception("Party not found"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun addParty(party: Party): Result<Long> {
        return withContext(dispatcher) {
            try {
                partiesDao.insert(party.asEntity())
                Result.Success(0)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateParty(party: Party): Result<Party> {
        return withContext(dispatcher) {
            try {
                partiesDao.upsertAll(listOf(party).map(Party::asEntity))
                Result.Success(party)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteParty(partyId: Long): Result<Unit> {
        return withContext(dispatcher) {
            try {
                partiesDao.delete(partyId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override fun getBanksPagingSource(query: String): Flow<PagingData<Bank>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { banksDao.banksPagingSource(query) }
        ).flow
            .map { pagingData -> pagingData.map(BankEntity::toBank) }
            .flowOn(dispatcher)
    }

    override fun getBanksStream(): Flow<List<Bank>> {
        return banksDao.banksStream()
            .map { banks -> banks.map(BankEntity::toBank) }
            .flowOn(dispatcher)
    }

    override fun getBankStream(id: Long): Flow<Bank?> {
        return banksDao.observeBank(id)
            .mapNotNull { it?.toBank() }
            .flowOn(dispatcher)
    }

    override suspend fun getBank(bankId: Long): Result<Bank> {
        return withContext(dispatcher) {
            try {
                val bank = banksDao.getBank(bankId)?.toBank()
                if (bank != null) {
                    Result.Success(bank)
                } else {
                    Result.Error(Exception("Bank not found"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun addBank(bank: Bank): Result<Long> {
        return withContext(dispatcher) {
            try {
                banksDao.insert(bank.asEntity())
                Result.Success(0)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateBank(bank: Bank): Result<Bank> {
        return withContext(dispatcher) {
            try {
                banksDao.upsertAll(listOf(bank).map(Bank::asEntity))
                Result.Success(bank)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteBank(bankId: Long): Result<Unit> {
        return withContext(dispatcher) {
            try {
                banksDao.delete(bankId)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getAttachments(accountEntryId: Long): Flow<List<Attachment>> {
        return attachmentsDao.attachmentsStreamByEntryId(entryId = accountEntryId)
            .map { list ->
                list.map(AttachmentEntity::toAttachment)
            }
            .flowOn(dispatcher)
    }

    override suspend fun addAttachments(attachments: List<Attachment>): Result<List<Long>> {
        Timber.d("addAttachments() called with: attachments = $attachments")
        return withContext(dispatcher) {
            try {
                val attachmentIds = attachmentsDao.upsertAll(attachments.map(Attachment::toAttachmentEntity))
                Result.Success(attachmentIds)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateAttachment(attachment: Attachment): Result<Attachment> {
        return withContext(dispatcher) {
            try {
                attachmentsDao.update(attachment.toAttachmentEntity())
                Result.Success(attachment)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun deleteAttachments(attachmentIds: List<Long>): Result<Unit> {
        Timber.d("deleteAttachments() called with: attachmentIds = $attachmentIds")
        return withContext(dispatcher) {
            try {
                attachmentsDao.deleteByIds(attachmentIds)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}