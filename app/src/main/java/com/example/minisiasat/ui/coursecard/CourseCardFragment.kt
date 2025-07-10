package com.example.minisiasat.ui.coursecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Lecturer
import com.example.minisiasat.domain.model.Schedule
import com.example.minisiasat.domain.model.Users
import com.example.minisiasat.ui.schedule.GroupedScheduleAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CourseCardFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): CourseCardFragment {
            return CourseCardFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable(ARG_USER) as Users }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kartu_studi, container, false)
        recyclerView = view.findViewById(R.id.enrolledCoursesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadLecturerData()
        return view
    }

    private fun loadLecturerData() {
        val lecturerNamesMap = mutableMapOf<String, String>()
        DatabaseNodes.lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(Lecturer::class.java)
                    val id = lecturerSnap.key
                    if (lecturer != null && id != null && lecturer.name != null) {
                        lecturerNamesMap[id] = lecturer.name
                    }
                }
                loadEnrolledCourses(lecturerNamesMap)
            }

            override fun onCancelled(error: DatabaseError) {
                loadEnrolledCourses(emptyMap())
                Toast.makeText(context, "Gagal memuat data dosen.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadEnrolledCourses(lecturerNames: Map<String, String>) {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCourseCodes = enrolledSnapshot.children.mapNotNull { it.key }

                    DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(coursesSnapshot: DataSnapshot) {
                            val enrolledCourses = mutableListOf<Course>()
                            enrolledCourseCodes.forEach { code ->
                                coursesSnapshot.child(code).getValue(Course::class.java)?.let {
                                    enrolledCourses.add(it)
                                }
                            }

                            val adapter = GroupedScheduleAdapter(
                                items = enrolledCourses.map { Schedule.CourseData(it) },
                                lecturerNames = lecturerNames,
                                onItemClicked = { selectedCourse ->
                                    // PERBAIKAN: Menggunakan nama class yang benar
                                    val fragment = CourseCardAdapter.newInstance(users, selectedCourse)
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            )
                            recyclerView.adapter = adapter
                        }
                        override fun onCancelled(error: DatabaseError) { /* ... */ }
                    })
                }
                override fun onCancelled(error: DatabaseError) { /* ... */ }
            })
    }
}