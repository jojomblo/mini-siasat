package com.example.minisiasat.utils

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Period(
    val isRegistrationOpen: Boolean? = null,
    val isLectureOpen: Boolean? = null
)