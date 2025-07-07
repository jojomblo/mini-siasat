package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Lecturer(
    val name: String? = null,
    val department: String? = null,
    val email: String? = null,
    val userUid: String? = null
)