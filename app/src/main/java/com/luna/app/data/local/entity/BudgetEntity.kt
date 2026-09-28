package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryName: String,
    val limitAmount: Double,
    val period: String = "MONTHLY", // "WEEKLY", "MONTHLY", "SEMESTER"
    val alertThresholdPercent: Double = 80.0,
    val currencyCode: String = "USD"
)
