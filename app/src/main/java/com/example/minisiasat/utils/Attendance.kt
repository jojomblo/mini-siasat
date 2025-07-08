package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Attendance(
    val isOpen: Boolean? = null,
    val students: Map<String, Boolean>? = null  // NIM ke kehadiran
)

