package com.luna.app.domain.model

import androidx.compose.ui.graphics.Color

enum class Priority(
    val level: Int,
    val label: String,
    val shortLabel: String,
    val color: Color
) {
    NONE(0, "No Priority", "", Color.Transparent),
    P4(1, "Low Priority", "P4", Color(0xFF94A3B8)),
    P3(2, "Medium Priority", "P3", Color(0xFF3B82F6)),
    P2(3, "High Priority", "P2", Color(0xFFF59E0B)),
    P1(4, "Urgent", "P1", Color(0xFFEF4444));

    companion object {
        fun fromLevel(level: Int): Priority {
            return entries.firstOrNull { it.level == level } ?: NONE
        }

        fun fromString(str: String?): Priority {
            if (str == null) return NONE
            val clean = str.trim().uppercase()
            return when {
                clean == "P1" || clean.contains("URGENT") -> P1
                clean == "P2" || clean.contains("HIGH") -> P2
                clean == "P3" || clean.contains("MED") -> P3
                clean == "P4" || clean.contains("LOW") -> P4
                else -> NONE
            }
        }
    }
}
