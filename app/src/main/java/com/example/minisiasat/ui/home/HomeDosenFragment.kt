package com.example.minisiasat.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.MainActivity
import com.example.minisiasat.R
import com.example.minisiasat.ui.schedule.ScheduleAdapter
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HomeDosenFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var todayScheduleRecyclerView: RecyclerView
    private lateinit var noScheduleTextView: TextView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val todayCourses = mutableListOf<Course>()

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): HomeDosenFragment {
            val fragment = HomeDosenFragment()
            val args = Bundle()
            args.putSerializable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            users = it.getSerializable(ARG_USER) as Users
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_dosen, container, false)

        val greetingTextView = view.findViewById<TextView>(R.id.greetingTextView)
        val roleTextView = view.findViewById<TextView>(R.id.roleTextView)
        todayScheduleRecyclerView = view.findViewById(R.id.todayScheduleRecyclerView)
        noScheduleTextView = view.findViewById(R.id.noScheduleTextView)

        greetingTextView.text = "Selamat Datang, ${users.name}"
        roleTextView.text = "${users.position ?: "Dosen"}"

        scheduleAdapter = ScheduleAdapter(todayCourses)
        todayScheduleRecyclerView.layoutManager = LinearLayoutManager(context)
        todayScheduleRecyclerView.adapter = scheduleAdapter

        loadTodaySchedule()

        view.findViewById<Button>(R.id.quickAccessJadwal).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_jadwal_mengajar)
        }
        view.findViewById<Button>(R.id.quickAccessNilai).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_input_nilai)
        }

        return view
    }


    private fun loadTodaySchedule() {
        val lecturerId = users.kode ?: return

        val calendar = Calendar.getInstance()
        val dayName = SimpleDateFormat("EEEE", Locale("id", "ID")).format(calendar.time)

        DatabaseNodes.coursesRef.orderByChild("lecturerId").equalTo(lecturerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    todayCourses.clear()
                    for (courseSnap in snapshot.children) {
                        val course = courseSnap.getValue(Course::class.java)
                        if (course != null && course.day == dayName) {
                            todayCourses.add(course)
                        }
                    }

                    if (todayCourses.isEmpty()) {
                        todayScheduleRecyclerView.visibility = View.GONE
                        noScheduleTextView.visibility = View.VISIBLE
                    } else {
                        todayScheduleRecyclerView.visibility = View.VISIBLE
                        noScheduleTextView.visibility = View.GONE
                        todayCourses.sortBy { it.time }
                        scheduleAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}