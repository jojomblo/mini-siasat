package com.example.minisiasat.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Students(
    val name: String? = null,
    val major: String? = null,
    val entryYear: String? = null,
    val userUid: String? = null
): java.io.Serializable
