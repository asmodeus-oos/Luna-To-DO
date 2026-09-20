package com.luna.app.domain.model

enum class SmartFilter(val label: String, val icon: String) {
    TODAY("Today", "☀️"),
    TOMORROW("Tomorrow", "🌅"),
    THIS_WEEK("This Week", "📅"),
    OVERDUE("Overdue", "⚠️"),
    PINNED("Pinned", "📌"),
    HIGH_PRIORITY("P1 Urgent", "🔥"),
    DEEP_WORK("Deep Work", "⚡"),
    QUICK_WINS("Quick Wins", "🍃"),
    ALL("All Tasks", "📋"),
    COMPLETED("Completed", "✅")
}
