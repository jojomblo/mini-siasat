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
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Lecturer
import com.example.minisiasat.domain.model.Schedule
import com.example.minisiasat.domain.model.Users
import com.example.minisiasat.ui.course.CourseDetailFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LecturerScheduleFragment : Fragment() {

    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var adapter: GroupedScheduleAdapter
    private val displayList = mutableListOf<Schedule>()
    private val lecturerNamesMap = mutableMapOf<String, String>()
    private val dayOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    private var currentUser: Users? = null

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): LecturerScheduleFragment {
            val fragment = LecturerScheduleFragment()
            val args = Bundle()
            args.putSerializable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUser = it.getSerializable(ARG_USER) as? Users
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jadwal_mengajar, container, false)
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView)


        adapter = GroupedScheduleAdapter(displayList, lecturerNamesMap) { selectedCourse ->

            val fragment = CourseDetailFragment.newInstance(selectedCourse)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
        scheduleRecyclerView.adapter = adapter

        currentUser?.let {
            loadAllData(it.kode)
        } ?: run {
            Toast.makeText(context, "Gagal memuat data pengguna.", Toast.LENGTH_SHORT).show()
        }

        return view
    }


    private fun loadAllData(lecturerId: String?) {
        if (lecturerId == null) {
            Toast.makeText(context, "Kode Dosen tidak ditemukan.", Toast.LENGTH_SHORT).show()
            return
        }

        DatabaseNodes.lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerNamesMap.clear()
                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(Lecturer::class.java)
                    val id = lecturerSnap.key
                    if (lecturer != null && id != null && lecturer.name != null) {
                        lecturerNamesMap[id] = lecturer.name
                    }
                }
                loadTeachingSchedule(lecturerId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data dosen: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadTeachingSchedule(lecturerId: String) {
        DatabaseNodes.coursesRef
            .orderByChild("lecturerId")
            .equalTo(lecturerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val myCourses = mutableListOf<Course>()
                    for (snap in snapshot.children) {
                        val course = snap.getValue(Course::class.java)
                        if (course != null) {
                            myCourses.add(course)
                        }
                    }
                    processAndGroupSchedules(myCourses)
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memuat jadwal mengajar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun processAndGroupSchedules(courses: List<Course>) {
        displayList.clear()
        if (courses.isEmpty()) {
            Toast.makeText(context, "Anda tidak memiliki jadwal mengajar.", Toast.LENGTH_SHORT).show()
        }

        val groupedByDay = courses.groupBy { it.day }
        for (day in dayOrder) {
            groupedByDay[day]?.let { coursesOnThisDay ->
                displayList.add(Schedule.DayHeader(day))
                val sortedCourses = coursesOnThisDay.sortedBy { it.time?.substringBefore(" - ") }
                sortedCourses.forEach { course ->
                    displayList.add(Schedule.CourseData(course))
                }
            }
        }
    }
}