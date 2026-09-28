package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val accountType: String = "CASH", // "CASH", "BANK", "SAVINGS", "CREDIT", "CRYPTO"
    val balance: Double = 0.0,
    val currencyCode: String = "USD",
    val colorHex: String = "#3B82F6",
    val icon: String = "Wallet",
    val isArchived: Boolean = false
)
