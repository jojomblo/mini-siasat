package com.example.minisiasat.ui.schedule

import com.example.minisiasat.domain.model.Course

sealed class ScheduleListData {
    data class CourseData(val course: Course) : ScheduleListData()
    data class DayHeader(val day: String) : ScheduleListData()
}