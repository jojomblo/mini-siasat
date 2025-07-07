package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Course(
    val courseCode: String? = null,
    val courseName: String? = null,
    val credits: Int? = null,
    val description: String? = null,
    val lecturerId: String? = null
)