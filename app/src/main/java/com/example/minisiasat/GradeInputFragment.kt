package com.example.minisiasat

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
import com.example.minisiasat.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GradeInputFragment : Fragment() {

    private lateinit var courseCode: String
    private lateinit var courseTitleTextView: TextView
    private lateinit var studentsRecyclerView: RecyclerView
    private lateinit var adapter: GradeInputAdapter

    // Daftar untuk menggabungkan data mahasiswa dan nilainya
    private val studentGradeList = mutableListOf<Triple<String, Students, Grade?>>()

    companion object {
        private const val ARG_COURSE_CODE = "course_code"
        fun newInstance(courseCode: String): GradeInputFragment {
            val fragment = GradeInputFragment()
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
        val view = inflater.inflate(R.layout.fragment_grade_input, container, false)
        courseTitleTextView = view.findViewById(R.id.gradeInputCourseTitle)
        studentsRecyclerView = view.findViewById(R.id.studentsForGradingRecyclerView)

        adapter = GradeInputAdapter { studentId, _ ->
            showGradeSelectionDialog(studentId)
        }

        studentsRecyclerView.layoutManager = LinearLayoutManager(context)
        studentsRecyclerView.adapter = adapter

        loadCourseAndStudentData()

        return view
    }

    private fun showGradeSelectionDialog(studentId: String) {
        val grades = arrayOf("A", "AB", "B", "BC", "C", "D", "E")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Nilai untuk $studentId")
            .setItems(grades) { _, which ->
                val selectedGrade = grades[which]
                saveGrade(studentId, selectedGrade)
            }
            .show()
    }

    private fun saveGrade(studentId: String, gradeValue: String) {
        val gradeData = Grade(
            academicYear = "2024/2025", // Ini bisa dibuat dinamis nanti
            semester = "Ganjil", // Ini bisa dibuat dinamis nanti
            finalScore = null, // Skor angka bisa ditambahkan nanti
            grade = gradeValue
        )

        DatabaseNodes.gradesRef.child(studentId).child(courseCode).setValue(gradeData)
            .addOnSuccessListener {
                Toast.makeText(context, "Nilai untuk $studentId berhasil disimpan", Toast.LENGTH_SHORT).show()
                // Tidak perlu reload semua data, cukup update list lokal dan refresh adapter
                val index = studentGradeList.indexOfFirst { it.first == studentId }
                if (index != -1) {
                    val (nim, student, _) = studentGradeList[index]
                    studentGradeList[index] = Triple(nim, student, gradeData)
                    adapter.submitList(studentGradeList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menyimpan nilai: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadCourseAndStudentData() {
        // 1. Dapatkan detail mata kuliah untuk menampilkan judul
        DatabaseNodes.coursesRef.child(courseCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val course = snapshot.getValue(Course::class.java)
                courseTitleTextView.text = "${course?.courseCode} - ${course?.courseName}"
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })

        // 2. Dapatkan daftar mahasiswa dari course_rosters
        DatabaseNodes.courseRostersRef.child(courseCode).child("students")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(rosterSnapshot: DataSnapshot) {
                    val studentIds = rosterSnapshot.children.mapNotNull { it.key }
                    if (studentIds.isNotEmpty()) {
                        // 3. Gabungkan dengan data dari 'students' dan 'grades'
                        combineStudentAndGradeData(studentIds)
                    } else {
                        Toast.makeText(context, "Belum ada mahasiswa di kelas ini.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun combineStudentAndGradeData(studentIds: List<String>) {
        // Ambil semua data students dan grades sekali jalan untuk efisiensi
        DatabaseNodes.studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentsSnapshot: DataSnapshot) {
                DatabaseNodes.gradesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(gradesSnapshot: DataSnapshot) {
                        studentGradeList.clear()
                        studentIds.forEach { id ->
                            val student = studentsSnapshot.child(id).getValue(Students::class.java)
                            val grade = gradesSnapshot.child(id).child(courseCode).getValue(Grade::class.java)
                            if (student != null) {
                                studentGradeList.add(Triple(id, student, grade))
                            }
                        }
                        studentGradeList.sortBy { it.second.name } // Urutkan berdasarkan nama
                        adapter.submitList(studentGradeList)
                    }
                    override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                })
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }
}