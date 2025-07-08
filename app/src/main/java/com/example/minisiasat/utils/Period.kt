package com.example.minisiasat.utils
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Period(
    val courseRegistrationOpen: Boolean? = null,
    val attendanceOpen: Boolean? = null,
    val gradeInputOpen: Boolean? = null,
    val startDate: String? = null,
    val endDate: String? = null
)
