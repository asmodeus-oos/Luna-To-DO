package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: String = "EXPENSE", // "INCOME", "EXPENSE", "TRANSFER"
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val accountId: Long = 1L,
    val currency: String = "USD",
    val receiptUri: String? = null,
    val recurrenceRule: String = "NONE",
    val linkedTaskId: Long? = null,
    val linkedProjectId: Long? = null,
    val linkedGoalId: Long? = null,
    val notes: String? = null,
    val isTaxDeductible: Boolean = false,
    val isExcludedFromBudget: Boolean = false
)
