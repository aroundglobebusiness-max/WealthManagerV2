package com.soorya.wealthmanager.domain.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
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
)

enum class TransactionType { INCOME, EXPENSE }

data class MonthlyData(
    val month: String,
    val income: Double,
    val expense: Double
)
