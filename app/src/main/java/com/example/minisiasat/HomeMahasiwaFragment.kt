package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HomeMahasiswaFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var student: Students

    // UI Elements
    private lateinit var studentNameTextView: TextView
    private lateinit var studentNimTextView: TextView
    private lateinit var totalSksTextView: TextView
    private lateinit var semesterPositionTextView: TextView
    private lateinit var todayScheduleRecyclerView: RecyclerView
    private lateinit var noScheduleTextView: TextView

    companion object {
        fun newInstance(user: Users) = HomeMahasiswaFragment().apply {
            arguments = Bundle().apply { putSerializable("user", user) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable("user") as Users }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_mahasiswa, container, false)

        // Bind UI
        studentNameTextView = view.findViewById(R.id.studentNameTextView)
        studentNimTextView = view.findViewById(R.id.studentNimTextView)
        totalSksTextView = view.findViewById(R.id.totalSksTextView)
        semesterPositionTextView = view.findViewById(R.id.semesterPositionTextView)
        todayScheduleRecyclerView = view.findViewById(R.id.todayScheduleRecyclerView)
        noScheduleTextView = view.findViewById(R.id.noScheduleTextView)

        setupQuickAccessButtons(view)
        loadStudentData()

        return view
    }

    private fun loadStudentData() {
        val studentId = users.kode ?: return
        DatabaseNodes.studentsRef.child(studentId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Students::class.java)?.let {
                    student = it
                    // Setelah data mahasiswa didapat, tampilkan semua informasi
                    displayStudentInfo()
                    loadAcademicSummary()
                    loadTodaySchedule()
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun displayStudentInfo() {
        studentNameTextView.text = student.name
        studentNimTextView.text = "${users.kode} • Mahasiswa Aktif"
        semesterPositionTextView.text = calculateSemesterPosition()
    }

    private fun loadAcademicSummary() {
        val studentId = users.kode ?: return
        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val enrolledCodes = snapshot.children.mapNotNull { it.key }
                    calculateTotalSks(enrolledCodes)
                }
                override fun onCancelled(error: DatabaseError) { /* ... */ }
            })
    }

    private fun calculateTotalSks(codes: List<String>) {
        if (codes.isEmpty()) {
            totalSksTextView.text = "0"
            return
        }
        DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalCredits = 0
                codes.forEach { code ->
                    totalCredits += snapshot.child(code).child("credits").getValue(Int::class.java) ?: 0
                }
                totalSksTextView.text = "${totalCredits}/144"
            }
            override fun onCancelled(error: DatabaseError) { /* ... */ }
        })
    }

    private fun loadTodaySchedule() {
        noScheduleTextView.visibility = View.VISIBLE
        todayScheduleRecyclerView.visibility = View.GONE
    }

    private fun calculateSemesterPosition(): String {
        val entryYear = student.entryYear?.toIntOrNull() ?: return "N/A"
        val currentCalendar = Calendar.getInstance()
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentMonth = currentCalendar.get(Calendar.MONTH) // 0-11
        var academicYear = currentYear - entryYear

        // Ganjil: Agustus - Januari (bulan 7 - 0)
        // Genap: Februari - Juli (bulan 1 - 6)
        // Antara: (opsional, bisa ditambahkan jika perlu)
        val semesterType = if (currentMonth in 7..11){
            "Ganjil"
        }else if (currentMonth in 0..2){
            "Antara"
        }else{
            "Genap"
        }
        academicYear = if (currentMonth < 7) academicYear else academicYear + 1


        return "Tahun Ke-${academicYear } Semester $semesterType"
    }

    private fun setupQuickAccessButtons(view: View) {
        view.findViewById<Button>(R.id.quickAccessRegistrasi).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_registrasi)
        }
        view.findViewById<Button>(R.id.quickAccessKartuStudi).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_kartu_studi)
        }
        view.findViewById<Button>(R.id.quickAccessJadwal).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_jadwal_kuliah)
        }
        view.findViewById<Button>(R.id.quickAccessHasilStudi).setOnClickListener {
            (activity as? MainActivity)?.navigateToMenuItem(R.id.nav_hasil_studi)
        }
    }
}