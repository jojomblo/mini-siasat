package com.example.minisiasat.utils
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Transcript(
    val GPA: Double? = null,
    val creditsTaken: Int? = null,
    val creditsPassed: Int? = null
)
