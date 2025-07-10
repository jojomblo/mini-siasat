package com.example.minisiasat.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Course(
    val courseCode: String? = null,
    val courseName: String? = null,
    val credits: Int? = null,
    val department: String? = null,
    val lecturerId: String? = null,
    val day: String? = null,
    val time: String? = null,
    val room: String? = null,
    val academicYear: String? = null,
    val semester: String? = null,
    val capacity: Int? = null
) : java.io.Serializable