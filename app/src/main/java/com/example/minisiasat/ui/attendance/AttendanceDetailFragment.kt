package com.example.minisiasat.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.ui.schedule.EnrolledStudentAdapter
import com.example.minisiasat.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.AttendanceSession
import com.example.minisiasat.domain.model.Students

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

        val attendedRecyclerView = view.findViewById<RecyclerView>(R.id.attendedRecyclerView)
        attendedAdapter = EnrolledStudentAdapter(attendedList)
        attendedRecyclerView.layoutManager = LinearLayoutManager(context)
        attendedRecyclerView.adapter = attendedAdapter

        val absentRecyclerView = view.findViewById<RecyclerView>(R.id.absentRecyclerView)
        absentAdapter = EnrolledStudentAdapter(absentList)
        absentRecyclerView.layoutManager = LinearLayoutManager(context)
        absentRecyclerView.adapter = absentAdapter
        loadAttendanceDetails()
        return view
    }

    private fun loadAttendanceDetails() {
        DatabaseNodes.courseRostersRef.child(courseCode).child("students").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(rosterSnapshot: DataSnapshot) {
                val allStudentIdsInClass = rosterSnapshot.children.mapNotNull { it.key }

                val attendeesIds = session.attendees?.keys ?: emptySet()

                val attendedIds = allStudentIdsInClass.filter { it in attendeesIds }
                val absentIds = allStudentIdsInClass.filterNot { it in attendeesIds }

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