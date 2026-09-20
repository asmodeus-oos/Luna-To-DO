package com.luna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val targetValue: Int = 100,
    val currentValue: Int = 0,
    val unit: String = "%",
    val colorHex: String = "#008FFD",
    val icon: String = "🎯",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
