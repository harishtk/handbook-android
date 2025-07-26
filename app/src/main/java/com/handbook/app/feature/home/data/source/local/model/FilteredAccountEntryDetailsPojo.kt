package com.handbook.app.feature.home.data.source.local.model

import androidx.room.Embedded
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity
import com.handbook.app.feature.home.data.source.local.entity.BankEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity

data class FilteredAccountEntryDetailsPojo(
    @Embedded
    val entry: AccountEntryEntity,

    @Embedded(prefix = "category_")
    val category: CategoryEntity,

    @Embedded(prefix = "party_")
    val party: PartyEntity? = null,

    @Embedded(prefix = "bank_")
    val bank: BankEntity? = null,
)
