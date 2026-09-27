package com.luna.app.data.local

import com.luna.app.data.repository.TaskRepository
import com.luna.app.domain.model.Priority
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class TimetableSession(
    val title: String,
    val courseCode: String,
    val type: String, // "Lecture" or "Clinic"
    val dayOfWeekName: String, // "MON", "TUE", "WED"
    val calendarDayOfWeek: Int, // Calendar.MONDAY, etc.
    val startHour: Int,
    val startMinute: Int = 0,
    val durationHours: Int,
    val place: String,
    val group: String,
    val instructor: String,
    val priority: Priority,
    val extraTags: List<String> = emptyList()
)

object DentalTimetableSeeder {

    val SESSIONS = listOf(
        // --- MONDAY ---
        TimetableSession(
            title = "Orthodontics I",
            courseCode = "OPD 511",
            type = "Lecture",
            dayOfWeekName = "MON",
            calendarDayOfWeek = Calendar.MONDAY,
            startHour = 10,
            durationHours = 1,
            place = "K302",
            group = "Full cohort",
            instructor = "Faculty Staff",
            priority = Priority.P2,
            extraTags = listOf("OPD511")
        ),
        TimetableSession(
            title = "Fixed Prosthodontics V",
            courseCode = "PDD 425",
            type = "Lecture",
            dayOfWeekName = "MON",
            calendarDayOfWeek = Calendar.MONDAY,
            startHour = 11,
            durationHours = 1,
            place = "K302",
            group = "Full cohort",
            instructor = "Faculty Staff",
            priority = Priority.P2,
            extraTags = listOf("PDD425")
        ),
        TimetableSession(
            title = "Fixed Prosthodontics V (Clinic)",
            courseCode = "PDD 425",
            type = "Clinic",
            dayOfWeekName = "MON",
            calendarDayOfWeek = Calendar.MONDAY,
            startHour = 13,
            durationHours = 2,
            place = "M116",
            group = "G4",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("PDD425", "G4")
        ),
        TimetableSession(
            title = "Orthodontics I (Clinic)",
            courseCode = "OPD 511",
            type = "Clinic",
            dayOfWeekName = "MON",
            calendarDayOfWeek = Calendar.MONDAY,
            startHour = 15,
            durationHours = 2,
            place = "M120",
            group = "G3",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("OPD511", "G3")
        ),

        // --- TUESDAY ---
        TimetableSession(
            title = "Periodontics I",
            courseCode = "OMR 531",
            type = "Lecture",
            dayOfWeekName = "TUE",
            calendarDayOfWeek = Calendar.TUESDAY,
            startHour = 9,
            durationHours = 2,
            place = "K302",
            group = "Full cohort",
            instructor = "Prof. Hossam Khalifa",
            priority = Priority.P2,
            extraTags = listOf("OMR531")
        ),
        TimetableSession(
            title = "Operative Dentistry V",
            courseCode = "CDD 415",
            type = "Lecture",
            dayOfWeekName = "TUE",
            calendarDayOfWeek = Calendar.TUESDAY,
            startHour = 11,
            durationHours = 1,
            place = "K302",
            group = "Full cohort",
            instructor = "Faculty Staff",
            priority = Priority.P2,
            extraTags = listOf("CDD415")
        ),
        TimetableSession(
            title = "Endodontics IV",
            courseCode = "CDD 424",
            type = "Lecture",
            dayOfWeekName = "TUE",
            calendarDayOfWeek = Calendar.TUESDAY,
            startHour = 12,
            durationHours = 1,
            place = "K302",
            group = "Full cohort",
            instructor = "Dr. Motaz Elsadat",
            priority = Priority.P2,
            extraTags = listOf("CDD424")
        ),
        TimetableSession(
            title = "Operative Dentistry V (Clinic)",
            courseCode = "CDD 415",
            type = "Clinic",
            dayOfWeekName = "TUE",
            calendarDayOfWeek = Calendar.TUESDAY,
            startHour = 13,
            durationHours = 2,
            place = "M106",
            group = "G6",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("CDD415", "G6")
        ),

        // --- WEDNESDAY ---
        TimetableSession(
            title = "Endodontics IV (Clinic)",
            courseCode = "CDD 424",
            type = "Clinic",
            dayOfWeekName = "WED",
            calendarDayOfWeek = Calendar.WEDNESDAY,
            startHour = 9,
            durationHours = 2,
            place = "M220",
            group = "G6",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("CDD424", "G6")
        ),
        TimetableSession(
            title = "Local & General Anesthesia",
            courseCode = "OMS 421",
            type = "Lecture",
            dayOfWeekName = "WED",
            calendarDayOfWeek = Calendar.WEDNESDAY,
            startHour = 11,
            durationHours = 1,
            place = "K301",
            group = "Full cohort",
            instructor = "Faculty Staff",
            priority = Priority.P2,
            extraTags = listOf("OMS421")
        ),
        TimetableSession(
            title = "Oral & Maxillofacial Surgery I",
            courseCode = "OMS 411",
            type = "Lecture",
            dayOfWeekName = "WED",
            calendarDayOfWeek = Calendar.WEDNESDAY,
            startHour = 12,
            durationHours = 1,
            place = "K301",
            group = "Full cohort",
            instructor = "Faculty Staff",
            priority = Priority.P2,
            extraTags = listOf("OMS411")
        ),
        TimetableSession(
            title = "Oral & Maxillofacial Surgery I (Clinic)",
            courseCode = "OMS 411",
            type = "Clinic",
            dayOfWeekName = "WED",
            calendarDayOfWeek = Calendar.WEDNESDAY,
            startHour = 13,
            durationHours = 2,
            place = "M117",
            group = "G5",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("OMS411", "G5")
        ),
        TimetableSession(
            title = "Periodontics I (Clinic)",
            courseCode = "OMR 531",
            type = "Clinic",
            dayOfWeekName = "WED",
            calendarDayOfWeek = Calendar.WEDNESDAY,
            startHour = 15,
            durationHours = 2,
            place = "K111 / M104",
            group = "G5",
            instructor = "Faculty Staff",
            priority = Priority.P1,
            extraTags = listOf("OMR531", "G5")
        )
    )

    data class DentalCourseMeta(
        val code: String,
        val name: String,
        val colorHex: String,
        val darkBgHex: String,
        val lightBgHex: String,
        val department: String,
        val creditHours: String
    )

    val COURSES = listOf(
        DentalCourseMeta("OPD 511", "Orthodontics I", "#6366F1", "#1E1B4B", "#EEF2FF", "Orthodontics Department", "3 Credits"),
        DentalCourseMeta("PDD 425", "Fixed Prosthodontics V", "#06B6D4", "#083344", "#ECFEFF", "Prosthodontics Department", "3 Credits"),
        DentalCourseMeta("OMR 531", "Periodontics I", "#F43F5E", "#4C0519", "#FFF1F2", "Periodontology & Diagnosis", "3 Credits"),
        DentalCourseMeta("CDD 415", "Operative Dentistry V", "#3B82F6", "#172554", "#EFF6FF", "Restorative & Operative", "3 Credits"),
        DentalCourseMeta("CDD 424", "Endodontics IV", "#EC4899", "#500724", "#FDF2F8", "Endodontics Department", "3 Credits"),
        DentalCourseMeta("OMS 421", "Local & General Anesthesia", "#F97316", "#431407", "#FFF7ED", "Oral & Maxillofacial Surgery", "2 Credits"),
        DentalCourseMeta("OMS 411", "Oral & Maxillofacial Surgery I", "#10B981", "#022C22", "#ECFDF5", "Oral & Maxillofacial Surgery", "3 Credits")
    )

    fun getCourseMeta(courseCode: String): DentalCourseMeta {
        return COURSES.firstOrNull { it.code.equals(courseCode, ignoreCase = true) }
            ?: DentalCourseMeta(courseCode, courseCode, "#8B5CF6", "#2E1065", "#F5F3FF", "Dental Faculty", "2 Credits")
    }

    /**
     * Calculates the nearest upcoming epoch timestamp for a given weekday and hour.
     */
    fun computeUpcomingEpoch(targetDayOfWeek: Int, hour: Int, minute: Int = 0): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Advance to target day of week if not already, or if today's time has already passed
        var attempts = 0
        while (attempts < 7 && (cal.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek || cal.timeInMillis < now - 3600_000L)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            attempts++
        }
        return cal.timeInMillis
    }

    /**
     * Seeds or injects the 13 Dentistry Level 4 timetable sessions into Luna database.
     * Skips existing tasks matching title + weeklyDay unless overwrite is specified.
     * Schedules alarms for start and finish.
     * @return Number of newly added or updated sessions
     */
    suspend fun seed(
        repository: TaskRepository,
        alarmScheduler: com.luna.app.notification.LunaAlarmScheduler? = null,
        overwrite: Boolean = false
    ): Int {
        val existingTasks = repository.getTasksWithDetailsFlow().first()
        var insertedCount = 0

        // 1. Purge any historical duplicate records already existing in the database:
        val groupedDuplicates = existingTasks
            .filter { it.task.weeklyDay != null }
            .groupBy { "${it.task.title.trim().lowercase()}_${it.task.weeklyDay}" }

        for ((_, group) in groupedDuplicates) {
            if (group.size > 1) {
                val toDelete = group.drop(1)
                for (dup in toDelete) {
                    repository.deleteTask(dup.task.id)
                    alarmScheduler?.cancelTaskAlarm(dup.task.id)
                }
            }
        }

        // Re-read current tasks after deduplication cleanup
        val currentTasks = repository.getTasksWithDetailsFlow().first()

        for (session in SESSIONS) {
            val existing = currentTasks.firstOrNull {
                it.task.title.equals(session.title, ignoreCase = true) &&
                        it.task.weeklyDay == session.dayOfWeekName
            }

            if (existing != null && !overwrite) {
                alarmScheduler?.scheduleTaskAlarms(existing.task)
                continue
            }

            val dueTimeStr = String.format("%02d:%02d", session.startHour, session.startMinute)
            val computedDueDate = computeUpcomingEpoch(session.calendarDayOfWeek, session.startHour, session.startMinute)

            val notesContent = buildString {
                appendLine("### Dentistry · Level 4")
                appendLine("- **Course**: ${session.title} (${session.courseCode})")
                appendLine("- **Type**: ${session.type}")
                appendLine("- **Room**: ${session.place}")
                appendLine("- **Cohort / Group**: ${session.group}")
                appendLine("- **Instructor**: ${session.instructor}")
                appendLine("- **Duration**: ${session.durationHours} Hour(s)")
                appendLine("- **Term**: Fall 2026-2027 · Week 7")
            }

            val tags = mutableListOf("Dentistry", session.type)
            tags.addAll(session.extraTags)

            if (existing != null && overwrite) {
                val updatedTask = existing.task.copy(
                    startDate = computedDueDate,
                    dueDate = computedDueDate,
                    dueTime = dueTimeStr,
                    alarmOnStart = true,
                    alarmOnFinish = true,
                    weeklyDay = session.dayOfWeekName,
                    weeklyTime = dueTimeStr,
                    place = session.place,
                    notes = notesContent,
                    priority = session.priority,
                    tags = tags.distinct()
                )
                repository.updateTask(updatedTask)
                alarmScheduler?.scheduleTaskAlarms(updatedTask)
                insertedCount++
            } else {
                val taskId = repository.addTask(
                    title = session.title,
                    notes = notesContent,
                    startDate = computedDueDate,
                    dueDate = computedDueDate,
                    dueTime = dueTimeStr,
                    deadlineMode = "FIXED",
                    durationValue = session.durationHours,
                    durationUnit = "HOURS",
                    priority = session.priority,
                    tags = tags.distinct(),
                    recurrenceRule = "WEEKLY",
                    estimatedMinutes = session.durationHours * 60,
                    category = "Dentistry",
                    alarmOnStart = true,
                    alarmOnFinish = true,
                    weeklyDay = session.dayOfWeekName,
                    weeklyTime = dueTimeStr,
                    place = session.place
                )
                val newlyAdded = repository.getTaskById(taskId)
                if (newlyAdded != null) {
                    alarmScheduler?.scheduleTaskAlarms(newlyAdded)
                }
                insertedCount++
            }
        }

        return insertedCount
    }
}
