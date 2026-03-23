package com.soorya.wealthmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soorya.wealthmanager.data.local.entity.TransactionDao
import com.soorya.wealthmanager.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class WealthDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    companion object { const val NAME = "wealth_db" }
}
