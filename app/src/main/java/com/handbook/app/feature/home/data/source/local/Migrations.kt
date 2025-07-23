package com.handbook.app.feature.home.data.source.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.handbook.app.feature.home.data.source.local.entity.AccountEntryTable
import com.handbook.app.feature.home.data.source.local.entity.BankTable

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Create the new BankEntity table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `${BankTable.NAME}` (
                `${BankTable.Columns.ID}` INTEGER PRIMARY KEY AUTOINCREMENT,
                `${BankTable.Columns.NAME}` TEXT NOT NULL,
                `${BankTable.Columns.DESCRIPTION}` TEXT,
                `${BankTable.Columns.CREATED_AT}` INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
                `${BankTable.Columns.UPDATED_AT}` INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)
            )
        """
        )

        // 2. Add an index for the name column in the new bank table
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_${BankTable.NAME}_${BankTable.Columns.NAME}`
            ON `${BankTable.NAME}` (`${BankTable.Columns.NAME}`)
        """
        )

        // 3. Add a foreign key constraint to the account_entries table
        db.execSQL("ALTER TABLE `${AccountEntryTable.NAME}` ADD COLUMN `${AccountEntryTable.Columns.FK_BANK_ID}` INTEGER DEFAULT NULL")

        // 4. Add a index for fk_bank_id in the account_entries table
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_${AccountEntryTable.NAME}_${AccountEntryTable.Columns.FK_BANK_ID}` ON `${AccountEntryTable.NAME}` (`${AccountEntryTable.Columns.FK_BANK_ID}`)")
    }
}