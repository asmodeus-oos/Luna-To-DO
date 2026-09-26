package com.luna.app.domain.parser

import com.luna.app.domain.model.Priority
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTask(
    val title: String,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val priority: Priority = Priority.NONE,
    val tags: List<String> = emptyList(),
    val place: String? = null
)

object NaturalLanguageTaskParser {

    private val TAG_PATTERN = Pattern.compile("#([\\w-]+)", Pattern.CASE_INSENSITIVE)
    private val PLACE_PATTERN = Pattern.compile("@([\\w-]+)", Pattern.CASE_INSENSITIVE)
    private val PRIORITY_PATTERN = Pattern.compile("\\b(p[1-4]|!urgent|!high|!med|!low)\\b", Pattern.CASE_INSENSITIVE)
    private val TIME_12H_PATTERN = Pattern.compile("\\b(?:at\\s+)?(1[0-2]|[1-9])(?::([0-5][0-9]))?\\s*(am|pm)\\b", Pattern.CASE_INSENSITIVE)
    private val TIME_24H_PATTERN = Pattern.compile("\\b(?:at\\s+)?([0-1]?[0-9]|2[0-3]):([0-5][0-9])\\b", Pattern.CASE_INSENSITIVE)
    private val DATE_KEYWORD_PATTERN = Pattern.compile(
        "\\b(?:on\\s+)?(today|tomorrow|tonight|next\\s+week|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(input: String): ParsedTask {
        if (input.isBlank()) return ParsedTask(title = "")

        var working = input.trim()

        // 1. Extract Tags
        val tags = mutableListOf<String>()
        val tagMatcher = TAG_PATTERN.matcher(working)
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1)!!.lowercase(Locale.getDefault()))
        }
        working = tagMatcher.replaceAll(" ")

        // 1.5 Extract Place (@place)
        var place: String? = null
        val placeMatcher = PLACE_PATTERN.matcher(working)
        if (placeMatcher.find()) {
            place = placeMatcher.group(1)?.replace("_", " ")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            working = placeMatcher.replaceFirst(" ")
        }

        // 2. Extract Priority
        var priority = Priority.NONE
        val priorityMatcher = PRIORITY_PATTERN.matcher(working)
        if (priorityMatcher.find()) {
            priority = Priority.fromString(priorityMatcher.group(1))
            working = priorityMatcher.replaceFirst(" ")
        }

        // 3. Extract Time
        var hourOfDay: Int? = null
        var minuteOfHour: Int? = null
        var timeLabel: String? = null

        val time12Matcher = TIME_12H_PATTERN.matcher(working)
        if (time12Matcher.find()) {
            val h = time12Matcher.group(1)!!.toInt()
            val m = time12Matcher.group(2)?.toInt() ?: 0
            val amPm = time12Matcher.group(3)!!.lowercase(Locale.getDefault())

            hourOfDay = when {
                amPm == "pm" && h < 12 -> h + 12
                amPm == "am" && h == 12 -> 0
                else -> h
            }
            minuteOfHour = m
            timeLabel = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour)
            working = time12Matcher.replaceFirst(" ")
        } else {
            val time24Matcher = TIME_24H_PATTERN.matcher(working)
            if (time24Matcher.find()) {
                val h = time24Matcher.group(1)!!.toInt()
                val m = time24Matcher.group(2)!!.toInt()
                hourOfDay = h
                minuteOfHour = m
                timeLabel = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour)
                working = time24Matcher.replaceFirst(" ")
            }
        }

        // 4. Extract Date
        var dueDate: Long? = null
        val dateMatcher = DATE_KEYWORD_PATTERN.matcher(working)
        if (dateMatcher.find()) {
            val dateKeyword = dateMatcher.group(1)!!.lowercase(Locale.getDefault())
            val cal = Calendar.getInstance()

            when (dateKeyword) {
                "today" -> {
                    // today
                }
                "tomorrow" -> {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                "tonight" -> {
                    if (hourOfDay == null) {
                        hourOfDay = 20
                        minuteOfHour = 0
                        timeLabel = "20:00"
                    }
                }
                "next week" -> {
                    cal.add(Calendar.DAY_OF_YEAR, 7)
                }
                else -> {
                    // Day of week
                    val targetDay = when (dateKeyword) {
                        "sunday" -> Calendar.SUNDAY
                        "monday" -> Calendar.MONDAY
                        "tuesday" -> Calendar.TUESDAY
                        "wednesday" -> Calendar.WEDNESDAY
                        "thursday" -> Calendar.THURSDAY
                        "friday" -> Calendar.FRIDAY
                        "saturday" -> Calendar.SATURDAY
                        else -> null
                    }
                    if (targetDay != null) {
                        var diff = targetDay - cal.get(Calendar.DAY_OF_WEEK)
                        if (diff <= 0) diff += 7
                        cal.add(Calendar.DAY_OF_YEAR, diff)
                    }
                }
            }

            if (hourOfDay != null) {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minuteOfHour ?: 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 0)
            }
            dueDate = cal.timeInMillis
            working = dateMatcher.replaceFirst(" ")
        } else if (hourOfDay != null) {
            // Time specified without date keyword defaults to today (or tomorrow if time passed)
            val cal = Calendar.getInstance()
            val now = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minuteOfHour ?: 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            if (cal.before(now)) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            dueDate = cal.timeInMillis
        }

        // 5. Clean up title
        val cleanedTitle = working
            .replace("\\s+".toRegex(), " ")
            .trim()
            .removeSuffix(" at")
            .removeSuffix(" on")
            .trim()

        return ParsedTask(
            title = cleanedTitle.ifBlank { input.trim() },
            dueDate = dueDate,
            dueTime = timeLabel,
            priority = priority,
            tags = tags,
            place = place
        )
    }
}
