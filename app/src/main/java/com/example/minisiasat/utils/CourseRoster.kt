package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CourseRoster(
    // Map berisi <studentId, true>
    // Contoh: "672022006" to true
    val students: Map<String, Boolean>? = null
)