package com.handbook.app.feature.home.data.source.local.converter

import androidx.room.TypeConverter
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun fromEntryType(value: EntryType): String = value.name

    @TypeConverter
    fun toEntryType(value: String): EntryType = EntryType.fromString(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.fromString(value)

    // If you were using Instant or Date objects directly instead of Long for timestamps:
    // @TypeConverter
    // fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
    //
    // @TypeConverter
    // fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilli()
}