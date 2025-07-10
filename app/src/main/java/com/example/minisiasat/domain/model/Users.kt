package com.example.minisiasat.domain.model

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Users(
    val email: String? = null,
    val name: String? = null,
    val role: String? = null,
    val kode: String? = null,
    val position: String? = null
) : Serializable
