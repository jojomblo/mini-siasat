package com.example.minisiasat.domain.model

sealed class Schedule {
    data class CourseData(val course: Course) : Schedule()
    data class DayHeader(val day: String) : Schedule()
}