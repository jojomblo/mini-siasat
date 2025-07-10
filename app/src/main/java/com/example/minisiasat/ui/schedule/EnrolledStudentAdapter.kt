package com.example.minisiasat.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.Students

class EnrolledStudentAdapter(private val students: List<Pair<String, Students>>) :
    RecyclerView.Adapter<EnrolledStudentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rowNumber: TextView = view.findViewById(R.id.rowNumberTextView)
        val studentNim: TextView = view.findViewById(R.id.studentNimTextView)
        val studentName: TextView = view.findViewById(R.id.studentNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enrolled_student_table, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (nim, student) = students[position]

        holder.rowNumber.text = "${position + 1}."
        holder.studentNim.text = nim
        holder.studentName.text = student.name
    }

    override fun getItemCount() = students.size
}