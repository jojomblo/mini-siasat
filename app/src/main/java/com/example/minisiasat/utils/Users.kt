package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Users (
    val email: String? = null,
    val name: String?= null,
    val role: String? = null,
    val kode: String? = null,
)