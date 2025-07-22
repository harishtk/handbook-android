package com.handbook.app.feature.home.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.handbook.app.core.di.AiaDispatchers
import com.handbook.app.core.di.Dispatcher
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.local.dao.AccountEntryDao
import com.handbook.app.feature.home.data.source.local.dao.CategoryDao
import com.handbook.app.feature.home.data.source.local.dao.PartyDao
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity
import com.handbook.app.feature.home.data.source.local.entity.asEntity
import com.handbook.app.feature.home.data.source.local.entity.toCategory
import com.handbook.app.feature.home.data.source.local.entity.toParty
import com.handbook.app.feature.home.data.source.local.model.AccountEntryWithDetailsEntity
import com.handbook.app.feature.home.data.source.local.model.toAccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalAccountsRepository @Inject constructor(
    private val accountsDao: AccountEntryDao,
    private val categoriesDao: CategoryDao,
    private val partiesDao: PartyDao,
    @Dispatcher(AiaDispatchers.Io)
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
                    partyId = filters.partyId,
                    entryType = filters.entryType?.name,
                    transactionType = filters.transactionType?.name,
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
                accountsDao.upsertAccountEntry(account.asEntity())
                Result.Success(0)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun updateAccountEntry(account: AccountEntry): Result<AccountEntry> {
        return withContext(dispatcher) {
            try {
                accountsDao.upsertAccountEntry(account.asEntity())
                Result.Success(account)
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

    override fun getCategoriesPagingSource(query: String): Flow<PagingData<Category>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { categoriesDao.categoriesPagingSource(query) }
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
}