package com.example.minisiasat.ui.coursecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.ui.schedule.GroupedScheduleAdapter
import com.example.minisiasat.R
import com.example.minisiasat.ui.schedule.ScheduleListData
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CourseCardFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupedScheduleAdapter
    private val enrolledCourses = mutableListOf<Course>()

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

        loadEnrolledCourses()
        return view
    }

    private fun loadEnrolledCourses() {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCourseCodes = enrolledSnapshot.children.mapNotNull { it.key }

                    DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(coursesSnapshot: DataSnapshot) {
                            enrolledCourses.clear()
                            enrolledCourseCodes.forEach { code ->
                                coursesSnapshot.child(code).getValue(Course::class.java)?.let {
                                    enrolledCourses.add(it)
                                }
                            }

                            // --- INI BAGIAN YANG DIPERBARUI ---
                            // Setup adapter dengan aksi untuk navigasi ke detail
                            adapter = GroupedScheduleAdapter(
                                items = enrolledCourses.map { ScheduleListData.CourseData(it) },
                                lecturerNames = emptyMap()
                            ) { selectedCourse ->
                                // Buka fragment detail saat item diklik
                                val fragment = CourseCardAdapter.newInstance(users, selectedCourse)
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            recyclerView.adapter = adapter
                            // ---------------------------------
                        }
                        override fun onCancelled(error: DatabaseError) { /* ... */ }
                    })
                }
                override fun onCancelled(error: DatabaseError) { /* ... */ }
            })
    }
}