package com.soorya.wealthmanager.data.repository

import com.soorya.wealthmanager.data.local.entity.*
import com.soorya.wealthmanager.data.remote.NotionService
import com.soorya.wealthmanager.domain.model.Transaction
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WealthRepository @Inject constructor(
    private val dao: com.soorya.wealthmanager.data.local.entity.TransactionDao,
    private val notion: NotionService
) {
    fun getAll() = dao.getAll().map { it.map { e -> e.toDomain() } }
    fun getRecent() = dao.getRecent().map { it.map { e -> e.toDomain() } }
    fun totalIncome() = dao.totalIncome().map { it ?: 0.0 }
    fun totalExpense() = dao.totalExpense().map { it ?: 0.0 }
    fun incomeInRange(s: Long, e: Long) = dao.incomeInRange(s, e).map { it ?: 0.0 }
    fun expenseInRange(s: Long, e: Long) = dao.expenseInRange(s, e).map { it ?: 0.0 }

    suspend fun suggestions(q: String) = dao.suggestions("%$q%")
    suspend fun lastByTitle(t: String) = dao.lastByTitle(t)?.toDomain()

    suspend fun add(txn: Transaction, token: String, dbId: String): Long {
        val id = dao.insert(txn.toEntity())
        if (token.isNotEmpty() && dbId.isNotEmpty()) {
            val pageId = notion.sync(txn.copy(id = id), dbId)
            if (pageId != null) dao.update(txn.copy(id = id).toEntity().copy(syncedToNotion = true, notionPageId = pageId))
            else dao.update(txn.copy(id = id).toEntity().copy(pendingSync = true))
        }
        return id
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun syncPending(token: String, dbId: String): Int {
        if (token.isEmpty() || dbId.isEmpty()) return 0
        var count = 0
        dao.getPending().forEach { e ->
            val pageId = notion.sync(e.toDomain(), dbId)
            if (pageId != null) { dao.update(e.copy(syncedToNotion = true, notionPageId = pageId, pendingSync = false)); count++ }
        }
        return count
    }

    suspend fun testNotion(dbId: String) = notion.testConnection(dbId)
}
