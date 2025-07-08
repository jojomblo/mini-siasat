package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class StudentEnrollment(
    // Map berisi <courseCode, true>
    // Contoh: "TC531A" to true
    val courses: Map<String, Boolean>? = null
)