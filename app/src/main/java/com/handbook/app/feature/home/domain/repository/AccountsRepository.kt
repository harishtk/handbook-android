package com.handbook.app.feature.home.domain.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.data.source.local.model.AccountEntryWithDetailsEntity
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.Party
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {

    /* Accounts */
    fun getAccountsStream(): Flow<List<AccountEntryWithDetailsEntity>>
    fun getAccountStream(id: Long): Flow<AccountEntryWithDetailsEntity?>

    suspend fun addAccount(account: AccountEntryWithDetailsEntity): Result<Long>
    suspend fun updateAccount(account: AccountEntryWithDetailsEntity): Result<AccountEntryWithDetailsEntity>
    suspend fun deleteAccount(account: AccountEntryWithDetailsEntity): Result<Unit>

    /* Categories */
    fun getCategoriesStream(): Flow<List<Category>>
    fun getCategoryStream(id: Long): Flow<Category?>

    suspend fun addCategory(category: Category): Result<Long>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(category: Category): Result<Unit>

    /* Parties */
    fun getPartiesPagingSource(query: String): Flow<PagingData<Party>>
    fun getPartiesStream(): Flow<List<Party>>
    fun getPartyStream(id: Long): Flow<Party?>

    suspend fun getParty(partyId: Long): Result<Party>
    suspend fun addParty(party: Party): Result<Long>
    suspend fun updateParty(party: Party): Result<Party>
    suspend fun deleteParty(party: Party): Result<Unit>
}