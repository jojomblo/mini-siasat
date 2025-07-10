package com.example.minisiasat.ui.gradereport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.Course
import com.example.minisiasat.domain.model.Grade

data class CourseGrade(val course: Course, val grade: Grade?)

class GradeReportAdapter(
    private val courseGrades: List<CourseGrade>
) : RecyclerView.Adapter<GradeReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.courseNameTextView)
        val courseInfo: TextView = view.findViewById(R.id.courseInfoTextView)
        val grade: TextView = view.findViewById(R.id.gradeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hasil_studi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = courseGrades[position]
        holder.courseName.text = item.course.courseName
        holder.courseInfo.text = "${item.course.courseCode} • ${item.course.credits} SKS"

        // Tampilkan nilai jika ada, jika tidak tampilkan "-"
        holder.grade.text = item.grade?.grade ?: "-"
    }

    override fun getItemCount() = courseGrades.size
}