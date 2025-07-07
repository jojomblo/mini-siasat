package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class StudentGpa(
    val gpa: Double? = null
)
