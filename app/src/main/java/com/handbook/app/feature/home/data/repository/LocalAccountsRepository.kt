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
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity
import com.handbook.app.feature.home.data.source.local.entity.asEntity
import com.handbook.app.feature.home.data.source.local.entity.toParty
import com.handbook.app.feature.home.data.source.local.model.AccountEntryWithDetailsEntity
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.repository.AccountsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    override fun getAccountsStream(): Flow<List<AccountEntryWithDetailsEntity>> {
        return flowOf()
    }

    override fun getAccountStream(id: Long): Flow<AccountEntryWithDetailsEntity?> {
        return flowOf()
    }

    override suspend fun addAccount(account: AccountEntryWithDetailsEntity): Result<Long> {
        return Result.Success(0L)
    }

    override suspend fun updateAccount(account: AccountEntryWithDetailsEntity): Result<AccountEntryWithDetailsEntity> {
        return Result.Success(account)
    }

    override suspend fun deleteAccount(account: AccountEntryWithDetailsEntity): Result<Unit> {
        return Result.Success(Unit)
    }

    override fun getCategoriesStream(): Flow<List<Category>> {
        return flowOf(emptyList())
    }

    override fun getCategoryStream(id: Long): Flow<Category?> {
        return flowOf(null)
    }

    override suspend fun addCategory(category: Category): Result<Long> {
        return Result.Success(0L)
    }

    override suspend fun updateCategory(category: Category): Result<Category> {
        return Result.Success(category)
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return Result.Success(Unit)
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
    }

    override fun getPartiesStream(): Flow<List<Party>> {
        return partiesDao.partiesStream()
            .map { parties -> parties.map(PartyEntity::toParty) }
    }

    override fun getPartyStream(id: Long): Flow<Party> {
        return partiesDao.observeParty(id)
            .mapNotNull { it?.toParty() }
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

    override suspend fun deleteParty(party: Party): Result<Unit> {
        return withContext(dispatcher) {
            try {
                partiesDao.delete(party.asEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}