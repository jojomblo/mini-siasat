package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.ImageButton

class AttendanceDetailFragment : Fragment() {

    private lateinit var session: AttendanceSession
    private lateinit var courseCode: String

    private lateinit var attendedAdapter: EnrolledStudentAdapter
    private lateinit var absentAdapter: EnrolledStudentAdapter

    private val attendedList = mutableListOf<Pair<String, Students>>()
    private val absentList = mutableListOf<Pair<String, Students>>()

    companion object {
        private const val ARG_SESSION = "session"
        private const val ARG_COURSE_CODE = "course_code"
        fun newInstance(session: AttendanceSession, courseCode: String): AttendanceDetailFragment {
            val fragment = AttendanceDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_SESSION, session)
            args.putString(ARG_COURSE_CODE, courseCode)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            session = it.getSerializable(ARG_SESSION) as AttendanceSession
            courseCode = it.getString(ARG_COURSE_CODE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attendance_detail, container, false)
        view.findViewById<TextView>(R.id.sessionTopicTextView).text = session.topic

        // Setup RecyclerView untuk mahasiswa yang HADIR
        val attendedRecyclerView = view.findViewById<RecyclerView>(R.id.attendedRecyclerView)
        attendedAdapter = EnrolledStudentAdapter(attendedList)
        attendedRecyclerView.layoutManager = LinearLayoutManager(context)
        attendedRecyclerView.adapter = attendedAdapter

        // Setup RecyclerView untuk mahasiswa yang BELUM HADIR
        val absentRecyclerView = view.findViewById<RecyclerView>(R.id.absentRecyclerView)
        absentAdapter = EnrolledStudentAdapter(absentList)
        absentRecyclerView.layoutManager = LinearLayoutManager(context)
        absentRecyclerView.adapter = absentAdapter
        loadAttendanceDetails()
        return view
    }

    private fun loadAttendanceDetails() {
        // 1. Ambil semua mahasiswa yang terdaftar di mata kuliah ini dari course_rosters
        DatabaseNodes.courseRostersRef.child(courseCode).child("students").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(rosterSnapshot: DataSnapshot) {
                val allStudentIdsInClass = rosterSnapshot.children.mapNotNull { it.key }

                // 2. Ambil semua mahasiswa yang sudah absen di sesi ini dari attendance node
                val attendeesIds = session.attendees?.keys ?: emptySet()

                // 3. Pisahkan ID mahasiswa
                val attendedIds = allStudentIdsInClass.filter { it in attendeesIds }
                val absentIds = allStudentIdsInClass.filterNot { it in attendeesIds }

                // 4. Ambil detail nama dan tampilkan di RecyclerView yang sesuai
                fetchStudentDetails(attendedIds, attendedList, attendedAdapter)
                fetchStudentDetails(absentIds, absentList, absentAdapter)
            }
            override fun onCancelled(error: DatabaseError) { /* ... */ }
        })
    }

    private fun fetchStudentDetails(ids: List<String>, targetList: MutableList<Pair<String, Students>>, targetAdapter: EnrolledStudentAdapter) {
        targetList.clear()
        if (ids.isEmpty()) {
            targetAdapter.notifyDataSetChanged()
            return
        }

        DatabaseNodes.studentsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ids.forEach { id ->
                    snapshot.child(id).getValue(Students::class.java)?.let { student ->
                        targetList.add(Pair(id, student))
                    }
                }
                targetList.sortBy { it.second.name }
                targetAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) { /* ... */ }
        })
    }
}