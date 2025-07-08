package com.example.minisiasat.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

object DatabaseNodes {

    // Firebase DB root
    private val database: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://mini-siasat-a36ae-default-rtdb.firebaseio.com")

    // Node names
    const val USERS = "users"
    const val STUDENTS = "students"
    const val LECTURERS = "lecturers"
    const val COURSES = "courses"
    const val ENROLLMENTS = "enrollments"
    const val ATTENDANCE = "attendance"
    const val GRADES = "grades"
    const val PERIODS = "periods"
    const val TRANSCRIPTS = "transcripts"

    // Firebase References
    val usersRef: DatabaseReference = database.getReference(USERS)
    val studentsRef: DatabaseReference = database.getReference(STUDENTS)
    val lecturersRef: DatabaseReference = database.getReference(LECTURERS)
    val coursesRef: DatabaseReference = database.getReference(COURSES)
    val enrollmentsRef: DatabaseReference = database.getReference(ENROLLMENTS)
    val attendanceRef: DatabaseReference = database.getReference(ATTENDANCE)
    val gradesRef: DatabaseReference = database.getReference(GRADES)
    val periodsRef: DatabaseReference = database.getReference(PERIODS)
    val transcriptsRef: DatabaseReference = database.getReference(TRANSCRIPTS)
}
