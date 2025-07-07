package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Enrollment(
    val courseId: String? = null,
    val scheduleId: String? = null,
    val academicYear: String? = null,
    val semester: String? = null,
    val enrollmentDate: String? = null
)