package com.example.minisiasat.ui.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.TextView
import com.example.minisiasat.R
import com.example.minisiasat.ui.course.CourseAdapter
import com.example.minisiasat.domain.model.CourseGroup
import com.example.minisiasat.ui.course.CourseSelectionFragment

class EnrollmentFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private lateinit var closedPeriodTextView: TextView

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): EnrollmentFragment {
            return EnrollmentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { users = it.getSerializable(ARG_USER) as Users }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrasi, container, false)
        recyclerView = view.findViewById(R.id.courseGroupRecyclerView)
        closedPeriodTextView = view.findViewById(R.id.closedPeriodTextView) // Inisialisasi
        recyclerView.layoutManager = LinearLayoutManager(context)

        checkActivePeriodAndLoadCourses()

        return view
    }

    private fun checkActivePeriodAndLoadCourses() {
        DatabaseNodes.periodsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeYear: String? = null
                var activeSemester: String? = null

                // Loop melalui setiap tahun ajaran (misal: "2024-2025")
                for (yearNode in snapshot.children) {
                    // Loop melalui setiap semester di dalamnya (misal: "1", "2", "3")
                    for (semesterNode in yearNode.children) {
                        val isRegistrationOpen = semesterNode.child("isRegistrationOpen").getValue(Boolean::class.java)
                        if (isRegistrationOpen == true) {
                            activeYear = yearNode.key
                            // Konversi ID semester menjadi nama semester
                            activeSemester = when (semesterNode.key) {
                                "1" -> "Ganjil"
                                "2" -> "Genap"
                                "3" -> "Pendek"
                                else -> null
                            }
                            break // Hentikan loop jika sudah ketemu
                        }
                    }
                    if (activeYear != null) break // Hentikan loop luar juga
                }

                // Jika ditemukan periode aktif, lanjutkan memuat mata kuliah
                if (activeYear != null && activeSemester != null) {
                    recyclerView.visibility = View.VISIBLE
                    closedPeriodTextView.visibility = View.GONE
                    // Kirim parameter tahun dan semester yang aktif
                    loadAndGroupCourses(activeYear.replace("-", "/"), activeSemester)
                } else {
                    // Jika tidak ada periode aktif, tampilkan pesan
                    recyclerView.visibility = View.GONE
                    closedPeriodTextView.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                recyclerView.visibility = View.GONE
                closedPeriodTextView.visibility = View.VISIBLE
                closedPeriodTextView.text = "Gagal memeriksa status periode."
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAndGroupCourses(academicYear: String, semester: String) {
        val studentId = users.kode ?: return

        // 1. Ambil daftar prefix mata kuliah yang sudah diambil mahasiswa
        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledPrefixes = enrolledSnapshot.children.mapNotNull { it.key?.take(5) }.toSet()

                    // 2. Ambil SEMUA mata kuliah, lalu filter berdasarkan tahun dan semester
                    DatabaseNodes.coursesRef.orderByChild("academicYear").equalTo(academicYear)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val availableCourses = snapshot.children.mapNotNull { it.getValue(Course::class.java) }
                                    // Filter lagi berdasarkan semester
                                    .filter { it.semester == semester }

                                if (availableCourses.isEmpty()) {
                                    Toast.makeText(context, "Tidak ada mata kuliah yang dibuka untuk periode ini.", Toast.LENGTH_LONG).show()
                                    return
                                }

                                // 3. Kelompokkan mata kuliah yang sudah difilter
                                val groupedCourses = availableCourses
                                    .groupBy { it.courseCode?.take(5) }
                                    .map { (prefix, courseList) ->
                                        CourseGroup(
                                            prefix = prefix ?: "N/A",
                                            representativeName = courseList.firstOrNull()?.courseName ?: "Tanpa Nama"
                                        )
                                    }

                                // 4. Set adapter dengan logika klik
                                adapter = CourseAdapter(groupedCourses) { group ->
                                    if (enrolledPrefixes.contains(group.prefix)) {
                                        Toast.makeText(context, "Anda sudah mengambil mata kuliah inti ini (${group.prefix}).", Toast.LENGTH_LONG).show()
                                    } else {
                                        val fragment = CourseSelectionFragment.newInstance(users, group.prefix)
                                        parentFragmentManager.beginTransaction()
                                            .replace(R.id.fragment_container, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                }
                                recyclerView.adapter = adapter
                            }
                            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
                        })
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
    }

}