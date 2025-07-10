package com.example.minisiasat.domain.model

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class AttendanceSession(
    val sessionId: String? = null,
    val topic: String? = null,
    val date: String? = null,
    var isOpen: Boolean = false,
    val attendees: Map<String, Boolean>? = null // Map dari NIM ke true
) : Serializable