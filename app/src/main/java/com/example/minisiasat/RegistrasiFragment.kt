package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Course
import com.example.minisiasat.utils.DatabaseNodes
import com.example.minisiasat.utils.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.TextView

class RegistrasiFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseGroupAdapter
    private lateinit var closedPeriodTextView: TextView

    companion object {
        private const val ARG_USER = "user"
        fun newInstance(user: Users): RegistrasiFragment {
            return RegistrasiFragment().apply {
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

        checkPeriodAndLoadCourses()

        return view
    }

    // Fungsi ini sekarang menjadi titik masuk utama
    private fun checkPeriodAndLoadCourses() {
        DatabaseNodes.periodsRef.orderByChild("isRegistrationOpen").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Periode dibuka: tampilkan daftar, sembunyikan pesan
                        recyclerView.visibility = View.VISIBLE
                        closedPeriodTextView.visibility = View.GONE
                        loadAndGroupCourses()
                    } else {
                        // Periode ditutup: sembunyikan daftar, tampilkan pesan
                        recyclerView.visibility = View.GONE
                        closedPeriodTextView.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    recyclerView.visibility = View.GONE
                    closedPeriodTextView.visibility = View.VISIBLE
                    closedPeriodTextView.text = "Gagal memeriksa status periode."
                }
            })
    }

    private fun loadAndGroupCourses() {
        val studentId = users.kode ?: return

        // 1. Ambil daftar prefix mata kuliah yang sudah diambil mahasiswa
        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledPrefixes = enrolledSnapshot.children.mapNotNull { it.key?.take(5) }.toSet()

                    // 2. Ambil semua mata kuliah yang tersedia
                    DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val allCourses = snapshot.children.mapNotNull { it.getValue(Course::class.java) }

                            // 3. Kelompokkan semua mata kuliah
                            val allGroupedCourses = allCourses
                                .groupBy { it.courseCode?.take(5) }
                                .map { (prefix, courseList) ->
                                    CourseGroup(
                                        prefix = prefix ?: "N/A",
                                        representativeName = courseList.firstOrNull()?.courseName ?: "Tanpa Nama"
                                    )
                                }

                            // 4. Set adapter dengan logika klik yang sudah benar
                            adapter = CourseGroupAdapter(allGroupedCourses) { group ->
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

    // Fungsi checkPeriodAndLoad() yang lama sudah dihapus dan logikanya diintegrasikan ke yang baru.
}