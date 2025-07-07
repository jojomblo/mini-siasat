package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Schedule(
    val courseId: String? = null,
    val lecturerId: String? = null,
    val day: String? = null,
    val time: String? = null,
    val room: String? = null,
    val semester: String? = null,
    val academicYear: String? = null,
    val isAttendanceOpen: Boolean? = false
)
