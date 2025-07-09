package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Course
import com.example.minisiasat.utils.DatabaseNodes
import com.example.minisiasat.utils.Lecturer
import com.example.minisiasat.utils.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class InputNilaiFragment : Fragment() {

    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var adapter: GroupedScheduleAdapter
    private val displayList = mutableListOf<ScheduleListItem>()
    private val lecturerNamesMap = mutableMapOf<String, String>()
    private val dayOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    private var currentUser: Users? = null

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): InputNilaiFragment {
            val fragment = InputNilaiFragment()
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
        val view = inflater.inflate(R.layout.fragment_input_nilai, container, false)
        coursesRecyclerView = view.findViewById(R.id.coursesForGradingRecyclerView)

        adapter = GroupedScheduleAdapter(displayList, lecturerNamesMap) { selectedCourse ->
            // Saat item mata kuliah diklik, buka fragment untuk input nilai
            val fragment = GradeInputFragment.newInstance(selectedCourse.courseCode!!)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        coursesRecyclerView.layoutManager = LinearLayoutManager(context)
        coursesRecyclerView.adapter = adapter

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
            Toast.makeText(context, "Anda tidak memiliki mata kuliah untuk dinilai.", Toast.LENGTH_SHORT).show()
        }

        val groupedByDay = courses.groupBy { it.day }
        for (day in dayOrder) {
            groupedByDay[day]?.let { coursesOnThisDay ->
                displayList.add(ScheduleListItem.DayHeader(day))
                val sortedCourses = coursesOnThisDay.sortedBy { it.time?.substringBefore(" - ") }
                sortedCourses.forEach { course ->
                    displayList.add(ScheduleListItem.CourseItem(course))
                }
            }
        }
    }
}