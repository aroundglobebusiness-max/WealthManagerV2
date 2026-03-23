package com.soorya.wealthmanager.data.local.entity

import androidx.room.*
import com.soorya.wealthmanager.domain.model.Transaction
import com.soorya.wealthmanager.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val currency: String = "INR",
    val symbol: String = "₹",
    val title: String,
    val category: String,
    val note: String = "",
    val paymentSource: String = "",
    val date: Long = System.currentTimeMillis(),
    val syncedToNotion: Boolean = false,
    val notionPageId: String? = null,
    val pendingSync: Boolean = false
) {
    fun toDomain() = Transaction(
        id = id, type = TransactionType.valueOf(type),
        amount = amount, currency = currency, symbol = symbol,
        title = title, category = category, note = note,
        paymentSource = paymentSource, date = date,
        syncedToNotion = syncedToNotion, notionPageId = notionPageId,
        pendingSync = pendingSync
    )
}

fun Transaction.toEntity() = TransactionEntity(
    id = id, type = type.name, amount = amount,
    currency = currency, symbol = symbol, title = title,
    category = category, note = note, paymentSource = paymentSource,
    date = date, syncedToNotion = syncedToNotion,
    notionPageId = notionPageId, pendingSync = pendingSync
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 10")
    fun getRecent(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type='INCOME'")
    fun totalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type='EXPENSE'")
    fun totalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type='INCOME' AND date >= :start AND date <= :end")
    fun incomeInRange(start: Long, end: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type='EXPENSE' AND date >= :start AND date <= :end")
    fun expenseInRange(start: Long, end: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE syncedToNotion=0 OR pendingSync=1")
    suspend fun getPending(): List<TransactionEntity>

    @Query("SELECT DISTINCT title FROM transactions WHERE title LIKE :q LIMIT 5")
    suspend fun suggestions(q: String): List<String>

    @Query("SELECT * FROM transactions WHERE title=:title ORDER BY date DESC LIMIT 1")
    suspend fun lastByTitle(title: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: TransactionEntity): Long

    @Update
    suspend fun update(t: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id=:id")
    suspend fun delete(id: Long)
}
