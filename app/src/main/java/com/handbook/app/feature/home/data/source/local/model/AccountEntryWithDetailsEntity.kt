package com.handbook.app.feature.home.data.source.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryEntity
import com.handbook.app.feature.home.data.source.local.entity.AttachmentEntity
import com.handbook.app.feature.home.data.source.local.entity.CategoryEntity
import com.handbook.app.feature.home.data.source.local.entity.PartyEntity
import com.handbook.app.feature.home.data.source.local.entity.toAccountEntry
import com.handbook.app.feature.home.data.source.local.entity.toAttachment
import com.handbook.app.feature.home.data.source.local.entity.toCategory
import com.handbook.app.feature.home.data.source.local.entity.toParty
import com.handbook.app.feature.home.domain.model.AccountEntryWithDetails

data class AccountEntryWithDetailsEntity(
    @Embedded
    val entry: AccountEntryEntity,

    @Relation(
        parentColumn = "fk_category_id",
        entityColumn = "category_id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "fk_party_id",
        entityColumn = "party_id"
    )
    val party: PartyEntity? = null, // Party is optional

    @Relation(
        parentColumn = "entry_id", // from AccountEntryEntity
        entityColumn = "fk_entry_id"  // from AttachmentEntity
    )
    val attachments: List<AttachmentEntity> = emptyList()
)

fun AccountEntryWithDetailsEntity.toAccountEntryWithDetails(): AccountEntryWithDetails {
    return AccountEntryWithDetails(
        entry = entry.toAccountEntry(),
        category = category.toCategory(),
        party = party?.toParty(),
        attachments = attachments.map(AttachmentEntity::toAttachment)
    )

}