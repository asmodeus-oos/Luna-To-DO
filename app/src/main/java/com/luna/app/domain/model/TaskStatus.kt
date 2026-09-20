package com.luna.app.domain.model

enum class TaskStatus(val label: String) {
    NOT_STARTED("To Do"),
    IN_PROGRESS("In Progress"),
    OVERDUE_PENDING_REASON("Overdue"),
    FAILED_LOGGED("Missed"),
    DONE("Completed");

    companion object {
        val TODO = NOT_STARTED

        fun fromString(name: String?): TaskStatus {
            return when (name?.uppercase()) {
                "TODO", "NOT_STARTED" -> NOT_STARTED
                "IN_PROGRESS" -> IN_PROGRESS
                "OVERDUE", "OVERDUE_PENDING_REASON" -> OVERDUE_PENDING_REASON
                "FAILED", "FAILED_LOGGED", "MISSED" -> FAILED_LOGGED
                "DONE", "COMPLETED" -> DONE
                else -> NOT_STARTED
            }
        }
    }
}
