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

class TranskripFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private lateinit var ipkTextView: TextView
    private lateinit var adapter: TranskripAdapter
    private val courseGradeList = mutableListOf<CourseGrade>()

    companion object {
        fun newInstance(user: Users) = TranskripFragment().apply {
            arguments = Bundle().apply { putSerializable("user", user) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable("user") as Users }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transkrip, container, false)
        recyclerView = view.findViewById(R.id.transkripRecyclerView)
        ipkTextView = view.findViewById(R.id.ipkTextView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TranskripAdapter(courseGradeList)
        recyclerView.adapter = adapter

        loadFullTranscript()
        return view
    }

    private fun loadFullTranscript() {
        val studentId = users.kode ?: return

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val codes = enrolledSnapshot.children.mapNotNull { it.key }
                    if (codes.isNotEmpty()) {
                        fetchDetailsAndCalculate(codes, studentId)
                    } else {
                        ipkTextView.text = "0.00"
                    }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

    private fun fetchDetailsAndCalculate(courseCodes: List<String>, studentId: String) {
        DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coursesSnapshot: DataSnapshot) {
                DatabaseNodes.gradesRef.child(studentId).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(gradesSnapshot: DataSnapshot) {
                        courseGradeList.clear()
                        var totalCredits = 0
                        var totalScorePoints = 0.0f

                        courseCodes.forEach { code ->
                            val course = coursesSnapshot.child(code).getValue(Course::class.java)
                            val grade = gradesSnapshot.child(code).getValue(Grade::class.java)
                            if (course != null) {
                                courseGradeList.add(CourseGrade(course, grade))

                                // Kalkulasi untuk IPK
                                val credits = course.credits ?: 0
                                if (grade != null && credits > 0) {
                                    val numericGrade = getNumericGrade(grade.grade)
                                    totalCredits += credits
                                    totalScorePoints += credits * numericGrade
                                }
                            }
                        }

                        // Hitung dan tampilkan IPK
                        val ipk = if (totalCredits > 0) totalScorePoints / totalCredits else 0.0f
                        ipkTextView.text = String.format("%.2f", ipk)

                        courseGradeList.sortBy { it.course.courseCode }
                        adapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                })
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    // Helper untuk konversi nilai huruf ke angka
    private fun getNumericGrade(letter: String?): Float {
        return when (letter) {
            "A" -> 4.0f
            "AB" -> 3.5f
            "B" -> 3.0f
            "BC" -> 2.5f
            "C" -> 2.0f
            "D" -> 1.0f
            "E" -> 0.0f
            else -> 0.0f
        }
    }
}