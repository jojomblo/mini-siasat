package com.example.minisiasat.ui.schedule

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
import com.example.minisiasat.domain.model.Users
import com.example.minisiasat.domain.model.Schedule
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ScheduleFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private val dayOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    companion object {
        fun newInstance(user: Users) = ScheduleFragment().apply {
            arguments = Bundle().apply { putSerializable("user", user) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable("user") as Users }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jadwal_kuliah, container, false)
        recyclerView = view.findViewById(R.id.scheduleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Memulai proses pengambilan data dari data dosen
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
                // Setelah data dosen didapat, ambil jadwal mahasiswa
                loadStudentSchedule(lecturerNamesMap)
            }

            override fun onCancelled(error: DatabaseError) {
                loadStudentSchedule(emptyMap())
                Toast.makeText(context, "Gagal memuat data dosen.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadStudentSchedule(lecturerNames: Map<String, String>) {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCodes = enrolledSnapshot.children.mapNotNull { it.key }
                    fetchCourseDetails(enrolledCodes, lecturerNames)
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun fetchCourseDetails(codes: List<String>, lecturerNames: Map<String, String>) {
        DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coursesSnapshot: DataSnapshot) {
                val enrolledCourses = codes.mapNotNull { coursesSnapshot.child(it).getValue(Course::class.java) }

                val groupedByDay = enrolledCourses.groupBy { it.day }
                val displayList = mutableListOf<Schedule>()
                for (day in dayOrder) {
                    groupedByDay[day]?.let { coursesOnDay ->
                        displayList.add(Schedule.DayHeader(day))
                        val sortedCourses = coursesOnDay.sortedBy { it.time?.substringBefore(" - ") }
                        sortedCourses.forEach { course ->
                            displayList.add(Schedule.CourseData(course))
                        }
                    }
                }

                val adapter = GroupedScheduleAdapter(displayList, lecturerNames) { selectedCourse ->
                    val fragment = ScheduleDetailFragment.newInstance(users, selectedCourse)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }
}