package com.example.minisiasat.ui.gradereport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Grade
import com.example.minisiasat.domain.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.minisiasat.domain.model.CourseGrade

class GradeReportFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GradeReportAdapter
    private val courseGradeList = mutableListOf<CourseGrade>()

    companion object {
        fun newInstance(user: Users) = GradeReportFragment().apply {
            arguments = Bundle().apply { putSerializable("user", user) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable("user") as Users }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_hasil_studi, container, false)
        recyclerView = view.findViewById(R.id.hasilStudiRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = GradeReportAdapter(courseGradeList)
        recyclerView.adapter = adapter

        loadEnrolledCoursesWithGrades()
        return view
    }

    private fun loadEnrolledCoursesWithGrades() {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCourseCodes = enrolledSnapshot.children.mapNotNull { it.key }
                    if (enrolledCourseCodes.isNotEmpty()) {
                        fetchDetailsAndGrades(enrolledCourseCodes, studentId)
                    }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun fetchDetailsAndGrades(courseCodes: List<String>, studentId: String) {
        DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coursesSnapshot: DataSnapshot) {
                DatabaseNodes.gradesRef.child(studentId).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(gradesSnapshot: DataSnapshot) {
                        courseGradeList.clear()
                        courseCodes.forEach { code ->
                            val course = coursesSnapshot.child(code).getValue(Course::class.java)
                            val grade = gradesSnapshot.child(code).getValue(Grade::class.java)
                            if (course != null) {
                                courseGradeList.add(CourseGrade(course, grade))
                            }
                        }
                        courseGradeList.sortBy { it.course.courseCode } // Urutkan
                        adapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                })
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }
}