package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class JadwalKuliahFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private val dayOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    companion object {
        fun newInstance(user: Users) = JadwalKuliahFragment().apply {
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
        loadStudentSchedule()
        return view
    }

    private fun loadStudentSchedule() {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCodes = enrolledSnapshot.children.mapNotNull { it.key }
                    fetchCourseDetails(enrolledCodes)
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun fetchCourseDetails(codes: List<String>) {
        DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coursesSnapshot: DataSnapshot) {
                val enrolledCourses = codes.mapNotNull { coursesSnapshot.child(it).getValue(Course::class.java) }

                // Kelompokkan berdasarkan hari dan urutkan
                val groupedByDay = enrolledCourses.groupBy { it.day }
                val displayList = mutableListOf<ScheduleListItem>()
                for (day in dayOrder) {
                    groupedByDay[day]?.let { coursesOnDay ->
                        displayList.add(ScheduleListItem.DayHeader(day))
                        val sortedCourses = coursesOnDay.sortedBy { it.time?.substringBefore(" - ") }
                        sortedCourses.forEach { course ->
                            displayList.add(ScheduleListItem.CourseItem(course))
                        }
                    }
                }

                val adapter = GroupedScheduleAdapter(displayList, emptyMap()) { selectedCourse ->
                    // Buka halaman detail absensi
                    val fragment = JadwalKuliahDetailFragment.newInstance(users, selectedCourse)
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