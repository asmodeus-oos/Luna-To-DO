package com.luna.app.domain.model

import java.util.Calendar

enum class RecurrenceType(val label: String) {
    NONE("Does not repeat"),
    DAILY("Every day"),
    WEEKDAYS("Weekdays (Mon–Fri)"),
    WEEKLY("Every week"),
    MONTHLY("Every month");

    fun nextOccurrenceEpoch(currentDueDate: Long?): Long {
        val base = Calendar.getInstance().apply {
            if (currentDueDate != null && currentDueDate > 0) {
                timeInMillis = currentDueDate
            }
        }

        val now = Calendar.getInstance()
        // If base is in the past, adjust base to today before advancing
        if (base.before(now)) {
            base.set(Calendar.YEAR, now.get(Calendar.YEAR))
            base.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR))
        }

        when (this) {
            NONE -> return base.timeInMillis
            DAILY -> base.add(Calendar.DAY_OF_YEAR, 1)
            WEEKDAYS -> {
                do {
                    base.add(Calendar.DAY_OF_YEAR, 1)
                } while (base.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    base.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            WEEKLY -> base.add(Calendar.WEEK_OF_YEAR, 1)
            MONTHLY -> base.add(Calendar.MONTH, 1)
        }
        return base.timeInMillis
    }

    companion object {
        fun fromString(str: String?): RecurrenceType {
            if (str == null) return NONE
            return try {
                valueOf(str.uppercase())
            } catch (_: Exception) {
                NONE
            }
        }
    }
}
