package com.example.minisiasat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Students

class EnrolledStudentAdapter(private val students: List<Pair<String, Students>>) :
    RecyclerView.Adapter<EnrolledStudentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.studentNameTextView)
        val studentNim: TextView = view.findViewById(R.id.studentNimTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enrolled_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (nim, student) = students[position]
        holder.studentName.text = student.name
        holder.studentNim.text = nim
    }

    override fun getItemCount() = students.size
}