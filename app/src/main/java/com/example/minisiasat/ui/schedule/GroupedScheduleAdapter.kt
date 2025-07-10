package com.example.minisiasat.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.Course
// Tambahan import yang diperlukan
import com.example.minisiasat.data.DatabaseNodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupedScheduleAdapter(
    private val items: List<ScheduleListData>,
    private val lecturerNames: Map<String, String>,
    private val onItemClicked: ((Course) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_COURSE = 1
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dayHeaderTextView: TextView = view.findViewById(R.id.dayHeaderTextView)
        fun bind(day: String) {
            dayHeaderTextView.text = day
        }
    }

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val courseName: TextView = view.findViewById(R.id.courseNameTextView)
        private val time: TextView = view.findViewById(R.id.timeTextView)
        private val lecturer: TextView = view.findViewById(R.id.lecturerTextView)
        private val room: TextView = view.findViewById(R.id.roomLocationTextView)
        private val sks: TextView = view.findViewById(R.id.sksTextView)
        private val year: TextView = view.findViewById(R.id.yearTextView)
        private val capacity: TextView = view.findViewById(R.id.capacityTextView)

        fun bind(course: Course) {
            val courseTitle = if (course.courseCode.isNullOrEmpty() || course.courseCode == "null") {
                course.courseName
            } else {
                "${course.courseCode} - ${course.courseName}"
            }
            courseName.text = courseTitle
            time.text = "${course.day}, ${course.time}"
            val lecturerName = lecturerNames[course.lecturerId] ?: course.lecturerId
            lecturer.text = "Dosen: $lecturerName"
            room.text = "Ruang : ${course.room}"
            sks.text = "SKS : ${course.credits}"
            year.text = "Tahun Akademik : ${course.academicYear} - ${course.semester}"

            capacity.text = "Kapasitas : ... / ${course.capacity ?: "N/A"}"

            course.courseCode?.let { code ->
                DatabaseNodes.courseRostersRef.child(code).child("students")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val enrolledCount = snapshot.childrenCount
                            capacity.text = "Kapasitas : $enrolledCount / ${course.capacity ?: "N/A"}"
                        }

                        override fun onCancelled(error: DatabaseError) {
                            capacity.text = "Kapasitas : - / ${course.capacity ?: "N/A"}"
                        }
                    })
            } ?: run {
                capacity.text = "Kapasitas : ${course.capacity ?: "Tidak diketahui"}"
            }

            if(onItemClicked != null) {
                itemView.setOnClickListener {
                    onItemClicked.invoke(course)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ScheduleListData.DayHeader -> TYPE_HEADER
            is ScheduleListData.CourseData -> TYPE_COURSE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course_card, parent, false)
            CourseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ScheduleListData.DayHeader -> (holder as HeaderViewHolder).bind(item.day)
            is ScheduleListData.CourseData -> (holder as CourseViewHolder).bind(item.course)
        }
    }

    override fun getItemCount(): Int = items.size
}