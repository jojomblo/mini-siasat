package com.example.minisiasat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Course

class SimpleScheduleAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<SimpleScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.simpleCourseName)
        val courseTime: TextView = view.findViewById(R.id.simpleCourseTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.courseName.text = course.courseName
        holder.courseTime.text = "${course.time} di ${course.room}"
    }

    override fun getItemCount() = courses.size
}