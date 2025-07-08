// com/example/minisiasat/GroupedScheduleAdapter.kt
package com.example.minisiasat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Course

class GroupedScheduleAdapter(private val items: List<ScheduleListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_COURSE = 1
    }

    // ViewHolder untuk Judul Hari
    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dayHeaderTextView: TextView = view.findViewById(R.id.dayHeaderTextView)
        fun bind(day: String) {
            dayHeaderTextView.text = day
        }
    }

    // ViewHolder untuk Kartu Mata Kuliah
    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val courseName: TextView = view.findViewById(R.id.courseNameTextView)
        private val time: TextView = view.findViewById(R.id.timeTextView)
        private val lecturer: TextView = view.findViewById(R.id.lecturerTextView)

        fun bind(course: Course) {
            val courseTitle = if (course.courseCode.isNullOrEmpty() || course.courseCode == "null") {
                course.courseName
            } else {
                "${course.courseCode} - ${course.courseName}"
            }
            courseName.text = courseTitle
            time.text = "${course.day}, ${course.time}" // Menggunakan field 'time' langsung
            lecturer.text = "Dosen: ${course.lecturerId}"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ScheduleListItem.DayHeader -> TYPE_HEADER
            is ScheduleListItem.CourseItem -> TYPE_COURSE
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
            is ScheduleListItem.DayHeader -> (holder as HeaderViewHolder).bind(item.day)
            is ScheduleListItem.CourseItem -> (holder as CourseViewHolder).bind(item.course)
        }
    }

    override fun getItemCount(): Int = items.size
}