package com.example.minisiasat.ui.coursecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.ui.schedule.EnrolledStudentAdapter
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Students
import com.example.minisiasat.domain.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CourseCardAdapter : Fragment() {

    private lateinit var users: Users
    private lateinit var course: Course
    private lateinit var studentAdapter: EnrolledStudentAdapter
    private val studentList = mutableListOf<Pair<String, Students>>()

    companion object {
        private const val ARG_USER = "user"
        private const val ARG_COURSE = "course"
        fun newInstance(user: Users, course: Course): CourseCardAdapter {
            return CourseCardAdapter().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                    putSerializable(ARG_COURSE, course)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            users = it.getSerializable(ARG_USER) as Users
            course = it.getSerializable(ARG_COURSE) as Course
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kartu_studi_detail, container, false)
        view.findViewById<TextView>(R.id.courseTitleTextView).text = course.courseName
        view.findViewById<TextView>(R.id.courseCodeTextView).text = course.courseCode

        val enrolledStudentsRecyclerView = view.findViewById<RecyclerView>(R.id.enrolledStudentsRecyclerView)
        studentAdapter = EnrolledStudentAdapter(studentList)
        enrolledStudentsRecyclerView.layoutManager = LinearLayoutManager(context)
        enrolledStudentsRecyclerView.adapter = studentAdapter

        val dropButton = view.findViewById<Button>(R.id.dropCourseButton)

        DatabaseNodes.periodsRef.orderByChild("isRegistrationOpen").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dropButton.visibility = View.VISIBLE
                    } else {
                        dropButton.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    dropButton.visibility = View.GONE
                }
            })

        dropButton.setOnClickListener {
            showDropConfirmationDialog()
        }

        loadEnrolledStudents()
        return view
    }

    private fun loadEnrolledStudents() {
        val courseCode = course.courseCode ?: return
        DatabaseNodes.courseRostersRef.child(courseCode).child("students")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(rosterSnapshot: DataSnapshot) {
                    val studentIds = rosterSnapshot.children.mapNotNull { it.key }
                    if (studentIds.isNotEmpty()) {
                        fetchStudentDetails(studentIds)
                    }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun fetchStudentDetails(ids: List<String>) {
        studentList.clear()
        DatabaseNodes.studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ids.forEach { id ->
                    snapshot.child(id).getValue(Students::class.java)?.let { student ->
                        studentList.add(Pair(id, student))
                    }
                }
                studentList.sortBy { it.second.name }
                studentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun showDropConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Mata Kuliah")
            .setMessage("Anda yakin ingin menghapus (drop) mata kuliah ${course.courseName}?")
            .setPositiveButton("Hapus") { _, _ -> dropCourse() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun dropCourse() {
        val studentId = users.kode ?: return
        val courseCode = course.courseCode ?: return

        val updates = hashMapOf<String, Any?>(
            "student_enrollments/$studentId/courses/$courseCode" to null,
            "course_rosters/$courseCode/students/$studentId" to null
        )

        DatabaseNodes.coursesRef.root.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "${course.courseName} berhasil dihapus.", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Kembali ke halaman Kartu Studi
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}