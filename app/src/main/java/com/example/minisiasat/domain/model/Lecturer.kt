package com.example.minisiasat.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Lecturer(
    val name: String? = null,
    val department: String? = null,
    val userUid: String? = null
): java.io.Serializable
