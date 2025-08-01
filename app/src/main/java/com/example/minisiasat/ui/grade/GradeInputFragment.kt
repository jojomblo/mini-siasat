package com.example.minisiasat.ui.grade

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
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Grade
import com.example.minisiasat.domain.model.Students
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GradeInputFragment : Fragment() {

    private lateinit var courseCode: String
    private var course: Course? = null
    private lateinit var courseTitleTextView: TextView
    private lateinit var studentsRecyclerView: RecyclerView
    private lateinit var adapter: CourseGradingAdapter

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

        adapter = CourseGradingAdapter { studentId, _ ->
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
        val currentCourse = course
        if (currentCourse == null) {
            Toast.makeText(context, "Data mata kuliah tidak ditemukan, tidak dapat menyimpan nilai.", Toast.LENGTH_SHORT).show()
            return
        }

        // PERBAIKAN: Menggunakan data dari variabel `course`
        val gradeData = Grade(
            academicYear = currentCourse.academicYear,
            semester = currentCourse.semester,
            finalScore = null,
            grade = gradeValue
        )

        DatabaseNodes.gradesRef.child(studentId).child(courseCode).setValue(gradeData)
            .addOnSuccessListener {
                Toast.makeText(context, "Nilai untuk $studentId berhasil disimpan", Toast.LENGTH_SHORT).show()
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
        DatabaseNodes.coursesRef.child(courseCode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // PERBAIKAN: Menyimpan objek Course ke dalam variabel
                course = snapshot.getValue(Course::class.java)
                courseTitleTextView.text = "${course?.courseCode} - ${course?.courseName}"
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data mata kuliah: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        DatabaseNodes.courseRostersRef.child(courseCode).child("students")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(rosterSnapshot: DataSnapshot) {
                    val studentIds = rosterSnapshot.children.mapNotNull { it.key }
                    if (studentIds.isNotEmpty()) {
                        combineStudentAndGradeData(studentIds)
                    } else {
                        Toast.makeText(context, "Belum ada mahasiswa di kelas ini.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memuat daftar mahasiswa: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun combineStudentAndGradeData(studentIds: List<String>) {
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
                        studentGradeList.sortBy { it.second.name }
                        adapter.submitList(studentGradeList)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Gagal memuat data nilai: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data mahasiswa: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}