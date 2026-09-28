package com.luna.app.data.local

import com.luna.app.data.repository.TaskRepository
import com.luna.app.domain.model.Priority
import java.util.Calendar

data class TimetableSession(
    val title: String,
    val courseCode: String = "",
    val type: String = "Session", // "Lecture", "Clinic", "Lab", etc.
    val dayOfWeekName: String, // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    val calendarDayOfWeek: Int = Calendar.MONDAY,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val durationHours: Int = 1,
    val place: String = "",
    val group: String = "",
    val instructor: String = "",
    val priority: Priority = Priority.NONE,
    val extraTags: List<String> = emptyList()
)

data class CourseMeta(
    val code: String,
    val name: String,
    val colorHex: String,
    val darkBgHex: String,
    val lightBgHex: String,
    val department: String,
    val creditHours: String
)

typealias DentalCourseMeta = CourseMeta

object DentalTimetableSeeder {

    val SESSIONS: List<TimetableSession> = emptyList()

    val COURSES: List<CourseMeta> = emptyList()

    fun getCourseMeta(courseCode: String): CourseMeta {
        return CourseMeta(courseCode, courseCode, "#8B5CF6", "#2E1065", "#F5F3FF", "General", "1 Credit")
    }

    /**
     * Pre-injected mock tasks and sessions have been stripped from source.
     * All timetable sessions are now driven dynamically from user-created tasks.
     * @return 0
     */
    suspend fun seed(
        repository: TaskRepository,
        alarmScheduler: com.luna.app.notification.LunaAlarmScheduler? = null,
        overwrite: Boolean = false
    ): Int {
        return 0
    }
}
