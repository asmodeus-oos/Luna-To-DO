package com.luna.app.domain.model

enum class TaskFilter(val displayName: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    UPCOMING("Upcoming"),
    ALL("All Tasks"),
    COMPLETED("Completed")
}
