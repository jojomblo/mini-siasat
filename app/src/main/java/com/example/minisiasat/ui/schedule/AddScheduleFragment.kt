package com.example.minisiasat.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.data.DatabaseNodes
import com.example.minisiasat.domain.model.Lecturer
import com.example.minisiasat.domain.model.Schedule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleFragment : Fragment() {

    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var addScheduleFab: FloatingActionButton
    private lateinit var adapter: GroupedScheduleAdapter
    private val displayList = mutableListOf<Schedule>()
    private val allCourses = mutableListOf<Course>()
    private val dayOrder = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    private val lecturerNamesMap = mutableMapOf<String, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_input_jadwal, container, false)
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView)
        addScheduleFab = view.findViewById(R.id.addScheduleFab)

        adapter = GroupedScheduleAdapter(displayList, lecturerNamesMap)
        scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
        scheduleRecyclerView.adapter = adapter

        loadAllData()

        addScheduleFab.setOnClickListener { showAddScheduleDialog() }
        return view
    }

    private fun loadAllData() {
        DatabaseNodes.lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerNamesMap.clear()
                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(Lecturer::class.java)
                    val lecturerId = lecturerSnap.key // ID Dosen adalah key dari node nya
                    if (lecturer != null && lecturerId != null && lecturer.name != null) {
                        lecturerNamesMap[lecturerId] = lecturer.name
                    }
                }
                loadSchedules()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat data dosen: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSchedules() {
        DatabaseNodes.coursesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allCourses.clear()
                for (snap in snapshot.children) {
                    val course = snap.getValue(Course::class.java)
                    if (course != null) allCourses.add(course)
                }
                processAndGroupSchedules(allCourses)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat jadwal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processAndGroupSchedules(courses: List<Course>) {
        displayList.clear()
        val groupedByDay = courses.groupBy { it.day }

        for (day in dayOrder) {
            groupedByDay[day]?.let { coursesOnThisDay ->
                displayList.add(Schedule.DayHeader(day))
                val sortedCourses = coursesOnThisDay.sortedBy { it.time?.substringBefore(" - ") }
                sortedCourses.forEach { course ->
                    displayList.add(Schedule.CourseData(course))
                }
            }
        }
    }

    private fun showAddScheduleDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_schedule, null)
        val kodeInput = dialogView.findViewById<EditText>(R.id.courseCodeInput)
        val courseNameInput = dialogView.findViewById<EditText>(R.id.courseNameInput)
        val creditsInput = dialogView.findViewById<EditText>(R.id.creditsInput)
        val departmentInput = dialogView.findViewById<EditText>(R.id.departmentInput)
        val lecturerIdInput = dialogView.findViewById<EditText>(R.id.lecturerIdInput)
        val daySpinner = dialogView.findViewById<Spinner>(R.id.daySpinner)
        val roomInput = dialogView.findViewById<EditText>(R.id.roomInput)
        val academicYearInput = dialogView.findViewById<EditText>(R.id.academicYearInput)
        val semesterInput = dialogView.findViewById<AutoCompleteTextView>(R.id.semesterInput) // Diubah ke AutoCompleteTextView
        val capacityInput = dialogView.findViewById<EditText>(R.id.capacityInput)
        val checkButton = dialogView.findViewById<Button>(R.id.checkAvailabilityButton)
        val timeSelectionLayout = dialogView.findViewById<LinearLayout>(R.id.timeSelectionLayout)
        val timeGrid = dialogView.findViewById<GridLayout>(R.id.timeGrid)
        val semesters = listOf("Ganjil", "Genap", "Pendek")
        val semesterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, semesters)
        semesterInput.setAdapter(semesterAdapter)
        // Setup Spinner Hari
        val daysOfWeek = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        daySpinner.adapter = dayAdapter

        // Variabel untuk menyimpan pilihan waktu
        var selectedStart: String? = null
        var selectedEnd: String? = null
        var selectedStartBtn: Button? = null
        var selectedEndBtn: Button? = null
        val timeButtons = mutableMapOf<String, Button>()

        fun parseTimeRange(timeRange: String): List<String> {
            val parts = timeRange.split(" - ")
            if (parts.size != 2) return emptyList()
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            try {
                val start = sdf.parse(parts[0])!!
                val end = sdf.parse(parts[1])!!
                val calendar = Calendar.getInstance().apply { time = start }
                val times = mutableListOf<String>()
                while (calendar.time.before(end)) {
                    times.add(sdf.format(calendar.time))
                    calendar.add(Calendar.HOUR_OF_DAY, 1)
                }
                return times
            } catch (e: Exception) {
                return emptyList()
            }
        }

        fun updateTimeGrid() {
            val lecturerId = lecturerIdInput.text.toString().trim()
            val selectedDay = daySpinner.selectedItem.toString()
            if (lecturerId.isEmpty()) return

            DatabaseNodes.coursesRef.orderByChild("lecturerId").equalTo(lecturerId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val occupiedSlots = mutableSetOf<String>()
                        snapshot.children.forEach { snap ->
                            val course = snap.getValue(Course::class.java)
                            if (course != null && course.day == selectedDay && course.time != null) {
                                occupiedSlots.addAll(parseTimeRange(course.time!!))
                            }
                        }

                        timeGrid.removeAllViews()
                        timeButtons.clear()
                        selectedStart = null; selectedEnd = null
                        selectedStartBtn = null; selectedEndBtn = null

                        val allTimes = (7..17).map { String.format("%02d:00", it) }

                        allTimes.forEach { time ->
                            val isEnabled = !occupiedSlots.contains(time)
                            val btn = Button(requireContext()).apply {
                                text = time
                                this.isEnabled = isEnabled
                                setBackgroundColor(ContextCompat.getColor(requireContext(), if (isEnabled) R.color.lightGray else R.color.darkGray))

                                setOnClickListener {
                                    if (!this.isEnabled) return@setOnClickListener

                                    if (this == selectedStartBtn) {
                                        this.setBackgroundColor(ContextCompat.getColor(requireContext(),
                                            R.color.lightGray
                                        ))
                                        selectedStart = null
                                        selectedStartBtn = null
                                        if(selectedEndBtn != null){
                                            selectedEndBtn?.setBackgroundColor(ContextCompat.getColor(requireContext(),
                                                R.color.lightGray
                                            ))
                                            selectedEnd = null
                                            selectedEndBtn = null
                                        }
                                        return@setOnClickListener
                                    }
                                    if (this == selectedEndBtn) {
                                        this.setBackgroundColor(ContextCompat.getColor(requireContext(),
                                            R.color.lightGray
                                        ))
                                        selectedEnd = null
                                        selectedEndBtn = null
                                        return@setOnClickListener
                                    }

                                    if (selectedStart == null) {
                                        selectedStart = time
                                        selectedStartBtn = this
                                        setBackgroundColor(ContextCompat.getColor(requireContext(),
                                            R.color.green
                                        ))
                                    } else if (selectedEnd == null) {
                                        if (time > selectedStart!!) {
                                            val startIdx = allTimes.indexOf(selectedStart!!)
                                            val endIdx = allTimes.indexOf(time)
                                            var isRangeValid = true
                                            for(i in startIdx..endIdx){
                                                if(occupiedSlots.contains(allTimes[i])){
                                                    isRangeValid = false
                                                    break
                                                }
                                            }

                                            if(isRangeValid) {
                                                selectedEnd = time
                                                selectedEndBtn = this
                                                setBackgroundColor(ContextCompat.getColor(requireContext(),
                                                    R.color.blue
                                                ))
                                            } else {
                                                Toast.makeText(context, "Rentang waktu tidak valid (ada jadwal terisi)", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Waktu selesai harus setelah waktu mulai", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Sudah memilih rentang. Batalkan pilihan untuk mengubah.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            timeButtons[time] = btn
                            val params = GridLayout.LayoutParams(
                                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                                GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            ).apply {
                                width = 0
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                setMargins(4, 4, 4, 4)
                            }
                            btn.layoutParams = params
                            timeGrid.addView(btn)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        checkButton.setOnClickListener {
            val lecturerId = lecturerIdInput.text.toString().trim()
            if (lecturerId.isEmpty()) {
                Toast.makeText(context, "Isi kode dosen terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            daySpinner.visibility = View.VISIBLE
            timeSelectionLayout.visibility = View.VISIBLE
            updateTimeGrid()
        }

        daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (timeSelectionLayout.visibility == View.VISIBLE) {
                    updateTimeGrid()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Jadwal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val prefix = kodeInput.text.toString().trim()
                val name = courseNameInput.text.toString().trim()
                val credits = creditsInput.text.toString().trim().toIntOrNull()
                val department = departmentInput.text.toString().trim()
                val lecturerId = lecturerIdInput.text.toString().trim()
                val day = daySpinner.selectedItem?.toString() ?: ""
                val room = roomInput.text.toString().trim()
                val academicYear = academicYearInput.text.toString().trim()
                val semester = semesterInput.text.toString().trim()
                val capacity = capacityInput.text.toString().trim().toIntOrNull()

                if (selectedStart == null || selectedEnd == null) {
                    Toast.makeText(context, "Pilih waktu mulai dan selesai pada grid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val sdf = SimpleDateFormat("HH:mm", Locale.US)
                val calendar = Calendar.getInstance()
                calendar.time = sdf.parse(selectedEnd!!)!!
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                val finalEndTime = sdf.format(calendar.time)
                val time = "$selectedStart - $finalEndTime"

                if (prefix.length != 5 || name.isEmpty() || credits == null || department.isEmpty() ||
                    lecturerId.isEmpty() || day.isEmpty() || room.isEmpty() ||
                    academicYear.isEmpty() || semester.isEmpty() || capacity == null) {
                    Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val fullCode = generateCourseCodePrefix(prefix)

                val newCourse = Course(
                    courseCode = fullCode, courseName = name, credits = credits,
                    department = department, lecturerId = lecturerId, day = day,
                    time = time, room = room, academicYear = academicYear,
                    semester = semester, capacity = capacity
                )

                DatabaseNodes.coursesRef.child(fullCode).setValue(newCourse).addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Toast.makeText(context, "Jadwal berhasil ditambah", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal menyimpan: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun generateCourseCodePrefix(prefix: String): String {
        val suffixesUsed = allCourses
            .filter { it.courseCode?.startsWith(prefix) == true }
            .mapNotNull { it.courseCode?.lastOrNull()?.toString() }
            .toSet()
        for (char in 'A'..'Z') {
            if (char.toString() !in suffixesUsed) return "$prefix$char"
        }
        return "${prefix}X"
    }
}