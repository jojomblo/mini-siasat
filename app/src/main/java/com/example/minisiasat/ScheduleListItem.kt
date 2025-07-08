package com.example.minisiasat

import com.example.minisiasat.utils.Course

sealed class ScheduleListItem {
    data class CourseItem(val course: Course) : ScheduleListItem()
    data class DayHeader(val day: String) : ScheduleListItem()
}