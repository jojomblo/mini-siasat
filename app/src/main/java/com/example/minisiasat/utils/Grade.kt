package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Grade(
    val scheduleId: String? = null,
    val academicYear: String? = null,
    val semester: String? = null,
    val finalScore: Double? = null,
    val grade: String? = null
)
