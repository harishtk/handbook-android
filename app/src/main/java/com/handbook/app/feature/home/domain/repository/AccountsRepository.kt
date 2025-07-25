package com.handbook.app.feature.home.domain.repository

import androidx.paging.PagingData
import com.handbook.app.core.util.Result
import com.handbook.app.feature.home.domain.model.AccountEntry
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.model.AccountEntryFilters
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails
import com.handbook.app.feature.home.domain.model.Attachment
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.CategoryFilters
import com.handbook.app.feature.home.domain.model.Party
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {

    /* Accounts */
    fun getAccountEntriesPagingSource(filters: AccountEntryFilters): Flow<PagingData<AccountEntryWithDetails>>
    fun getAccountEntriesStream(): Flow<List<AccountEntryWithDetails>>
    fun getAccountEntriesStream(id: Long): Flow<AccountEntryWithDetails?>

    suspend fun getAccountEntry(accountEntryId: Long): Result<AccountEntryWithDetails>
    suspend fun addAccountEntry(account: AccountEntry): Result<Long>
    suspend fun updateAccountEntry(account: AccountEntry): Result<Long>
    suspend fun deleteAccountEntry(accountEntryId: Long): Result<Unit>

    /* Categories */
    fun getCategoriesPagingSource(filters: CategoryFilters): Flow<PagingData<Category>>
    fun getCategoriesStream(): Flow<List<Category>>
    fun getCategoryStream(id: Long): Flow<Category?>

    suspend fun getCategory(categoryId: Long): Result<Category>
    suspend fun addCategory(category: Category): Result<Long>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(categoryId: Long): Result<Unit>

    /* Parties */
    fun getPartiesPagingSource(query: String): Flow<PagingData<Party>>
    fun getPartiesStream(): Flow<List<Party>>
    fun getPartyStream(id: Long): Flow<Party?>

    suspend fun getParty(partyId: Long): Result<Party>
    suspend fun addParty(party: Party): Result<Long>
    suspend fun updateParty(party: Party): Result<Party>
    suspend fun deleteParty(partyId: Long): Result<Unit>

    /* Banks */
    fun getBanksPagingSource(query: String): Flow<PagingData<Bank>>
    fun getBanksStream(): Flow<List<Bank>>
    fun getBankStream(id: Long): Flow<Bank?>

    suspend fun getBank(bankId: Long): Result<Bank>
    suspend fun addBank(bank: Bank): Result<Long>
    suspend fun updateBank(bank: Bank): Result<Bank>
    suspend fun deleteBank(bankId: Long): Result<Unit>

    /* Attachments */
    suspend fun getAttachments(accountEntryId: Long): Flow<List<Attachment>>

    suspend fun addAttachments(attachments: List<Attachment>): Result<List<Long>>
    suspend fun updateAttachment(attachment: Attachment): Result<Attachment>
    suspend fun deleteAttachments(attachmentIds: List<Long>): Result<Unit>

}