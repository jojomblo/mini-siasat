package com.example.minisiasat.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.ui.schedule.GroupedScheduleAdapter
import com.example.minisiasat.R
import com.example.minisiasat.ui.schedule.ScheduleListData
import com.example.minisiasat.data.DatabaseNodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import com.example.minisiasat.domain.model.Lecturer
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Users

class CourseSelectionFragment : Fragment() {

    private lateinit var users: Users
    private lateinit var coursePrefix: String
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val ARG_USER = "user"
        private const val ARG_PREFIX = "prefix"
        fun newInstance(user: Users, prefix: String): CourseSelectionFragment {
            return CourseSelectionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                    putString(ARG_PREFIX, prefix)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            users = it.getSerializable(ARG_USER) as Users
            coursePrefix = it.getString(ARG_PREFIX)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_selection, container, false)
        recyclerView = view.findViewById(R.id.courseSelectionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadSpecificCourses()
        return view
    }

    private fun loadSpecificCourses() {
        DatabaseNodes.lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(lecturerSnapshot: DataSnapshot) {
                val lecturerNamesMap = mutableMapOf<String, String>()
                for (snap in lecturerSnapshot.children) {
                    val lecturer = snap.getValue(Lecturer::class.java)
                    val lecturerId = snap.key
                    if (lecturer != null && lecturerId != null && lecturer.name != null) {
                        lecturerNamesMap[lecturerId] = lecturer.name
                    }
                }

                DatabaseNodes.coursesRef.orderByChild("courseCode").startAt(coursePrefix)
                    .endAt(coursePrefix + "\uf8ff")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val courses = snapshot.children.mapNotNull { it.getValue(Course::class.java) }

                            val adapter = GroupedScheduleAdapter(
                                items = courses.map { ScheduleListData.CourseData(it) },
                                lecturerNames = lecturerNamesMap,
                                onItemClicked = { selectedCourse ->
                                    showConfirmationDialog(selectedCourse)
                                }
                            )
                            recyclerView.adapter = adapter
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showConfirmationDialog(course: Course) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Pengambilan")
            .setMessage("Anda yakin ingin mengambil mata kuliah ${course.courseName} (${course.courseCode})?")
            .setPositiveButton("Ambil") { _, _ ->
                checkForScheduleConflict(course)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun checkForScheduleConflict(courseToEnroll: Course) {
        val studentId = users.kode ?: return
        val courseCodeToEnroll = courseToEnroll.courseCode ?: return
        val creditsToEnroll = courseToEnroll.credits ?: 0

        DatabaseNodes.studentEnrollmentsRef.child(studentId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(enrolledSnapshot: DataSnapshot) {
                    val enrolledCourseCodes = enrolledSnapshot.children.mapNotNull { it.key }

                    if (enrolledCourseCodes.contains(courseCodeToEnroll)) {
                        Toast.makeText(context, "Anda sudah mengambil mata kuliah ini.", Toast.LENGTH_LONG).show()
                        return
                    }

                    DatabaseNodes.coursesRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(coursesSnapshot: DataSnapshot) {
                            var currentTotalCredits = 0
                            val currentSchedule = mutableListOf<Course>()

                            enrolledCourseCodes.forEach { code ->
                                coursesSnapshot.child(code).getValue(Course::class.java)?.let {
                                    currentTotalCredits += it.credits ?: 0
                                    currentSchedule.add(it)
                                }
                            }

                            if (currentTotalCredits + creditsToEnroll > 20) {
                                Toast.makeText(context, "Batas pengambilan 20 SKS akan terlampaui. SKS Anda saat ini: $currentTotalCredits", Toast.LENGTH_LONG).show()
                                return
                            }
                            // ---------------------------

                            var isConflict = false
                            for (enrolledCourse in currentSchedule) {
                                if (isTimeConflict(enrolledCourse, courseToEnroll)) {
                                    isConflict = true
                                    break
                                }
                            }

                            if (isConflict) {
                                Toast.makeText(context, "Jadwal Bertabrakan! Tidak dapat mengambil mata kuliah ini.", Toast.LENGTH_LONG).show()
                            } else {
                                enrollStudent(studentId, courseToEnroll)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Gagal memuat data jadwal: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal memuat data registrasi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun isTimeConflict(existingCourse: Course, newCourse: Course): Boolean {
        if (existingCourse.day != newCourse.day) return false

        val sdf = SimpleDateFormat("HH:mm", Locale.US)

        try {
            val existingStart = sdf.parse(existingCourse.time!!.split(" - ")[0])
            val existingEnd = sdf.parse(existingCourse.time.split(" - ")[1])
            val newStart = sdf.parse(newCourse.time!!.split(" - ")[0])
            val newEnd = sdf.parse(newCourse.time.split(" - ")[1])

            return newStart.before(existingEnd) && existingStart.before(newEnd)
        } catch (e: Exception) {
            return true
        }
    }

    private fun enrollStudent(studentId: String, course: Course) {
        val courseCode = course.courseCode ?: return

        val updates = hashMapOf<String, Any>(
            "student_enrollments/$studentId/courses/$courseCode" to true,
            "course_rosters/$courseCode/students/$studentId" to true
        )

        DatabaseNodes.coursesRef.root.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Berhasil mengambil mata kuliah!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}