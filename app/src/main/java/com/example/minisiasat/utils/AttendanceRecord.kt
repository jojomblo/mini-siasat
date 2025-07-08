package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class AttendanceRecord(
    val studentId: String? = null,
    val studentName: String? = null,
    val timestamp: Long? = null
) : Serializable