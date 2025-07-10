package com.example.minisiasat.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.AttendanceSession
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Users
import com.example.minisiasat.ui.attendance.AttendanceAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ScheduleDetailFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var course: Course
    private lateinit var recyclerView: RecyclerView

    companion object {
        fun newInstance(user: Users, course: Course) = ScheduleDetailFragment().apply {
            arguments = Bundle().apply {
                putSerializable("user", user)
                putSerializable("course", course)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            users = it.getSerializable("user") as Users
            course = it.getSerializable("course") as Course
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jadwal_kuliah_detail, container, false)
        view.findViewById<TextView>(R.id.courseDetailTitle).text = course.courseName
        view.findViewById<TextView>(R.id.courseDetailInfo).text =
            "${course.courseCode} • ${course.credits} SKS • Ruang ${course.room}"

        recyclerView = view.findViewById(R.id.attendanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadAttendanceSessions()
        return view
    }

    private fun loadAttendanceSessions() {
        val courseCode = course.courseCode ?: return
        DatabaseNodes.attendanceRef.child(courseCode)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sessions = snapshot.children.mapNotNull {
                        it.getValue(AttendanceSession::class.java)
                    }
                    val adapter = AttendanceAdapter(sessions, users.kode!!) { sessionId ->
                        showAttendanceConfirmation(sessionId)
                    }
                    recyclerView.adapter = adapter
                }
                override fun onCancelled(error: DatabaseError) { /* ... */ }
            })
    }

    private fun showAttendanceConfirmation(sessionId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Absensi")
            .setMessage("Anda akan tercatat hadir pada sesi ini. Lanjutkan?")
            .setPositiveButton("Ya, Hadir") { _, _ ->
                markAsPresent(sessionId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun markAsPresent(sessionId: String) {
        val courseCode = course.courseCode ?: return
        val studentId = users.kode ?: return

        DatabaseNodes.attendanceRef.child(courseCode).child(sessionId)
            .child("attendees").child(studentId).setValue(true)
            .addOnSuccessListener {
                Toast.makeText(context, "Absensi berhasil!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal melakukan absensi.", Toast.LENGTH_SHORT).show()
            }
    }
}