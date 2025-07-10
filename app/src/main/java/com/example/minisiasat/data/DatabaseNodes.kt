package com.example.minisiasat.data

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
    const val ATTENDANCE = "attendance"
    const val GRADES = "grades"
    const val PERIODS = "periods"
    const val TRANSCRIPTS = "transcripts"

    // --- NODE BARU UNTUK ENROLLMENT ---
    const val STUDENT_ENROLLMENTS = "student_enrollments"
    const val COURSE_ROSTERS = "course_rosters"


    // Firebase References
    val usersRef: DatabaseReference = database.getReference(USERS)
    val studentsRef: DatabaseReference = database.getReference(STUDENTS)
    val lecturersRef: DatabaseReference = database.getReference(LECTURERS)
    val coursesRef: DatabaseReference = database.getReference(COURSES)
    val attendanceRef: DatabaseReference = database.getReference(ATTENDANCE)
    val gradesRef: DatabaseReference = database.getReference(GRADES)
    val periodsRef: DatabaseReference = database.getReference(PERIODS)
    val transcriptsRef: DatabaseReference = database.getReference(TRANSCRIPTS)

    val studentEnrollmentsRef: DatabaseReference = database.getReference(STUDENT_ENROLLMENTS)
    val courseRostersRef: DatabaseReference = database.getReference(COURSE_ROSTERS)
}