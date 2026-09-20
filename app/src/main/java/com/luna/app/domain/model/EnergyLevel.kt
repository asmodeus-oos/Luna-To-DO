package com.luna.app.domain.model

enum class EnergyLevel(
    val label: String,
    val icon: String,
    val description: String
) {
    HIGH("Deep Work", "⚡", "High focus & mental clarity required"),
    MEDIUM("Normal", "☕", "Standard balanced effort"),
    LOW("Quick Task", "🍃", "Low effort, easy win under 15m");

    companion object {
        fun fromString(value: String?): EnergyLevel {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
