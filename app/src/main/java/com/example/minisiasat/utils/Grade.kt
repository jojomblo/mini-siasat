package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Grade(
    val academicYear: String? = null,
    val semester: String? = null,
    val finalScore: Int? = null,
    val grade: String? = null
)
