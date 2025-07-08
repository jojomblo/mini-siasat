package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class CourseDetailFragment : Fragment() {

    private lateinit var course: Course

    // Untuk daftar mahasiswa
    private lateinit var enrolledStudentsRecyclerView: RecyclerView
    private lateinit var studentAdapter: EnrolledStudentAdapter
    private val studentList = mutableListOf<Pair<String, Students>>()

    // Untuk daftar sesi absensi
    private lateinit var attendanceSessionsRecyclerView: RecyclerView
    private lateinit var attendanceAdapter: AttendanceSessionAdapter
    private val attendanceSessionList = mutableListOf<AttendanceSession>()

    // Referensi ke node absensi untuk mata kuliah ini
    private val attendanceRef by lazy {
        DatabaseNodes.attendanceRef.child(course.courseCode!!)
    }

    companion object {
        private const val ARG_COURSE = "course"
        fun newInstance(course: Course): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            course = it.getSerializable(ARG_COURSE) as Course
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attendance_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.attendanceSessionsRecyclerView)

        // ==========================================================
        // --- PERUBAHAN UTAMA ADA DI BLOK INISIALISASI ADAPTER INI ---
        // ==========================================================
        attendanceAdapter = AttendanceSessionAdapter(sessionList) { session, action ->
            // Kita gunakan 'when' untuk membedakan aksi
            when (action) {
                AttendanceAction.TOGGLE -> toggleAttendanceStatus(session)
                AttendanceAction.VIEW_DETAILS -> viewAttendanceDetails(session)
            }
        }
        // ==========================================================
        // --- AKHIR DARI PERUBAHAN ---
        // ==========================================================

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = attendanceAdapter

        view.findViewById<View>(R.id.addAttendanceButton).setOnClickListener {
            showCreateAttendanceDialog()
        }

        loadAttendanceSessions()
        return view
    }
// ... (sisa fungsi lainnya tetap sama)

    private fun setupViews(view: View) {
        view.findViewById<TextView>(R.id.courseTitleTextView).text = course.courseName
        view.findViewById<TextView>(R.id.courseCodeTextView).text = course.courseCode

        // Setup RecyclerView Mahasiswa
        enrolledStudentsRecyclerView = view.findViewById(R.id.enrolledStudentsRecyclerView)
        studentAdapter = EnrolledStudentAdapter(studentList)
        enrolledStudentsRecyclerView.layoutManager = LinearLayoutManager(context)
        enrolledStudentsRecyclerView.adapter = studentAdapter

        // Setup RecyclerView Absensi
        attendanceSessionsRecyclerView = view.findViewById(R.id.attendanceSessionsRecyclerView)
        attendanceAdapter = AttendanceSessionAdapter(attendanceSessionList) { session ->
            // Aksi saat tombol Buka/Tutup di klik
            toggleAttendanceStatus(session)
        }
        attendanceSessionsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        attendanceSessionsRecyclerView.adapter = attendanceAdapter

        // Tombol untuk membuat sesi absensi baru
        view.findViewById<Button>(R.id.addAttendanceButton).setOnClickListener {
            showCreateAttendanceDialog()
        }
    }

    private fun showCreateAttendanceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_attendance, null)
        val topicInput = dialogView.findViewById<EditText>(R.id.topicInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buat Sesi Absensi Baru")
            .setView(dialogView)
            .setPositiveButton("Buat") { _, _ ->
                val topic = topicInput.text.toString().trim()
                if (topic.isNotEmpty()) {
                    createNewAttendanceSession(topic)
                } else {
                    Toast.makeText(context, "Nama pertemuan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createNewAttendanceSession(topic: String) {
        val sessionId = attendanceRef.push().key ?: return
        val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

        val newSession = AttendanceSession(
            sessionId = sessionId,
            topic = topic,
            date = date,
            isOpen = false // Default ditutup
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
                attendanceSessionList.clear()
                for (sessionSnap in snapshot.children) {
                    val session = sessionSnap.getValue(AttendanceSession::class.java)
                    if (session != null) {
                        attendanceSessionList.add(session)
                    }
                }
                attendanceSessionList.sortByDescending { it.date } // Urutkan dari yang terbaru
                attendanceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat sesi absensi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleAttendanceStatus(session: AttendanceSession) {
        val sessionId = session.sessionId ?: return
        val newStatus = !session.isOpen // Balikkan statusnya

        attendanceRef.child(sessionId).child("open").setValue(newStatus)
            .addOnSuccessListener {
                val statusText = if (newStatus) "dibuka" else "ditutup"
                Toast.makeText(context, "Absensi ${session.topic} berhasil $statusText", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengubah status: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi loadEnrolledStudents dan fetchStudentDetails tetap sama seperti sebelumnya
    private fun loadEnrolledStudents() {
        val courseCode = course.courseCode ?: return
        DatabaseNodes.courseRostersRef.child(courseCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(context, "Belum ada mahasiswa yang mengambil mata kuliah ini.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val roster = snapshot.getValue(CourseRoster::class.java)
                    val studentIds = roster?.students?.keys?.toList() ?: emptyList()
                    if (studentIds.isNotEmpty()) {
                        fetchStudentDetails(studentIds)
                    } else {
                        Toast.makeText(context, "Tidak ada mahasiswa terdaftar.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memuat data pendaftaran: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun fetchStudentDetails(studentIds: List<String>) {
        studentList.clear()
        DatabaseNodes.studentsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (id in studentIds) {
                    val student = snapshot.child(id).getValue(Students::class.java)
                    if (student != null) {
                        studentList.add(Pair(id, student))
                    }
                }
                studentList.sortBy { it.second.name }
                studentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat detail mahasiswa: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}