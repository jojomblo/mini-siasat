package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.ImageButton

class CourseDetailFragment : Fragment() {

    private lateinit var course: Course
    private lateinit var studentAdapter: EnrolledStudentAdapter
    private val studentList = mutableListOf<Pair<String, Students>>()

    companion object {
        private const val ARG_COURSE = "course"
        fun newInstance(course: Course): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            course = it.getSerializable(ARG_COURSE) as Course
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_detail, container, false)

        view.findViewById<TextView>(R.id.courseTitleTextView).text = course.courseName
        view.findViewById<TextView>(R.id.courseCodeTextView).text = course.courseCode

        val enrolledStudentsRecyclerView = view.findViewById<RecyclerView>(R.id.enrolledStudentsRecyclerView)
        studentAdapter = EnrolledStudentAdapter(studentList)
        enrolledStudentsRecyclerView.layoutManager = LinearLayoutManager(context)
        enrolledStudentsRecyclerView.adapter = studentAdapter

        view.findViewById<Button>(R.id.manageAttendanceButton).setOnClickListener {
            val fragment = AttendanceListFragment.newInstance(course.courseCode!!)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        loadEnrolledStudents()
        return view
    }

    private fun loadEnrolledStudents() {
        val courseCode = course.courseCode ?: return

        // --- PERBAIKAN DI SINI (menambahkan 'object :') ---
        DatabaseNodes.courseRostersRef.child(courseCode).child("students")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(rosterSnapshot: DataSnapshot) {
                    val studentIds = rosterSnapshot.children.mapNotNull { it.key }
                    if (studentIds.isNotEmpty()) {
                        fetchStudentDetails(studentIds)
                    } else {
                        Toast.makeText(context, "Belum ada mahasiswa yang terdaftar.", Toast.LENGTH_SHORT).show()
                        studentList.clear()
                        studentAdapter.notifyDataSetChanged()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memuat data mahasiswa: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchStudentDetails(ids: List<String>) {
        studentList.clear()

        // --- PERBAIKAN DI SINI (menambahkan 'object :') ---
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
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat detail mahasiswa: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}