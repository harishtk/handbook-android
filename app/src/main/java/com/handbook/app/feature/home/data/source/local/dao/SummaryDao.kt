package com.handbook.app.feature.home.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.handbook.app.feature.home.data.source.local.model.AccountSummaryAggregationPojo
import com.handbook.app.feature.home.data.source.local.model.FilteredAccountEntryDetailsPojo

@Dao
interface SummaryDao {

    /**
     * Fetches account entries with their details (category, party, bank) based on
     * a date range and optional filters for party, category, and bank.
     *
     * Note: Attachments are not fetched in this query directly as a list.
     * They would need to be fetched separately per entry if required for this view.
     */
    @Transaction
    @Query("""
        SELECT 
            ae.*, 
            c.category_id AS category_category_id,
            c.name AS category_name,                
            c.description AS category_description,  
            c.transaction_type AS category_transaction_type,
            c.created_at AS category_created_at,    
            c.updated_at AS category_updated_at,    
            
            p.party_id AS party_party_id,
            p.name AS party_name,              
            p.contact_number AS party_contact_number,
            p.description AS party_description,
            p.address AS party_address,        
            p.created_at AS party_created_at,  
            p.updated_at AS party_updated_at,  
            
            b.bank_id AS bank_bank_id,          
            b.name AS bank_name,                
            b.description AS bank_description,  
            b.created_at AS bank_created_at,    
            b.updated_at AS bank_updated_at      
        FROM 
            account_entries ae
        JOIN
            categories c ON ae.fk_category_id = c.category_id
        LEFT JOIN
            parties p ON ae.fk_party_id = p.party_id
        LEFT JOIN 
            banks b ON ae.fk_bank_id = b.bank_id
        WHERE
            ae.created_at BETWEEN :startDate AND :endDate
            AND (:categoryIds IS NULL OR ae.fk_category_id IN (:categoryIds))
            AND (:partyIds IS NULL OR ae.fk_party_id IN (:partyIds))
            AND (:bankIds IS NULL OR ae.fk_bank_id IN (:bankIds))
        ORDER BY
            ae.created_at DESC
    """)
    suspend fun getFilteredAccountEntries(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
    ): List<FilteredAccountEntryDetailsPojo>

    /**
     * Fetches account entries with their details (category, party, bank) based on
     * a date range and optional filters for party, category, and bank.
     *
     * Note: Attachments are not fetched in this query directly as a list.
     * They would need to be fetched separately per entry if required for this view.
     */
    @Transaction
    @Query("""
        SELECT 
            ae.*, 
            c.category_id AS category_category_id,
            c.name AS category_name,                
            c.description AS category_description,  
            c.transaction_type AS category_transaction_type,
            c.created_at AS category_created_at,    
            c.updated_at AS category_updated_at,    
            
            p.party_id AS party_party_id,
            p.name AS party_name,              
            p.contact_number AS party_contact_number,
            p.description AS party_description,
            p.address AS party_address,        
            p.created_at AS party_created_at,  
            p.updated_at AS party_updated_at,  
            
            b.bank_id AS bank_bank_id,          
            b.name AS bank_name,                
            b.description AS bank_description,  
            b.created_at AS bank_created_at,    
            b.updated_at AS bank_updated_at      
        FROM 
            account_entries ae
        JOIN
            categories c ON ae.fk_category_id = c.category_id
        LEFT JOIN
            parties p ON ae.fk_party_id = p.party_id
        LEFT JOIN 
            banks b ON ae.fk_bank_id = b.bank_id
        WHERE
            ae.created_at BETWEEN :startDate AND :endDate
            AND (:categoryIds IS NULL OR ae.fk_category_id IN (:categoryIds))
            AND (:partyIds IS NULL OR ae.fk_party_id IN (:partyIds))
            AND (:bankIds IS NULL OR ae.fk_bank_id IN (:bankIds))
        ORDER BY
            ae.created_at DESC
    """)
    fun getFilteredAccountEntriesPagingSource(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
    ): PagingSource<Int, FilteredAccountEntryDetailsPojo>

    /**
     * Calculates the total income and total expenses based on the provided filters
     * and date range.
     * Assumes positive amounts in 'entry_amount' are income and negative amounts are expenses.
     */
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN ae.transaction_type = 'INCOME' THEN ae.amount ELSE 0 END), 0.0) AS totalIncome,
            COALESCE(SUM(CASE WHEN ae.transaction_type = 'EXPENSE' THEN ae.amount ELSE 0 END), 0.0) AS totalExpenses
        FROM 
            account_entries ae
        LEFT JOIN
            categories c ON ae.fk_category_id = c.category_id
        LEFT JOIN
            parties p ON ae.fk_party_id = p.party_id
        LEFT JOIN
            banks b ON ae.fk_bank_id = b.bank_id
        WHERE
            ae.created_at BETWEEN :startDate AND :endDate
            AND (:categoryIds IS NULL OR ae.fk_category_id IN (:categoryIds))
            AND (:partyIds IS NULL OR ae.fk_party_id IN (:partyIds))
            AND (:bankIds IS NULL OR ae.fk_bank_id IN (:bankIds))
            /* Ensure these filters are consistent with getFilteredAccountEntries */
    """)
    suspend fun getAccountSummaryAggregation(
        startDate: Long,
        endDate: Long,
        categoryIds: List<Long>? = null,
        partyIds: List<Long>? = null,
        bankIds: List<Long>? = null
        // Add more filter parameters consistent with getFilteredAccountEntries
    ): AccountSummaryAggregationPojo? // Nullable if no entries match, though COALESCE handles SUM of NULL


    // In SummaryDao.kt

    // You might need to adjust the extraction based on how you store dates (epoch millis or ISO string)
// Assuming created_at is epoch milliseconds
    @Query("SELECT DISTINCT strftime('%Y-%m', created_at / 1000, 'unixepoch') FROM account_entries ORDER BY created_at DESC")
    suspend fun getDistinctYearMonthStrings(): List<String> // Returns "YYYY-MM"

    // Or if you can work with epoch millis directly in Kotlin to form YearMonth:
    @Query("SELECT DISTINCT created_at FROM account_entries")
    suspend fun getAllEntryCreationTimestamps(): List<Long>


    @Query("SELECT DISTINCT strftime('%Y', created_at / 1000, 'unixepoch') FROM account_entries ORDER BY created_at DESC")
    suspend fun getDistinctYearStrings(): List<String> // Returns "YYYY"
}