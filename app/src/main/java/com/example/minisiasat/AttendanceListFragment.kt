package com.example.minisiasat

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.AttendanceSession
import com.example.minisiasat.utils.DatabaseNodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AttendanceListFragment : Fragment() {

    private lateinit var courseCode: String
    private lateinit var attendanceAdapter: AttendanceSessionAdapter
    private val sessionList = mutableListOf<AttendanceSession>()
    private val attendanceRef by lazy { DatabaseNodes.attendanceRef.child(courseCode) }

    companion object {
        private const val ARG_COURSE_CODE = "course_code"
        fun newInstance(courseCode: String): AttendanceListFragment {
            val fragment = AttendanceListFragment()
            val args = Bundle()
            args.putString(ARG_COURSE_CODE, courseCode)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString(ARG_COURSE_CODE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attendance_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.attendanceSessionsRecyclerView)

        attendanceAdapter = AttendanceSessionAdapter(sessionList) { session, action ->
            when (action) {
                AttendanceAction.TOGGLE -> toggleAttendanceStatus(session)
                AttendanceAction.VIEW_DETAILS -> viewAttendanceDetails(session)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = attendanceAdapter

        view.findViewById<View>(R.id.addAttendanceButton).setOnClickListener {
            showCreateAttendanceDialog()
        }

        loadAttendanceSessions()
        return view
    }

    private fun showCreateAttendanceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_attendance, null)
        val topicInput = dialogView.findViewById<EditText>(R.id.topicInput)
        val dateInput = dialogView.findViewById<EditText>(R.id.dateInput)

        // Membuat input tanggal tidak bisa diketik, hanya bisa dipilih dari kalender
        dateInput.isFocusable = false
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                // Format tanggal sesuai keinginan
                val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(selectedDate.time)
                dateInput.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buat Sesi Absensi Baru")
            .setView(dialogView)
            .setPositiveButton("Buat") { _, _ ->
                val topic = topicInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                if (topic.isNotEmpty() && date.isNotEmpty()) {
                    createNewAttendanceSession(topic, date)
                } else {
                    Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createNewAttendanceSession(topic: String, date: String) {
        val sessionId = attendanceRef.push().key ?: return

        val newSession = AttendanceSession(
            sessionId = sessionId,
            topic = topic,
            date = date,
            isOpen = false
        )

        attendanceRef.child(sessionId).setValue(newSession).addOnSuccessListener {
            Toast.makeText(context, "Sesi absensi berhasil dibuat", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAttendanceSessions() {
        attendanceRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sessionList.clear()
                for (sessionSnap in snapshot.children) {
                    sessionSnap.getValue(AttendanceSession::class.java)?.let { sessionList.add(it) }
                }
                sessionList.sortByDescending { it.date }
                attendanceAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat sesi absensi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- FUNGSI YANG HILANG (SEKARANG SUDAH ADA) ---
    private fun toggleAttendanceStatus(session: AttendanceSession) {
        val sessionId = session.sessionId ?: return
        val newStatus = !session.isOpen

        attendanceRef.child(sessionId).child("open").setValue(newStatus)
    }

    // --- FUNGSI YANG HILANG (SEKARANG SUDAH ADA) ---
    private fun viewAttendanceDetails(session: AttendanceSession) {
        val fragment = AttendanceDetailFragment.newInstance(session, courseCode)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}