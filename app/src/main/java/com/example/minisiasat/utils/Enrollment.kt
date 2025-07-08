package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Enrollment(
    val courses: Map<String, Boolean>? = null  // contoh: "TC531A" to true
)
